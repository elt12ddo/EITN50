package utility;

import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public abstract class AbstractClientServer {
	protected final byte HELLO = 0;
	protected final byte INIT_DH = 1;
	protected final byte PUBLIC_KEY = 2;
	protected final byte MSG = 3;
	protected final byte DISCONNECT = 4;

	protected InetAddress host;
	protected DatagramSocket socket;
	protected BigInteger p;
	protected BigInteger g;
	protected Crypto crypt;
	private final int allowedTimeOffset = 2; 
	
	protected byte[] calcHash(byte[] in) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		return md.digest(in);
	}
	
	protected boolean checkTimeStamp(long time){
		Instant i = Instant.now();
		Instant o = Instant.ofEpochMilli(time);
		return i.isAfter(o.minusSeconds(allowedTimeOffset)) && i.isBefore(o.plusSeconds(allowedTimeOffset));
	}
}
