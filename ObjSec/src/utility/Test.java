package utility;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.time.Instant;
import java.util.Random;

import org.bouncycastle.util.Arrays;

public class Test {

	public static void main(String[] args) {
		
		Instant inst = Instant.now();
		System.out.println(inst);
		long time = inst.toEpochMilli();
		System.out.println(time);
		Instant inst2 = Instant.ofEpochMilli(time);
		System.out.println(inst2);
		
		
		String s = "Hello";
		byte[] buffer = new byte[1024];
		BigInteger i = BigInteger.probablePrime(1024, new Random());
		buffer = Utility.concatByte(s.getBytes(), i.toByteArray());
		DatagramPacket p = new DatagramPacket(buffer,buffer.length);
		String b = new String(p.getData(), 0, 5);
		BigInteger j = new BigInteger(Arrays.copyOfRange(p.getData(), 5, p.getLength()));
		if(s.equals(b)){
			System.out.println("Sträng ok!");
		}
		if(i.compareTo(j)==0){
			System.out.println("BigInt ok!");
		}
		try{
		Crypto c = new Crypto();
		c.setKey(i);
		byte[] enc = c.encrypt(j.toByteArray());
		byte[] dec = c.decrypt(enc);
		BigInteger k = new BigInteger(dec);
		if(j.compareTo(k)==0){
			System.out.println("H");
		}
		}catch(Exception e){
			System.out.println("B");
		}
		

	}

}
