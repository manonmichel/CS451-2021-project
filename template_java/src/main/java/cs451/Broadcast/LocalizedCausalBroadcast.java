package cs451.Broadcast;

import cs451.Host;
import cs451.Messages.Message;
import cs451.ProcessHandlers.PerfectLink;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;

// implementation according to Introduction to Reliable and Secure Distributed Programming page 108 algorithm
public class LocalizedCausalBroadcast implements Broadcast{

    private UniformReliableBroadcast urb;
    private Host currentHost;
    private List <Host> hosts;

    // From config, keeps track of which processes each process depends on
    private final HashMap<Integer, HashSet<Integer>> dependencies;

    private final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Message>> pending = new ConcurrentHashMap<>();
    // Every process p maintains a vector clock V such that entry V [rank(q)] represents the number of msgs that p has crb-delivered from process q;
    private final AtomicIntegerArray vectorClock;




    public LocalizedCausalBroadcast(PerfectLink perfectLink, List<Host> hosts, Host currentHost, HashMap<Integer, HashSet<Integer>> configDependencies){
        this.urb = new UniformReliableBroadcast(perfectLink, hosts, this);
        this.currentHost = currentHost;
        this.hosts = hosts;
        this.dependencies = configDependencies;

        this.vectorClock = new AtomicIntegerArray(hosts.size());
    }

    @Override
    public void broadcast(Message msg) {
        msg.setVectorClock(getLocalizedCausalVectorClock(msg.getSeqNumber()));

        urb.broadcast(msg);
        currentHost.deliver(msg);
        vectorClock.getAndIncrement(currentHost.getId()-1);
    }

    @Override
    public void deliver(Message msg) {



        int authorID = msg.getAuthorID();

        // because the current process delivers its own messages directly
        if(authorID != currentHost.getId()){

            if(checkCanCausallyDeliver(msg)) {
                currentHost.deliver(msg);
                vectorClock.getAndIncrement(authorID-1);
                deliverPending();

            } else {
                ConcurrentLinkedQueue<Message> awaiting_msgs = pending.getOrDefault(authorID, new ConcurrentLinkedQueue<Message>());
                awaiting_msgs.add(msg);
                pending.put(authorID, awaiting_msgs);
            }

            deliverPending();
        }

    }

    // In the case of *Localized* Causal Broadcast, vector clock of each process should only consider dependencies of the specific process on which it depends
    // Example: let's say there are 5 processes, and you are process A which depends only on B.
    // - Let's assume you've already delivered 15 msgs from B and 10 from C,D and E.
    // - If you broadcast your msg 20, the vector clock in that msg should be [19,15,0,0,0] instead of [19,15,10,10,10] (latter case is for causal broadcast)
    private int[] getLocalizedCausalVectorClock(int seqn){
        int localizedCausalVectorClock[] = new int[hosts.size()];

        localizedCausalVectorClock[currentHost.getId()-1] = seqn-1;

        HashSet<Integer> hostDependencies = dependencies.get(currentHost.getId());


        for (Integer i : hostDependencies){

            localizedCausalVectorClock[i-1] = vectorClock.get(i-1);

        }

        return localizedCausalVectorClock;
    }

    private boolean checkCanCausallyDeliver(Message msg){


        int[] msgVC = msg.getVectorClock();

        //System.out.println("Checking for message: " + msg.toString() + " with vc " + Arrays.toString(msgVC));
        //System.out.println("Global vector clock: " + vectorClock);

        for(Host host : hosts){
            // Can deliver m' from p' if W' ≤ V for (p', W', m')
/*            System.out.println("msgVC[host.getId()-1]: " + msgVC[host.getId()-1]);
            System.out.println("vectorClock.get(host.getId() -1): " + vectorClock.get(host.getId() -1));
            System.out.println(!(msgVC[host.getId()-1] <= vectorClock.get(host.getId() -1)) );*/
            if(!(msgVC[host.getId()-1] <= vectorClock.get(host.getId() -1)) ){

                return false;
            }
        }
        return true;

    }

    public void deliverPending(){



        int initialSize = getNumberOfPending();



        for (Map.Entry<Integer, ConcurrentLinkedQueue<Message>> hostPending : pending.entrySet()) {
            for (Message message : hostPending.getValue()) {
                if (checkCanCausallyDeliver(message)) {
                    pending.get(message.getAuthorID()).remove(message);

                    vectorClock.getAndIncrement(hostPending.getKey() - 1);

                    currentHost.deliver(message);

                }

            }

        }

        int remainingSize = getNumberOfPending();

        if(remainingSize == initialSize){
            //System.out.println(" number remaining: " + remainingSize + "  | pending: " + pending);
            return;
        }else{
            deliverPending();
        }


    }


    public int getNumberOfPending(){
        int counter = 0;

        for (Map.Entry<Integer, ConcurrentLinkedQueue<Message>> hostPending : pending.entrySet()) {
            for (Message message : hostPending.getValue()) {
                counter++;
            }
        }

        return counter;
    }


}
