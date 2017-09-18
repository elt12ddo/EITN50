package utility;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
	private Key key;
	private Cipher cipher;
	private static final int IV_LENGTH = 16;
	/**
	 * This is the Constructor and it creates the Cipher class used to perform encryption and decryption, if it fail to create the Cipher class it will throw an exception.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 */
	public Crypto() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException{
		Security.addProvider(new BouncyCastleProvider());
		cipher = Cipher.getInstance("AES/EAX/NoPadding", "BC");
	}
	/**
	 * This method sets the key that is used by this object to encrypt and  decrypt.
	 * @param The encryption key
	 * @throws NoSuchAlgorithmException 
	 */
	public void setKey(BigInteger integerKey) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		key = new SecretKeySpec(md.digest(integerKey.toByteArray()), "AES");
	}
	private byte[] ivGenerator() {
		SecureRandom random = new SecureRandom();
		byte[] iv = new byte[IV_LENGTH];
		random.nextBytes(iv);
		return iv;
	}
	/**
	 * This method encrypts the inputed byte array with AES in EAX mode, it also concatenates in the IV used to by the encryption.
	 * @param The byte array that is to be encrypted.
	 * @return The concatenated data of the encrypted byte array and the IV needed to decrypt it again.
	 * @throws NoKeyException
	 */
	public byte[] encrypt(byte[] inData) throws NoKeyException{
		if(key == null) {
			throw new NoKeyException();
		}
		byte[] iv = ivGenerator();
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			System.out.println("Something went wrong, Error code 11");
			return null;
		}
		byte[] outData = null;
		try {
			outData = cipher.doFinal(inData);
		} catch (IllegalBlockSizeException e) {
			return null;
		} catch (BadPaddingException e) {
			return null;
		}
		return Utility.concatByte(outData, iv);
	}
	/**
	 * This method decrypts data that have been encrypted with the encrypt method, it automatically reads the IV from the last bytes of the byte array.
	 * If the encrypted byte array has been altered or the key is wrong the method will return a Null instead of the decrypted byte array.
	 * @param The encrypted byte array concatenated with the IV.
	 * @return The decrypted byte array or a Null if the encrypted data cannot be decrypted.
	 * @throws NoKeyException
	 */
	public byte[] decrypt(byte[] inData) throws NoKeyException {
		if(key == null) {
			throw new NoKeyException();
		}
		byte[] iv = Arrays.copyOfRange(inData, inData.length - IV_LENGTH, inData.length);
		inData = Arrays.copyOf(inData, inData.length - IV_LENGTH);
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			System.out.println("Something went wrong, Error code 21");
			return null;
		}
		byte[] outData = null;
		try {
			outData = cipher.doFinal(inData);
		} catch (IllegalBlockSizeException e) {
			return null;
		} catch (BadPaddingException e) {
			return null;
		}
		return outData;
	}
}