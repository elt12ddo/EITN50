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
	private BigInteger key;//TODO remove and process key locally

	public static void main(String[] args) throws Exception{
		new Server().doSetup();
		
		
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

		host = InetAddress.getByName("localhost");
		socket = new DatagramSocket(6789);
		p = BigInteger.probablePrime(1024, new Random());
		crypt = new Crypto();
		//TODO
		
		
		doHandshake();
		return;
	}
	
	private void doHandshake() throws Exception{
		byte[] allreceived;
		byte[] allsent;
		byte[] buffer = new byte[1024];
		byte[] temp;
		DatagramPacket packet = new DatagramPacket(buffer,1024);
		//Accept client hello with prime g
		socket.receive(packet);
		allreceived = packet.getData();
		String flag = new String(packet.getData(),0,5);
		if(flag.equals("Hello")){
			g = new BigInteger(Arrays.copyOfRange(packet.getData(), 5, packet.getLength()));
			
			//Send server hello with prime p
			temp = Utility.concatByte(flag.getBytes(), p.toByteArray());
			allsent = temp; 
			DatagramPacket sendPacket = new DatagramPacket(temp, temp.length);
			socket.send(sendPacket);
			
			//Accept initial DH packet from client
			socket.receive(packet);
			allreceived = Utility.concatByte(allreceived, packet.getData());
			flag = new String(packet.getData(),0,6);
			if(flag.equals("initDH")){
				BigInteger initmsg = new BigInteger(Arrays.copyOfRange(packet.getData(), 6, packet.getLength()));
				
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
				temp = Utility.concatByte(flag.getBytes(), msg.toByteArray());
				allsent = Utility.concatByte(allsent, temp); 
				sendPacket = new DatagramPacket(temp, temp.length);
				socket.send(sendPacket);
				
				//Accept clients public y
				socket.receive(packet);
				allreceived = Utility.concatByte(allreceived, packet.getData());
				flag = new String(packet.getData(),0,6);
				if(flag.equals("pubKey")){
					BigInteger clientPubY = new BigInteger(Arrays.copyOfRange(packet.getData(), 6, packet.getLength()));
					
					//calc the shared key 
					DHPublicKeyParameters clientPublParams = new DHPublicKeyParameters(clientPubY,DHparams);
					key = dha.calculateAgreement(clientPublParams, initmsg);
					//TODO
					//Fix key length
					//and set key in crypt
					
					//send server public y
					temp = Utility.concatByte(flag.getBytes(), publParams.getY().toByteArray());
					allsent = Utility.concatByte(allsent, temp);
					sendPacket = new DatagramPacket(temp,temp.length);
					socket.send(sendPacket);
					
					//accept clients final (handshake that is) message i.e the all previous messages encrypted with the shared key + a nonce
					socket.receive(packet);
					allreceived = Utility.concatByte(allreceived, packet.getData());
					
					byte[] finalMsg = crypt.decrypt(packet.getData());
					if(finalMsg != null){
						byte[] messages = Arrays.copyOfRange(finalMsg, 0, finalMsg.length - 8);
						
						if(Arrays.equals(allsent, messages)){
							byte[] nonce = Arrays.copyOfRange(finalMsg, finalMsg.length - 8, finalMsg.length);
							
							//send servers final (handshake) message
							byte[] m = Utility.concatByte(allreceived, nonce);
							m = crypt.encrypt(m);//TODO handle null?
							temp = m;
							sendPacket = new DatagramPacket(temp, temp.length);
							socket.send(sendPacket);
							
							//All good on the server proceed to data transfer
							doComunication();
							
						}else{
							return;
						}
						
					}else{
						return;
					}
					
				}else{
					return;
				}
				
			}else{
				return;
			}
			
		}else{
			return;
		}	

		return;
	}
	
	private void doComunication() throws Exception{
		//TODO
		
		return;
	}

}
