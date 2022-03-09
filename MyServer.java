import java.io.*;
import java.net.*;

public class MyServer {
	public static void main(String[] args) throws IOException{
		ServerSocket ss = new ServerSocket (6666);
		try{
			Socket s = ss.accept(); //establishes connection				
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			
			String str = (String)in.readUTF();
			System.out.println("Received: " + str);
			
			out.writeUTF("G'Day");
			System.out.println("Sent: G'Day");
			
			str = (String)in.readUTF();
			System.out.println("Received: " + str);
			
			out.writeUTF("Bye");
			System.out.println("Sent: Bye");
							
			s.close();
			
		} catch (Exception e) {
			System.out.println(e);
		}
	
	}
}
