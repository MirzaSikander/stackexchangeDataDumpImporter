package featureVectorCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FeatureVectorCreator {

	public FeatureVectorCreator() {
		// TODO Auto-generated constructor stub
	}

	public static void CreateFeatureVectors(FileOutputStream output) {
		String TagFile = "/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TagsGreaterThan10.csv";
		String QuestionFile = "/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/QuestionsWithTags.csv";

		List<String> tags;

		Pattern pattern = Pattern.compile("<([^>]+)>");

		// Read in all the lines from the tag file.
		try {
			tags = Files.readAllLines(Paths.get(TagFile),
					StandardCharsets.UTF_8);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		try (Stream<String> questions = Files.lines(Paths.get(QuestionFile))) {
			questions.forEachOrdered((String q) -> {

				String[] fields = q.split(",");

				if (fields.length != 2) {
					throw new RuntimeException("Illegal format " + q);
				}

				Matcher m = pattern.matcher(fields[1]);
				int count = 0;
				List<String> associatedtags = new LinkedList<String>();

				while (m.find()) {
					count++;
					associatedtags.add(m.group(1));

				}

				if (count == 0) {
					// There should be at least one tag.
					throw new RuntimeException("Tag not found" + q);
				}

				for (String tag : tags) {
					boolean found = false;

					for (String associatedTag : associatedtags) {
						if (tag.compareTo(associatedTag) == 0) {
							try {
								output.write("1,".getBytes());
								found = true;
								break;
							} catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException(
										"Cannot write further");
							}
						}
					}

					if (!found) {
						try {
							output.write("0,".getBytes());
						} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException("Cannot write further");
						}
					}
				}

				try {
					output.write("\n".getBytes());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Cannot write further");
				}

				System.out.println("Done with question number: " + fields[0]);
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		String FeatureVectorFile = "/Users/mirzasikander/Desktop/TagFeatureVectors.csv";

		try (FileOutputStream output = new FileOutputStream(new File(
				FeatureVectorFile))) {
			CreateFeatureVectors(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
