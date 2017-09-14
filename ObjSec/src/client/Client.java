package client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.math.BigInteger;
import java.net.DatagramPacket;

public class Client {

	public static void main(String[] args) throws Exception {
		DatagramSocket socket = new DatagramSocket();
		int port = 9876;
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		InetAddress host = InetAddress.getByName("localhost");
		
		String s = "Some string that is sent over the internet!";
		
		//DatagramPacket sendpacket = new DatagramPacket(s.getBytes(),s.length(),host,port);
		BigInteger g = BigInteger.probablePrime(2048, new Random());
		DatagramPacket sendPacket = new DatagramPacket(g.toByteArray(),g.toByteArray().length,host,port);
		socket.send(sendPacket);
		/*
		socket.receive(packet);
		System.out.println(packet.getAddress()+", "+packet.getPort()+", \n"+new String(packet.getData(),0,packet.getLength()));
		*/
		socket.close();
		
		
		
		System.out.println("Hello, client here");

	}

}
