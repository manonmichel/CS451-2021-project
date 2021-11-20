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

public class Host implements Serializable{

    private static final String IP_START_REGEX = "/";

    private int id;
    private String ip;
    private int port = -1;

    // Keeps track of sent messages and whether they were acked or not
    private transient final HashMap<Integer, Boolean> sent = new HashMap();
    // Keeps track of received messages
    private transient final HashSet<Integer> received = new HashSet<>();

    private transient PrintWriter printWriter;

    private transient Broadcast broadcastMethod;
    private transient PerfectLink perfectLink;

    private int nMsgs ;
    private int mask;

    public void init(int nMsgs, PrintWriter printWriter, Broadcast broadcastMethod){
        this.nMsgs = nMsgs;
        this.printWriter = printWriter;
        this.broadcastMethod = broadcastMethod;
        this.perfectLink = broadcastMethod.getPerfectLink();

        this.mask = String.valueOf(nMsgs).length();
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
            int seqn = (int) (id*Math.pow(10,mask) + i);
            Message m = new Message(seqn, Integer.toString(i), MessageType.BROADCAST, this);
            /*printWriter.println("b " + seqn);
            System.out.println("b " + seqn);*/
            printWriter.println("b " + i);

            //sent.put(seqn, false);

            // PL
            //pl.send(m);

            // DEBUGGING
            if(seqn == 300){
                System.out.println("Message 300 is in " + "host:start");
            }

            // Best effort
            broadcastMethod.broadcast(m);
        }

        this.receive();

/*        while(received.size() <= nMsgs*3){
        }*/
    }



    public void deliver(Message msg){
        // DEBUGGING
        if(msg.getSeqNumber() == 300){
            System.out.println("Message 300 is in " + "host:deliver");
        }


        if (!received.contains(msg.getSeqNumber())) {
            received.add(msg.getSeqNumber());

            // extract OG seqn
            int trueSeqN = getTrueSeqN(msg);

            printWriter.println("d " + msg.getSrcHost().getId() + " " + trueSeqN);
            System.out.println("d " + msg.getSrcHost().getId() + " " + trueSeqN + " | " + msg.getSeqNumber() );
        }
    }

    public void deliverAck(Message ack){
        if (!sent.get(ack.getSeqNumber())) {
            sent.replace(ack.getSeqNumber(), true);
        }
        sent.putIfAbsent(ack.getSeqNumber(), true);
    }

    public boolean ackReceived(Message msg){
        return sent.get(msg.getSeqNumber());
    }

    public void receive(){
        while(true){
            perfectLink.receive();
        }
    }

    public void notifySent(Message msg){
        int i = getTrueSeqN(msg);
        int seqn = msg.getSeqNumber();


        System.out.println("b " + i + " | " + seqn + " ------");
        sent.put(seqn, false);
    }

    public int getTrueSeqN(Message msg){
        return (int) ( msg.getSeqNumber() - msg.getSrcHost().getId() * Math.pow(10,mask));
    }

    public Broadcast getBroadcastMethod() {
        return broadcastMethod;
    }

    public int getMask(){
        return mask;
    }
}
