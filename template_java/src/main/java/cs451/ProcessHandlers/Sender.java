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

    public Sender(FairlossLink fll) {
        this.fll = fll;
    }

    @Override
    public void run() {
        while (true) {
            updateBroadcastBuffer();
            updateAckBuffer();

            for (Map.Entry<UUID, Message> entry : broadcast.entrySet()) {
                fll.send(entry.getValue());
            }

            for (Message message : ack) {
                fll.send(message);
            }

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

    public void send(Message message) {
        switch (message.getMsgType()) {
            case BROADCAST:
                broadcastBuffer.add(message);
                break;
            case ACK:
                ackBuffer.add(message);
                break;
        }
    }

    public void notifyAck(Message ack) {
        broadcast.remove(ack.getUid());
    }

}
