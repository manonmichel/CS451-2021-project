package cs451.Messages;

import cs451.Host;

import java.io.*;
import java.util.Comparator;

public class Message implements Serializable {

    private final int SeqNumber;
    private String signature;
    private final String content;
    private final Host srcHost;
    private Host dstHost;
    private final MessageType msgType ;


/*    public Message(int SeqNumber, String content, MessageType msgType){
        this.content = content ;
        this.SeqNumber = SeqNumber ;
        this.msgType = msgType;
    }*/

    public Message(int SeqNumber, String content, MessageType msgType, Host srcHost){
        this.content = content ;
        this.SeqNumber = SeqNumber ;
        this.srcHost = srcHost;
        this.msgType = msgType;
        setSignature();
    }

    public Message(int SeqNumber, String content, MessageType msgType, Host srcHost, Host dstHost){
        this.content = content ;
        this.SeqNumber = SeqNumber ;
        this.msgType = msgType;
        this.srcHost = srcHost;
        this.dstHost = dstHost ;
        setSignature();
    }

    private void setSignature(){
        String sign = "";
        switch (msgType) {
            case BROADCAST:
                sign = srcHost.getId() + "." + SeqNumber;
                break;
            case ACK:
                sign = dstHost.getId() + "." + SeqNumber;
                break;
        }

        this.signature = sign;
    }

    public String getSignature(){
        return signature;
    }

    public int getSeqNumber() { return SeqNumber;}

    public String getContent() {
        return content;
    }

    public MessageType getMsgType() { return msgType; }

    public Host getSrcHost(){ return this.srcHost; }

    public Host getDstHost(){ return this.dstHost; }

/*    public int getSrcPort(){ return this.srcHost.getPort(); }

    public int getDstPort(){ return this.dstHost.getPort(); }

    public int getSrcID(){ return this.srcHost.getId(); }

    public int getDstID(){ return this.dstHost.getId(); }

    public String getSrcIP(){ return this.srcHost.getIp(); }

    public String getDstIP(){ return this.dstHost.getIp(); }*/

/*

    public Message createCopy(){
        if(this.dstHost == null){
            return new Message(this.SeqNumber, this.content, this.msgType, this.srcHost);
        } else {
            return new Message(this.SeqNumber, this.content, this.msgType, this.srcHost, this.dstHost);
        }
    }*/



    public MessageType getMessageType(){
        return msgType;
    }

    @Override
    public boolean equals(Object m1){
        if(m1 instanceof Message){
            Message otherMsg = (Message)(m1);  // Removed comparison of message type because otherwise the comparison needed in PerfectLinks is faulty
            return this.signature == otherMsg.getSignature() && this.msgType == otherMsg.getMsgType() && this.content == otherMsg.getContent() && this.srcHost == otherMsg.getSrcHost()  && this.dstHost == otherMsg.getDstHost();
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public byte[] msgToBytes() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = null;
        byte[] msgAsBytes = null ;
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

        if(msgAsBytes == null){
            throw new InvalidObjectException("Resulting byte array is null") ;
        }

        return msgAsBytes;
    }

    public static Message msgFromBytes(byte[] msgAsBytes){
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

        if(msg == null){
            throw new NullPointerException("Resulting Message is null") ;
        }

        return msg ;
    }

    public Message genAck(){
        if (this.msgType == MessageType.ACK) {
            throw new IllegalCallerException("Cannot generate an ACK message from an already ACK type");
        }
        Message ack = new Message(this.SeqNumber, this.content, MessageType.ACK, this.dstHost, this.srcHost);
        //ack.setSignature();
        return ack;
    }

    /**
     * String representation of a Message
     *
     * @return
     */
    @Override
    public String toString() {
        return msgType + " " + signature + " " + srcHost.getId() + " " + dstHost.getId() ;
    }

}