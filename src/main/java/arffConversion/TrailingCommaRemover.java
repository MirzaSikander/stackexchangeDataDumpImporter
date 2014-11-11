package arffConversion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TrailingCommaRemover {
    public static String file;
    public static String FeatureVectorFile;

    public static void RemoveComma(FileOutputStream output) {

        try (Stream<String> questions = Files.lines(Paths.get(file))) {
            questions.forEachOrdered((String q) -> {

                char[] charArray = q.toCharArray();

                charArray[charArray.length - 1] = '\n';

                try {
                    output.write(String.copyValueOf(charArray).getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Cannot write further");
                }

            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {

        String inputFile = System.getProperty("inputFile");
        String outputFile = System.getProperty("outputFile");

        if(inputFile == null){
            throw new Exception("input file missing: "+inputFile);
        }

        if(outputFile == null){
            throw new Exception("output file missing: "+outputFile);
        }

        file = System.getProperty("user.home") + "/data_files/"+inputFile+".csv";

        FeatureVectorFile = System.getProperty("user.home") + "/data_files/" + outputFile+".csv";

        try (FileOutputStream output = new FileOutputStream(new File(
                FeatureVectorFile))) {
            RemoveComma(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
