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
    private final Host currentHost;

    public BestEffortBroadcast(PerfectLink pl, List<Host> hosts, Broadcast broadcastMethod, Host currentHost) {
        this.perfectLink = pl;
        pl.setBroadcastMethod(this);
        this.hosts = new ArrayList<>(hosts);
        this.broadcastMethod = broadcastMethod;
        this.currentHost = currentHost;
    }

    public void broadcast(Message msg) {
        // DEBUGGING
        if(msg.getSeqNumber() == 300){
            System.out.println("Message 300 is in " + "beb:broadcast");
        }

        for (Host receivingHost : hosts) {
            Message msgCopy = msg.createCopy();
            msgCopy.setDstHost(receivingHost);
            int dstMask = (int) (currentHost.getId()*Math.pow(10,currentHost.getMask()+1));
            msgCopy.setSeqNumber(msg.getSeqNumber() + dstMask);
            currentHost.notifySent(msgCopy);
            perfectLink.send(msgCopy);

        }

    }

    public void deliver(Message msg) {
        // DEBUGGING
/*        if(msg.getSeqNumber() == 300){
            System.out.println("Message 300 is in " + "beb:deliver");
        }*/

        broadcastMethod.deliver(msg);

    }

    public PerfectLink getPerfectLink() {
        return perfectLink;
    }
}