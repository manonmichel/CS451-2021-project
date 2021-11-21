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
    private transient final HashMap<String, List<Host>> acked ;
    // Keeps track of delivered messages
    private transient final HashSet<String> delivered ;
    // Keeps track of pending messages
    private transient final HashSet<String> pending ;

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
        this.MIN_ACK = ((hosts.size()+1) / 2)  ;

        this.pl = pl;
        this.beb = new BestEffortBroadcast(pl, hosts, this, currentHost);

    }

    @Override
    public void broadcast(Message message) {


        pending.add(message.getSignature());
        beb.broadcast(message);


    }

    // Must make sure that N/2 ACKS before delivering
    public void deliver(Message message) {


        String sign = message.getSignature();
        if(!delivered.contains(sign)){
            Host srcHost = message.getSrcHost();
            if (!acked.getOrDefault(sign, new ArrayList<>()).contains(srcHost)) {
                acked.putIfAbsent(sign, new ArrayList<>());
                acked.get(sign).add(srcHost);

                if(checkDeliverable(sign)){
                    delivered.add(sign);
                    pending.remove(sign);
                    currentHost.deliver(message);
                }


            }

            //If not already forwarded, we forward it by broadcasting it.
            if (!pending.contains(sign) && !delivered.contains(sign)) {
                pending.add(sign);
                beb.broadcast(message);
            }

        }



    }

    @Override
    public PerfectLink getPerfectLink() {
        return this.pl;
    }

    //Checks if the Message can be delivered or not
    private boolean checkDeliverable(String sign) {
        return acked.get(sign) != null && acked.get(sign).size() >= MIN_ACK ;
    }
}
