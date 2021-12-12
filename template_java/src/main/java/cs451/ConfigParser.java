package cs451;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigParser {

    private String path;
    String[] firstLine;
    List<String> allContent;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        try {
            allContent = Files.readAllLines(file.toPath());
            firstLine = allContent.get(0).split(" ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public String getPath() {
        return path;
    }

    public int getnMsgs() {
        return Integer.valueOf(firstLine[0]);
    }

    public int getProcessIndex() {
        return Integer.valueOf(firstLine[1]);
    }

    public HashMap<Integer, HashSet<Integer>> getCausalDependencies(int numberOfHosts){
        HashMap<Integer, HashSet<Integer>> dependencies = new HashMap<Integer, HashSet<Integer>>();
        for(Integer hostID : dependencies.keySet()){
            String[] hostLine = allContent.get(hostID).split(" ");
            HashSet<Integer> lineAsInts = new HashSet<>(Arrays.stream(hostLine).mapToInt(Integer::parseInt).boxed().collect(Collectors.toSet()));
            lineAsInts.remove(hostID); // Remove current host from set of dependencies

            dependencies.put(hostID, lineAsInts);
        }
        return dependencies;
    }

    public String getConfigType(int numberOfHosts) {
        if (firstLine.length == 2) {
            return "pl";
        } else if (firstLine.length == 1) {
            return "fifo";
        } else if (allContent.size() == numberOfHosts + 1){
            return "lcausal";
        } else {
            return "unknown";
        }
    }
}
