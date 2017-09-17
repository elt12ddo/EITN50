package client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Random;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.agreement.DHAgreement;
import org.bouncycastle.crypto.generators.DHKeyPairGenerator;
import org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.util.Arrays;

import utility.Crypto;
import utility.MockClientServer;
import utility.Utility;

import java.math.BigInteger;
import java.net.DatagramPacket;

public class Client extends MockClientServer {
	private final int port = 6789;
	/*
	private InetAddress host;
	private DatagramSocket socket;
	private BigInteger p;
	private BigInteger g;
	private Crypto crypt;*/

	public static void main(String[] args) throws Exception {
		new Client().doSetup();
	}

	private void doSetup() throws Exception {
		host = InetAddress.getByName("localhost");
		socket = new DatagramSocket(9877);
		g = BigInteger.probablePrime(1024, new Random());
		crypt = new Crypto();
		//TODO
		
		
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
		allReceived = packet.getData();
		
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
		allSent = Utility.concatByte(allSent, temp);
		sendPacket = new DatagramPacket(temp,temp.length,host,port);
		
		// Fetch server response to INIT_DH and extract it.
		socket.receive(packet);
		if(packet.getData()[0] != INIT_DH) { return; }
		temp = packet.getData();
		allReceived = Utility.concatByte(allReceived, temp);
		BigInteger msgServer = new BigInteger(Arrays.copyOfRange(temp, 1, packet.getData().length));
		
		// Send public key to server
		temp = Utility.concatByte(PUBLIC_KEY, publParams.getY().toByteArray());
		allSent = Utility.concatByte(allSent, temp);
		sendPacket = new DatagramPacket(temp,temp.length,host,port);
		socket.send(sendPacket);
		
		// Fetch public key from server
		socket.receive(packet);
		allReceived = Utility.concatByte(allReceived, packet.getData());
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
		temp = crypt.encrypt(Utility.concatByte(calcHash(allReceived),nonce));
		sendPacket = new DatagramPacket(temp,temp.length,host,port);
		socket.send(sendPacket);
		
		// Fetch response from server and decrypt it
		socket.receive(packet);
		temp = crypt.decrypt(packet.getData());
		
		// Check nonce
		if(!Arrays.areEqual(nonce, Arrays.copyOfRange(temp, temp.length - nonce.length, temp.length))) { return; }
		
		// Compare hash to server response
		if(!Arrays.areEqual(calcHash(allSent), Arrays.copyOfRange(temp, 0, temp.length - nonce.length))) { return; }
		
		System.out.println("Handshake DONE");
		doComunication();
		return;
	}

	private void doComunication() {
		// TODO Auto-generated method stub
		
	}

}
