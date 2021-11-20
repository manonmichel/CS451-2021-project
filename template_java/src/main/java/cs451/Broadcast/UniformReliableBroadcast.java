package cs451.Broadcast;

import cs451.Host;
import cs451.Messages.Message;
import cs451.ProcessHandlers.PerfectLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class UniformReliableBroadcast implements Broadcast{

    // Keeps track of delivered messages
    private transient final HashMap<Integer, List<Host>> acked ;
    // Keeps track of delivered messages
    private transient final HashSet<Integer> delivered ;
    // Keeps track of pending messages
    private transient final HashSet<Integer> pending ;

    private transient PerfectLink pl;
    private transient BestEffortBroadcast beb;
    private final List<Host> hosts;
    private final Host currentHost;

    // serves as a failure detector
    private final int MIN_ACK;

    public UniformReliableBroadcast(PerfectLink pl, List<Host> hosts, Host currentHost){
        this.acked = new HashMap<>();
        this.delivered = new HashSet<>();
        this.pending = new HashSet<>();

        this.hosts = hosts;
        this.currentHost = currentHost;
        this.MIN_ACK = ((hosts.size()+1) / 2) + 1 ;

        this.pl = pl;
        this.beb = new BestEffortBroadcast(pl, hosts, this, currentHost);

    }

    @Override
    public void broadcast(Message message) {
        // DEBUGGING
/*        if(message.getSeqNumber() == 300){
            System.out.println("Message 300 is in " + "urb:broadcast");
        }*/

        pending.add(message.getSeqNumber());
        beb.broadcast(message);


    }

    // Must make sure that N/2 ACKS before delivering
    public void deliver(Message message) {
        // DEBUGGING
/*        if(message.getSeqNumber() == 300){
            System.out.println("Message 300 is in " + "urb:deliver");
        }*/

        int seqn = message.getSeqNumber();
        if(!delivered.contains(seqn)){

            if (!acked.getOrDefault(seqn, new ArrayList<>()).contains(message.getSrcHost())) {
                acked.putIfAbsent(seqn, new ArrayList<>());
                acked.get(seqn).add(message.getSrcHost());
                //System.out.println("Deliverable? : " + checkDeliverable(seqn));
                if(checkDeliverable(seqn)){
                    delivered.add(seqn);
                    //acked.remove(seqn);
                    pending.remove(seqn);
                    currentHost.deliver(message); //Not sure about this
                }


            }

            //If not already forwarded, we forward it by broadcasting it.
            if (!pending.contains(seqn) && !delivered.contains(seqn)) {
                pending.add(seqn);
                beb.broadcast(message);
            }

        }



    }

    @Override
    public PerfectLink getPerfectLink() {
        return this.pl;
    }

    //Checks if the Message can be delivered or not
    private boolean checkDeliverable(int seqn) {
        return acked.get(seqn) != null && acked.get(seqn).size() >= MIN_ACK ;
    }
}
