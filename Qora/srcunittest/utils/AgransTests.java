package utils;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.Test;

import network.Peer;
import qora.account.PrivateKeyAccount;
import qora.crypto.AEScrypto;
import qora.crypto.Base58;
import qora.crypto.Base64;
import qora.crypto.Crypto;

public class AgransTests {

	@Test
	public void testLinkedHashMap() throws UnknownHostException 
	{
		Map<Peer, Integer> peerHeight;
		peerHeight = new LinkedHashMap<Peer, Integer>();
		
		Peer peer1 = new Peer(InetAddress.getByName("127.7.6.5"));
		Peer peer2 = new Peer(InetAddress.getByName("127.3.4.5"));
		
		peerHeight.put(peer2, 1);
		
		System.out.println(peerHeight.toString());

		peerHeight.remove(peer2);
		peerHeight.remove(peer1);
		
		System.out.println("peer1 "+peerHeight.get(peer1));
		
		System.out.println(peerHeight.toString());
	}
	
	@Test
	public void testBigDecimal() {
		
		System.out.println( BigDecimal.ONE.compareTo(BigDecimal.ZERO));
		System.out.println( BigDecimal.ZERO.compareTo(BigDecimal.ONE));
		System.out.println( BigDecimal.ONE.compareTo(BigDecimal.ONE));
		
	}

	@Test
	public void testLong() {
		assertEquals(0l, 0L);
		assertEquals(1243124l, 1243124L);
		// L differs more from unity
	}

	
	@Test
	public void stripTrailingZerosTest() {

        // create 4 BigDecimal objects
        BigDecimal bg1, bg2, bg3, bg4, bg5, bg6;

        bg1 = new BigDecimal("235.000");
        bg2 = new BigDecimal("23500");
        bg3 = new BigDecimal("235.010");
        
        // assign the result of stripTrailingZeros method to bg3, bg4
        bg4 = bg1.stripTrailingZeros();
        bg5 = bg2.stripTrailingZeros();
        bg6 = bg3.stripTrailingZeros();

        
        String str1 = bg1 + " after removing trailing zeros " +bg4.toPlainString();
        String str2 = bg2 + " after removing trailing zeros " +bg5.toPlainString();
        String str3 = bg3 + " after removing trailing zeros " +bg6.toPlainString();

        // print bg3, bg4 values
        System.out.println( str1 );
        System.out.println( str2 );
        System.out.println( str3 );
    }
	 
	@Test
	public void testSign() {
		//address: QQQQD8UkkJPnW3yRZqJAUH9Pi9BzVKJCv8
		//wallet seed: AsF8sY23poJZro7to4ifXQyMzJQsVGFdDgkQd1uihnrg
		//address seed: ETWEM8bdV2DQxjaS8p9qn9Q5556htaLXoZPc6Hz4Qo3j
		
		String text = "Test message. Rus:Тестовое сообщение.";
		
		String signerSeed = "ETWEM8bdV2DQxjaS8p9qn9Q5556htaLXoZPc6Hz4Qo3j";
		byte[] signerSeedByte = Base58.decode(signerSeed);
	
		Pair<byte[], byte[]> signerKeyPair = Crypto.getInstance().createKeyPair(signerSeedByte);
		
		byte[] signerPublicKey = signerKeyPair.getB();
		
		assertEquals(Crypto.getInstance().getAddress(signerPublicKey), "QQQQD8UkkJPnW3yRZqJAUH9Pi9BzVKJCv8");
		
		PrivateKeyAccount account = new PrivateKeyAccount(signerSeedByte); 
		
		byte[] textByte = text.getBytes(StandardCharsets.UTF_8); 
		byte[] signatureByte = Crypto.getInstance().sign(account, textByte);
		assertEquals(Crypto.getInstance().verify(signerPublicKey, signatureByte, textByte), true);
		
		String wrongText = text + " wrong"; 
		byte[] wrongTextByte = wrongText.getBytes(StandardCharsets.UTF_8);
		assertEquals(Crypto.getInstance().verify(signerPublicKey, signatureByte, wrongTextByte), false);
		
		byte[] wrongSignatureByte = new byte[signatureByte.length];
		System.arraycopy(signatureByte, 0, wrongSignatureByte, 0, signatureByte.length);
		wrongSignatureByte[0] = (byte) (wrongSignatureByte[0] + 1); 
		assertEquals(Crypto.getInstance().verify(signerPublicKey, wrongSignatureByte, textByte), false);

		byte[] wrongSignerPublicKey = new byte[signerPublicKey.length];
		System.arraycopy(signerPublicKey, 0, wrongSignerPublicKey, 0, signerPublicKey.length);
		wrongSignerPublicKey[0] = (byte) (wrongSignerPublicKey[0] + 1); 
		assertEquals(Crypto.getInstance().verify(wrongSignerPublicKey, signatureByte, textByte), false);
		
		assertEquals(Crypto.getInstance().verify(signerPublicKey, signatureByte, textByte), true);
	}
	
	@Test
	public void testMessages() {
		
		//address: QQQQD8UkkJPnW3yRZqJAUH9Pi9BzVKJCv8
		//wallet seed: AsF8sY23poJZro7to4ifXQyMzJQsVGFdDgkQd1uihnrg
		//address seed: ETWEM8bdV2DQxjaS8p9qn9Q5556htaLXoZPc6Hz4Qo3j
		String senderSeed = "ETWEM8bdV2DQxjaS8p9qn9Q5556htaLXoZPc6Hz4Qo3j";
		
		byte[] senderSeedByte = Base58.decode(senderSeed);
		
		assertEquals(Base58.encode(senderSeedByte), senderSeed);
		
		Pair<byte[], byte[]> senderKeyPair = Crypto.getInstance().createKeyPair(senderSeedByte);
		
		byte[] senderPrivateKey = senderKeyPair.getA();
		byte[] senderPublicKey = senderKeyPair.getB();
		
		assertEquals(Crypto.getInstance().getAddress(senderPublicKey), "QQQQD8UkkJPnW3yRZqJAUH9Pi9BzVKJCv8");
		
		//address: QQQdEJ9xYHkBru1tCg2V7m2jPiHHcrJT4r
		//wallet seed: 7Zn2MtvqF8kYNwTwzgyWz9C7rSM5CUVm2MfcoTiT67ii
		//address seed: BExrh6dUDNSG1dLyVChTxBY4pfA3NPAqLkEMtbMdgX6V
		String recipientSeed = "BExrh6dUDNSG1dLyVChTxBY4pfA3NPAqLkEMtbMdgX6V";
		
		byte[] recipientSeedByte = Base58.decode(recipientSeed);
		
		Pair<byte[], byte[]> recipientKeyPair = Crypto.getInstance().createKeyPair(recipientSeedByte);
		
		byte[] recipientPrivateKey = recipientKeyPair.getA();
		byte[] recipientPublicKey = recipientKeyPair.getB();
		
		assertEquals(Crypto.getInstance().getAddress(recipientPublicKey), "QQQdEJ9xYHkBru1tCg2V7m2jPiHHcrJT4r");
		
		String StartMessage = "Test message. Rus:Тестовое сообщение.";
		
		byte[] messageBytes;
		
		messageBytes = StartMessage.getBytes( Charset.forName("UTF-8") );
		
		messageBytes = AEScrypto.dataEncrypt(messageBytes, senderPrivateKey, recipientPublicKey);
		
		try {
			messageBytes = AEScrypto.dataDecrypt(messageBytes, recipientPrivateKey, senderPublicKey);
		} catch (InvalidCipherTextException e) {
			e.printStackTrace();
		}
		
		String EndMessage = new String( messageBytes, Charset.forName("UTF-8") );
		
		assertEquals(EndMessage, StartMessage);
		
	}

	@Test
	public void testBase_58_64() {
		String source = "skerberus\nvbcs\n" + "\uAA75" + "\uBCFA" + "\u5902" + "\u2ed8";
		
		String base58 = Base58.encode(source.getBytes(StandardCharsets.UTF_8));
		String result = new String(Base58.decode(base58), StandardCharsets.UTF_8);
		assertEquals(source, result);
		
		String base64 = Base64.encode(source.getBytes(StandardCharsets.UTF_8));
		result = new String(Base64.decode(base64), StandardCharsets.UTF_8);
		assertEquals(source, result);
	}
}
