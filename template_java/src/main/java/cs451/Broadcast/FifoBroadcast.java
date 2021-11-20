package cs451.Broadcast;

import cs451.Host;
import cs451.ProcessHandlers.PerfectLink;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FifoBroadcast {


    private UniformReliableBroadcast urb;


    public FifoBroadcast(PerfectLink perfectLink, List<Host> hosts, Host currentHost){
        this.urb = new UniformReliableBroadcast(perfectLink, hosts, currentHost);
    }
}
