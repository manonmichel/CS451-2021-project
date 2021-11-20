package cs451.Messages;

import cs451.Host;

import java.io.*;

public class Message implements Serializable {

    private int SeqNumber;
    private final String content;
    private Host srcHost, dstHost;
    private final MessageType msgType ;


    public Message(int SeqNumber, String content, MessageType msgType){
        this.content = content ;
        this.SeqNumber = SeqNumber ;
        this.msgType = msgType;
    }

    public Message(int SeqNumber, String content, MessageType msgType, Host srcHost){
        this.content = content ;
        this.SeqNumber = SeqNumber ;
        this.srcHost = srcHost;
        this.msgType = msgType;
    }

    public Message(int SeqNumber, String content, MessageType msgType, Host srcHost, Host dstHost){
        this.content = content ;
        this.SeqNumber = SeqNumber ;
        this.msgType = msgType;
        this.srcHost = srcHost;
        this.dstHost = dstHost ;
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

    public boolean compare(Message m1, Message m2){
        return m2.SeqNumber == m1.SeqNumber && m1.content == m2.content && m1.msgType == m2.msgType ;
    }

    public Message createCopy(){
        if(this.srcHost == null){
            return new Message(this.SeqNumber, this.content, this.msgType);
        } else if(this.dstHost == null){
            return new Message(this.SeqNumber, this.content, this.msgType, this.srcHost);
        } else {
            return new Message(this.SeqNumber, this.content, this.msgType, this.srcHost, this.dstHost);
        }
    }

    public void setDstHost(Host dstHost) {
        this.dstHost = dstHost;
    }

    public void setSrcHost(Host srcHost) {
        this.srcHost = srcHost;
    }

    public void setSeqNumber(int seqNumber){
        this.SeqNumber = seqNumber;
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
        return new Message(this.getSeqNumber(), "", MessageType.ACK, this.dstHost, this.srcHost);
    }

}