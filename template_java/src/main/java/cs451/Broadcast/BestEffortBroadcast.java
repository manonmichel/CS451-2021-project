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
        this.perfectLink = pl;
        pl.setBroadcastMethod(this);
        this.hosts = new ArrayList<>(hosts);
        this.broadcastMethod = broadcastMethod;

    }

    public void broadcast(Message msg) {

        for (Host receivingHost : hosts) {
            Message msgCopy = new Message(msg.getSeqNumber(), msg.getContent(), msg.getMsgType(), msg.getSrcHost(), receivingHost);
            perfectLink.send(msgCopy);

        }

    }

    public void deliver(Message msg) {

        broadcastMethod.deliver(msg);

    }

    public PerfectLink getPerfectLink() {
        return perfectLink;
    }
}