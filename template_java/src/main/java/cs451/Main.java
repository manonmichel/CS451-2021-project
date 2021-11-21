package cs451;

import cs451.Broadcast.FifoBroadcast;
import cs451.ProcessHandlers.PerfectLink;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static PrintWriter printWriter = new PrintWriter(System.out);

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        printWriter.close();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Parser parser = new Parser(args);
        parser.parse();

        int nMsgs = 0;
        int dstID = -1;

        String configType = parser.getConfigType();
        if (configType.equals("pl")) {
            nMsgs = parser.getnMsgs();
            dstID = parser.getProcessIndex();
        } else if (configType.equals("fifo")) {
            nMsgs = parser.getnMsgs();
        } else {
            System.out.println("Unknown Config");
        }


        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host : parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Config content:");
        System.out.println("===============");
        System.out.println("Number of messages each process should send: " + nMsgs);
        System.out.println("Index of the process that should receive the messages: " + dstID + "\n");

        System.out.println("Doing some initialization\n");

        Host currentHost = parser.getCurrentHost();
        List<Host> otherHosts = new ArrayList<Host>(parser.hosts());
        otherHosts.remove(currentHost);

        PerfectLink pl = new PerfectLink(currentHost);

        printWriter = new PrintWriter(new FileWriter(parser.output()));

        //UniformReliableBroadcast urb = new UniformReliableBroadcast(pl, otherHosts, currentHost);
        FifoBroadcast fifo = new FifoBroadcast(pl, otherHosts, currentHost);

        currentHost.init(nMsgs, printWriter, fifo);

        System.out.println("Broadcasting and delivering messages...\n");

        int expectedMsgs = nMsgs * otherHosts.size();

        List<Integer> otherhostIDs = otherHosts.stream().map(Host::getId).collect(Collectors.toList());

        System.out.println("Other hosts: " + otherhostIDs);

        System.out.println("Expecting :" + expectedMsgs + " messages");

        System.out.println("Config type :" + configType);

        currentHost.start();

        System.out.println("Signaling end of broadcasting messages");


        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
