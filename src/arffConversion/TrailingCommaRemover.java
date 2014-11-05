package arffConversion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TrailingCommaRemover {

	public static void RemoveComma(FileOutputStream output) {
		String file = "/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TagFeatureVectorsWithTagNames.csv";

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
		String FeatureVectorFile = "/Users/mirzasikander/Desktop/TagFeatureVectorsWithTagNames2.csv";

		try (FileOutputStream output = new FileOutputStream(new File(
				FeatureVectorFile))) {
			RemoveComma(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
