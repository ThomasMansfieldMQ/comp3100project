package dsclient;

public class Server{
    String serverType;
    int serverID;
    String serverState;
    int bootupTime;
    double hourlyRate;
    int coreNum;
    int memory;
    int diskSize;

    public Server(String type, String id, String state, String bootup, String rate, String cores, String mem, String disk) {
        serverType = type;
        serverID = Integer.parseInt(id);
        serverState = state;
        bootupTime = Integer.parseInt(bootup);
        hourlyRate = Double.parseDouble(rate);
        coreNum = Integer.parseInt(cores);
        memory = Integer.parseInt(mem);
        diskSize = Integer.parseInt(disk);
    }
}
