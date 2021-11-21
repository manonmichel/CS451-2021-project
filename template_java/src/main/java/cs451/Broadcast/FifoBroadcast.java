package cs451.Broadcast;

import cs451.Host;
import cs451.Messages.Message;
import cs451.ProcessHandlers.PerfectLink;

import java.util.ArrayList;
import java.util.List;

public class FifoBroadcast implements Broadcast {


    private UniformReliableBroadcast urb;
    private PerfectLink pl;
    //private ArrayList order;
    private Host currentHost;


    public FifoBroadcast(PerfectLink perfectLink, List<Host> hosts, Host currentHost) {
        this.urb = new UniformReliableBroadcast(perfectLink, hosts, this);
        this.pl = perfectLink;
        this.currentHost = currentHost;
        //this.order = new ArrayList<Integer>(hosts.size());
    }


    @Override
    public void broadcast(Message message) {
        urb.broadcast(message);
    }

    @Override
    public void deliver(Message message) {
        //int srcID = message.getSrcHost().getId();
        currentHost.deliver(message);
    }

    @Override
    public PerfectLink getPerfectLink() {
        return pl;
    }
}
