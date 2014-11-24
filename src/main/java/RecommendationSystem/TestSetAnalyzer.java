package RecommendationSystem;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by mirzasikander on 11/23/14.
 */
public class TestSetAnalyzer {

    public static final int QUESTION_ID_INDEX = 0;
    public static final int USER_ID_EXPERTS_INDEX = 10;

    private static Map<String, LinkedList<String>> getUserQuestionsAnsweredMap(String testSetAttrFileName) throws IOException {

        Map<String, LinkedList<String>> userQuestionsMap = new LinkedHashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(testSetAttrFileName));

        //skipping header
        String sCurrentLine = br.readLine();

        if (sCurrentLine == null) {
            throw new IOException("File is empty");
        }

        while ((sCurrentLine = br.readLine()) != null) {

            String[] fields = sCurrentLine.split(",");
            String question_id = fields[QUESTION_ID_INDEX];
            String sUser = fields[USER_ID_EXPERTS_INDEX];
            String[] users = sUser.substring(1, sUser.length()-1).split(";");

            for (String user : users) {

                LinkedList<String> answers = userQuestionsMap.get(user);

                if (answers == null) {

                    answers = new LinkedList<>();
                    answers.add(question_id);
                    userQuestionsMap.put(user, answers);
                } else {

                    answers.add(question_id);
                }
            }
        }

        return userQuestionsMap;
    }

    public static void main(String args[]) throws Exception {

        String testSetAttrFileName = System.getProperty("testSetAttrFile");
        String outputFileName = System.getProperty("outputFile");

        if (testSetAttrFileName == null) {
            throw new Exception("test set attribute file missing: " + testSetAttrFileName);
        }

        if (outputFileName == null) {
            throw new Exception("output directory missing: " + outputFileName);
        }

        String testSetAttrFilePath = System.getProperty("user.home") + "/data_files/" + testSetAttrFileName + ".csv";
        String outputFilePath = System.getProperty("user.home") + "/data_files/" + outputFileName + ".csv";

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath));

        Map<String, LinkedList<String>> userQuestionsMap = getUserQuestionsAnsweredMap(testSetAttrFilePath);

        for (Map.Entry<String, LinkedList<String>> userQuestionsPair : userQuestionsMap.entrySet()) {
            String userId = userQuestionsPair.getKey();
            LinkedList<String> questionsAnswered = userQuestionsPair.getValue();

            bw.write(userId);
            bw.write(",");
            bw.write(Integer.toString(questionsAnswered.size()));
            bw.write("\n");
        }

        bw.close();
    }
}
