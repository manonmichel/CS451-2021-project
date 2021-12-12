package cs451;

import cs451.Broadcast.Broadcast;
import cs451.Messages.Message;
import cs451.Messages.MessageType;
import cs451.ProcessHandlers.PerfectLink;

import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Host implements Serializable {

    private static final String IP_START_REGEX = "/";

    private int id;
    private String ip;
    private int port = -1;

    // Keeps track of sent messages and whether they were acked or not
    private transient final List<Message> sent = Collections.synchronizedList(new ArrayList<>());
    // Keeps track of received messages
    public transient List<Message> received = Collections.synchronizedList(new ArrayList<>());

    private transient PrintWriter printWriter;
    private static ConcurrentLinkedQueue<String> outputBuffer;


    private transient Broadcast broadcastMethod;

    private int nMsgs;


    public void init(int nMsgs, PrintWriter printWriter, Broadcast broadcastMethod, ConcurrentLinkedQueue outputBuffer) {
        this.nMsgs = nMsgs;
        this.printWriter = printWriter;
        this.outputBuffer = outputBuffer;

        this.broadcastMethod = broadcastMethod;
    }

    public boolean populate(String idString, String ipString, String portString) {
        try {
            id = Integer.parseInt(idString);

            String ipTest = InetAddress.getByName(ipString).toString();
            if (ipTest.startsWith(IP_START_REGEX)) {
                ip = ipTest.substring(1);
            } else {
                ip = InetAddress.getByName(ipTest.split(IP_START_REGEX)[0]).getHostAddress();
            }

            port = Integer.parseInt(portString);
            if (port <= 0) {
                System.err.println("Port in the hosts file must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            if (port == -1) {
                System.err.println("Id in the hosts file must be a number!");
            } else {
                System.err.println("Port in the hosts file must be a number!");
            }
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return true;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void start() {
        for (int i = 1; i <= nMsgs; i++) {

            String msgSign = Message.makeSignature(this.id, i);
            Message m = new Message(i, Integer.toString(i), MessageType.BROADCAST, msgSign);

            //printWriter.println("b " + i);
            outputBuffer.add("b " + i);

            System.out.println("b " + i);

            // URB
            broadcastMethod.broadcast(m);
        }

        while (sent.size() < nMsgs) {
        }

        System.out.println("Host " + id + " done");

    }


    public void deliver(Message msg) {
        received.add(msg);

        if (msg.getSignature().charAt(0) == this.id) {
            sent.add(msg);
        }
        //printWriter.println("d " + msg.getSrcHost().getId() + " " + msg.getSeqNumber());
        outputBuffer.add("d " + msg.getSignature().charAt(0) + " " + msg.getSignature().substring(2));
        System.out.println("d " + msg.getSignature().charAt(0) + " " + msg.getSignature().substring(2));
    }


/*    public void receive() {
        while (true) {
            perfectLink.receive();
        }
    }*/


    @Override
    public boolean equals(Object h1) {
        if (h1 instanceof Host) {
            Host otherHost = (Host) (h1);
            return this.id == otherHost.getId();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id;
    }
}
