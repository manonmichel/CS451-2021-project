package cs451.Broadcast;

import cs451.Messages.Message;
import cs451.ProcessHandlers.PerfectLink;

public interface Broadcast {
    void broadcast(Message message);

    void deliver(Message message);
}
