package edu.buffalo.cse.cse486586.simpledynamo;

import android.util.Log;

/**
 * Created by srajappa on 5/5/15.
 */
public class HostInfo {
    String hostPort;
    String successorPort;
    String predecessorPort;

    public HostInfo(){
        Log.i("Class: HOST_INFO","Object Created");
    }
    public HostInfo(String hostPort,String successorPort, String predecessorPort){
        this.hostPort = hostPort;
        this.successorPort = successorPort;
        this.predecessorPort = predecessorPort;
    }
}
