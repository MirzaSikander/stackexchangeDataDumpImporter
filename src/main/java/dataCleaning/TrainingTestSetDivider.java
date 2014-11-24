package dataCleaning;

import java.io.*;
import java.util.Random;

public class TrainingTestSetDivider {

    public static void main(String args[]) throws Exception {
        String inputFileName = System.getProperty("inputFile");
        String trainingSetFileName = System.getProperty("trainingSetFile");
        String testSetFileName = System.getProperty("testSetFile");

        if (inputFileName == null) {
            throw new Exception("input file missing: " + inputFileName);
        }

        if (trainingSetFileName == null) {
            throw new Exception("output file missing: " + trainingSetFileName);
        }

        if (testSetFileName == null) {
            throw new Exception("output file missing: " + testSetFileName);
        }

        String inputFile = System.getProperty("user.home") + "/data_files/" + inputFileName + ".csv";
        String trainingSetFile = System.getProperty("user.home") + "/data_files/" + trainingSetFileName + ".csv";
        String testSetFile = System.getProperty("user.home") + "/data_files/" + testSetFileName + ".csv";

        int questions_count = countQuestions(inputFile);

        byte[] trainingSetIdsMap = generateRandomIds(questions_count);

        divideIntoTrainingTestSets(trainingSetIdsMap, inputFile, trainingSetFile, testSetFile);
    }

    private static void divideIntoTrainingTestSets(byte[] trainingSetIdsMap, String inputFile, String trainingSetFile, String testSetFile) throws Exception {
        FileOutputStream trainingSet = new FileOutputStream(new File(
                trainingSetFile));
        FileOutputStream testSet = new FileOutputStream(new File(
                testSetFile));

        trainingSet.write(("question_id,title,body,tags,score,favorite_count," +
                "view_count,creation_date,last_activity_date," +
                "last_edit_date,user_id_experts,answers," +
                "body_snippets,answers_snippets\n").getBytes());
        testSet.write(("question_id,title,body,tags,score,favorite_count," +
                "view_count,creation_date,last_activity_date," +
                "last_edit_date,user_id_experts,answers," +
                "body_snippets,answers_snippets\n").getBytes());

        BufferedReader br = new BufferedReader(new FileReader(inputFile));

        String sCurrentLine;
        int index = 0;

        while ((sCurrentLine = br.readLine()) != null) {

            if(trainingSetIdsMap[index] == 1){
                trainingSet.write((sCurrentLine+"\n").getBytes());
            }else{
                testSet.write((sCurrentLine+"\n").getBytes());
            }

            index++;
        }

        if(index != trainingSetIdsMap.length){
           throw new Exception("Mismatch between file contents and the mapping of ids");
        }

        trainingSet.close();
        testSet.close();
    }

    private static byte[] generateRandomIds(int totalIds) {
        int halfIds = totalIds / 2;
        byte[] idsMap = new byte[totalIds];

        //initialize
        for (int i = 0; i < totalIds; i++) {
            idsMap[i] = 0;
        }

        Random randomGenerator = new Random();

        for (int i = 0; i < halfIds; i++) {

            int randomInt = randomGenerator.nextInt(totalIds);

            while (idsMap[randomInt] == 1) {
                randomInt = randomGenerator.nextInt(totalIds);
            }

            idsMap[randomInt] = 1;
        }

        return idsMap;
    }

    private static int countQuestions(String inputFile) throws IOException {
        int count = 0;

        BufferedReader br = new BufferedReader(new FileReader(inputFile));

        String sCurrentLine;

        while ((sCurrentLine = br.readLine()) != null) {
            count++;
        }

        return count;
    }
}
