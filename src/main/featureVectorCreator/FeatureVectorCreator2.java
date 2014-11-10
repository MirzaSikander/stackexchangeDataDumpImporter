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

//Put questions as columns
public class FeatureVectorCreator2 {

	public static String FeatureVectorFile = "/home/azureuser/data_files/TagFeatureVectors_Horizontal.csv";
	public static String TagFile = "/home/azureuser/data_files/TagsGreaterThan10.csv";
	public static String QuestionFile = "/home/azureuser/data_files/QuestionsWithTags.csv";
//	public static String FeatureVectorFile = "/Users/mirzasikander/Desktop/TagFeatureVectors.csv";
//	public static String TagFile = "/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/TagsGreaterThan10.csv";
//	public static String QuestionFile = "/Users/mirzasikander/Dropbox/school/CSCI 599/Data Files/QuestionsWithTags.csv";

	public static void CreateFeatureVectors(FileOutputStream output) {

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

		// creating the header. Will be used for arff conversion.
		try (Stream<String> questions = Files.lines(Paths.get(QuestionFile))) {
			questions.forEachOrdered((String q) -> {

				String[] fields = q.split(",");

				if (fields.length != 2) {
					throw new RuntimeException("Illegal format " + q);
				}

				try {
					output.write((fields[0] + ",").getBytes());
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Cannot write further");
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done with headers");

		try {
			output.write("\n".getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot write further");
		}

		for (int i = 0; i < tags.size(); i++) {
			int index = i;

			try (Stream<String> questions = Files
					.lines(Paths.get(QuestionFile))) {
				questions.forEachOrdered((String q) -> {

					Matcher m = pattern.matcher(q);
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

					boolean found = false;

					for (String associatedTag : associatedtags) {
						if (tags.get(index).compareTo(associatedTag) == 0) {
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

				});

			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				output.write("\n".getBytes());
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot write further");
			}

			System.out.println(i + ") tag name: " + tags.get(i));
		}
	}

	public static void main(String[] args) {

		try (FileOutputStream output = new FileOutputStream(new File(
				FeatureVectorFile))) {
			CreateFeatureVectors(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
