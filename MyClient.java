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

				if (jobData[0].equals("JOBN") || jobData[0].equals("JOBP")){ // Handle JOBN and JOBP commands
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
					for (int i = 0; i < serverlist.size(); i++) {
						Server currentServer = serverlist.get(i);				
						int fitness = currentServer.coreNum - jobSize; //Fitness value

						//Find a server that is the best fit
						if (fitness < bestFit && fitness >= 0) {
							bestFit = fitness;
							serverIndex = i;
						}
					}

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

					String command = "SCHD " + jobData[2] + " " + serverlist.get(serverIndex).serverType + " " + serverlist.get(serverIndex).serverID + "\n";

					out.write(command.getBytes()); // Sends SCHD command to current server in the rotation

					str = (String)in.readLine(); // Get the OK command
				} else if (jobData[0].equals("JCPL")) {//search for waiting jobs that can be transfered to the server that was just freed
					//Get Servers				 Core				Memory			   Disk
					out.write(("GETS All\n").getBytes());
					str = (String)in.readLine();
					out.write(("OK\n").getBytes());

					int numOfServers = Integer.parseInt(str.split(" ")[1]); //Get number of servers sent over DATA
					List<Server> serverlist = new LinkedList<>();
					Server targetServer = new Server("0","0","0","0","0","0","0","0","0");

					for (int i = 0; i < numOfServers; i++){ //Loop through until all servers have been recorded
						str = (String)in.readLine(); // Read input, which contains server data
						String[] serverData = str.split(" "); // Split data
						serverlist.add(new Server(serverData[0], serverData[1], serverData[2], serverData[3], serverData[4], serverData[5], serverData[6], 
							serverData[7], serverData[8]));
						if (serverlist.get(i).serverType.equals(jobData[3]) && serverlist.get(i).serverID == Integer.parseInt(jobData[4])) {
							targetServer = serverlist.get(i);
						}
					}

					out.write(("OK\n").getBytes());
					str = (String)in.readLine();

					if (targetServer.serverType.equals("0")) {
						System.out.println("ERROR: Source server for JCPL does not exist");
					} else {
						//Search server-by-server for a waiting job and try to assign it to the freed-up server
						boolean migrationComplete = false;

						for (int i = 0; i < serverlist.size() && !migrationComplete; i++) {
							Server currentServer = serverlist.get(i);

							if (!currentServer.equals(targetServer)){ //skip target server
								if (currentServer.waitingJobs > 0) {
									String command = "LSTJ " + currentServer.serverType + " " + currentServer.serverID + "\n";
									out.write(command.getBytes()); //Send LSTJ command
									str = (String)in.readLine(); //Get the DATA command
	
									int noJobs = Integer.parseInt(str.split(" ")[1]);
									String[][] jobs = new String[noJobs][8];
	
									out.write(("OK\n").getBytes());
	
									for (int j = 0; j < noJobs; j++) {
										str = (String)in.readLine(); // Read input, which contains job data
										String[] split = str.split(" "); // Split data
										jobs[j] = split; // Save job data to array
									}
	
									out.write(("OK\n").getBytes());
									str = (String)in.readLine();
	
									for (int j = 0; j < noJobs && !migrationComplete; j++) {
										if (jobs[j][1].equals("1")){ //job is waiting
											// Check to see whether the target server is capable of running the job.
											int jobSize = Integer.parseInt(jobs[j][5]);
											int jobMem = Integer.parseInt(jobs[j][6]);
											int jobDisk = Integer.parseInt(jobs[j][7]);

											// If server is capable of running the job immediately, migrate the job
											if (jobSize <= targetServer.coreNum && jobMem <= targetServer.memory && jobDisk <= targetServer.diskSize) {
												command = "MIGJ " + jobs[j][0] + " " + currentServer.serverType + " " + currentServer.serverID + " " + targetServer.serverType + " " + targetServer.serverID + "\n";
												out.write(command.getBytes());
												str = (String)in.readLine();

												migrationComplete = true;
											}
										}
									}
								}
							} 
						}
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