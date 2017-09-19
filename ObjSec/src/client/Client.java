package client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
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
import utility.NoKeyException;
import utility.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;

public class Client extends AbstractClientServer {
	private final int port = 6789;

	public static void main(String[] args) throws Exception {
		new Client().doSetup();
	}

	private void doSetup() throws Exception {
		host = InetAddress.getByName("localhost");
		socket = new DatagramSocket(9877);
		g = BigInteger.probablePrime(1024, new Random());
		doHandshake();
		return;
	}

	private void doHandshake() throws Exception {
		byte[] allReceived;
		byte[] allSent;
		byte[] temp;
		byte[] buffer = new byte[1024];
		
		allSent = Utility.concatByte(HELLO, g.toByteArray());
		
		// Create and send initial HELLO packet to server with the prime g.
		DatagramPacket sendPacket = new DatagramPacket(allSent,allSent.length,host,port);
		socket.send(sendPacket);
		
		// Fetch server response, if the flag is not HELLO return.
		DatagramPacket packet = new DatagramPacket(buffer,1024);
		socket.receive(packet);
		if(packet.getData()[0] != HELLO) { return; }
		allReceived = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		
		// Extract the prime p from data.
		p = new BigInteger(Arrays.copyOfRange(packet.getData(), 1, packet.getLength()));
		
		// Initial Diffie-Hellman calculations
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
		
		// Send msg
		temp = Utility.concatByte(INIT_DH, msg.toByteArray());
		if(temp[0] != INIT_DH){System.out.println("HELLO THERE");return;}
		allSent = Utility.concatByte(allSent, temp);
		sendPacket = new DatagramPacket(temp,temp.length,host,port);
		socket.send(sendPacket);
		
		// Fetch server response to INIT_DH and extract it.
		socket.receive(packet);
		if(packet.getData()[0] != INIT_DH) { return; }
		temp = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		allReceived = Utility.concatByte(allReceived, temp);
		BigInteger msgServer = new BigInteger(Arrays.copyOfRange(packet.getData(), 1, packet.getLength()));
		
		// Send public key to server
		temp = Utility.concatByte(PUBLIC_KEY, publParams.getY().toByteArray());
		allSent = Utility.concatByte(allSent, temp);
		sendPacket = new DatagramPacket(temp,temp.length,host,port);
		socket.send(sendPacket);
		
		// Fetch public key from server
		socket.receive(packet);
		allReceived = Utility.concatByte(allReceived, Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
		if(packet.getData()[0] != PUBLIC_KEY) { return; }
		BigInteger serverPubY = new BigInteger(Arrays.copyOfRange(packet.getData(), 1, packet.getLength()));
		
		// Calculate shared secret
		DHPublicKeyParameters serverPublParams = new DHPublicKeyParameters(serverPubY,DHparams);
		BigInteger key = dha.calculateAgreement(serverPublParams, msgServer);
		
		// Create the Crypto object
		crypt = new Crypto();
		crypt.setKey(key);
		
		// Calculate hash of allReceived and send it
		SecureRandom random = new SecureRandom();
		byte[] nonce = new byte[8];
		random.nextBytes(nonce);
		allSent = Utility.concatByte(allSent, Utility.concatByte(calcHash(allReceived),nonce));
		temp = crypt.encrypt(Utility.concatByte(calcHash(allReceived),nonce));
		sendPacket = new DatagramPacket(temp,temp.length,host,port);
		socket.send(sendPacket);
		
		// Fetch response from server and decrypt it
		socket.receive(packet);
		temp = crypt.decrypt(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
		
		// Check so that it actually was decrypted
		if(temp == null) { return; }
		
		// Check nonce
		if(!Arrays.equals(nonce, Arrays.copyOfRange(temp, temp.length - nonce.length, temp.length))) {System.out.println("Hi");return; }
		
		// Compare hash to server response
		if(!Arrays.equals(calcHash(allSent), Arrays.copyOfRange(temp, 0, temp.length - nonce.length))) {System.out.println("Hi2");return; }
		
		System.out.println("Handshake DONE");
		doComunication();
		return;
	}

	private void doComunication() throws IOException, NoKeyException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		DatagramPacket packet = new DatagramPacket(new byte[1024],1024);
		ByteBuffer bb;
		byte[] data;
		DatagramPacket sendPacket;
		while(true) {
			// First we send a line we read from the terminal
			System.out.print("Write message: ");
			bb = ByteBuffer.allocate(Long.BYTES);
			byte[] line = br.readLine().getBytes();
			byte[] time = bb.putLong(Instant.now().toEpochMilli()).array();
			if(Arrays.equals(line, "exit".getBytes())) {
				data = crypt.encrypt(Utility.concatByte(DISCONNECT, time));
				sendPacket = new DatagramPacket(data, data.length, host, port);
				socket.send(sendPacket);
				break;
			} else {
				data = crypt.encrypt(Utility.concatByte(MSG, Utility.concatByte(line, time)));
				sendPacket = new DatagramPacket(data, data.length, host, port);
				socket.send(sendPacket);
			}
			
			// Receive something from the server...
			socket.receive(packet);
			byte[] temp = crypt.decrypt(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
			
			// Check if decrypt atually returns somthing
			if(temp == null) { return; }

			// Check timestamp
			bb = ByteBuffer.allocate(Long.BYTES);
			bb.put(temp, temp.length - 8, 8);
			bb.flip();
			if(!checkTimeStamp(bb.getLong())) { return; }
			
			// Check if the flag is correct
			if(temp[0] != MSG) { return; }
			
			// Print the message from the server
			System.out.println(new String(temp, 1, temp.length - 9));
		}
		System.out.println("Shutting down client...");
	}
}