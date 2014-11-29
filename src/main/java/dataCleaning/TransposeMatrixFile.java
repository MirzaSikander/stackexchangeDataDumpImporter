package dataCleaning;

import Jama.Matrix;

import java.io.*;

/**
 * Created by mirzasikander on 11/22/14.
 */
public class TransposeMatrixFile {
    public static void main(String args[]) throws Exception {

        String inputFile = System.getProperty("trainingSetAttrFile");
        String outputFile = System.getProperty("trainingSetSvdUFile");

        if (inputFile == null) {
            throw new Exception("training set attributes file missing: " + inputFile);
        }

        if (outputFile == null) {
            throw new Exception("training set svd U file missing: " + outputFile);
        }

        String inputFilePath = System.getProperty("user.home") + "/data_files/" + inputFile + ".csv";
        String outputFilePath = System.getProperty("user.home") + "/data_files/" + outputFile + ".csv";

        int rows = 0;
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String sCurrentLine = br.readLine();

        int cols = sCurrentLine.split(",").length;

        while ((sCurrentLine = br.readLine()) != null) {
            rows++;
        }

        br.close();

        Matrix m = new Matrix(rows, cols);

        int i = 0;
        BufferedReader br2 = new BufferedReader(new FileReader(inputFilePath));

        while ((sCurrentLine = br2.readLine()) != null) {
            String[] fields = sCurrentLine.split(",");

            for(int j = 0; j< fields.length; j++){
                m.set(i, j, Double.parseDouble(fields[j]));
            }

            i++;
        }

        m.transpose();

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath));

        for(i = 0; i<m.getArray().length; i++){

            bw.write(Double.toString(m.get(i,0)));

            for(int j=1; j<m.getArray()[i].length; j++){
                bw.write(",");
                bw.write(Double.toString(m.get(i,j)));
            }
        }

        bw.close();
    }
}
