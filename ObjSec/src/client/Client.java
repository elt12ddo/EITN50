package client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;

public class Client {

	public static void main(String[] args) throws Exception {
		int port = 6789;
		DatagramSocket socket = new DatagramSocket(9877);
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		InetAddress host = InetAddress.getByName("localhost");
		
		String s = "Some string that is sent over the internet!";
		
		//DatagramPacket sendpacket = new DatagramPacket(s.getBytes(),s.length(),host,port);
		BigInteger g = BigInteger.probablePrime(1024, new Random());
		DatagramPacket sendPacket = new DatagramPacket(g.toByteArray(),g.toByteArray().length,host,port);
		socket.send(sendPacket);
		
		socket.receive(packet);
		System.out.println(packet.getAddress()+", "+packet.getPort()+", \n"+new BigInteger(packet.getData()));
		
		
		while(true){
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Write msg: ");
			String text = in.readLine();
			
			
			if(text.equals("Exit")){
				break;
			}else{
				buffer = text.getBytes();
				DatagramPacket out = new DatagramPacket(buffer,buffer.length,host,port);
				socket.send(out);
				
				socket.receive(packet);
				String msg = "Server says: "+new String(packet.getData(),0,packet.getLength());
				System.out.println(msg);
				
				
			}
		}
		
		socket.close();
		
		System.out.println("Hello, client here");

	}

}
