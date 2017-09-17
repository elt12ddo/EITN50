package utility;

import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class MockClientServer {
	protected final byte HELLO = 0;
	protected final byte INIT_DH = 1;
	protected final byte PUBLIC_KEY = 2;

	protected InetAddress host;
	protected DatagramSocket socket;
	protected BigInteger p;
	protected BigInteger g;
	protected Crypto crypt;
	
	protected byte[] calcHash(byte[] in) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		return md.digest(in);
	}
}
