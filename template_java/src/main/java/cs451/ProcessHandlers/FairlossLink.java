package cs451.ProcessHandlers;

import cs451.Host;
import cs451.Messages.Message;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FairlossLink {

    private static final int MAX_BYTES = 65535;

    private final int MAX_CACHE_SIZE = 400;

    private DatagramSocket socket;

    private final int timeout = 2;

    private final int port;
    private final String ip;
    private final int id;

    private final HashMap<Message, byte[]> cache = new HashMap(MAX_CACHE_SIZE); // cache to avoid reserializing messages

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


/*        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }*/

        byte[] msg = new byte[MAX_BYTES];
        DatagramPacket udpPacket = new DatagramPacket(msg, msg.length);
        try {
            socket.receive(udpPacket);
        } /*catch (SocketTimeoutException e) {
            // socket timeout, handles the case where message was sent before receiving processes were launched
            return null;
        } */catch (IOException e) {
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

    public void reduceCache() {
        if (cache.size() > MAX_CACHE_SIZE) {
            List<Message> cacheKeySet = new ArrayList<>(cache.keySet());
            Collections.sort(cacheKeySet, Message.messageComparator);

            for (int i = 0; i < 100; i++) {
                cache.remove(cacheKeySet.get(i));
            }
        }
    }
}