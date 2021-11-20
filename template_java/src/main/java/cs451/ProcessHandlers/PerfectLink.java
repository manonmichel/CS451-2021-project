package cs451.ProcessHandlers;

import cs451.Broadcast.Broadcast;
import cs451.Host;
import cs451.Messages.Message;

import java.util.HashMap;
import java.util.HashSet;

public class PerfectLink{

    private final FairlossLink fll;
    private final Host currentHost;

    private final int timeout = 1;

    // Keeps track of sent messages and whether they were acked or not
    private final HashMap<Integer, Boolean> sent = new HashMap();
    // Keeps track of received messages
    private final HashSet<Integer> received = new HashSet<>();

    Broadcast broadcastMethod;

    public PerfectLink(Host currentHost){
        this.fll = new FairlossLink(currentHost);
        this.currentHost = currentHost;
    }


    public void receive() {
        Message msg = null;

        msg = fll.receive();

        if(msg == null){
            return;
        }

/*        // DEBUGGING
        if(msg.getSeqNumber() == 300){
            System.out.println("Message 300 is in " + "pl:receive");
        }*/

        switch (msg.getMsgType()) {
            case BROADCAST:
                this.deliver(msg);
                fll.send(msg.genAck());
                return;

            case ACK:
                //msg.getDstHost()
                currentHost.deliverAck(msg);
                return;
        }

    }

    public void send(Message msg) {
        if (this.currentHost == msg.getDstHost()) {
            deliver(msg);
        } else {
           // fll.send(msg);
            while(!currentHost.ackReceived(msg)){
                fll.send(msg);

                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //receive ack
                receive();
            }
        }

/*        // DEBUGGING
        if(msg.getSeqNumber() == 300){
            System.out.println("Message 300 is in " + "pl:send");
        }*/
    }

    public void deliver(Message message) {
        broadcastMethod.deliver(message);
    }

    public void setBroadcastMethod(Broadcast broadcastMethod){
        this.broadcastMethod = broadcastMethod;
    }

/*    public void send(Message msg, Host dstHost) {
        msg.setDstHost(dstHost);

        if (this.currentHost == msg.getDstHost()) {
            currentHost.deliver(msg);
        } else {
            while(!currentHost.ackReceived(msg)){
                fll.send(msg);

                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //receive ack
                receive();
            }
        }
    }*/
}