package RecommendationSystem;

import Jama.Matrix;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mirzasikander on 11/21/14.
 */
public class RecommendationCreator {
    public static final int QUESTION_ID_INDEX = 0;
    public static final int TAGS_INDEX = 3;
    public static final int SCORE_INDEX = 4;
    public static final int FAVORITE_COUNT_INDEX = 5;
    public static final int VIEW_COUNT_INDEX = 6;
    public static final int USER_ID_EXPERTS_INDEX = 10;
    private static final Pattern TAGS_EXTRACTOR = Pattern.compile("<([^>]+)>");

    public static void main(String args[]) throws Exception {

        String trainingSetAttrFileName = System.getProperty("trainingSetAttrFile");
        String trainingSetSvdUFileName = System.getProperty("trainingSetSvdUFile");
        String trainingSetSvdSFileName = System.getProperty("trainingSetSvdSFile");
        String trainingSetSvdVFileName = System.getProperty("trainingSetSvdVFile");
        String maxComponents = System.getProperty("maxComponents");
        String testSetAttrFileName = System.getProperty("testSetAttrFile");
        String testSetBowFileName = System.getProperty("testSetBowFile");
        String outputDirectoryName = System.getProperty("outputDirectory");


        if (trainingSetAttrFileName == null) {
            throw new Exception("training set attributes file missing: " + trainingSetAttrFileName);
        }

        if (trainingSetSvdUFileName == null) {
            throw new Exception("training set svd U file missing: " + trainingSetSvdUFileName);
        }

        if (trainingSetSvdSFileName == null) {
            throw new Exception("training set svd S file missing: " + trainingSetSvdSFileName);
        }

        if (trainingSetSvdVFileName == null) {
            throw new Exception("training set svd V file missing: " + trainingSetSvdVFileName);
        }

        if (maxComponents == null) {
            throw new Exception("parameter max components is missing");
        }

        if (testSetAttrFileName == null) {
            throw new Exception("test set attribute file missing: " + testSetAttrFileName);
        }

        if (testSetBowFileName == null) {
            throw new Exception("test set bag of words file missing: " + testSetBowFileName);
        }

        if (outputDirectoryName == null) {
            throw new Exception("output directory missing: " + outputDirectoryName);
        }

        String trainingSetAttrFilePath = System.getProperty("user.home") + "/data_files/" + trainingSetAttrFileName + ".csv";
        String trainingSetSvdUFilePath = System.getProperty("user.home") + "/data_files/" + trainingSetSvdUFileName + ".csv";
        String trainingSetSvdSFilePath = System.getProperty("user.home") + "/data_files/" + trainingSetSvdSFileName + ".csv";
        String trainingSetSvdVFilePath = System.getProperty("user.home") + "/data_files/" + trainingSetSvdVFileName + ".csv";
        String testSetAttrFilePath = System.getProperty("user.home") + "/data_files/" + testSetAttrFileName + ".csv";
        String testSetBowFilePath = System.getProperty("user.home") + "/data_files/" + testSetBowFileName + ".csv";
        String outputDirectoryPath = System.getProperty("user.home") + "/data_files/" + outputDirectoryName;

        int maxComp = Integer.parseInt(maxComponents);

        Path outputDirectory = Paths.get(outputDirectoryPath);

        if (!Files.exists(outputDirectory)) {
            Files.createDirectory(outputDirectory);
        }

        Map<String, LinkedList<String>> userQuestionsMap = getUserQuestionsAnsweredMap(trainingSetAttrFileName);
        Matrix singularValuesInverse = getSingularValueMatrixInverse(trainingSetSvdSFilePath, maxComp);
        Matrix termVectors = getTermVectors(trainingSetSvdVFilePath, maxComp);

        //Iterate through all users
        for (Map.Entry<String, LinkedList<String>> userQuestionsPair : userQuestionsMap.entrySet()) {

            String userId = userQuestionsPair.getKey();
            LinkedList<String> questionsAnsweredId = userQuestionsPair.getValue();
            String outputFilePath = outputDirectoryPath + "/" + userId;

            generateRecommendations(
                    questionsAnsweredId,
                    trainingSetAttrFilePath,
                    trainingSetSvdUFilePath,
                    singularValuesInverse,
                    termVectors,
                    testSetAttrFilePath,
                    testSetBowFilePath,
                    outputFilePath,
                    maxComp);

            //TODO: remove this line
            break;
        }
    }

    private static Matrix getTermVectors(String trainingSetSvdVFilePath, int maxComp) throws IOException {

        BufferedReader trainingSetSvdVBr = new BufferedReader(new FileReader(trainingSetSvdVFilePath));

        String sCurrentLine = trainingSetSvdVBr.readLine();
        int cols = sCurrentLine.split(",").length;
        int rows = 1;

        while ((sCurrentLine = trainingSetSvdVBr.readLine()) != null) {
            rows++;
        }

        trainingSetSvdVBr.close();
        int comp = Math.min(cols, maxComp);
        Matrix termVectors = new Matrix(rows, comp);
        trainingSetSvdVBr = new BufferedReader(new FileReader(trainingSetSvdVFilePath));
        int i = 0;

        while ((sCurrentLine = trainingSetSvdVBr.readLine()) != null) {

            String[] fields = sCurrentLine.split(",");

            for (int j = 0; j < comp; j++) {
                termVectors.set(i, j, Double.parseDouble(fields[j]));
            }
        }

        return termVectors;
    }

    private static Matrix getSingularValueMatrixInverse(String trainingSetSvdSFilePath, int maxComp) throws IOException {

        BufferedReader trainingSetSvdSBr = new BufferedReader(new FileReader(trainingSetSvdSFilePath));

        //skipping header
        String sCurrentLine = trainingSetSvdSBr.readLine();

        if (sCurrentLine == null) {
            throw new IOException("File is empty");
        }

        String[] values = sCurrentLine.split(",");

        int matrixRowCount = Math.min(values.length, maxComp);
        Matrix singularValues = new Matrix(matrixRowCount, matrixRowCount, 0);

        for (int i = 0; i < matrixRowCount; i++) {
            singularValues.set(i, i, Double.parseDouble(values[i]));
        }

        Matrix inverse = singularValues.inverse();

        return inverse;
    }

    private static void generateRecommendations(LinkedList<String> questionsAnsweredId, String trainingSetAttrFilePath, String trainingSetSvdUFilePath, Matrix singularValueInverse, Matrix termVectors, String testSetAttrFilePath, String testSetBowFilePath, String outputFilePath, int maxComp) throws IOException {

        BufferedReader testSetAttrBr = new BufferedReader(new FileReader(testSetAttrFilePath)),
                testSetBowBr = new BufferedReader(new FileReader(testSetBowFilePath));

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath));

        String currentTestQAttr, currentTestQBow;

        while ((currentTestQAttr = testSetAttrBr.readLine()) != null) {

            String[] testAttr = currentTestQAttr.split(",");

            //Getting tags and other attributes
            String testQuestionId = testAttr[QUESTION_ID_INDEX];
            String testQuestionUserIdExpert = testAttr[USER_ID_EXPERTS_INDEX];
            int testQuestionScore = Integer.parseInt(testAttr[SCORE_INDEX]);
            int testQuestionFavoriteCount = Integer.parseInt(testAttr[FAVORITE_COUNT_INDEX]);
            String testQuestionViewCount = testAttr[VIEW_COUNT_INDEX];

            List<String> testQuestionTags = getTagsList(testAttr[TAGS_INDEX]);


            //Generating query vector
            currentTestQBow = testSetBowBr.readLine();

            Matrix result = generateQueryVector(currentTestQBow, singularValueInverse, termVectors);
            double[] queryVector = result.getArray()[0];
            //TODO: do normalization
            //queryVector = normalize(queryVector);

            if (currentTestQBow == null) {
                throw new RuntimeException("Somethings wrong. Test bow file mismatch with attributes file");
            }

            BufferedReader trainingSetAttrBr = new BufferedReader(new FileReader(trainingSetAttrFilePath)),
                    trainingSetSvdUBr = new BufferedReader(new FileReader(trainingSetSvdUFilePath));

            String currentTrainingQAttr, currentTrainingQBow, mostSimilarTrainingQAttr;

            //skip the first line for header
            currentTrainingQBow = trainingSetSvdUBr.readLine();
            currentTrainingQAttr = trainingSetAttrBr.readLine();
            mostSimilarTrainingQAttr = currentTrainingQAttr;

            double similarity = -1;

            while ((currentTrainingQAttr = trainingSetAttrBr.readLine()) != null) {

                String[] trainingAttr = currentTrainingQAttr.split(",");

                //Getting tags and other attributes
                String trainingQuestionId = trainingAttr[QUESTION_ID_INDEX];

                if(questionsAnsweredId.contains(trainingQuestionId)) {

                    //Calculate similarity


                    currentTrainingQBow = trainingSetSvdUBr.readLine();

                    if (currentTrainingQBow == null) {
                        throw new RuntimeException("Somethings wrong. Training bow file mismatch with attributes file");
                    }

                    String[] values = currentTrainingQBow.split(",");

                    int comp = Math.min(maxComp, values.length);

                    double[] significantValues = new double[comp];

                    for (int i = 0; i < comp; i++) {
                        significantValues[i] = Double.parseDouble(values[i]);
                    }

                    //TODO: do normalization.
                    //significantValues = normalize(significantValues);

                    double cosineSimilarity = cosineSimilarity(queryVector, significantValues);

                    if(cosineSimilarity > similarity){

                        similarity = cosineSimilarity;
                        mostSimilarTrainingQAttr = currentTrainingQAttr;
                    }
                }
            }

            String[] trainingAttr = mostSimilarTrainingQAttr.split(",");
            List<String> trainingQuestionTags = getTagsList(trainingAttr[TAGS_INDEX]);
            int commonTagCount = findCommonTagsCount(trainingQuestionTags, testQuestionTags);

            double rank = testQuestionScore * (similarity * 2 + commonTagCount);


            bw.write(Double.toString(rank));
            bw.write(",");
            bw.write(currentTestQAttr);
            bw.write(currentTrainingQAttr);
            bw.write("\n");
        }

        bw.close();
    }

//    private static double findRank(double[] queryVector, double[] significantValues, List<String> testQuestionTags, List<String> trainingQuestionTags, int testQuestionScore) {
//        int tagIntersection = findCommonTagsCount(testQuestionTags, trainingQuestionTags);
//
//        return testQuestionScore * ( cosineSimilarity * 2 + tagIntersection );
//    }

    private static int findCommonTagsCount(List<String> testQuestionTags, List<String> trainingQuestionTags) {

        int commonTags = 0;

        for(String testTag: testQuestionTags){
            if(trainingQuestionTags.contains(testTag)){
                commonTags++;
            }
        }

        return commonTags;
    }

    private static double cosineSimilarity(double[] docVector1, double[] docVector2) {

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;
        double cosineSimilarity = 0.0;

        for (int i = 0; i < docVector1.length; i++) //docVector1 and docVector2 must be of same length
        {
            dotProduct += docVector1[i] * docVector2[i];  //a.b
            magnitude1 += Math.pow(docVector1[i], 2);  //(a^2)
            magnitude2 += Math.pow(docVector2[i], 2); //(b^2)
        }

        magnitude1 = Math.sqrt(magnitude1);//sqrt(a^2)
        magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)

        if (magnitude1 != 0.0 | magnitude2 != 0.0) {
            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
        } else {
            return 0.0;
        }
        return cosineSimilarity;
    }

    private static double[] normalize(double[] vector) {

        int sum = 0;

        for (int i = 0; i < vector.length; i++) {
            sum += vector[i] * vector[i];
        }

        double length = Math.sqrt(sum);

        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / length;
        }

        return vector;
    }

    private static List<String> getTagsList(String stags) {

        List<String> tags = new ArrayList<>(5);
        Matcher m = TAGS_EXTRACTOR.matcher(stags);
        int count = 0;

        while (m.find()) {

            count++;
            tags.add(m.group(1));
        }

        if (count == 0) {
            // There should be at least one tag.
            throw new RuntimeException("Tag not found: " + tags);
        }

        return tags;
    }

    private static Matrix generateQueryVector(String currentTestQBow, Matrix singularValueInverse, Matrix termVectors) {

        String[] bow = currentTestQBow.split(",");

        Matrix bowVector = new Matrix(1, bow.length);

        for (int i = 0; i < bow.length; i++) {
            bowVector.set(0, i, Double.parseDouble(bow[i]));
        }

        return bowVector.times(termVectors).times(singularValueInverse);
    }

    private static Map<String, LinkedList<String>> getUserQuestionsAnsweredMap(String trainingSetAttrFileName) throws IOException {

        Map<String, LinkedList<String>> userQuestionsMap = new LinkedHashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(trainingSetAttrFileName));

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

}
