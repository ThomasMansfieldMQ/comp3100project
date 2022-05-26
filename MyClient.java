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
				String[] jobData = job.split(" ");

				if (jobData[0].equals("JOBN") || jobData[0].equals("JOBP")){ // Hand JOBN and JOBP commands, ignore JCPL commands
					//Get Servers				 Core				Memory			   Disk
					out.write(("GETS Capable " + jobData[4] + " " + jobData[5] + " " + jobData[6] + "\n").getBytes());
					str = (String)in.readLine();
					out.write(("OK\n").getBytes());

					int numOfServers = Integer.parseInt(str.split(" ")[1]); //Get number of servers sent over DATA
					List<Server> serverlist = new LinkedList<>();

					for (int i = 0; i < numOfServers; i++){ //Loop through until all servers have been recorded
						str = (String)in.readLine(); // Read input, which contains server data
						String[] serverData = str.split(" "); // Split data
						serverlist.add(new Server(serverData[0], serverData[1], serverData[2], serverData[3], serverData[4], serverData[5], serverData[6], 
							serverData[7], serverData[8]));
					}

					out.write(("OK\n").getBytes());		
					str = (String)in.readLine();

					int jobSize = Integer.parseInt(jobData[4]); //Number of cores needed by the job
					
					int serverIndex = -1; //Holds index of the server to be used
					int bestFit = 99; //Holds best fit
					
					// Enumerate through servers until a suitable one is found
					while (serverIndex == -1){
						for (int i = 0; i < serverlist.size(); i++) {					
							int fitness = serverlist.get(i).coreNum - jobSize; //Fitness value

							//Find a server that is the best fit
							if (fitness < bestFit && fitness > -1) {
								bestFit = fitness;
								serverIndex = i;
							}
						}
						
						// If no server is found, determine why that is the case
						if (serverIndex == -1) {
							boolean solutionFound = false;
							//Enumerate through servers and see if they would pass Best-Fit
							for (int i = 0; i < serverlist.size() && !solutionFound; i++) {		
								Server currentServer = serverlist.get(i);			
								int fitness = currentServer.coreNum - jobSize; //Fitness value

								if (fitness < bestFit) {
									//If yes, the problem is likely because there are servers capable of EVENTUALLY running the job,
									//but not immediately. Check if server is capable of eventually running the job.
										
									String command = "LSTJ " + currentServer.serverType + " " + currentServer.serverID + "\n";
									out.write(command.getBytes()); //Send LSTJ command
									str = (String)in.readLine(); //Get the DATA command

									int noJobs = Integer.parseInt(str.split(" ")[1]);
									int usedCores = 0;

									out.write(("OK\n").getBytes());

									for (int j = 0; j < noJobs; j++) {
										str = (String)in.readLine(); // Read input, which contains job data
										String[] split = str.split(" "); // Split data

										usedCores += Integer.parseInt(split[5]); // Cores used by job
									}

									out.write(("OK\n").getBytes());
									str = (String)in.readLine();

									// Determine if server can eventually run the job, and save its index if yes.
									if ((usedCores + currentServer.coreNum) >= jobSize) {
										serverIndex = i;
										solutionFound = true;
									}
								}
							}						
						}
					}

					String command = "SCHD " + jobData[2] + " " + serverlist.get(serverIndex).serverType + " " + serverlist.get(serverIndex).serverID + "\n";

					out.write(command.getBytes()); // Sends SCHD command to current server in the rotation

					str = (String)in.readLine(); // Get the OK command
				}

				//Search for waiting jobs
				out.write(("GETS All\n").getBytes());
				str = (String)in.readLine();
				out.write(("OK\n").getBytes());

				int numOfServers = Integer.parseInt(str.split(" ")[1]); //Get number of servers sent over DATA
				List<Server> serverlist = new LinkedList<>();

				for (int i = 0; i < numOfServers; i++){ //Loop through until all servers have been recorded
					str = (String)in.readLine(); // Read input, which contains server data
					String[] serverData = str.split(" "); // Split data
					serverlist.add(new Server(serverData[0], serverData[1], serverData[2], serverData[3], serverData[4], serverData[5], serverData[6], 
						serverData[7], serverData[8]));
				}

				out.write(("OK\n").getBytes());		
				str = (String)in.readLine();

				for (int i = 0; i < serverlist.size(); i++) {
					Server currentServer = serverlist.get(i);

					if (currentServer.waitingJobs > 0) {
						//Get info for waiting jobs
						String command = "LSTJ " + currentServer.serverType + " " + currentServer.serverID + "\n";
						out.write(command.getBytes()); //Send LSTJ command
						str = (String)in.readLine(); //Get the DATA command

						int noJobs = Integer.parseInt(str.split(" ")[1]);

						out.write(("OK\n").getBytes());

						//Migrate waiting jobs to the next available server, if possible
						for (int j = 0; j < noJobs; j++) {
							str = (String)in.readLine(); // Read input, which contains job data
							String[] split = str.split(" "); // Split data

							if (split[1] == "1") { //Job is waiting
								//Get Servers				 Core			  Memory		   Disk
								out.write(("GETS Capable " + split[5] + " " + split[6] + " " + split[7] + "\n").getBytes());
								str = (String)in.readLine();
								out.write(("OK\n").getBytes());

								numOfServers = Integer.parseInt(str.split(" ")[1]); //Get number of servers sent over DATA
								List<Server> capableServers = new LinkedList<>();

								for (int k = 0; k < numOfServers; k++){ //Loop through until all servers have been recorded
									str = (String)in.readLine(); // Read input, which contains server data
									String[] serverData = str.split(" "); // Split data
									serverlist.add(new Server(serverData[0], serverData[1], serverData[2], serverData[3], serverData[4], serverData[5], serverData[6], serverData[7], serverData[8]));
								}

								out.write(("OK\n").getBytes());		
								str = (String)in.readLine();

								int jobSize = Integer.parseInt(split[5]); //Number of cores needed by the job
					
								int serverIndex = -1; //Holds index of the server to be used
								int bestFit = 99; //Holds best fit

								for (int k = 0; k < capableServers.size(); k++) {					
									int fitness = capableServers.get(k).coreNum - jobSize; //Fitness value
		
									//Find a server that is the best fit
									if (fitness < bestFit && fitness > -1) {
										bestFit = fitness;
										serverIndex = k;
									}
								}

								// If a free server was found, migrate the job. Otherwise, leave it.
								if (serverIndex != -1) {
									String migration = "MIGJ " + split[0] + " " + currentServer.serverType + " " + currentServer.serverID + " " + capableServers.get(serverIndex).serverType + capableServers.get(serverIndex).serverID + "\n";
									out.write(migration.getBytes()); // Sends SCHD command to current server in the rotation

									str = (String)in.readLine(); // Get the OK command
								}
							}
						}

						out.write(("OK\n").getBytes());
						str = (String)in.readLine();
					}
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