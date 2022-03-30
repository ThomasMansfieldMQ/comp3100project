package dsclient;
import java.io.*;
import java.net.*;
import java.util.*;

public class MyClient {
	public static void main(String[] args) {
		try {
			//Initiation
			Socket s = new Socket("localhost",60000);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream out = new DataOutputStream(s.getOutputStream());

			//Initial handshake			
			out.write(("HELO\n").getBytes());
			System.out.println("Sent: HELO");
			
			String str = (String)in.readLine();
			System.out.println("Received: " + str);
			
			//Authenticate
			String username = System.getProperty("user.name");
			out.write(("AUTH " + username + "\n").getBytes());
			System.out.println("Sent: AUTH");
			
			str = (String)in.readLine();
			System.out.println("Received: " + str);

			//Ready
			out.write(("REDY\n").getBytes());
			System.out.println("Sent: REDY");

			str = (String)in.readLine();
			System.out.println("Received: " + str);
			
			//Get Servers
			out.write(("GETS All\n").getBytes());
			System.out.println("Sent: GETS All");

			str = (String)in.readLine();
			System.out.println("Received: " + str);

			out.write(("OK\n").getBytes());
			System.out.println("Sent: OK");

			int numOfServers = Integer.parseInt(str.split(" ")[1]);
			List<Server> serverlist = new LinkedList<>();

			for (int i = 0; i < numOfServers; i++){
				str = (String)in.readLine();
				String[] splString = str.split(" ");
				serverlist.add(new Server(splString[0], splString[1], splString[2], splString[3], splString[4], splString[5], splString[6], splString[7]));
			}

			out.write(("OK\n").getBytes());
			System.out.println("Sent: OK");
	
			str = (String)in.readLine();
			System.out.println("Received: " + str);
			
			//Sort servers in ascending size
			serverlist.sort(new Comparator<Server>() {
				public int compare(Server a, Server b) {
					return b.coreNum - a.coreNum;
				}
			});

			Server largest = serverlist.get(0);

			// Assign 1 job to the largest server.
			out.write(("REDY\n").getBytes());
			System.out.println("Sent: REDY");

			str = (String)in.readLine();
			System.out.println("Received: " + str);

			int jobID = Integer.parseInt(str.split(" ")[2]);

			String command = "SCHD " + jobID + " " + largest.serverType + " " + largest.serverID + "\n";
			out.write(command.getBytes());
			System.out.print("Sent: " + command);

			str = (String)in.readLine();
			System.out.println("Received: " + str);

			// Assign jobs to the largest servers (LRR) (WIP)

			// Exit
			out.write(("QUIT\n").getBytes());
			System.out.println("Sent: QUIT");
			
			str = (String)in.readLine();
			System.out.println("Received: " + str);
			
			s.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}