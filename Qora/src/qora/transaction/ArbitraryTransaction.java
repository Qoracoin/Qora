package qora.transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Charsets;
import com.google.common.primitives.Longs;

import api.BlogPostResource;
import database.BalanceMap;
import database.DBSet;
import qora.account.Account;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.naming.Name;
import qora.payment.Payment;
import qora.web.blog.BlogEntry;
import utils.BlogUtils;
import utils.StorageUtils;

public abstract class ArbitraryTransaction extends Transaction {

	private int version; 
	protected PublicKeyAccount creator;
	protected int service;
	protected byte[] data;

	protected List<Payment> payments;
	
	public ArbitraryTransaction(BigDecimal fee, long timestamp, byte[] reference, byte[] signature) {
		super(ARBITRARY_TRANSACTION, fee, timestamp, reference, signature);
		
		if(timestamp < Transaction.getPOWFIX_RELEASE()) {
			version = 1;
		} else {
			version = 3;
		}
	}
	
	public int getVersion()
	{
		return this.version;
	}
	
	// GETTERS/SETTERS

	public int getService() {
		return this.service;
	}

	public byte[] getData() {
		return this.data;
	}

	public List<Payment> getPayments() {
		if(this.payments != null) {
			return this.payments;
		} else {
			return new ArrayList<Payment>();
		}
	}
	
	// PARSE CONVERT

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() {
		// GET BASE
		JSONObject transaction = this.getJsonBase();

		// ADD CREATOR/SERVICE/DATA
		transaction.put("creator", this.creator.getAddress());
		transaction.put("service", this.service);
		transaction.put("data", Base58.encode(this.data));

		JSONArray payments = new JSONArray();
		for (Payment payment : this.payments) {
			payments.add(payment.toJson());
		}
		
		if(payments.size()>0) {
			transaction.put("payments", payments);
		}
		
		return transaction;
	}
	
	@Override
	public abstract byte[] toBytes();

	@Override
	public abstract int getDataLength();

	// VALIDATE

	@Override
	public abstract boolean isSignatureValid();

	@Override
	public abstract int isValid(DBSet db);
	
	public static Transaction Parse(byte[] data) throws Exception
	{
		// READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, 0, TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
	
		if(timestamp < Transaction.getPOWFIX_RELEASE()) {
			return ArbitraryTransactionV1.Parse(data);			
		} else {
			return ArbitraryTransactionV3.Parse(data);
		}
	}
	
	@Override
	public PublicKeyAccount getCreator() {
		return this.creator;
	}

	@Override
	public List<Account> getInvolvedAccounts() {
		List<Account> accounts = new ArrayList<Account>();

		accounts.add(this.creator);

		for (Payment payment : this.payments) {
			accounts.add(payment.getRecipient());
		}

		return accounts;
	}

	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		for(Account involved: this.getInvolvedAccounts())
		{
			if(address.equals(involved.getAddress()))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public BigDecimal getAmount(Account account) {
		BigDecimal amount = BigDecimal.ZERO.setScale(8);
		String address = account.getAddress();

		// IF SENDER
		if (address.equals(this.creator.getAddress())) {
			amount = amount.subtract(this.fee);
		}

		// CHECK PAYMENTS
		for (Payment payment : this.payments) {
			// IF QORA ASSET
			if (payment.getAsset() == BalanceMap.QORA_KEY) {
				// IF SENDER
				if (address.equals(this.creator.getAddress())) {
					amount = amount.subtract(payment.getAmount());
				}

				// IF RECIPIENT
				if (address.equals(payment.getRecipient().getAddress())) {
					amount = amount.add(payment.getAmount());
				}
			}
		}

		return amount;
	}
	
	// PROCESS/ORPHAN
	@Override
	public void process(DBSet db) {

		try {
			// NAME STORAGE UPDATE
			if (this.getService() == 10) {
				StorageUtils.processUpdate(getData(), signature, this.getCreator(),
						DBSet.getInstance());
				StorageUtils.processUpdate(getData(), signature, this.getCreator(), db);
				// BLOGPOST?
			} else if (this.getService() == 777) {
				addToBlogMapOnDemand(DBSet.getInstance());
				addToBlogMapOnDemand(db);
			} else if (this.getService() == BlogUtils.COMMENT_SERVICE_ID) {
				addToCommentMapOnDemand(DBSet.getInstance());
				addToCommentMapOnDemand(db);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// UPDATE CREATOR
		this.getCreator().setConfirmedBalance(this.getCreator().getConfirmedBalance(db)
				.subtract(this.fee), db);

		// UPDATE REFERENCE OF CREATOR
		this.getCreator().setLastReference(this.signature, db);

		// PROCESS PAYMENTS
		for (Payment payment : this.getPayments()) {
			payment.process(this.getCreator(), db);

			// UPDATE REFERENCE OF RECIPIENT
			if (Arrays.equals(payment.getRecipient().getLastReference(db),
					new byte[0])) {
				payment.getRecipient().setLastReference(this.signature, db);
			}
		}
	}

	@Override
	public void orphan(DBSet db) {

		// NAME STORAGE UPDATE ORPHAN
		// if (service == 10) {
		// StorageUtils.processOrphan(getData(), signature, db);
		// // BLOGPOST?
		// } else {
		// removeFromBlogMapOnDemand(db);
		// }

		// UPDATE CREATOR
		this.getCreator().setConfirmedBalance(this.getCreator().getConfirmedBalance(db)
				.add(this.fee), db);

		// UPDATE REFERENCE OF CREATOR
		this.getCreator().setLastReference(this.reference, db);

		// ORPHAN PAYMENTS
		for (Payment payment : this.getPayments()) {
			payment.orphan(this.getCreator(), db);

			// UPDATE REFERENCE OF RECIPIENT
			if (Arrays.equals(payment.getRecipient().getLastReference(db),
					this.signature)) {
				payment.getRecipient().removeReference(db);
			}
		}
	}
	
	public void addToCommentMapOnDemand(DBSet db) {

		if (getService() == BlogUtils.COMMENT_SERVICE_ID) {
			byte[] data = getData();
			String string = new String(data, Charsets.UTF_8);

			JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
			if (jsonObject != null) {

				String signatureOfCommentOpt = (String) jsonObject
						.get(BlogPostResource.DELETE_KEY);

				// CHECK IF THIS IS A DELETE OR CREATE OF A COMMENT
				if (StringUtils.isNotBlank(signatureOfCommentOpt)) {
					BlogEntry commentEntryOpt = BlogUtils
							.getCommentBlogEntryOpt(signatureOfCommentOpt);

					if (commentEntryOpt != null) {
						String creatorOfDeleteTX = getCreator().getAddress();
						String creatorOfEntryToDelete = commentEntryOpt
								.getCreator();

						// OWNER IS DELETING OWN POST?
						if (creatorOfDeleteTX.equals(creatorOfEntryToDelete)) {
							deleteCommentInternal(db, commentEntryOpt);
							// BLOGOWNER IS DELETING POST
						} else if (
								commentEntryOpt.getBlognameOpt() != null) {
							Name name = db.getNameMap().get(
									commentEntryOpt.getBlognameOpt());
							if (name != null
									&& name.getOwner().getAddress()
											.equals(creatorOfDeleteTX)) {
								deleteCommentInternal(db, commentEntryOpt);

							}
						}

					}
				} else {
					String post = (String) jsonObject
							.get(BlogPostResource.POST_KEY);

					String postid = (String) jsonObject
							.get(BlogPostResource.COMMENT_POSTID_KEY);

					// DOES POST MET MINIMUM CRITERIUM?
					if (StringUtils.isNotBlank(post)
							&& StringUtils.isNotBlank(postid)) {

						db.getPostCommentMap().add(Base58.decode(postid),
								getSignature());
						db.getCommentPostMap().add(getSignature(),
								Base58.decode(postid));
					}
				}

			}

		}

	}
	
	private void addToBlogMapOnDemand(DBSet db) {

		if (getService() == 777) {
			byte[] data = this.getData();
			String string = new String(data, Charsets.UTF_8);

			JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
			if (jsonObject != null) {
				String post = (String) jsonObject
						.get(BlogPostResource.POST_KEY);

				String blognameOpt = (String) jsonObject
						.get(BlogPostResource.BLOGNAME_KEY);

				String share = (String) jsonObject
						.get(BlogPostResource.SHARE_KEY);

				String delete = (String) jsonObject
						.get(BlogPostResource.DELETE_KEY);

				String author = (String) jsonObject
						.get(BlogPostResource.AUTHOR);

				boolean isShare = false;
				if (StringUtils.isNotEmpty(share)) {
					isShare = true;
					byte[] sharedSignature = Base58.decode(share);
					if (sharedSignature != null) {
						db.getSharedPostsMap().add(sharedSignature, author);
					}
				}

				if (StringUtils.isNotEmpty(delete)) {
					BlogEntry blogEntryOpt = BlogUtils.getBlogEntryOpt(delete);

					if (blogEntryOpt != null) {
						String creatorOfDeleteTX = getCreator().getAddress();
						String creatorOfEntryToDelete = blogEntryOpt
								.getCreator();
						if (blogEntryOpt != null) {

							// OWNER IS DELETING OWN POST?
							if (creatorOfDeleteTX
									.equals(creatorOfEntryToDelete)) {
								deleteInternal(db, isShare, blogEntryOpt);
								// BLOGOWNER IS DELETING POST
							} else if (author != null
									&& blogEntryOpt.getBlognameOpt() != null) {
								Name name = db.getNameMap().get(
										blogEntryOpt.getBlognameOpt());
								if (name != null
										&& name.getOwner().getAddress()
												.equals(creatorOfDeleteTX)) {
									deleteInternal(db, isShare, blogEntryOpt);
								}
							}

						}
					}

				} else {

					// DOES POST MET MINIMUM CRITERIUM?
					if (StringUtils.isNotBlank(post)) {

						// Shares won't be hashtagged!
						if (!isShare) {
							List<String> hashTags = BlogUtils.getHashTags(post);
							for (String hashTag : hashTags) {
								db.getHashtagPostMap().add(hashTag,
										getSignature());
							}
						}

						db.getBlogPostMap().add(blognameOpt, getSignature());
					}
				}

			}
		}
	}
	
	public void deleteInternal(DBSet db, boolean isShare, BlogEntry blogEntryOpt) {
		if (isShare) {
			byte[] sharesignature = Base58.decode(blogEntryOpt
					.getShareSignatureOpt());
			db.getBlogPostMap().remove(blogEntryOpt.getBlognameOpt(),
					sharesignature);
			db.getSharedPostsMap().remove(sharesignature,
					blogEntryOpt.getNameOpt());
		} else {
			// removing from hashtagmap
			List<String> hashTags = BlogUtils.getHashTags(blogEntryOpt
					.getDescription());
			for (String hashTag : hashTags) {
				db.getHashtagPostMap().remove(hashTag,
						Base58.decode(blogEntryOpt.getSignature()));
			}
			db.getBlogPostMap().remove(blogEntryOpt.getBlognameOpt(),
					Base58.decode(blogEntryOpt.getSignature()));
		}
	}
	
	public void deleteCommentInternal(DBSet db, BlogEntry commentEntry) {

		byte[] signatureOfComment = Base58.decode(commentEntry.getSignature());
		byte[] signatureOfBlogPostOpt = db.getCommentPostMap().get(
				Base58.decode(commentEntry.getSignature()));
		// removing from hashtagmap

		if (signatureOfBlogPostOpt != null) {
			db.getPostCommentMap().remove(signatureOfBlogPostOpt,
					signatureOfComment);
			db.getCommentPostMap().remove(signatureOfComment);

		}
	}
	
	// TODO implement readd delete if orphaned!
	@SuppressWarnings("unused")
	private void removeFromBlogMapOnDemand(DBSet db) {
		if (getService() == 777) {
			byte[] data = getData();
			String string = new String(data, Charsets.UTF_8);

			JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
			if (jsonObject != null) {
				String blognameOpt = (String) jsonObject
						.get(BlogPostResource.BLOGNAME_KEY);

				String share = (String) jsonObject
						.get(BlogPostResource.SHARE_KEY);

				String author = (String) jsonObject
						.get(BlogPostResource.AUTHOR);

				if (StringUtils.isNotEmpty(share)) {
					byte[] sharedSignature = Base58.decode(share);
					if (sharedSignature != null) {
						db.getSharedPostsMap().remove(sharedSignature, author);
					}
				}

				db.getBlogPostMap().remove(blognameOpt, getSignature());

			}
		}
	}
}
