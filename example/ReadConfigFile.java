import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ReadConfigFile{

     public static void main(String []args){
        Path path = Paths.get("configs/lcausal-broadcast.config");
        //List<String> all = new HashList<>();

        try {
            List<String> all = Files.readAllLines(path);
            System.out.println(all.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


     }
}
