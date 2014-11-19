package singularValueDecompositonMaker;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

import java.io.*;

public class SingularValueDecompositionMaker {
    public static void main(String[] args) throws Exception {

        String inputFile = System.getProperty("user.home") + "/data_files/" + "TagFeatureVectors_Horizontal_2" + ".csv";


        String outputFile = System.getProperty("user.home") + "/data_files/" + "FeatureFileAfterSVD" + ".csv";

        FileReader reader = new FileReader(inputFile);
        BufferedReader bufferedReader = new BufferedReader(reader);

        int i = 0;
        String header = bufferedReader.readLine();
        String[] fields = header.split(",");

        int numberOfFields = fields.length;
        Matrix matrix = new Matrix((int) bufferedReader.lines().count(), numberOfFields);

        String line = null ;

        //Skip the first line
        bufferedReader.readLine();

        while((line=bufferedReader.readLine())!=null) {
            String[] cols = line.split(",");
            for (int j = 0; j < cols.length; j++) {
                matrix.set(i, j, Double.parseDouble(cols[j]));
            }
            i++;
        }
        bufferedReader.close();


        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        SingularValueDecomposition singularValueDecomposition = matrix.svd();

        //Fill in the first row of the file with column headers
        for(int k=0;k<100;k++){
            bufferedWriter.write("Component"+k+",");
        }

        bufferedWriter.write("\n");

        Matrix U = singularValueDecomposition.getU();

        for (int j = 0; j < U.getRowDimension(); j++) {
            for(int k=0;k<100;k++){
                bufferedWriter.write(Double.toString(U.get(j,k)));
                bufferedWriter.write(",");
            }

            bufferedWriter.write("\n");
        }

        bufferedWriter.close();
    }
}
