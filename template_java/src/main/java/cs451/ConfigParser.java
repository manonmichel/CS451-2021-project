package cs451;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigParser {

    private String path;
    private int nMsgs;
    private int processIndex ;
    String[] content;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        try {
            content = Files.readAllLines(file.toPath()).get(0).split(" ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(content.length > 0){
            nMsgs = Integer.valueOf(content[0]);
        }
        if(content.length > 1){
            processIndex = Integer.valueOf(content[1]);
        }


        return true;
    }

    public String getPath() {
        return path;
    }

    public int getnMsgs() {
        return nMsgs ;
    }

    public int getProcessIndex() {
        return processIndex;
    }

    public String getConfigType() {
        if(content.length == 2){
            return "pl";
        } else if(content.length == 1){
            return "fifo";
        } else {
            return "unknown";
        }
    }
}
