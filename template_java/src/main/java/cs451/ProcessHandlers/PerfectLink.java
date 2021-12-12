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

    private final int timeout = 1;


/*    // Keeps track of sent messages and whether they were acked or not
    private final HashMap<Integer, HashMap<String, Boolean>> sent = new HashMap();
    // Keeps track of received messages
    private final HashSet<String> received = new HashSet<>();
    // Keeps track of received acks
    private final HashSet<String> receivedACKS = new HashSet<>();*/


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


/*    public void receive() {
        Message msg = null;

        msg = fll.receive();

        if (msg == null) {
            return;
        }

        Host srcHost = msg.getSrcHost();
        String sign = msg.getSignature();
        if(msg.getDstHost().equals(srcHost)){
            System.out.println("pl:receive ---- wtf - msg: " + msg.toString());
        }

        switch (msg.getMsgType()) {
            case BROADCAST:
                System.out.println("pl:receive ---- received msg: " + msg.toString());
                if (!received.contains(sign)) {
                    received.add(sign);
                    deliver(msg);
                }

                Message ack = msg.genAck();
                sent.putIfAbsent(msg.getDstHost().getId(), new HashMap<>());
                sent.get(msg.getDstHost().getId()).put(ack.getSignature(), false);
                fll.send(ack);
                return;

            case ACK:
                sent.putIfAbsent(srcHost.getId(), new HashMap<>());
                if (!(sent.get(srcHost.getId()).containsKey(sign))) {
                    System.out.println("currenthost: " + currentHost.getId());
                    System.out.println("pl:receive - Received an ack for a message that was never sent. :(");
                    System.out.println(sent);
                    System.out.println("Ack received: " + msg.toString());
                }
                sent.get(srcHost.getId()).put(sign, true);

                return;
        }

    }*/


    public void send(Message msg, Host dstHost) {
        UUID uid = UUID.randomUUID();
        Message netMsg = msg.addNetworkLayer(currentHost, dstHost, uid);

        if (this.currentHost.equals(dstHost)) {
            deliver(netMsg);
        } else {
            sender.send(netMsg);
            // fll.send(msg);
/*            System.out.println("Sending msg " + msg.toString());
            sent.putIfAbsent(dstHost.getId(), new HashMap<>());
            sent.get(dstHost.getId()).put(msg.getSignature(), false);
            while (!sent.get(dstHost.getId()).get(msg.getSignature())) {
                fll.send(msg);
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //receive ack
                receive();
            }*/
        }

    }





    public void deliver(Message message) {
        if(broadcastMethod == null){
            System.out.println("Broadcast method: " + broadcastMethod);
        }

        broadcastMethod.deliver(message);
    }


    public void setBroadcastMethod(Broadcast broadcastMethod) {
        this.broadcastMethod = broadcastMethod;
    }

}