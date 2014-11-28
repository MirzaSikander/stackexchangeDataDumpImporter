package Helper;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        File directoryPath = new File(System.getProperty("user.home") + "/data_files/rankingFiles/");
        File[] files = directoryPath.listFiles();

        for(File file: files){
            SortFile sortFile = new SortFile(file.getName());
            sortFile.extractFile();
            System.out.print(sortFile.checkForUserMatch());
        }
    }
}
