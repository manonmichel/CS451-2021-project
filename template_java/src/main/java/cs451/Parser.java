package cs451;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Parser {

    private String[] args;
    private long pid;
    private IdParser idParser;
    private HostsParser hostsParser;
    private OutputParser outputParser;
    private ConfigParser configParser;

    public Parser(String[] args) {
        this.args = args;
    }

    public void parse() {
        pid = ProcessHandle.current().pid();

        idParser = new IdParser();
        hostsParser = new HostsParser();
        outputParser = new OutputParser();
        configParser = new ConfigParser();

        int argsNum = args.length;
        if (argsNum != Constants.ARG_LIMIT_CONFIG) {
            help();
        }

        if (!idParser.populate(args[Constants.ID_KEY], args[Constants.ID_VALUE])) {
            help();
        }

        if (!hostsParser.populate(args[Constants.HOSTS_KEY], args[Constants.HOSTS_VALUE])) {
            help();
        }

        if (!hostsParser.inRange(idParser.getId())) {
            help();
        }

        if (!outputParser.populate(args[Constants.OUTPUT_KEY], args[Constants.OUTPUT_VALUE])) {
            help();
        }

        if (!configParser.populate(args[Constants.CONFIG_VALUE])) {
            help();
        }
    }

    private void help() {
        System.err.println("Usage: ./run.sh --id ID --hosts HOSTS --output OUTPUT CONFIG");
        System.exit(1);
    }

    public int myId() {
        return idParser.getId();
    }

    public List<Host> hosts() {
        return hostsParser.getHosts();
    }

    public String output() {
        return outputParser.getPath();
    }

    public String config() {
        return configParser.getPath();
    }

    public String getConfigType(int nHosts){
        return configParser.getConfigType(nHosts);
    }

    public HashMap<Integer, HashSet<Integer>> getCausalDependencies(int numberOfHosts){
        return configParser.getCausalDependencies(numberOfHosts);
    }

    public int getnMsgs(){
        return configParser.getnMsgs();
    }

    public int getProcessIndex(){
        return configParser.getProcessIndex();
    }

/*    public int[] configContent() {
        return new int[]{configParser.getnMsgs(), configParser.getProcessIndex()};
    }*/

    public Host getCurrentHost(){
        Host currentHost = null;
        for (Host host : hosts()){
            if(host.getId() == myId()){
                currentHost = host;
            }
        }

        if(currentHost == null){
            throw new IllegalStateException("This process has no corresponding host");
        }

        return currentHost;

    }

    public Host getHostFromID(int id) {
        Host ret = null;
        for(Host host: hosts()){
            if(host.getId() == id){
                ret = host;
            }
        }

        if(ret == null){
            throw new NullPointerException("No host found with given ID");
        }

        return ret;
    }

}
