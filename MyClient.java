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

				if (jobData[0].equals("JOBN") || jobData[0].equals("JOBP")){ // Hand JOBN and JOBP commands
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
							Server currentServer = serverlist.get(i);				
							int fitness = currentServer.coreNum - jobSize; //Fitness value

							//Find a server that is the best fit
							if (fitness < bestFit) {
								if (fitness < 0) {
									String command = "LSTJ " + currentServer.serverType + " " + currentServer.serverID + "\n";
									out.write(command.getBytes()); //Send LSTJ command
									str = (String)in.readLine(); //Get the DATA command

									int noJobs = Integer.parseInt(str.split(" ")[1]);
									int usedCores = 0;

									String[][] jobs = new String[noJobs][8];

									out.write(("OK\n").getBytes());

									//Save jobs
									for (int j = 0; j < noJobs; j++) {
										str = (String)in.readLine(); // Read input, which contains job data
										String[] split = str.split(" "); // Split data
										
										jobs[j] = split;
										usedCores += Integer.parseInt(jobs[j][5]);
									}

									out.write(("OK\n").getBytes());
									str = (String)in.readLine();

									// Determine if server has enough cores to run the job, and migrate the currently running jobs to a different
									// server to make room.
									if ((usedCores + currentServer.coreNum) >= jobSize) {
										bestFit = usedCores + currentServer.coreNum - fitness;
										serverIndex = i;

										for (int j = 0; j < noJobs; j++) {
											if (jobs[j][1] == "2"){
												int migrationIndex = serverIndex; //Holds index of the server to be used
												int migrationFit = 99; //Holds best fit
						
												// Enumerate through servers until a suitable one is found
												for (int k = 0; k < serverlist.size(); k++) {
													Server migrationServer = serverlist.get(k);				
													int f = migrationServer.coreNum - Integer.parseInt(jobs[j][5]); //Fitness value

													//Find a server that is the best fit
													if (f < migrationFit && f >= 0) {
														migrationFit = f;
														migrationIndex = k;
													}
												}

												if (migrationIndex != serverIndex) {
													Server migrationServer = serverlist.get(migrationIndex);
													command = "MIGJ " + jobs[j][0] + " " + currentServer.serverType + " " + currentServer.serverID + " " + migrationServer.serverType + " " + migrationServer.serverID + "\n";

													out.write(command.getBytes()); // Sends MIGJ command to current server in the rotation

													str = (String)in.readLine(); // Get the OK command
												}
											}
										}
									}
								} else {
									bestFit = fitness;
									serverIndex = i;
								}
							}
						}
					}

					String command = "SCHD " + jobData[2] + " " + serverlist.get(serverIndex).serverType + " " + serverlist.get(serverIndex).serverID + "\n";

					out.write(command.getBytes()); // Sends SCHD command to current server in the rotation

					str = (String)in.readLine(); // Get the OK command
				} else if (jobData[0].equals("JCPL")) {
					//search for waiting jobs that can be transfered to a new server
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