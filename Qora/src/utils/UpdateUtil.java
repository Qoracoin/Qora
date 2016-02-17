package utils;

import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.block.Block;
import qora.block.GenesisBlock;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import api.BlogPostResource;
import database.BlockMap;
import database.DBSet;
import database.SortableList;

public class UpdateUtil {

	public static void repopulateNameStorage(int height) {
		DBSet.getInstance().getNameStorageMap().reset();
		DBSet.getInstance().getOrphanNameStorageHelperMap().reset();
		DBSet.getInstance().getOrphanNameStorageMap().reset();
		DBSet.getInstance().getHashtagPostMap().reset();

		SortableList<byte[], Block> blocks = DBSet.getInstance().getBlockMap()
				.getList();
		blocks.sort(BlockMap.HEIGHT_INDEX);

		Block b = new GenesisBlock();
		do
		{
			if ( b.getHeight() >= height )
			{
				List<Transaction> txs = b.getTransactions();
				for (Transaction tx : txs) {
					if (tx instanceof ArbitraryTransaction) {
						ArbitraryTransaction arbTx = (ArbitraryTransaction) tx;
						int service = arbTx.getService();
						if (service == 10) {
							StorageUtils.processUpdate(arbTx.getData(),
									arbTx.getSignature(), arbTx.getCreator(),
									DBSet.getInstance());
						} else if (service == 777) {
							byte[] data = arbTx.getData();
							String string = new String(data);



							JSONObject jsonObject = (JSONObject) JSONValue
									.parse(string);
							if (jsonObject != null) {
								String post = (String) jsonObject
										.get(BlogPostResource.POST_KEY);

								String share = (String) jsonObject
										.get(BlogPostResource.SHARE_KEY);


								boolean isShare = false;
								if (StringUtils.isNotEmpty(share)) {
									isShare = true;
								}

								// DOES POST MET MINIMUM CRITERIUM?
								if (StringUtils.isNotBlank(post)) {

									// Shares won't be hashtagged!
									if (!isShare) {
										List<String> hashTags = BlogUtils
												.getHashTags(post);
										for (String hashTag : hashTags) {
											DBSet.getInstance()
											.getHashtagPostMap()
											.add(hashTag,
													arbTx.getSignature());
										}
									}

								}

							}
						}
					}
				}
			}
			b = b.getChild();
		}while ( b != null );

	}



	public static void repopulateTransactionFinalMap() {
		DBSet.getInstance().getTransactionFinalMap().reset();
		DBSet.getInstance().commit();
		Block b = new GenesisBlock();
		do
		{
			List<Transaction> txs = b.getTransactions();
			int counter = 1;
			for (Transaction tx : txs)
			{
				DBSet.getInstance().getTransactionFinalMap().add(b.getHeight(), counter, tx);
				counter++;
			}
			if ( b.getHeight()%2000 == 0 )
			{
				Logger.getGlobal().info("UpdateUtil - Repopulating TransactionMap : " + b.getHeight());
				DBSet.getInstance().commit();
			}
			b = b.getChild();
		}while ( b != null );

	}
	
	public static void repopulateCommentPostMap() {
		DBSet.getInstance().getPostCommentMap().reset();
		DBSet.getInstance().commit();
		Block b = new GenesisBlock();
		do
		{
			List<Transaction> txs = b.getTransactions();
			for (Transaction tx : txs)
			{
				if(tx instanceof ArbitraryTransaction)
				{
					int service = ((ArbitraryTransaction) tx).getService();
					if(service == BlogUtils.COMMENT_SERVICE_ID)
					{
						((ArbitraryTransaction) tx).addToCommentMapOnDemand(DBSet.getInstance());
					}
				}
			}
			if ( b.getHeight()%2000 == 0 )
			{
				Logger.getGlobal().info("UpdateUtil - Repopulating CommentPostMap : " + b.getHeight());
				DBSet.getInstance().commit();
			}
			b = b.getChild();
		}while ( b != null );
		
	}
}

