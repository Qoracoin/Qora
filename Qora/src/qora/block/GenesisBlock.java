package qora.block;

import java.math.BigDecimal;
import java.util.Arrays;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;
import qora.account.Account;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.transaction.GenesisTransaction;
import qora.transaction.Transaction;
import settings.Settings;
import utils.Pair;

public class GenesisBlock extends Block{
	
	private static int genesisVersion = 1;
	private static byte[] genesisReference =  new byte[]{1,1,1,1,1,1,1,1};
	//private static long genesisTimestamp = 1400247274336l;
	private static long genesisGeneratingBalance = 10000000L;
	private static PublicKeyAccount genesisGenerator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
	
	private String testnetInfo; 
	
	public GenesisBlock()
	{
		//SET HEADER
		super(genesisVersion, genesisReference, Settings.getInstance().getGenesisStamp() , genesisGeneratingBalance, genesisGenerator, generateHash());
		
		long genesisTimestamp = Settings.getInstance().getGenesisStamp();
		
		if(genesisTimestamp != Settings.DEFAULT_MAINNET_STAMP) {
			this.testnetInfo = ""; 
			
			//ADD TESTNET GENESIS TRANSACTIONS
			this.testnetInfo += "\ngenesisStamp: " + String.valueOf(genesisTimestamp);	

			byte[] seed = Crypto.getInstance().digest(Longs.toByteArray(genesisTimestamp));

			this.testnetInfo += "\ngenesisSeed: " + Base58.encode(seed);
			
			for(int nonce=0; nonce<10; nonce++)
		    {
				byte[] accountSeed = generateAccountSeed(seed, nonce);
				
				Pair<byte[], byte[]> keyPair = Crypto.getInstance().createKeyPair(accountSeed);
				byte[] publicKey = keyPair.getB();
				String address = Crypto.getInstance().getAddress(publicKey);

				this.addTransaction(new GenesisTransaction(new Account(address), new BigDecimal(10000000000L/10).setScale(8), genesisTimestamp));
				
				this.testnetInfo += "\ngenesisAccount(" + String.valueOf(nonce) + "): " + address +  " / POST addresses " + Base58.encode(accountSeed);
		    }
			this.testnetInfo += "\nStart the other nodes with command:";
			this.testnetInfo += "\njava -Xms512m -Xmx1024m -jar Qora.jar -testnet=" + genesisTimestamp;

			
			//GENERATE AND VALIDATE TRANSACTIONSSIGNATURE
			this.setTransactionsSignature(generateHash());
		} else {
			
			//ADD MAINNET GENESIS TRANSACTIONS
			
			this.addTransaction(new GenesisTransaction(new Account("QUD9y7NZqTtNwvSAUfewd7zKUGoVivVnTW"), new BigDecimal("7032468.191").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QVafvKkE5bZTkq8PcXvdaxwuLNN2DGCwYk"), new BigDecimal("1716146.084").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QV42QQP7frYWqsVq536g7zSk97fUpf2ZSN"), new BigDecimal("5241707.06").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QgkLTm5GkepJpgr53nAgUyYRsvmyHpb2zT"), new BigDecimal("854964.0816").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qc8kN338XQULMBuUa6mTqL5tipvELDhzeZ"), new BigDecimal("769467.6734").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QQ81BA75jZcpjQBLZE1qcHrXV8ARC1DEec"), new BigDecimal("85496408.16").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QeoSe4DscWX4AFkNBCdm4WS1V7QkUFSQLP"), new BigDecimal("854968.3564").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qdfu3Eh21ZVHNDY1xyNaFqTTEYscSmfSsm"), new BigDecimal("85496408.16").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QeDSr4abXKRg9j5hTN3TK9UGuH3umThZ42"), new BigDecimal("4445813.224").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QQKDuo1txYB9E2xim79YVR6SQ1ZbJtJtFX"), new BigDecimal("47023024.49").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QLeaeGr4CDA95FmeMtFh8okJRMLoq8Cge5"), new BigDecimal("170992816.3").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QSwN5oa8ZHWJmc6FeAJ8Xr1SHaEuSahw1J"), new BigDecimal("3419856.326").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QWnoGd4a7iXqQmNEpUtCb1x7nWgcya8QbE"), new BigDecimal("17056533.43").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QbJqhsJjcy3vkzsJ1kHvgn26pQF3sZEypc"), new BigDecimal("42705455.87").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QiBhBcseKzaDnHKyqEJs8z1Xx2rSb9XhBr"), new BigDecimal("141069073.5").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QTwYwxBhzivFEWY5yfzyz1pqhJ8XCroKwv"), new BigDecimal("85496408.16").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QfikxUU15Dy1oxbcDNEcLeU5cHvbrceq3A"), new BigDecimal("17099281.63").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QhdqBmKZeQ3Hg1XUuR5nKtAkw47tuoRi2q"), new BigDecimal("12824461.22").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QaVNyTqsTHA6JWMcqntcJf1u9c3qid76xH"), new BigDecimal("128244612.2").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QYaDa7bmgo5L9qkcfJKjhPPrQkvGjEoc7y"), new BigDecimal("85496408.16").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QQPddvWaYf4pbCyVHEVoyfC72EiaAv4JhT"), new BigDecimal("25648922.45").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QSQpTNtTZMwaDuNq56Jz73KHWXaey9JrT1"), new BigDecimal("26341443.35").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QVjcFWE6TnGePGJEtbNc1thwD2sgHBLvUV"), new BigDecimal("42940528.25").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qga93mWNqTuJYx6o33vjUpFH7Cn4sxLyoG"), new BigDecimal("2564892.245").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QXyHKyQPJnb4ejyTkvS26x9sjWnhTTJ1Uc"), new BigDecimal("10259568.98").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QLurSSgJvW7WXHDFSobgfakgqXxjoZzwUH"), new BigDecimal("85496408.16").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QadxfiARcsmwzE93wqq82yXFi3ykM2qdtS"), new BigDecimal("79118376.11").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QRHhhtz3Cv9RPKB1QBBfkRmRfpXx8vkRa5"), new BigDecimal("22435418.54").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qh8UnEs55n8jcnBaBwVtrTGkFFFBDyrMqH"), new BigDecimal("128757590.7").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QhF7Fu3f54CTYA7zBQ223NQEssi2yAbAcx"), new BigDecimal("258481290.8").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QPk9VB6tigoifrUYQrw4arBNk7i8HEgsDD"), new BigDecimal("128244612.2").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QXWJnEPsdtaLQAEutJFR4ySiMUJCWDzZJX"), new BigDecimal("85496408.16").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QVFs42gM4Cixf4Y5vDFvKKxRAamUPMCAVq"), new BigDecimal("85496408.16").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qec5ueWc4rcBrty47GZfFSqvLymxvcycFm"), new BigDecimal("129091026.7").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QfYiztbDz1Nb9EMhgHidLycvuPN8HEcHEj"), new BigDecimal("128244612.2").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QPdWsZtaZcAKqk2HWVhEVbws4qG5KUTXmg"), new BigDecimal("179285967.9").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QVkNs5NcwQpsrCXpWzuMXkMehJr5mkvLVy"), new BigDecimal("8558190.456").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qg19DzyEfyZANx6JLy4GrSGF5LuZ2MLqyZ"), new BigDecimal("42748204.08").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qf3A8L5WJNHt1xZxmayrTp2d5owzdkcxM6"), new BigDecimal("50519827.58").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QeKR4W6qkFJGF7Hmu7rSUzTSQiqJzZLXdt"), new BigDecimal("10216820.77").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QWg7T5i3uBY3xeBLFTLYYruR15Ln11vwo4"), new BigDecimal("170992816.3").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QUYdM5fHECPZxKQQAmoxoQa2aWg8TZYfPw"), new BigDecimal("85496408.16").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QjhfEZCgrjUbnLRnWqWxzyYqKQpjjxkuA8"), new BigDecimal("86665653.61").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QMA53u3wrzDoxC57CWUJePNdR8FoqinqUS"), new BigDecimal("85496408.16").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QSuCp6mB5zNNeJKD62aq2hR9h84ks1WhHf"), new BigDecimal("161588211.4").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QS2tCUk7GQefg4zGewwrumxSPmN6fgA7Xc"), new BigDecimal("170992816.3").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qcn6FZRxAgp3japtvjgUkBY6KPfbPZMZtM"), new BigDecimal("170992816.3").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QZrmXZkRmjV2GwMt72Rr1ZqHJjv8raDk5J"), new BigDecimal("17099281.63").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QeZzwGDfAHa132jb6r4rQHbgJstLuT8QJ3"), new BigDecimal("255875360.3").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qj3L139sMMuFvvjKQDwRnoSgKUnoMhDQs5"), new BigDecimal("76946767.34").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QWJvpvbFRZHu7LRbY5MjzvrMBgzJNFYjCX"), new BigDecimal("178251461.4").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QRyECqW54ywKVt4kZTEXyRY17aaFUaxzc4"), new BigDecimal("8772355.539").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QgpH3K3ArkQTg15xjKqGq3BRgE3aNH9Q2P"), new BigDecimal("46766535.26").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QVZ6pxi8e3K3S44zLbnrLSLwSoYT8CWbwV"), new BigDecimal("233172022.2").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QNbA69dbnmwqJHLQeS9v63hSLZXXGkmtC6"), new BigDecimal("46626632.05").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QgzudSKbcLUeQUhFngotVswDSkbU42dSMr"), new BigDecimal("83786479.99").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QfkQ2UzKMBGPwj8Sm31SArjtXoka1ubU3i"), new BigDecimal("116345066.7").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QgxHHNwawZeTmQ3i5d9enchi4T9VmzNZ5k"), new BigDecimal("155448014.8").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QMNugJWNsLuV4Qmbzdf8r8RMEdXk5PNM69"), new BigDecimal("155448014.8").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QVhWuJkCjStNMV4U8PtNM9Qz4PvLAEtVSj"), new BigDecimal("101041209.6").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QXjNcckFG9gTr9YbiA3RrRhn3mPJ9zyR4G"), new BigDecimal("3108960.297").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QThnuBadmExtxk81vhFKimSzbPaPcuPAdm"), new BigDecimal("155448014.8").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QRc6sQthLHjfkmm2BUhu74g33XtkDoB7JP"), new BigDecimal("77773983.95").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QcDLhirHkSbR4TLYeShLzHw61B8UGTFusk"), new BigDecimal("23317202.22").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QXRnsXE6srHEf2abGh4eogs2mRsmNiuw6V"), new BigDecimal("5440680.519").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QRJmEswbDw4x1kwsLyxtMS9533fv5cDvQV"), new BigDecimal("3886200.371").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qg43mCzWmFVwhVfx34g6shXnSU7U7amJNx"), new BigDecimal("6217920.593").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QQ9PveFTW64yUcXEE6AxhokWCwhmn8F2TD"), new BigDecimal("8549640.816").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QQaxJuTkW5XXn4DhhRekXpdXaWcsxEfCNG"), new BigDecimal("3886200.371").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QifWFqW8XWL5mcNxtdr5z1LVC7XUu9tNSK"), new BigDecimal("3116732.697").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QavhBKRN4vuyzHNNqcWxjcohRAJNTdTmh4"), new BigDecimal("154670774.8").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QMQyR3Hybof8WpQsXPxh19AZFCj4Z4mmke"), new BigDecimal("77724007.42").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QbT3GGjp1esTXtowVk2XCtBsKoRB8mkP61"), new BigDecimal("77724007.42").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QT13tVMZEtbrgJEsBBcTtnyqGveC7mtqAb"), new BigDecimal("23317202.22").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QegT2Ws5YjLQzEZ9YMzWsAZMBE8cAygHZN"), new BigDecimal("12606834").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QXoKRBJiJGKwvdA3jkmoUhkM7y6vuMp2pn"), new BigDecimal("65117173.41").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QY6SpdBzUev9ziqkmyaxESZSbdKwqGdedn"), new BigDecimal("89382608.53").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QeMxyt1nEE7tbFbioc87xhiKb4szx5DsjY"), new BigDecimal("15544801.48").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QcTp3THGZvJ42f2mWsQrawGvgBoSHgHZyk"), new BigDecimal("39639243.78").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QjSH91mTDN6TeV1naAcfwPhmRogufV4n1u"), new BigDecimal("23317202.22").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QiFLELeLm2TFWsnknzje51wMdt3Srkjz8g"), new BigDecimal("1554480.148").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QhxtJ3vvhsvVU9x2j5n2R3TXzutfLMUvBR"), new BigDecimal("23317202.22").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QUtUSNQfqexZZkaZ2s9LcpqjnTezPTnuAx"), new BigDecimal("15544801.48").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qg6sPLxNMYxjEDGLLaFkkWx6ip3py5fLEt"), new BigDecimal("777240.0742").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QeLixskYbdkiAHmhBVMa2Pdi85YPFqw3Ed"), new BigDecimal("38862003.71").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qary17o9qvZ2fifiVC8tF5zoBJm79n18zA"), new BigDecimal("3893972.772").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QLvCWDGwzwpR29XgiThMGDX2vxyFW5rFHB"), new BigDecimal("8790585.239").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qgc77fSoAoUSVJfq62GxxTin6dBtU7Y6Hb"), new BigDecimal("194310018.5").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QPmPKjwPLCuRei6abuhMtMocxAEeSuLVcv"), new BigDecimal("23317202.22").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QcGfZePUN7JHs9WEEkJhXGzALy4JybiS3N"), new BigDecimal("194224522.1").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QSeXGwk7eQjR8j7bndJST19qWtM2qnqL1u"), new BigDecimal("38862003.71").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QU9i68h71nKTg4gwc5yJHzNRQdQEswP7Kn"), new BigDecimal("139592317.3").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QdKrZGCkwXSSeXJhVA1idDXsA4VFtrjPHN"), new BigDecimal("15544801.48").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QiYJ2B797xFpWYFu4XWivhGhyPXLU7S5Mr"), new BigDecimal("77724007.42").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QWxqtsNXUWSjYns2wdngh4WBSWQzLoQHvx"), new BigDecimal("232613963.9").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QTAGfu4FpTZ1bnvnd17YPtB3zabxfWKNeM"), new BigDecimal("101041209.6").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QPtRxchgRdwdnoZRwhiAoa77AvVPNSRcQk"), new BigDecimal("114254290.9").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QMcfoVc9Jat2pMFLHcuPEPnY6p6uBK6Dk7"), new BigDecimal("77724007.42").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qi84KosdwSWHZX3qz4WcMgqYGutBmj14dd"), new BigDecimal("15544801.48").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QjAtcHsgig2tvdGr5tR4oGmRarhuojrAK1"), new BigDecimal("2883560.675").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QPJPNLP2NMHu5athB7ydezdTA6zviCV378"), new BigDecimal("6373368.608").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QfVLpmLbuUnA1JEe9FmeUAzihoBvqYDp8B"), new BigDecimal("15544801.48").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QVVFdy6VLFqAFCb6XSBJLLZybiKgfgDDZV"), new BigDecimal("10725913.02").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QVFXyeG1xpAR8Xg3u7oAmW8unueGAfeaKi"), new BigDecimal("31221733.78").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QdtQtrM1h3TLtwAGCNyoTrW5HyiPRLhrPq"), new BigDecimal("138426457.2").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QMukUMr84Mi2niz6rdhEJMkKJBve7uuRfe"), new BigDecimal("116586011.1").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QZR8c7dmfwqGPujebFH1miQToJZ4JQfU1X"), new BigDecimal("217938116.8").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QVV5Uu8eCxufTrBtquDKA96d7Kk8S4V7yX"), new BigDecimal("40091961.25").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QY9YdgfTEUFvQ2UJszGS63qkwdENkW1PQ5"), new BigDecimal("154670774.8").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QNgiswyhVyNJG4UMzvoSf29wDvGZqqs7WG"), new BigDecimal("11658601.11").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QabjgFiY34oihNkUcy9hpFjQdCaypCShMe"), new BigDecimal("54406805.19").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QionidPRekdshCTRL3c7idWWRAqGYcKaFN"), new BigDecimal("7772400.742").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QcJdBJiVgiNBNg6ZwZAiEfYDMi5ZTQaYAa"), new BigDecimal("81386689.86").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QNc8XMpPwM1HESwB7kqw8HoQ5sK2miZ2un"), new BigDecimal("190423818.2").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QUP1SeaNw7CvCnmDp5ai3onWYwThS4GEpu"), new BigDecimal("3886200.371").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QinToqEztNN1TsLdQEuzTHh7vUrEo6JTU2"), new BigDecimal("102440241.8").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QcLJYLV4RD4GmPcoNnh7dQrWeYgiiPiqFQ"), new BigDecimal("32644083.11").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QdYdYGYfgmMX4jQNWMZqLr81R3HdnuiKkv"), new BigDecimal("76169527.27").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qi62mUW5zfJhgRL8FRmCpjSCCnSKtf76S6"), new BigDecimal("76169527.27").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QgFkxqQGkLW6CD95N2zTnT1PPqb9nxWp6b"), new BigDecimal("76169527.27").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QfNUBudYsrrq27YqiHGLUg6BtG52W1W1ci"), new BigDecimal("15544801.48").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QPSFoexnGoMH7EPdg72dM7SvqA7d4M2cu7"), new BigDecimal("37307523.56").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QQxt5WMvoJ2TNScAzcoxHXPnLTeQ43nQ7N"), new BigDecimal("21995894.1").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QicpACxck2oDYpzP8iWRQYD4oirCtvjok9"), new BigDecimal("93268808.9").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QVTJkdQkTGgqEED9kAsp4BZbYNJqWfhgGw"), new BigDecimal("153909079.5").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QQL5vCkhpXnP9F4wqNiBQsNaCocmRcDSUY"), new BigDecimal("15512934.64").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QSvEex3p2LaZCVBaCyL8MpYsEpHLwed17r"), new BigDecimal("155448014.8").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qb3Xv96GucQpBG8n96QVFgcs2xXsEWW4CE"), new BigDecimal("38862003.71").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QdRua9MqXufALpQFDeYiQDYk3EBGdwGXSx"), new BigDecimal("230303229.1").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("Qh16Umei91JqiHEVWV8AC6ED9aBqbDYuph"), new BigDecimal("231073474").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QMu6HXfZCnwaNmyFjjhWTYAUW7k1x7PoVr"), new BigDecimal("231073474").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QgcphUTiVHHfHg8e1LVgg5jujVES7ZDUTr"), new BigDecimal("115031531").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QbQk9s4j4EAxAguBhmqA8mdtTct3qGnsrx"), new BigDecimal("138348733.2").setScale(8), genesisTimestamp));
			this.addTransaction(new GenesisTransaction(new Account("QT79PhvBwE6vFzfZ4oh5wdKVsEazZuVJFy"), new BigDecimal("6360421.343").setScale(8), genesisTimestamp));
			
			//GENERATE AND VALIDATE TRANSACTIONSSIGNATURE
			this.setTransactionsSignature(generateHash());
		}
	}
	
	public String getTestNetInfo() 
	{
		return this.testnetInfo;
	}
	
	//GETTERS
	
	@Override
	public Block getParent()
	{
		//PARENT DOES NOT EXIST
		return null;
	}
	
	//SIGNATURE

	public static byte[] generateHash()
	{
		byte[] data = new byte[0];
		
		//WRITE VERSION
		byte[] versionBytes = Longs.toByteArray(genesisVersion);
		versionBytes = Bytes.ensureCapacity(versionBytes, 4, 0);
		data = Bytes.concat(data, versionBytes);
		
		//WRITE REFERENCE
		byte[] referenceBytes = Bytes.ensureCapacity(genesisReference, 64, 0);
		data = Bytes.concat(data, referenceBytes);
		
		//WRITE GENERATING BALANCE
		byte[] generatingBalanceBytes = Longs.toByteArray(genesisGeneratingBalance);
		generatingBalanceBytes = Bytes.ensureCapacity(generatingBalanceBytes, 8, 0);
		data = Bytes.concat(data, generatingBalanceBytes);
		
		//WRITE GENERATOR
		byte[] generatorBytes = Bytes.ensureCapacity(genesisGenerator.getPublicKey(), 32, 0);
		data = Bytes.concat(data, generatorBytes);
		
		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);		
		digest = Bytes.concat(digest, digest);
		
		return digest;
	}
	
	//VALIDATE
	
	@Override
	public boolean isSignatureValid()
	{
		byte[] data = new byte[0];
		
		//WRITE VERSION
		byte[] versionBytes = Longs.toByteArray(genesisVersion);
		versionBytes = Bytes.ensureCapacity(versionBytes, 4, 0);
		data = Bytes.concat(data, versionBytes);
				
		//WRITE REFERENCE
		byte[] referenceBytes = Bytes.ensureCapacity(genesisReference, 64, 0);
		data = Bytes.concat(data, referenceBytes);
				
		//WRITE GENERATING BALANCE
		byte[] generatingBalanceBytes = Longs.toByteArray(genesisGeneratingBalance);
		generatingBalanceBytes = Bytes.ensureCapacity(generatingBalanceBytes, 8, 0);
		data = Bytes.concat(data, generatingBalanceBytes);
				
		//WRITE GENERATOR
		byte[] generatorBytes = Bytes.ensureCapacity(genesisGenerator.getPublicKey(), 32, 0);
		data = Bytes.concat(data, generatorBytes);
				
		//DIGEST
		byte[] digest = Crypto.getInstance().digest(data);		
		digest = Bytes.concat(digest, digest);
						
		//VALIDATE BLOCK SIGNATURE
		if(!Arrays.equals(digest, this.generatorSignature))
		{
			return false;
		}
		
		//VALIDATE TRANSACTIONS SIGNATURE
		if(!Arrays.equals(digest, this.transactionsSignature))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isValid(DBSet db)
	{
		//CHECK IF NO OTHER BLOCK IN DB
		if(db.getBlockMap().getLastBlock() != null)
		{
			return false;
		}
		
		//VALIDATE TRANSACTIONS
		for(Transaction transaction: this.getTransactions())
		{
			if(transaction.isValid(db) != Transaction.VALIDATE_OK)
			{
				return false;
			}
		}
		
		return true;
	}
	
	private static byte[] generateAccountSeed(byte[] seed, int nonce) 
	{		
		byte[] nonceBytes = Ints.toByteArray(nonce);
		byte[] accountSeed = Bytes.concat(nonceBytes, seed, nonceBytes);
		return Crypto.getInstance().doubleDigest(accountSeed);		
	}	
}
