import java.io.*;
import java.net.*;
import java.util.List;
import java.util.LinkedList;

public class MyClient {
	public static void main(String[] args) {
		try {
			//Initiation
			Socket s = new Socket("localhost",50000);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream out = new DataOutputStream(s.getOutputStream());

			//Initial handshake			
			out.write(("HELO\n").getBytes());
			
			String str = (String)in.readLine();
			// System.out.println("Reached Handshake");
			
			//Authenticate
			String username = System.getProperty("user.name");
			out.write(("AUTH " + username + "\n").getBytes());
			
			str = (String)in.readLine();
			// System.out.println("Reached Authenticate");

			//Ready
			out.write(("REDY\n").getBytes());

			str = (String)in.readLine();
			// System.out.println("Reached Ready");

			String job = String.valueOf(str);

			// Assign jobs to the largest servers (LRR)
			while (!job.equals("NONE")) {
				String[] splStrings = job.split(" ");

				if (splStrings[0].equals("JOBN") || splStrings[0].equals("JOBP")){ // Hand JOBN and JOBP commands, ignore JCPD commands
					//Get Servers
					out.write(("GETS Capable " + splStrings[4] + " " + splStrings[5] + " " + splStrings[6] + "\n").getBytes());

					str = (String)in.readLine();

					out.write(("OK\n").getBytes());

					// System.out.println("Reached Get Servers");

					int numOfServers = Integer.parseInt(str.split(" ")[1]); //Get number of servers sent over DATA
					List<Server> serverlist = new LinkedList<>();

					for (int i = 0; i < numOfServers; i++){ //Loop through until all servers have been recorded
						str = (String)in.readLine(); // Read input, which contains server data
						String[] splString = str.split(" "); // Split data
						serverlist.add(new Server(splString[0], splString[1], splString[2], splString[3], splString[4], splString[5], splString[6], splString[7]));
						// System.out.println("Added Server");
					}

					out.write(("OK\n").getBytes());
					// System.out.println("Reached Serverlist");
			
					str = (String)in.readLine();
					String command = "SCHD " + splStrings[2] + " " + serverlist.get(0).serverType + " " + serverlist.get(0).serverID + "\n";

					out.write(command.getBytes()); // Sends SCHD command to current server in the rotation

					str = (String)in.readLine(); // Get the OK command
					// System.out.println("Reached Schedule Command");
				}

				out.write(("REDY\n").getBytes());

				str = (String)in.readLine();
				job = String.valueOf(str);
				// System.out.println("Reached Ready");
			}

			// Exit
			out.write(("QUIT\n").getBytes());
			
			str = (String)in.readLine();
			
			s.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}