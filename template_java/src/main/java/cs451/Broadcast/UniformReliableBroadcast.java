package cs451.Broadcast;

import cs451.Host;
import cs451.Messages.Message;
import cs451.Messages.MessageType;
import cs451.ProcessHandlers.PerfectLink;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class UniformReliableBroadcast implements Broadcast{

    // Keeps track of delivered msgs
    private final ConcurrentHashMap<String, List<Host>> acked;
    // Keeps track of delivered msgs
    private final ConcurrentSkipListSet<String> delivered;
    // Keeps track of pending msgs
    private final ConcurrentSkipListSet<String> pending;

    private transient PerfectLink pl;
    private transient BestEffortBroadcast beb;
    private final List<Host> hosts;
    private Host currentHost;

    private Broadcast broadcastMethod;


    // serves as a failure detector
    private final int MIN_ACK;

    public UniformReliableBroadcast(PerfectLink pl, List<Host> hosts, Broadcast broadcastMethod){
        this.acked = new ConcurrentHashMap<>();
        this.delivered = new ConcurrentSkipListSet<>();
        this.pending = new ConcurrentSkipListSet<>();

        this.broadcastMethod = broadcastMethod;

        this.hosts = hosts;

        this.MIN_ACK = ((hosts.size()) / 2)  + 1 ;
        System.out.println("Min ack: " + MIN_ACK);

        this.pl = pl;
        this.beb = new BestEffortBroadcast(pl, hosts, this);

    }

    public UniformReliableBroadcast(PerfectLink pl, List<Host> hosts, Host currentHost){
        this.acked = new ConcurrentHashMap<>();
        this.delivered = new ConcurrentSkipListSet<>();
        this.pending = new ConcurrentSkipListSet<>();

        this.currentHost = currentHost;

        this.hosts = hosts;

        this.MIN_ACK = ((hosts.size()) / 2)  + 1 ;
        System.out.println("Min ack: " + MIN_ACK);

        this.pl = pl;
        this.beb = new BestEffortBroadcast(pl, hosts, this);

    }

    @Override
    public void broadcast(Message msg) {
        pending.add(msg.getSignature());
        beb.broadcast(msg);
    }

    // Must make sure that N/2 ACKS before delivering
    public void deliver(Message msg) {
        String sign = msg.getSignature();
        if(!delivered.contains(sign)){
            Host srcHost = msg.getSrcHost();

            if (!acked.getOrDefault(sign, new ArrayList<>()).contains(srcHost)) {
                acked.putIfAbsent(sign, new ArrayList<>());
                acked.get(sign).add(srcHost);

                if(checkDeliverable(sign)){
                    delivered.add(sign);
                    acked.remove(sign);
                    pending.remove(sign);
                    broadcastMethod.deliver(new Message(msg.getSeqNumber(), msg.getContent(), MessageType.BROADCAST, sign)); //TODO: put this back when FIFO implemented
                    //currentHost.deliver(new Message(msg.getSeqNumber(), msg.getContent(), MessageType.BROADCAST, sign));
                }
            }

            //If not already forwarded, we forward it by broadcasting it.
            if (!pending.contains(sign) && !delivered.contains(sign)) {
                pending.add(sign);
                beb.broadcast(msg);
            }
        }
    }


    //Checks if the Message can be delivered or not
    private boolean checkDeliverable(String sign) {
        return acked.get(sign) != null && acked.get(sign).size() >= MIN_ACK ;
    }
}
