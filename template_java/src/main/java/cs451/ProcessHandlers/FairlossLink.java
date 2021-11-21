package cs451.ProcessHandlers;

import cs451.Host;
import cs451.Messages.Message;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.HashMap;

public class FairlossLink implements Serializable {

    private static final int MAX_BYTES = 65535;

    private DatagramSocket socket;

    private final int timeout = 2;

    private final int port;
    private final String ip;
    private final int id;

    private final HashMap<Message, byte[]> cache = new HashMap(); // cache to avoid reserializing messages

    public FairlossLink(Host currentHost) {

        this.id = currentHost.getId();
        this.ip = currentHost.getIp();
        this.port = currentHost.getPort();

        try {
            this.socket = new DatagramSocket(this.port, InetAddress.getByName(this.ip));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public Message receive() {


        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        byte[] msg = new byte[MAX_BYTES];
        DatagramPacket udpPacket = new DatagramPacket(msg, msg.length);
        try {
            socket.receive(udpPacket);
        } catch (SocketTimeoutException e) {
            // socket timeout, handles the case where message was sent before receiving processes were launched
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (udpPacket.getData() == null) {
            return null;
        }

        return Message.msgFromBytes(udpPacket.getData());
    }

    public void send(Message msg) {
        try {
            byte[] msgAsBytes = cache.computeIfAbsent(msg, m -> {   // Need this because msgToBytes() throws an exception
                try {
                    return m.msgToBytes();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });
            if (msgAsBytes == null) {
                throw new NullPointerException("Null byte array");
            }
            DatagramPacket udpPacket = new DatagramPacket(msgAsBytes, msgAsBytes.length, InetAddress.getByName(msg.getDstHost().getIp()), msg.getDstHost().getPort());
            socket.send(udpPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}