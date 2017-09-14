package objSec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptTest {

	public static void main(String[] args) {
		System.out.println("Hello");
		Security.addProvider(new BouncyCastleProvider());
		
		String keyString = "Hello World 1234";
		byte[] key = keyString.getBytes();
		String dataString = "Hello World, Hello World, Hello World";
		byte[] data = dataString.getBytes();
		System.out.println(key.length + " " + data.length + "\n");
		
		
		try {
			encrypt(key, data);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static void encrypt (byte[] keyBytes, byte[] inData) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		Key						key;
		Cipher					in, out;
		
		key = new SecretKeySpec(keyBytes, "AES");
		
		in = Cipher.getInstance("AES/EAX/NoPadding", "BC");
		out = Cipher.getInstance("AES/EAX/NoPadding", "BC");

		SecureRandom random = new SecureRandom();
		byte[] ivBytes = new byte[in.getBlockSize()];
		random.nextBytes(ivBytes);
		
		try {
			in.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivBytes));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		try {
			out.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivBytes));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		
		byte[] encryptedData = null;
		try {
			encryptedData = in.doFinal(inData);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//encryptedData[0]=encryptedData[1];
		byte[] outData = null;
		try {
			outData = out.doFinal(encryptedData);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			System.out.println("Someone have tampered with the data during transfer");
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		if(outData != null && encryptedData != null){
		System.out.println(new String(encryptedData,0,encryptedData.length));
		System.out.println(new String(outData,0,outData.length));
		}
		
	}
}