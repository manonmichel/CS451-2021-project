package cs451.ProcessHandlers;

import cs451.Messages.Message;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;


public class Sender extends Thread{
    private final FairlossLink fll;
    private final int maxBufferSize = 100;


    //Buffers to store the pending ACKS and BROADCASTS
    private final ConcurrentSkipListSet<Message> broadcastBuffer = new ConcurrentSkipListSet<>(Message.uidComparator);
    private final ConcurrentSkipListSet<Message> ackBuffer = new ConcurrentSkipListSet<>(Message.uidComparator);
    //The messages to send
    private final ConcurrentHashMap<UUID, Message> broadcast = new ConcurrentHashMap<>(maxBufferSize);
    private final HashSet<Message> ack = new HashSet<>(maxBufferSize);

    private final int ACK_SEND = 1;

    private int ack_counter = 0;


    public Sender(FairlossLink fll) {
        this.fll = fll;
    }

    @Override
    public void run() {
/*        long startTime = System.currentTimeMillis();
        int temp = 0 ;*/
        while (true) {
/*            long currentTime = System.currentTimeMillis();


            if(22000 > currentTime-startTime && currentTime-startTime > 20000 && temp == 0 ){
                System.out.println("broadcast: " + broadcast);
                System.out.println("broadcast buffer: " + broadcastBuffer);
                System.out.println("ack: " + ack);
                System.out.println("ack buffer: " + ackBuffer);
                temp = -1 ;
            }*/
            updateBroadcastBuffer();
            updateAckBuffer();

            for (Map.Entry<UUID, Message> entry : broadcast.entrySet()) {


                fll.send(entry.getValue());

            }

            for (Message message : ack) {

                fll.send(message);
            }

            // Allows us to send ACKs ACK_SEND amount of times and then clears them
            // We do this in order to somewhat compensate for ack loss
            if (ack_counter == ACK_SEND) {
                ack.clear();
                ack_counter = 0;
            }
            ack_counter++;

            fll.reduceCache();

        }
    }

    private void updateBroadcastBuffer() {
        while (broadcast.size() < maxBufferSize) {
            Message nextBroadcast = broadcastBuffer.pollFirst();
            if (nextBroadcast != null) {
                broadcast.put(nextBroadcast.getUid(), nextBroadcast);
            } else {
                break;
            }
        }
    }

    private void updateAckBuffer() {
        while (ack.size() < maxBufferSize) {
            Message nextAck = ackBuffer.pollFirst();
            if (nextAck != null) {
                ack.add(nextAck);
            } else {
                break;
            }
        }
    }

    public void send(Message msg) {

        switch (msg.getMsgType()) {
            case BROADCAST:
                broadcastBuffer.add(msg);
                break;
            case ACK:
                ackBuffer.add(msg);
                break;
        }
    }

    public void notifyAck(Message ack) {
        broadcast.remove(ack.getUid());
    }

}
