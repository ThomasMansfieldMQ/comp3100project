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

			// Determine largest server type
			String largestType = serverlist.get(0).serverType;
			List<Server> largestServers = new LinkedList<Server>();

			for (Server server : serverlist) {
				if (server.serverType.equals(largestType)) {
					largestServers.add(server);
				} else break;
			}

			out.write(("REDY\n").getBytes());
			System.out.println("Sent: REDY");

			str = (String)in.readLine();
			System.out.println("Received: " + str);

			int serverIndex = 0;
			// Assign jobs to the largest servers (LRR) (WIP)
			do {
				String[] splStrings = str.split(" ");

				if (splStrings[0].equals("JOBN") || splStrings[0].equals("JOBP")){ // Job Handling
					Server currentServer = largestServers.get(serverIndex);
					String command = "SCHD " + splStrings[2] + " " + currentServer.serverType + " " + currentServer.serverID + "\n";

					out.write(command.getBytes());
					System.out.print("Sent: " + command);

					str = (String)in.readLine();
					System.out.println("Received: " + str);

					serverIndex++;
					serverIndex = serverIndex % largestServers.size();
				}

				out.write(("REDY\n").getBytes());
				System.out.println("Sent: REDY");

				str = (String)in.readLine();
				System.out.println("Received: " + str);
			} while (!str.equals("NONE"));

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