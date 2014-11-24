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


        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

        int count = 0;
	//cannot use header for split because commas inside words
        String header = bufferedReader.readLine();

	String sCurrentLine = bufferedReader.readLine();
	String[] fields = sCurrentLine.split(",");

	//bug
        int numberOfFields = fields.length;
	count++;

        while ((sCurrentLine = bufferedReader.readLine()) != null) {
            count++;
        }

        bufferedReader.close();

	System.out.println("\nFinished collecting metadata.");

	int numberOfQuestions = count;
	System.out.println("\n Rows:"+ numberOfQuestions+" Cols:"+numberOfFields);
        Matrix matrix = new Matrix(numberOfQuestions, numberOfFields);

        String line = null ;
        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(inputFile));


        //Skip the first line
        bufferedReader1.readLine();
	int i = 0;

        while((line=bufferedReader1.readLine())!=null) {

            String[] cols = line.split(",");

            for (int j = 0; j < cols.length; j++) {

		if(cols[j].isEmpty()){
			System.out.println("\n i:"+i+" j:"+j+"\n");
		}

                matrix.set(i, j, Double.parseDouble(cols[j]));
            }

            i++;
        }

        bufferedReader1.close();

	System.out.println("\nFinished processing file.\n");

        BufferedWriter bufferedWriterForU = new BufferedWriter(new FileWriter(outputFileForU));
        BufferedWriter bufferedWriterForV = new BufferedWriter(new FileWriter(outputFileForV));
        BufferedWriter bufferedWriterForS = new BufferedWriter(new FileWriter(outputFileForS));

	System.out.println("Starting svd...");

        SingularValueDecomposition singularValueDecomposition = matrix.svd();

        Matrix U = singularValueDecomposition.getU();
        Matrix V = singularValueDecomposition.getV();
        double[] S = singularValueDecomposition.getSingularValues();

	System.out.println("\nFinished svd\n");
	System.out.println("\nWriting to file...\n");

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

	System.out.println("\nFinished writing U\n");

        for (int j = 0; j < V.getArray().length; j++) {
            for(int k=0;k<V.getArray()[j].length;k++){
                bufferedWriterForV.write(Double.toString(V.get(j, k)));
                bufferedWriterForV.write(",");
            }
            bufferedWriterForV.write("\n");
        }

        bufferedWriterForV.close();
	System.out.println("\nFinished writing V\n");

        for (int j = 0; j < S.length; j++) {
                bufferedWriterForS.write(Double.toString(S[j]));
                bufferedWriterForS.write(",");
            }

        bufferedWriterForS.close();
	System.out.println("\nFinished writing S\n");
    }
}
