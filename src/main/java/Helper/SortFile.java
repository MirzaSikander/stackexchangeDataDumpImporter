package Helper;

import java.io.*;
import java.util.Comparator;
import java.util.TreeMap;

public class SortFile {

    private String inputFile;
    private String inputFileName;
    private int noOfLineInFile;
    BufferedReader bufferedReader;
    public static final int USER_ID_EXPERTS_INDEX = 11;
    public int noOfRelevantPosts = 10;

    TreeMap<String, String> treeMap = new TreeMap<>(new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            if (o1 instanceof String && o2 instanceof String) {
                double o1_score = Double.parseDouble((String) o1);
                double o2_score = Double.parseDouble((String) o2);

                return Double.compare(o1_score, o2_score);
            }
            else{
                throw new IllegalArgumentException("parameter not string");
            }
        }
    });


    public SortFile(String directoryPath, String inputFileName, String noOfRelevantPosts) throws FileNotFoundException {
        this.inputFileName = inputFileName;
        this.noOfRelevantPosts = Integer.parseInt(noOfRelevantPosts);
        this.inputFile = directoryPath + inputFileName;
        bufferedReader = new BufferedReader(new FileReader(this.inputFile));
        noOfLineInFile = (int) bufferedReader.lines().count();
    }

    public void extractFile() throws IOException {
        bufferedReader = new BufferedReader(new FileReader(this.inputFile));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            treeMap.put(line.split(",")[0], line);
        }
        bufferedReader.close();
    }

    public int checkForUserMatch() {
        int count = 0;

        for(int i=0; i< noOfRelevantPosts; i++){
            String testQuestion = treeMap.pollFirstEntry().getValue();
            String userField = testQuestion.split(",")[USER_ID_EXPERTS_INDEX];
            String[] users = userField.substring(1, userField.length()-1).split(";");
            for(String user:users){
                if(user.equals(inputFileName)){
                    count++;
                }
            }
        }
        return count;
    }

}
