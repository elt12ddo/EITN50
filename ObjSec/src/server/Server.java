package server;

//import java.io.BufferedReader;
//import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
//import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Random;
import java.util.Arrays;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.agreement.DHAgreement;
import org.bouncycastle.crypto.generators.DHKeyPairGenerator;
import org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;

import utility.Crypto;
import utility.AbstractClientServer;
import utility.Utility;

public class Server extends AbstractClientServer{
	private final int port = 9877;

	public static void main(String[] args){
		try{
			new Server().doSetup();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void doSetup() throws Exception{

		System.out.println("Server initialization");
		host = InetAddress.getByName("localhost");
		socket = new DatagramSocket(6789);
		p = BigInteger.probablePrime(1024, new Random());
		crypt = new Crypto();
		//TODO

		System.out.println("Start handshake");
		doHandshake();
		return;
	}

	private void doHandshake() throws Exception{
		byte[] allReceived;
		byte[] allSent;
		byte[] buffer = new byte[1024];
		byte[] temp;
		DatagramPacket packet = new DatagramPacket(buffer,1024);
		//Accept client hello with prime g
		socket.receive(packet);
		allReceived = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		if(allReceived[0] != HELLO){return;}
		//TODO Might be better to throw failedHandshakeException instead of just returning, 
		//also might be a good idea to loop the handshake call so that the server does not have to be restarted every time
		g = new BigInteger(Arrays.copyOfRange(packet.getData(), 1, packet.getLength()));

		//Send server hello with prime p
		temp = Utility.concatByte(HELLO, p.toByteArray());
		allSent = temp; 
		DatagramPacket sendPacket = new DatagramPacket(temp, temp.length,host,port);
		socket.send(sendPacket);

		//Accept initial DH packet from client
		socket.receive(packet);
		allReceived = Utility.concatByte(allReceived, Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
		if(packet.getData()[0] != INIT_DH){return;}
		BigInteger initmsg = new BigInteger(Arrays.copyOfRange(packet.getData(), 1, packet.getLength()));

		//calc DH stuff
		DHKeyPairGenerator gen = new DHKeyPairGenerator();
		DHParameters DHparams = new DHParameters(p,g);
		DHKeyGenerationParameters params = new DHKeyGenerationParameters(new SecureRandom(), DHparams);
		gen.init(params);
		AsymmetricCipherKeyPair keyPair = gen.generateKeyPair();

		DHPublicKeyParameters publParams = (DHPublicKeyParameters)keyPair.getPublic();
		DHPrivateKeyParameters privParams = (DHPrivateKeyParameters)keyPair.getPrivate();

		DHAgreement dha = new DHAgreement();
		dha.init(privParams);
		BigInteger msg = dha.calculateMessage();


		//send initial DH packet from server
		temp = Utility.concatByte(INIT_DH, msg.toByteArray());
		allSent = Utility.concatByte(allSent, temp); 
		sendPacket = new DatagramPacket(temp, temp.length,host,port);
		socket.send(sendPacket);

		//Accept clients public y
		socket.receive(packet);
		allReceived = Utility.concatByte(allReceived, Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
		if(packet.getData()[0] != PUBLIC_KEY){return;}
		BigInteger clientPubY = new BigInteger(Arrays.copyOfRange(packet.getData(), 1, packet.getLength()));

		//calc the shared key 
		DHPublicKeyParameters clientPublParams = new DHPublicKeyParameters(clientPubY,DHparams);
		BigInteger key = dha.calculateAgreement(clientPublParams, initmsg);
		crypt.setKey(key);

		//send server public y
		temp = Utility.concatByte(PUBLIC_KEY, publParams.getY().toByteArray());
		allSent = Utility.concatByte(allSent, temp);
		sendPacket = new DatagramPacket(temp,temp.length,host,port);
		socket.send(sendPacket);

		//accept clients final (handshake that is) message i.e encrypt(hash(all previous messages) + nonce)
		socket.receive(packet);
		byte[] finalMsg = crypt.decrypt(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
		if(finalMsg == null){return;}
		System.out.println("Haaaaaaa");
		allReceived = Utility.concatByte(allReceived, finalMsg);
		byte[] messages = Arrays.copyOfRange(finalMsg, 0, finalMsg.length - 8);

		if(!Arrays.equals(calcHash(allSent), messages)){return;}
		byte[] nonce = Arrays.copyOfRange(finalMsg, finalMsg.length - 8, finalMsg.length);

		//send servers final (handshake) message
		byte[] m = Utility.concatByte(calcHash(allReceived), nonce);
		m = crypt.encrypt(m);//TODO handle null?
		temp = m;
		sendPacket = new DatagramPacket(temp, temp.length,host,port);
		socket.send(sendPacket);

		//All good on the server proceed to data transfer
		System.out.println("Handshake done...");
		System.out.println("Start data transfer");
		doComunication();

		return;
	}

	private void doComunication() throws Exception{
		byte[] buf = new byte[1024];
		DatagramPacket p = new DatagramPacket(buf,1024);
		DatagramPacket sendP;
		long time;
		ByteBuffer bb;
		
		while(true){
		socket.receive(p);
		byte[] msg = crypt.decrypt(Arrays.copyOfRange(p.getData(),0,p.getLength()));
		if(msg == null){System.out.println("Error1");return;}
		if(msg[0] == DISCONNECT){System.out.println("Client disconnected");break;}
		if(msg[0] != MSG){System.out.println("Error2");break;}

		bb = ByteBuffer.allocate(Long.BYTES);
		bb.put(Arrays.copyOfRange(msg, msg.length-8, msg.length));
		bb.flip();
		if(!checkTimeStamp(bb.getLong())){System.out.println("Error3");break;}
		String message = new String(msg,1,msg.length-9);
		String response = "The server received the following message from the client: "+message;
		byte[] m = Utility.concatByte(MSG, response.getBytes());
		time = Instant.now().toEpochMilli();
		bb = ByteBuffer.allocate(Long.BYTES);
		bb.putLong(time);
		m = Utility.concatByte(m, bb.array());
		m = crypt.encrypt(m);//TODO handle null?
		sendP = new DatagramPacket(m,m.length,host,port);
		socket.send(sendP);
		}
		System.out.println("Shutting down server...");

		return;
	}

}

/*

public byte[] longToBytes(long x) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(x);
    return buffer.array();
}

public long bytesToLong(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.put(bytes);
    buffer.flip();//need flip 
    return buffer.getLong();
}
*/