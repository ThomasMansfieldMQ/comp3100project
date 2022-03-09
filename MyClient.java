import java.io.*;
import java.net.*;

public class MyClient {
	public static void main(String[] args) {
		try {
			Socket s = new Socket("localhost",6666);
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			
			out.writeUTF("Hello");
			System.out.println("Sent: Hello");
			
			String str = (String)in.readUTF();
			System.out.println("Received: " + str);
			
			out.writeUTF("Bye");
			System.out.println("Sent: Bye");
			
			str = (String)in.readUTF();
			System.out.println("Received: " + str);
			
			s.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
