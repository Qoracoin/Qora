package api;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import database.DBSet;
import ntp.NTP;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.AEScrypto;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.Transaction;
import utils.APIUtils;
import utils.Converter;
import utils.Pair;

@Path("message")
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

	@Context
	HttpServletRequest request;

	@POST
	@Consumes(MediaType.WILDCARD)
	public String sendMessage(String x) {
		try {
			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String amount = (String) jsonObject.get("amount");
			String assetKeyString = (String) jsonObject.get("asset");
			String sender = (String) jsonObject.get("sender");
			String recipient = (String) jsonObject.get("recipient");
			String message = (String) jsonObject.get("message");
			String isTextMessageString = (String) jsonObject
					.get("istextmessage");
			String encryptString = (String) jsonObject.get("encrypt");

			boolean isTextMessage = true;
			if (isTextMessageString != null) {
				isTextMessage = Boolean.valueOf(isTextMessageString);
			}

			long assetKey = 0l;
			if (assetKeyString != null) {
				assetKey = Long.valueOf(assetKeyString);
			}

			if(assetKey != 0l && NTP.getTime() < Transaction.POWFIX_RELEASE)
			{	
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ASSET_ID);
			}
			
			boolean encrypt = true;
			if (encryptString != null) {
				encrypt = Boolean.valueOf(encryptString);
			}

			if (StringUtils.isBlank(message)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_MESSAGE_BLANK);
			}

			Name senderObj = DBSet.getInstance().getNameMap().get(sender);

			if (senderObj != null) {
				sender = senderObj.getOwner().getAddress();
			}

			
			Name recipientObj = DBSet.getInstance().getNameMap().get(recipient);
			
			if (recipientObj != null) {
				recipient = recipientObj.getOwner().getAddress();
			}
			
			Account recipientAccount = new Account(recipient);

			// PARSE AMOUNT
			BigDecimal bdAmount;
			try {
				bdAmount = new BigDecimal(amount);
				bdAmount = bdAmount.setScale(8);
			} catch (Exception e) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_AMOUNT);
			}

//			// PARSE FEE
//			BigDecimal bdFee;
//			try {
//				bdFee = new BigDecimal(fee);
//				bdFee = bdFee.setScale(8);
//			} catch (Exception e) {
//				throw ApiErrorFactory.getInstance().createError(
//						ApiErrorFactory.ERROR_INVALID_FEE);
//			}

			// CHECK ADDRESS
			if (!Crypto.getInstance().isValidAddress(sender)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_SENDER);
			}

			// check this up here to avoid leaking wallet information to remote user
			// full check is later to prompt user with calculated fee
			APIUtils.disallowRemote(request);

			// CHECK IF WALLET EXISTS
			if (!Controller.getInstance().doesWalletExists()) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
			}

			// CHECK WALLET UNLOCKED
			if (!Controller.getInstance().isWalletUnlocked()) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_WALLET_LOCKED);
			}

			// GET ACCOUNT
			PrivateKeyAccount account = Controller.getInstance()
					.getPrivateKeyAccountByAddress(sender);
			if (account == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_SENDER);
			}

			// TODO this is duplicate code -> Send money Panel, we should add
			// that to a common place later
			byte[] messageBytes;
			if (isTextMessage) {
				messageBytes = message.getBytes(StandardCharsets.UTF_8);
			} else {
				try {
					messageBytes = Converter.parseHexString(message);
				} catch (Exception e) {
					e.printStackTrace();
					throw ApiErrorFactory.getInstance().createError(
							ApiErrorFactory.ERROR_MESSAGE_FORMAT_NOT_HEX);
				}
			}

			if (messageBytes.length < 1 || messageBytes.length > 4000) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_MESSAGESIZE_EXCEEDED);
			}

			// TODO duplicate code -> SendMoneyPanel
			if (encrypt) {
				// sender
				PrivateKeyAccount pkAccount = Controller.getInstance()
						.getPrivateKeyAccountByAddress(sender);
				byte[] privateKey = pkAccount.getPrivateKey();

				// recipient
				byte[] publicKey = Controller.getInstance()
						.getPublicKeyByAddress(recipient);
				if (publicKey == null) {
					throw ApiErrorFactory.getInstance().createError(
							ApiErrorFactory.ERROR_NO_PUBLIC_KEY);
				}

				messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey,
						publicKey);
			}
			
			BigDecimal bdFee = Controller.getInstance().calcRecommendedFeeForMessage(messageBytes).getA();

			APIUtils.askAPICallAllowed("POST message\n" + x + "\n Fee: "+ bdFee.toPlainString(), request);

			byte[] encrypted = (encrypt) ? new byte[] { 1 } : new byte[] { 0 };
			byte[] isTextByte = (isTextMessage) ? new byte[] { 1 }
					: new byte[] { 0 };

			Pair<Transaction, Integer> result = Controller.getInstance()
					.sendMessage(
							Controller.getInstance()
									.getPrivateKeyAccountByAddress(sender),
							recipientAccount, assetKey, bdAmount, bdFee, messageBytes,
							isTextByte, encrypted);

			switch (result.getB()) {
			case Transaction.VALIDATE_OKE:

				return result.getA().toJson().toJSONString();

			case Transaction.INVALID_NAME_LENGTH:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_NAME_LENGTH);

			case Transaction.INVALID_VALUE_LENGTH:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_VALUE_LENGTH);

			case Transaction.INVALID_ADDRESS:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_RECIPIENT);

			case Transaction.NEGATIVE_FEE:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_FEE);

			case Transaction.FEE_LESS_REQUIRED:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_FEE_LESS_REQUIRED);

			case Transaction.NO_BALANCE:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NO_BALANCE);

			default:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_UNKNOWN);
			}

		} catch (NullPointerException e) {
			// JSON EXCEPTION
			// e.printStackTrace();
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		} catch (ClassCastException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		}
	}
}
