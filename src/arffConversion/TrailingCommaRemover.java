package arffConversion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TrailingCommaRemover {
//		public static String file = "/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TagFeatureVectorsWithTagNames.csv";
//		public static String FeatureVectorFile = "/Users/mirzasikander/Desktop/TagFeatureVectorsWithTagNames2.csv";
		public static String file = "/home/azureuser/data_files/TagFeatureVectors_Horizontal.csv";
		public static String FeatureVectorFile = "/home/azureuser/data_files/TagFeatureVectors_Horizontal_2.csv";

	public static void RemoveComma(FileOutputStream output) {

		try (Stream<String> questions = Files.lines(Paths.get(file))) {
			questions.forEachOrdered((String q) -> {
				
				char[] charArray = q.toCharArray();

				charArray[charArray.length-1] = '\n';
				
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

	public static void main(String[] args) {

		try (FileOutputStream output = new FileOutputStream(new File(
				FeatureVectorFile))) {
			RemoveComma(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
