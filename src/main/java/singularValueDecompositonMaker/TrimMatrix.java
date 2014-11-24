package singularValueDecompositonMaker;


import java.io.*;

public class TrimMatrix {
    public static void main(String[] args) throws IOException {

        String inputFile = System.getProperty("user.home") + "/data_files/" +"TagFeatureVectors_Horizontal_2"+ ".csv";
        String outputFile = System.getProperty("user.home") + "/data_files/" + "TrimmedOutputFileForU"+ ".csv";

        String row;

        FileReader fileReader = new FileReader(new File(inputFile));
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        FileWriter fileWriter = new FileWriter(new File(outputFile));
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        while((row=bufferedReader.readLine())!=null) {
            String[] fields = row.split(",");
            for (int i=0;i<100;i++) {
                if(i==99){
                    bufferedWriter.write(fields[i]);
                    bufferedWriter.write("\n");
                }
                else{
                    bufferedWriter.write(fields[i]);
                    bufferedWriter.write(",");
                }
            }
        }
        bufferedReader.close();
        bufferedWriter.close();
    }
}

