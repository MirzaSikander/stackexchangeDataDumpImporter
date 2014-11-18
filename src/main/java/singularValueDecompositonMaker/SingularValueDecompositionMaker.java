package singularValueDecompositonMaker;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class SingularValueDecompositionMaker {
    public static void main(String[] args) throws Exception {
        String inputFileName = System.getProperty("inputFile");
        String outputFileName = System.getProperty("outputFile");

        if (inputFileName == null) {
            throw new Exception("input file missing: " + inputFileName);
        }

        if (outputFileName == null) {
            throw new Exception("output file missing: " + outputFileName);
        }

        String inputFile = System.getProperty("user.home") + "/data_files/" + inputFileName + ".csv";


        String outputFile = System.getProperty("user.home") + "/data_files/" + outputFileName + ".csv";

        FileReader reader = new FileReader(inputFile);
        BufferedReader bufferedReader = new BufferedReader(reader);

        int i = 0;
        String header = bufferedReader.readLine();
        String[] fields = header.split(",");

        Matrix matrix = new Matrix((int) bufferedReader.lines().count(),fields.length);

        String line = null ;

        while((line=bufferedReader.readLine())!=null){
            String[] cols = line.split(",");
            for(int j=0; j<cols.length;j++){
                matrix.set(i,j,Double.parseDouble(cols[j]));
            }
            i++;
        }
        bufferedReader.close();
        FileOutputStream fileOutputStream = new FileOutputStream(new File(outputFile));

        SingularValueDecomposition singularValueDecomposition = new SingularValueDecomposition(matrix);

        fileOutputStream.write(singularValueDecomposition.getU().toString().getBytes());
    }

}
