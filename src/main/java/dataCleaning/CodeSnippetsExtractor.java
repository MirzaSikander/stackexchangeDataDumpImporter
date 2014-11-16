package dataCleaning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by mirzasikander on 11/15/14.
 * Expects a csv file with this format:
 * question_id,title,body,tags,score,favorite_count,view_count,creation_date,last_activity_date,last_edit_date,user_id_experts,answers
 */
public class CodeSnippetsExtractor {
    public static final int BODY_INDEX = 2;
    public static final int ANSWERS_INDEX = 11;
    public static final int BODY_SNIPPETS_INDEX = 12;
    public static final int ANSWERS_SNIPPETS_INDEX = 13;
    public static final int NUMBER_OF_COLUMNS = 12;

    public static void main(String args[]) throws Exception {
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

        extractCodeSnippets(inputFile, outputFile);
    }

    private static void extractCodeSnippets(String inputFile, String outputFile) throws IOException {
        FileOutputStream output = new FileOutputStream(new File(
                outputFile));

        output.write(("question_id,title,body,tags,score,favorite_count," +
                "view_count,creation_date,last_activity_date," +
                "last_edit_date,user_id_experts,answers," +
                "body_snippets,answers_snippets\n").getBytes());

        Stream<String> questions = Files.lines(Paths.get(inputFile));

        questions.forEachOrdered((String q) -> {

            String[] fields = q.split(",");

            if (fields.length != NUMBER_OF_COLUMNS) {
                throw new RuntimeException("Illegal format " + q);
            }

            String body = fields[BODY_INDEX];
            String answers = fields[ANSWERS_INDEX];

            Pattern pattern = Pattern.compile("%3Ccode%3E(?:(?!%3C%2Fcode%3E).)*%3C%2Fcode%3E");

            StringBuilder newBody = new StringBuilder(body.length());
            StringBuilder bodySnippets = new StringBuilder();
            Matcher m = pattern.matcher(body);
            int index = 0;

            while (m.find()) {
                newBody.append(body.substring(index, m.start()));
                index = m.end();
                bodySnippets.append(m.group(0));
            }

            newBody.append(body.substring(index));

            StringBuilder newAnswers = new StringBuilder(answers.length());
            StringBuilder answersSnippets = new StringBuilder();
            m = pattern.matcher(answers);
            index = 0;

            while (m.find()) {
                newAnswers.append(answers.substring(index, m.start()));
                index = m.end();
                answersSnippets.append(m.group(0));
            }

            newAnswers.append(answers.substring(index));

            StringBuilder newFields = new StringBuilder(q.length());

            for (int i = 0; i < fields.length + 2; i++) {
                if (i == BODY_INDEX) {
                    newFields.append(newBody);
                    newFields.append(',');
                } else if (i == ANSWERS_INDEX) {
                    newFields.append(newAnswers);
                    newFields.append(',');
                } else if (i == BODY_SNIPPETS_INDEX) {
                    newFields.append(bodySnippets);
                    newFields.append(',');
                } else if (i == ANSWERS_SNIPPETS_INDEX) {
                    newFields.append(answersSnippets);
                } else {
                    newFields.append(fields[i]);
                    newFields.append(',');
                }
            }

            newFields.append('\n');

            try {
                output.write(newFields.toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Cannot write further");
            }

        });

        questions.close();

        output.close();
    }
}
