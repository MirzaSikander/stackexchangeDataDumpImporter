package Helper;

import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class SortFile {

    private String inputFile;
    private String inputFileName;
    private int noOfLineInFile;
    BufferedReader bufferedReader;
    public static final int USER_ID_EXPERTS_INDEX = 11;
    public int noOfRelevantPosts = 10;

    class Tuple{
        Double rank;
        String question;

        public Tuple(String rank, String question){
            this.rank = Double.parseDouble(rank);
            this.question = question;
        }
    }

    PriorityQueue<Tuple> queue = new PriorityQueue<>(new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            if (o1 instanceof Tuple && o2 instanceof Tuple) {
                double o1_score = ((Tuple) o1).rank;
                double o2_score = ((Tuple) o2).rank;

                return -1 * Double.compare(o1_score, o2_score);
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
            queue.add(new Tuple(line.split(",")[0], line));
        }
        bufferedReader.close();
    }

    public int checkForUserMatch() {
        int count = 0;

        for(int i=0; i< noOfRelevantPosts; i++){
            String testQuestion = queue.poll().question;
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
