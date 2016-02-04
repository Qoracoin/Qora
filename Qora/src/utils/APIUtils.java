package utils;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JOptionPane;
import javax.ws.rs.WebApplicationException;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.crypto.Crypto;
import qora.transaction.Transaction;
import qora.web.ServletUtils;
import api.ApiErrorFactory;
import controller.Controller;
import gui.PasswordPane;

public class APIUtils {

	public static String processPayment(String assetKeyString, String amount, String fee,
			String sender, String recipient, String x,
			HttpServletRequest request) {
		
		// PARSE AMOUNT		
		Asset asset;
		
		if(assetKeyString == null)
		{
			asset = Controller.getInstance().getAsset(0l);
		}
		else
		{
			try {
				asset = Controller.getInstance().getAsset(new Long(assetKeyString));
			} catch (Exception e) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ASSET_ID);
			}
		}
		
		// PARSE AMOUNT
		BigDecimal bdAmount;
		try {
			bdAmount = new BigDecimal(amount);
			bdAmount = bdAmount.setScale(8);
		} catch (Exception e) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_AMOUNT);
		}

		// PARSE FEE
		BigDecimal bdFee;
		try {
			bdFee = new BigDecimal(fee);
			bdFee = bdFee.setScale(8);
		} catch (Exception e) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_FEE);
		}

		// CHECK ADDRESS
		if (!Crypto.getInstance().isValidAddress(sender)) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_SENDER);
		}

		APIUtils.askAPICallAllowed("POST payment\n" + x, request);

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

		Pair<Transaction, Integer> result;
		if(asset.getKey() == 0l)
		{
			// SEND QORA PAYMENT
			result = Controller.getInstance()
				.sendPayment(account, new Account(recipient), bdAmount, bdFee);
		}
		else
		{
			// SEND ASSET PAYMENT
			result = Controller.getInstance()
				.transferAsset(account, new Account(recipient), asset, bdAmount, bdFee);
		}
			
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

		case Transaction.NAME_ALREADY_REGISTRED:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_NAME_ALREADY_EXISTS);

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
	}

	public static void disallowRemote(HttpServletRequest request) throws WebApplicationException {
		if (ServletUtils.isRemoteRequest(request)) {
			throw ApiErrorFactory
				      .getInstance()
				      .createError(
					      ApiErrorFactory.ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER);
		}
	}

	public static void askAPICallAllowed(final String messageToDisplay,
			HttpServletRequest request) throws WebApplicationException {
		// CHECK API CALL ALLOWED
		try {
			disallowRemote(request);

			if (Controller.getInstance().checkAPICallAllowed(messageToDisplay,
					request) != JOptionPane.YES_OPTION) {
				throw ApiErrorFactory
						.getInstance()
						.createError(
								ApiErrorFactory.ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER);
			}
			if(!Controller.getInstance().isWalletUnlocked())
			{
				String password = PasswordPane.showUnlockWalletDialog(); 
				if(!password.equals("") && !Controller.getInstance().unlockWallet(password))
				{
					JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (Exception e) {
			if (e instanceof WebApplicationException) {
				throw (WebApplicationException) e;
			}
			e.printStackTrace();
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_UNKNOWN);
		}

	}

}
