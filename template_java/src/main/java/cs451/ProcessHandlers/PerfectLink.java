package cs451.ProcessHandlers;

import cs451.Broadcast.Broadcast;
import cs451.Host;
import cs451.Messages.Message;

import java.util.HashMap;
import java.util.HashSet;

public class PerfectLink {

    private final FairlossLink fll;
    private final Host currentHost;

    private final int timeout = 1;


    // Keeps track of sent messages and whether they were acked or not
    private final HashMap<Host, HashMap<String, Boolean>> sent = new HashMap();
    // Keeps track of received messages
    private final HashSet<String> received = new HashSet<>();
    // Keeps track of received acks
    private final HashSet<String> receivedACKS = new HashSet<>();

    Broadcast broadcastMethod;

    public PerfectLink(Host currentHost) {
        this.fll = new FairlossLink(currentHost);
        this.currentHost = currentHost;
    }


    public void receive() {
        Message msg = null;

        msg = fll.receive();

        if (msg == null) {
            return;
        }

        Host srcHost = msg.getSrcHost();
        String sign = msg.getSignature();

        switch (msg.getMsgType()) {
            case BROADCAST:
                if (!received.contains(sign)) {
                    received.add(sign);
                    deliver(msg);
                }
                fll.send(msg.genAck());
                return;

            case ACK:
                sent.putIfAbsent(srcHost, new HashMap<>());
                if (!(sent.get(srcHost).containsKey(sign))) {
/*                    System.out.println("pl:receive - Received an ack for a message that was never sent. :(");
                    System.out.println(sent);
                    System.out.println(msg.toString());*/
                }
                sent.get(srcHost).replace(sign, true);

                return;
        }

    }

    public void send(Message msg) {
        Host dstHost = msg.getDstHost();
        if (this.currentHost == dstHost) {
            deliver(msg);
        } else {
            // fll.send(msg);
            sent.putIfAbsent(dstHost, new HashMap<>());
            sent.get(dstHost).put(msg.getSignature(), false);
            while (!sent.get(dstHost).get(msg.getSignature())) {
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

    }

    public void deliver(Message message) {
        broadcastMethod.deliver(message);
    }

/*    public void notifyAck(Message ack) {
        broadcast.remove(ack.getUid());
    }*/

    public void setBroadcastMethod(Broadcast broadcastMethod) {
        this.broadcastMethod = broadcastMethod;
    }

}