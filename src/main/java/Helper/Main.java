package Helper;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String directoryPath =  System.getProperty("user.home") + "/data_files/"+System.getProperty("ResultsDirectory")+"/";
        File directory = new File(directoryPath);
        String noOfRelevantPosts = System.getProperty("NoOfRelevantPosts");
        File[] files = directory.listFiles();

        System.out.println();



        for(File file: files){
            SortFile sortFile = new SortFile(directoryPath, file.getName(), noOfRelevantPosts);
            sortFile.extractFile();
            System.out.println(file.getName()+", "+sortFile.checkForUserMatch());
        }

        System.out.println();
    }
}
