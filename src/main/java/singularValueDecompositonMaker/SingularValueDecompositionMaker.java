package singularValueDecompositonMaker;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

import java.io.*;

public class SingularValueDecompositionMaker {
    public static void main(String[] args) throws Exception {

        String inputFile = System.getProperty("user.home") + "/data_files/" + System.getProperty("inputFile") + ".csv";

        String outputFileForU = System.getProperty("user.home") + "/data_files/" + System.getProperty("outputFileForU") + ".csv";
        String outputFileForV = System.getProperty("user.home") + "/data_files/" + System.getProperty("outputFileForV") + ".csv";
        String outputFileForS = System.getProperty("user.home") + "/data_files/" + System.getProperty("outputFileForS") + ".csv";


        FileReader reader = new FileReader(inputFile);
        BufferedReader bufferedReader = new BufferedReader(reader);

        int i = 0;
        String header = bufferedReader.readLine();
        String[] fields = header.split(",");

        int numberOfFields = fields.length;
        Matrix matrix = new Matrix((int) bufferedReader.lines().count(), numberOfFields);
        bufferedReader.close();

        String line = null ;
        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(inputFile));


        //Skip the first line
        bufferedReader1.readLine();

        while((line=bufferedReader1.readLine())!=null) {
            String[] cols = line.split(",");
            for (int j = 0; j < cols.length; j++) {
                matrix.set(i, j, Double.parseDouble(cols[j]));
            }
            i++;
        }
        bufferedReader1.close();


        BufferedWriter bufferedWriterForU = new BufferedWriter(new FileWriter(outputFileForU));
        BufferedWriter bufferedWriterForV = new BufferedWriter(new FileWriter(outputFileForV));
        BufferedWriter bufferedWriterForS = new BufferedWriter(new FileWriter(outputFileForS));

        SingularValueDecomposition singularValueDecomposition = matrix.svd();

        Matrix U = singularValueDecomposition.getU();
        Matrix V = singularValueDecomposition.getV();
        double[] S = singularValueDecomposition.getSingularValues();


        //Fill in the first row of the file with column headers

        for(int k=0;k<U.getArray()[1].length;k++){
            bufferedWriterForU.write("Component" + k + ",");
        }

        bufferedWriterForU.write("\n");

        for (int j = 0; j < U.getArray().length; j++) {
            for(int k=0;k<U.getArray()[j].length;k++){
                bufferedWriterForU.write(Double.toString(U.get(j, k)));
                bufferedWriterForU.write(",");
            }
            bufferedWriterForU.write("\n");
        }
        bufferedWriterForU.close();

        for (int j = 0; j < V.getArray().length; j++) {
            for(int k=0;k<V.getArray()[j].length;k++){
                bufferedWriterForV.write(Double.toString(V.get(j, k)));
                bufferedWriterForV.write(",");
            }
            bufferedWriterForV.write("\n");
        }
        bufferedWriterForV.close();

        for (int j = 0; j < S.length; j++) {
                bufferedWriterForS.write(Double.toString(S[j]));
                bufferedWriterForS.write(",");
            }
        bufferedWriterForS.close();
    }
}
