package cs451.ProcessHandlers;

import cs451.Messages.Message;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Receiver extends Thread{

    private final FairlossLink fll;
    private PerfectLink pl;
    private final HashSet<UUID> receivedACKS = new HashSet<>();
    private final HashSet<UUID> receivedBroadcast = new HashSet<>();
    private final Sender sender;

    private final ExecutorService executor;


    public Receiver(Sender sender, FairlossLink fll) {
        this.sender = sender;
        this.fll = fll;

        this.executor = Executors.newFixedThreadPool(1);

    }

    @Override
    public void run() {
        while (true) {
            //Message msg = null;

            Message msg = fll.receive();

            if (msg == null)
                break;


            switch (msg.getMsgType()) {
                case BROADCAST:
                    if (!receivedBroadcast.contains(msg.getUid())) {
                        receivedBroadcast.add(msg.getUid());
                        executor.execute(() -> pl.deliver(msg));
                    }
                    sender.send(msg.genAck());
                    break;

                case ACK:
                    if (!receivedACKS.contains(msg.getUid())) {
                        receivedACKS.add(msg.getUid());
                        sender.notifyAck(msg);
                    }
                    break;
            }
        }

    }

    public void setPerfectLink(PerfectLink pl) {
        this.pl = pl;
    }
}
