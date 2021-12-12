package cs451.Broadcast;

import cs451.Host;
import cs451.Messages.Message;
import cs451.ProcessHandlers.PerfectLink;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicIntegerArray;

// implementation according to Introduction to Reliable and Secure Distributed Programming page 102 algorithm
public class FifoBroadcast implements Broadcast {


    private UniformReliableBroadcast urb;
    private Host currentHost;
    // The process maintains an array next contains an entry for every process p with the sequence number of the next message to be frb-delivered from sender p
    private final AtomicIntegerArray next;
    // The process buffers all messages received via the reliable broadcast primitive in a set pending and frb-delivers them according to the sequence number assigned per the sender.
    private final ConcurrentHashMap<Integer, ConcurrentSkipListSet<Message>> pending = new ConcurrentHashMap<>();
    private final Comparator<Message> seqnComparator = Comparator.comparingInt(Message::getSeqNumber);




    public FifoBroadcast(PerfectLink perfectLink, List<Host> hosts, Host currentHost) {
        this.next = new AtomicIntegerArray(hosts.size());

        this.urb = new UniformReliableBroadcast(perfectLink, hosts, this);
        this.currentHost = currentHost;
    }


    @Override
    public void broadcast(Message msg) {
        // don't need to add lsn (seqn) as in book because our messages already have seqn
        urb.broadcast(msg);
    }

    @Override
    public void deliver(Message msg) {
        //int authorID = Integer.valueOf(msg.getSignature().charAt(0));
        int authorID = msg.getAuthorID();

        int currentMsg = next.get(authorID-1); // need to do id-1 because array starts at 0 but host id's start at 1

        //If the received message is the next message to deliver, deliver it
        if (currentMsg == msg.getSeqNumber() - 1) {

            currentHost.deliver(msg);
            next.getAndIncrement(authorID - 1); // once the message has been delivered, increment seqn of next message to be delivered from that sender

            ConcurrentSkipListSet<Message> awaiting_msgs = pending.getOrDefault(authorID, new ConcurrentSkipListSet<>(seqnComparator));

            //If there is a pending message whose seqn is the next message we should deliver, deliver it
            if (!awaiting_msgs.isEmpty()) {
                // Only need to check first because ordered by seqn
                Message nextMsg = awaiting_msgs.first();
                if (nextMsg.getSeqNumber() - 1 == next.get(authorID - 1)) {
                    deliver(awaiting_msgs.pollFirst());
                }
            }

        }
        // else we add this message to the buffer for this process
        else {
            ConcurrentSkipListSet<Message> tmp = pending.getOrDefault(authorID, new ConcurrentSkipListSet<>(seqnComparator));
            tmp.add(msg);
            pending.put(authorID, tmp);
        }

    }


}
