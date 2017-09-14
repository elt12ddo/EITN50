package server;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class Server {

	public static void main(String[] args) throws Exception {
		int port = 9876;
		InetAddress host = InetAddress.getByName("localhost");
		DatagramSocket socket = new DatagramSocket(port);
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer,1024);
		socket.receive(packet);
		//String s = new String(packet.getData(),0,packet.getLength());
		System.out.println(packet.getAddress()+", "+packet.getPort()+", \n"+new BigInteger(packet.getData()));
		//System.out.println(s);
		/*
		BigInteger g = BigInteger.probablePrime(2048, new Random());
		DatagramPacket sendPacket = new DatagramPacket(g.toByteArray(),g.toByteArray().length,host,port);
		socket.send(sendPacket);
		*/
		socket.close();
		
		System.out.println("I am the Server, feel my drives power");

	}

}
