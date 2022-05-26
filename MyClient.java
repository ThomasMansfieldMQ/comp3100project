import java.io.*;
import java.net.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Comparator;

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
			
			//Authenticate
			String username = System.getProperty("user.name");
			out.write(("AUTH " + username + "\n").getBytes());
			
			str = (String)in.readLine();

			//Ready
			out.write(("REDY\n").getBytes());

			str = (String)in.readLine();

			String job = String.valueOf(str);

			// Assign jobs 
			while (!job.equals("NONE")) {
				String[] splStrings = job.split(" ");
				int jobSize = Integer.parseInt(splStrings[4]);

				if (splStrings[0].equals("JOBN") || splStrings[0].equals("JOBP")){ // Hand JOBN and JOBP commands, ignore JCPD commands
					//Get Servers
					out.write(("GETS All \n").getBytes());

					str = (String)in.readLine();

					out.write(("OK\n").getBytes());

					int numOfServers = Integer.parseInt(str.split(" ")[1]); //Get number of servers sent over DATA
					List<Server> serverlist = new LinkedList<>();

					for (int i = 0; i < numOfServers; i++){ //Loop through until all servers have been recorded
						str = (String)in.readLine(); // Read input, which contains server data
						String[] splString = str.split(" "); // Split data
						serverlist.add(new Server(splString[0], splString[1], splString[2], splString[3], splString[4], splString[5], splString[6], splString[7]));
					}

					out.write(("OK\n").getBytes());
			
					str = (String)in.readLine();

					//Sort servers in ascending size
					serverlist.sort(new Comparator<Server>() {
						public int compare(Server a, Server b) {
							return b.coreNum - a.coreNum;
						}
					});

					int serverIndex = -1;
					
					for (int i = 0; i < serverlist.size() && serverIndex == -1; i++) {
						if (serverlist.get(i).coreNum >= jobSize) {
							serverIndex = i;
						}
					}

					if (serverIndex == -1) serverIndex = 0;

					String command = "SCHD " + splStrings[2] + " " + serverlist.get(serverIndex).serverType + " " + serverlist.get(serverIndex).serverID + "\n";

					out.write(command.getBytes()); // Sends SCHD command to current server in the rotation

					str = (String)in.readLine(); // Get the OK command
				}

				out.write(("REDY\n").getBytes());

				str = (String)in.readLine();
				job = String.valueOf(str);
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