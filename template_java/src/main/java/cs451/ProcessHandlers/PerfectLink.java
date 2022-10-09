package cs451.ProcessHandlers;

import cs451.Broadcast.Broadcast;
import cs451.Host;
import cs451.Messages.Message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class PerfectLink {

    private final FairlossLink fll;
    private final Host currentHost;

    private final Sender sender;
    public Receiver receiver;



    private Broadcast broadcastMethod;

    public PerfectLink(Host currentHost) {
        this.fll = new FairlossLink(currentHost);
        this.currentHost = currentHost;
        this.sender = new Sender(fll);
        this.receiver = new Receiver(sender, fll);
        receiver.setPerfectLink(this);
        receiver.start();
        sender.start();
    }


    public void send(Message msg, Host dstHost) {
        UUID uid = UUID.randomUUID();
        Message netMsg = msg.addNetworkLayer(currentHost, dstHost, uid);
        netMsg.setVectorClock(msg.getVectorClock());


        if (this.currentHost.equals(dstHost)) {
            deliver(netMsg);
        } else {
            sender.send(netMsg);
        }

    }





    public void deliver(Message msg) {

        if(broadcastMethod == null){
            System.out.println("Broadcast method: " + broadcastMethod);
        }


        broadcastMethod.deliver(msg);
    }


    public void setBroadcastMethod(Broadcast broadcastMethod) {
        this.broadcastMethod = broadcastMethod;
    }

}