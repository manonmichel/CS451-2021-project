package cs451.Messages;

import cs451.Host;
import cs451.HostsParser;

import java.io.*;
import java.util.Comparator;
import java.util.UUID;

public class Message implements Serializable {

    private final int SeqNumber;
    private final String signature;
    private final String content;
    private final MessageType msgType;
    private Host srcHost;
    private Host dstHost;
    private UUID uid;

    //Optional, used for Localized Causal broadcast
    private int[] vectorClock;


    public Message(int SeqNumber, String content, MessageType msgType, String msgSign) {
        this.content = content;
        this.SeqNumber = SeqNumber;
        this.msgType = msgType;
        this.signature = msgSign;
    }

    public Message(int SeqNumber, String content, MessageType msgType, String msgSign, Host srcHost, Host dstHost, UUID uid) {
        this.content = content;
        this.SeqNumber = SeqNumber;
        this.msgType = msgType;
        this.signature = msgSign;
        this.srcHost = srcHost;
        this.dstHost = dstHost;
        this.uid = uid;
    }


    // ------------------------- SETTERS & GETTERS  ---------------------------

    public static String makeSignature(int authorID, int seqN){
        return authorID + "." + seqN ;
    }

    public int getAuthorID(){
        return Integer.parseInt(signature.substring(0,1));
    }

    public String getSignature() {
        return signature;
    }

    public int getSeqNumber() {
        return SeqNumber;
    }

    public String getContent() {
        return content;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public Host getSrcHost() {
        return this.srcHost;
    }

    public Host getDstHost() {
        return this.dstHost;
    }

    public UUID getUid() {
        return uid;
    }

    public void setVectorClock(int[] vectorClock){
        this.vectorClock = vectorClock;
    }

    public int[] getVectorClock(){
        return this.vectorClock;
    }


    // ---------------------------- UTILS -------------------------------

    public Message addNetworkLayer(Host srcHost, Host dstHost, UUID uid){
        return new Message(this.SeqNumber, this.content, this.msgType, this.signature, srcHost, dstHost, uid);
    }

    public Message genAck() {
        if (this.msgType == MessageType.ACK) {
            throw new IllegalCallerException("Cannot generate an ACK message from an already ACK type");
        }
        if(this.dstHost.equals(this.srcHost)){
            System.out.println("WTF");
        }
        Message ack = new Message(this.SeqNumber, this.content, MessageType.ACK, this.signature, this.dstHost, this.srcHost, this.uid);
        return ack;
    }

    @Override
    public String toString() {
        return msgType + " " + signature + " " + srcHost.getId() + " " + dstHost.getId();
    }


    // ---------------------------- COMPARATORS -------------------------------

    @Override
    public boolean equals(Object m1) {
        if (m1 instanceof Message) {
            Message otherMsg = (Message) (m1);  // Removed comparison of message type because otherwise the comparison needed in PerfectLinks is faulty
            return this.signature == otherMsg.getSignature() && this.msgType == otherMsg.getMsgType() && this.content == otherMsg.getContent() && this.srcHost.equals(otherMsg.getSrcHost()) && this.dstHost.equals(otherMsg.getDstHost());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static Comparator<Message> messageComparator = (Message m1, Message m2) -> {
        if (m1.getUid().equals(m2.getUid())) {
            return m1.getMsgType().compareTo(m2.getMsgType());
        }
        return m1.getSignature().compareTo(m2.getSignature());
    };

    public static Comparator<Message> uidComparator = (Message m1, Message m2) -> {
            if (m1.getSignature().equals(m2.getSignature())) {
                return m1.getUid().compareTo(m2.getUid());
            } else {
                return m1.getSignature().compareTo(m2.getSignature());
            }
    };


    // ---------------------------- SERIALIZATION -------------------------------

    public byte[] msgToBytes() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = null;
        byte[] msgAsBytes = null;
        try {
            objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(this);
            objectStream.flush();
            msgAsBytes = byteStream.toByteArray();
        } finally {
            try {
                byteStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (msgAsBytes == null) {
            throw new InvalidObjectException("Resulting byte array is null");
        }

        return msgAsBytes;
    }

    public static Message msgFromBytes(byte[] msgAsBytes) {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(msgAsBytes);
        ObjectInput obj = null;
        Message msg = null;
        try {
            obj = new ObjectInputStream(byteStream);
            msg = (Message) obj.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (obj != null) {
                    obj.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

/*        if (msg == null) {
            throw new NullPointerException("Resulting Message is null");
        }*/

        return msg;
    }



}