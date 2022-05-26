

public class Server{
    String serverType;
    int serverID;
    String serverState;
    int bootupTime;
    int coreNum;
    int memory;
    int diskSize;
    int waitingJobs;
    int runningJobs;

    public Server(String type, String id, String state, String bootup, String cores, String mem, String disk, String wjobs, String rjobs) {
        serverType = type;
        serverID = Integer.parseInt(id);
        serverState = state;
        bootupTime = Integer.parseInt(bootup);
        coreNum = Integer.parseInt(cores);
        memory = Integer.parseInt(mem);
        diskSize = Integer.parseInt(disk);
        waitingJobs = Integer.parseInt(wjobs);
        runningJobs = Integer.parseInt(rjobs);
    }
}
