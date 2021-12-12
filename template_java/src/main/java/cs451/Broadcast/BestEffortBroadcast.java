package cs451.Broadcast;

import cs451.Host;
import cs451.Messages.Message;
import cs451.ProcessHandlers.PerfectLink;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BestEffortBroadcast implements Broadcast, Serializable {
    private final PerfectLink perfectLink;
    private final ArrayList<Host> hosts;
    private final Broadcast broadcastMethod;


    public BestEffortBroadcast(PerfectLink pl, List<Host> hosts, Broadcast broadcastMethod) {

        this.hosts = new ArrayList<>(hosts);
        this.broadcastMethod = broadcastMethod;
        this.perfectLink = pl;
        pl.setBroadcastMethod(this);

    }

    public void broadcast(Message msg) {

        for (Host receivingHost : hosts) {
            perfectLink.send(msg, receivingHost);
        }

    }

    public void deliver(Message msg) {

        broadcastMethod.deliver(msg);

    }

}