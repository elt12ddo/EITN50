package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {

	public static void main(String[] args) throws Exception {
		int port = 9876;
		DatagramSocket socket = new DatagramSocket(port);
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer,1024);
		socket.receive(packet);
		String s = new String(packet.getData(),0,packet.getLength());
		System.out.println(s);
		socket.close();
		
		//System.out.println("I am the Server, feel my drives power");

	}

}
