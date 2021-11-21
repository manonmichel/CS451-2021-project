package cs451;

import cs451.Broadcast.Broadcast;
import cs451.Messages.Message;
import cs451.Messages.MessageType;
import cs451.ProcessHandlers.PerfectLink;

import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;

public class Host implements Serializable {

    private static final String IP_START_REGEX = "/";

    private int id;
    private String ip;
    private int port = -1;

    // Keeps track of sent messages and whether they were acked or not
    private transient final HashMap<String, Boolean> sent = new HashMap();
    // Keeps track of received messages
    private transient final HashSet<String> received = new HashSet<>();

    private transient PrintWriter printWriter;

    private transient Broadcast broadcastMethod;
    private transient PerfectLink perfectLink;

    private int nMsgs;


    public void init(int nMsgs, PrintWriter printWriter, Broadcast broadcastMethod) {
        this.nMsgs = nMsgs;
        this.printWriter = printWriter;
        this.broadcastMethod = broadcastMethod;
        this.perfectLink = broadcastMethod.getPerfectLink();
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

            Message m = new Message(i, Integer.toString(i), MessageType.BROADCAST, this);

            printWriter.println("b " + i);
            System.out.println("b " + i);

            String sign = m.getSignature();
            sent.put(sign, false);

            // URB
            broadcastMethod.broadcast(m);
        }

        this.receive();

    }


    public void deliver(Message msg) {
        if (!received.contains(msg.getSignature())) {
            received.add(msg.getSignature());

            printWriter.println("d " + msg.getSrcHost().getId() + " " + msg.getSeqNumber());
            System.out.println("d " + msg.getSrcHost().getId() + " " + msg.getSeqNumber() + " | " + msg.getSignature());
        }
    }


    public void receive() {
        while (true) {
            perfectLink.receive();
        }
    }


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
