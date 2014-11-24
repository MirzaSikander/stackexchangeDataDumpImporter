package dataCleaning;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mirzasikander on 11/20/14.
 */
public class VectorCombiner {
    public static final int QUESTION_ID_INDEX = 0;
    public static final int TAGS_INDEX = 3;
    public static final int SCORE_INDEX = 4;
    public static final int FAVORITE_COUNT_INDEX = 5;
    public static final int VIEW_COUNT_INDEX = 6;
    public static final int CREATION_DATE_INDEX = 7;
    public static final int USER_ID_EXPERTS_INDEX = 10;
    private static final Pattern TAGS_EXTRACTOR = Pattern.compile("<([^>]+)>");
    private static final Pattern TIME_OF_DAY_EXTRACTOR = Pattern.compile("(\\d\\d):(\\d\\d):(\\d\\d)");

    public static void main(String args[]) throws Exception {

        String questionsFileName = System.getProperty("questionsFile");
        String questionsBowFileName = System.getProperty("questionsBowFile");
        String tagsFileName = System.getProperty("tagsFile");
        String outputFileName = System.getProperty("outputFile");

        if (questionsFileName == null) {
            throw new Exception("questions file missing: " + questionsFileName);
        }

        if (questionsBowFileName == null) {
            throw new Exception("questions file missing: " + questionsBowFileName);
        }

        if (tagsFileName == null) {
            throw new Exception("tags file missing: " + tagsFileName);
        }

        if (outputFileName == null) {
            throw new Exception("output file missing: " + outputFileName);
        }

        String questionsFilePath = System.getProperty("user.home") + "/data_files/" + questionsFileName + ".csv";
        String questionsBowFilePath = System.getProperty("user.home") + "/data_files/" + questionsBowFileName + ".csv";
        String tagsFilePath = System.getProperty("user.home") + "/data_files/" + tagsFileName + ".csv";
        String outputFilePath = System.getProperty("user.home") + "/data_files/" + outputFileName + ".csv";

        combine(questionsFilePath, questionsBowFilePath, tagsFilePath, outputFilePath);
    }

    private static void combine(String questionsFilePath, String questionsBowFilePath, String tagsFilePath, String outputFilePath) throws IOException {

        List<String> allTags = getTags(tagsFilePath);
        File file = new File(outputFilePath);

        // if file doesnt exists, then create it.
        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));


        BufferedReader questionsReader = new BufferedReader(new FileReader(questionsFilePath));
        String currentQuestion = questionsReader.readLine();

        if (currentQuestion == null) {
            throw new IOException("Questions file is empty");
        }

        //Creating the header for csv file.
        bw.write("question_id,user_id_expert,score,favorite_count,view_count,creation_time");

        //Adding tag names to the header
        for (String tag : allTags) {
            bw.write("," + tag);
        }

        //Adding bow components to the header
        BufferedReader bowReader = new BufferedReader(new FileReader(questionsBowFilePath));
        String currentBowVector = bowReader.readLine();

        if (currentBowVector == null) {
            throw new IOException("bow file is empty");
        }

        bw.write(",");
        bw.write(currentQuestion);
        bw.write("\n");

        //actual content
        while ((currentQuestion = questionsReader.readLine()) != null) {
            String[] fields = currentQuestion.split(",");

            String questionId = fields[QUESTION_ID_INDEX];
            String userIdExpert = fields[USER_ID_EXPERTS_INDEX];
            String score = fields[SCORE_INDEX];
            String favoriteCount = fields[FAVORITE_COUNT_INDEX];
            String viewCount = fields[VIEW_COUNT_INDEX];

            StringBuilder sb = new StringBuilder(questionId.length()
                    + userIdExpert.length()
                    + score.length()
                    + favoriteCount.length()
                    + viewCount.length());

            sb.append(questionId);
            sb.append("," + userIdExpert);
            sb.append("," + score);
            sb.append("," + favoriteCount);
            sb.append("," + viewCount);

            bw.write(sb.toString());

            String creationDate = fields[CREATION_DATE_INDEX];
            String creationTime = getTime(creationDate);
            bw.write(",");
            bw.write(creationTime);

            //add tag vector
            String tags = fields[TAGS_INDEX];
            int[] tagVector = getTagVector(allTags, tags);

            for (int i = 0; i < tagVector.length; i++) {
                bw.write(",");
                bw.write(tagVector[i]);
            }

            //add bow
            currentBowVector = bowReader.readLine();

            if (currentBowVector == null) {
                throw new RuntimeException("Mismatch between questions file and bow file. Exiting");
            }

            bw.write(",");
            bw.write(currentBowVector);
        }

        questionsReader.close();
        bowReader.close();
        bw.close();
    }

    private static List<String> getTags(String tagsFilePath) throws IOException {

        List<String> allTags = new LinkedList<>();
        BufferedReader br = new BufferedReader(new FileReader(tagsFilePath));
        String sCurrentLine;

        while ((sCurrentLine = br.readLine()) != null) {
            allTags.add(sCurrentLine);
        }

        return allTags;
    }

    private static int[] getTagVector(List<String> allTags, String tags) {

        int[] tagVector = new int[allTags.size()];

        Matcher m = TAGS_EXTRACTOR.matcher(tags);
        int count = 0;
        List<String> associatedTags = new LinkedList<String>();

        while (m.find()) {

            count++;
            associatedTags.add(m.group(1));
        }

        if (count == 0) {
            // There should be at least one tag.
            throw new RuntimeException("Tag not found: " + tags);
        }

        for (int i = 0; i < allTags.size(); i++) {
            String tag = allTags.get(i);
            boolean found = false;

            for (String associatedTag : associatedTags) {
                if (tag.compareTo(associatedTag) == 0) {
                    tagVector[i] = 1;
                    found = true;
                    break;
                }
            }

            if (!found) {
                tagVector[i] = 0;
            }
        }

        return tagVector;
    }

    private static String getTime(String creationDate) {
        //Expected format: 2011-11-25 22:32:41

        Matcher m = TIME_OF_DAY_EXTRACTOR.matcher(creationDate);
        m.find();
        return m.group(1) + m.group(2) + m.group(3);
    }
}
