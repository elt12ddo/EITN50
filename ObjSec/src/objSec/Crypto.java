package objSec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

public class Crypto {
	private Key key;
	private Cipher cipher;
	private static final int IV_LENGTH = 16;
	
	public Crypto(){
		Security.addProvider(new BouncyCastleProvider());
		try {
			cipher = Cipher.getInstance("AES/EAX/NoPadding", "BC");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Something went wrong, Error code 1");
		} catch (NoSuchProviderException e) {
			System.out.println("Something went wrong, Error code 2");
		} catch (NoSuchPaddingException e) {
			System.out.println("Something went wrong, Error code 3");
		}
	}
	public void setKey(Key key) {
		this.key = key;
	}
	private byte[] ivGenerator() {
		SecureRandom random = new SecureRandom();
		byte[] iv = new byte[IV_LENGTH];
		random.nextBytes(iv);
		return iv;
	}
	public byte[] encrypt(byte[] inData){
		byte[] iv = ivGenerator();
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return null;
	}
	public byte[] decrypt(byte[] inData) {
		byte[] iv = Arrays.copyOfRange(inData, inData.length - IV_LENGTH, inData.length);
		inData = Arrays.copyOf(inData, inData.length - IV_LENGTH);
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return null;
	}
}