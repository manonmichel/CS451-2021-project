package cs451.Broadcast;

import cs451.Host;
import cs451.Messages.Message;
import cs451.ProcessHandlers.PerfectLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class FifoBroadcast implements Broadcast{


    private UniformReliableBroadcast urb;
    private PerfectLink pl;
    private ArrayList order;


    public FifoBroadcast(PerfectLink perfectLink, List<Host> hosts, Host currentHost){
        this.urb = new UniformReliableBroadcast(perfectLink, hosts, currentHost);
        this.pl = perfectLink;
        this.order = new ArrayList<Integer>(hosts.size());
    }


    @Override
    public void broadcast(Message message) {
        urb.broadcast(message);
    }

    @Override
    public void deliver(Message message) {
        int srcID = message.getSrcHost().getId();


    }

    @Override
    public PerfectLink getPerfectLink() {
        return pl;
    }
}
