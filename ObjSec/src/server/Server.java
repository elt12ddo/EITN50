package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.bouncycastle.util.Arrays;

import objSec.Crypto;
import objSec.Utility;

public class Server {
	private InetAddress host;
	private final int port = 9877;
	private DatagramSocket socket;
	private BigInteger p;
	private BigInteger g;
	private Crypto crypt;

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
		DatagramPacket packet = new DatagramPacket(buffer,1024);
		//Accept client hello with prime g
		socket.receive(packet);
		allreceived = packet.getData();
		String flag = new String(packet.getData(),0,5);
		if(flag.equals("Hello")){
			g = new BigInteger(Arrays.copyOfRange(packet.getData(), 5, packet.getLength()));
			
			//Send server hello with prime p
			buffer = Utility.concatByte(flag.getBytes(), p.toByteArray());
			allsent = buffer; 
			DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length);
			socket.send(sendPacket);
			
			//Accept initial DH packet from client
			socket.receive(packet);
			allreceived = Utility.concatByte(allreceived, packet.getData());
			flag = new String(packet.getData(),0,6);
			if(flag.equals("initDH")){
				BigInteger initmsg = new BigInteger(Arrays.copyOfRange(packet.getData(), 6, packet.getLength()));
				
				
				
				
				
				
				
				
			}else{
				return;
			}
			
		}else{
			return;
		}	
		
		//om allt ok, do com
		doComunication();
		return;
	}
	
	private void doComunication() throws Exception{
		//TODO
		
		return;
	}

}
