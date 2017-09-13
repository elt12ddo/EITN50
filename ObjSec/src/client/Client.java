package client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;

public class Client {

	public static void main(String[] args) throws Exception {
		DatagramSocket socket = new DatagramSocket();
		int port = 9876;
		InetAddress host = InetAddress.getByName("localhost");
		
		String s = "Some string that is sent over the internet!";
		
		DatagramPacket packet = new DatagramPacket(s.getBytes(),s.length(),host,port);
		socket.send(packet);
		socket.close();
		
		
		
		//System.out.println("Hello, client here");

	}

}
