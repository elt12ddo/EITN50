package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class Server {

	public static void main(String[] args) throws Exception {
		int port = 6789;
		InetAddress host = InetAddress.getByName("localhost");
		DatagramSocket socket = new DatagramSocket(port);
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer,1024);
		socket.receive(packet);
		//String s = new String(packet.getData(),0,packet.getLength());
		System.out.println(packet.getAddress()+", "+packet.getPort()+", \n"+new BigInteger(packet.getData()));
		//System.out.println(s);
		int receivePort = 9877;
		BigInteger g = BigInteger.probablePrime(1024, new Random());
		DatagramPacket sendPacket = new DatagramPacket(g.toByteArray(),g.toByteArray().length,host,receivePort);
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
				DatagramPacket out = new DatagramPacket(buffer,buffer.length,host,receivePort);
				socket.send(out);
			
		}
		
		socket.close();
		
		System.out.println("I am the Server, feel my drives power");

	}

}
