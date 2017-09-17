package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
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
import utility.MockClientServer;
import utility.Utility;

public class Server extends MockClientServer{
	//private InetAddress host;
	private final int port = 9877;
	//private DatagramSocket socket;
	//private BigInteger p;
	//private BigInteger g;
	//private Crypto crypt;

	public static void main(String[] args){
		try{
		new Server().doSetup();
		}catch(Exception e){
			e.printStackTrace();
		}


		/*
		int port = 9877;//Port the client listens to
		InetAddress host = InetAddress.getByName("localhost");
		DatagramSocket socket = new DatagramSocket(6789);
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer,1024);
		socket.receive(packet);
		System.out.println(packet.getAddress()+", "+packet.getPort()+", \n"+new BigInteger(packet.getData()));
		BigInteger g = BigInteger.probablePrime(1024, new Random());
		DatagramPacket sendPacket = new DatagramPacket(g.toByteArray(),g.toByteArray().length,host,port);
		socket.send(sendPacket);


		while(true){
			socket.receive(packet);
			String msg = new String(packet.getData(),0,packet.getLength());
			if(msg.equals("SERVER_EXIT")){
				break;
			}
			System.out.println("Client says: " +msg);

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Write your msg: ");
			String text = in.readLine();
			buffer = text.getBytes();
			DatagramPacket out = new DatagramPacket(buffer,buffer.length,host,port);
			socket.send(out);

		}

		socket.close();

		System.out.println("I am the Server, feel my drives power");
		 */

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
		allReceived = packet.getData();
		if(allReceived[0] != HELLO){return;}
		//TODO Might be better to throw failedHandshakeException instead of just returning, 
		//also might be a good idea to loop the handshake call so that the server does not have to be restarted every time
		g = new BigInteger(Arrays.copyOfRange(packet.getData(), 1, packet.getLength()));
		System.out.println("After receiving Hello");

		//Send server hello with prime p
		temp = Utility.concatByte(HELLO, p.toByteArray());
		allSent = temp; 
		DatagramPacket sendPacket = new DatagramPacket(temp, temp.length,host,port);
		socket.send(sendPacket);
		System.out.println("After sending hello");

		//Accept initial DH packet from client
		socket.receive(packet);
		allReceived = Utility.concatByte(allReceived, packet.getData());
		if(packet.getData()[0] != INIT_DH){System.out.println("This?");return;}
		BigInteger initmsg = new BigInteger(Arrays.copyOfRange(packet.getData(), 1, packet.getLength()));
		System.out.println("After receiving init DH");

		//calc DH stuff
		System.out.println("p: "+p);
		System.out.println("g: "+g);
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
		System.out.println("After calc DH");


		//send initial DH packet from server
		temp = Utility.concatByte(INIT_DH, msg.toByteArray());
		allSent = Utility.concatByte(allSent, temp); 
		sendPacket = new DatagramPacket(temp, temp.length,host,port);
		socket.send(sendPacket);
		System.out.println("After sending DH");

		//Accept clients public y
		socket.receive(packet);
		allReceived = Utility.concatByte(allReceived, packet.getData());
		if(packet.getData()[0] != PUBLIC_KEY){return;}
		BigInteger clientPubY = new BigInteger(Arrays.copyOfRange(packet.getData(), 1, packet.getLength()));
		System.out.println("After receiving public y");
		System.out.println(clientPubY);

		//calc the shared key 
		DHPublicKeyParameters clientPublParams = new DHPublicKeyParameters(clientPubY,DHparams);
		BigInteger key = dha.calculateAgreement(clientPublParams, initmsg);
		crypt.setKey(BigInteger.ONE);
		System.out.println("Key: "+key);
		System.out.println("After calc shared key");

		//send server public y
		temp = Utility.concatByte(PUBLIC_KEY, publParams.getY().toByteArray());
		System.out.println(publParams.getY());
		allSent = Utility.concatByte(allSent, temp);
		sendPacket = new DatagramPacket(temp,temp.length,host,port);
		socket.send(sendPacket);
		System.out.println("After sending publ y");

		//accept clients final (handshake that is) message i.e encrypt(hash(all previous messages) + nonce)
		socket.receive(packet);
		System.out.println(new String(packet.getData(),0,packet.getLength()).getBytes().length);
		System.out.println(packet.getLength());
		byte[] finalMsg = crypt.decrypt(packet.getData());//new String(packet.getData(),0,packet.getLength()).getBytes());//packet.getData()); <-- this apparently does not work
		if(finalMsg == null){System.out.println("THis one?");return;}
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
		//TODO

		return;
	}

}
