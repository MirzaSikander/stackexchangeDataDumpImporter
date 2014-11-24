package dataCleaning;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mirzasikander on 11/16/14.
 */
public class BagOfWordsVectorCreator {
    public static final int TITLE_INDEX = 1;
    public static final int BODY_INDEX = 2;
    public static final int ANSWERS_INDEX = 11;
    private static final int SIGNIFICANCE_THRESHOLD = 100;

    private String URL_MATCHER = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    private static final Pattern REMOVE_HTML_TAGS = Pattern.compile("<.+?>");
    private static final Pattern REMOVE_ANSWER_SEPARATOR = Pattern.compile("\\s\\|\\|\\s");

    private int documentCount;
    private Set<String> stopWords;
    private LinkedHashMap<String, Integer> wordsDocumentFreqMap;

    public BagOfWordsVectorCreator() {

        stopWords = new HashSet<>();
        wordsDocumentFreqMap = new LinkedHashMap<>();
    }

    public void Create(String trainingSetFilePath, String testSetFilePath, String trainingBowFilePath, String testBowFilePath) throws IOException {

        generateStopWords();
        combine(trainingSetFilePath, trainingSetAnalysis(trainingSetFilePath), trainingBowFilePath);
        combine(testSetFilePath, testSetAnalysis(testSetFilePath), testBowFilePath);
    }

    private void combine(String inputFilePath, ArrayList<Map<String, Integer>> questionsBow, String outputFilePath) throws IOException {
        File file = new File(outputFilePath);

        // if file doesnt exists, then create it.
        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        Iterator<String> it = wordsDocumentFreqMap.keySet().iterator();

        while (it.hasNext()) {
            String word = it.next();
            bw.write(",\"" + word+"\"");
        }

        bw.write("\n");


        for (Map<String, Integer> termFreqMap : questionsBow) {
            //add textual feature vector
            boolean first = true;

            for (Map.Entry<String, Integer> wordDocFreqPair : wordsDocumentFreqMap.entrySet()) {

                boolean found = false;

                if(first){
                    first = false;
                }else{
                    bw.write(",");
                }

                for (Map.Entry<String, Integer> termFreqPair : termFreqMap.entrySet()) {

                    if (termFreqPair.getKey().equals(wordDocFreqPair.getKey())) {

                        found = true;
                        double inverseTermFreq = getInverseTermFreq(wordDocFreqPair.getValue());
                        double tfIdf = termFreqPair.getValue() * inverseTermFreq;
                        bw.write(Double.toString(tfIdf));
                        break;
                    }
                }

                if (!found) {
                    bw.write("0");
                }
            }

            bw.write("\n");
        }

        bw.close();
    }

    private double getInverseTermFreq(Integer value) {
        return Math.log10((double) documentCount / (double) value);
    }

    public static String removeHtmlTags(String string) {

        if (string == null || string.length() == 0) {
            return string;
        }

        Matcher m = REMOVE_HTML_TAGS.matcher(string);
        return m.replaceAll("");
    }

    public static String removeAnswerSeparator(String string) {

        if (string == null || string.length() == 0) {
            return string;
        }

        Matcher m = REMOVE_ANSWER_SEPARATOR.matcher(string);
        return m.replaceAll("");
    }

    public static boolean isNumeric(String str) {
        return str.matches("[-+]?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean isDate(String str) {
        return str.matches("\\b[0-9]{2}/[0-9]{2}/(?:[0-9]{4})?(?:[0-9]{2})?\\b");
    }



    private void generateStopWords() {

        String[] stopWordsArray = ("a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can," +
                "cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however," +
                "i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only," +
                "or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this," +
                "tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your").split(",");

        for (String word : stopWordsArray) {
            stopWords.add(word);
        }

        stopWords.add("--");
        stopWords.add("...");
        stopWords.add("''");
        stopWords.add("``");
        stopWords.add("---------------------");
        stopWords.add("--------------------");
        stopWords.add("???????????????");
        stopWords.add("'''");
    }

    public ArrayList<Map<String, Integer>> trainingSetAnalysis(String trainingSetFilePath) throws IOException {

        //List containing bag of words for each question.
        ArrayList<Map<String, Integer>> questionsBow = new ArrayList<>();

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        BufferedReader br = new BufferedReader(new FileReader(trainingSetFilePath));

        String sCurrentLine = br.readLine();

        if (sCurrentLine == null) {
            throw new IOException("File is empty");
        }

        int docCount = 0;

        while ((sCurrentLine = br.readLine()) != null) {

            HashMap<String, Integer> bow = new HashMap<>();

            String[] fields = sCurrentLine.split(",");


            //Preprocessing for text analysis
            String title = fields[TITLE_INDEX];
            String body = fields[BODY_INDEX];
            String answer = fields[ANSWERS_INDEX];

            //title
            title = URLDecoder.decode(title, "UTF8");
            title = title.substring(1, title.length() - 1);

            //body
            body = URLDecoder.decode(body, "UTF8");
            body = body.substring(1, body.length() - 1);
            body = removeHtmlTags(body);

            //answer
            answer = URLDecoder.decode(answer, "UTF8");
            answer = answer.substring(1, answer.length() - 1);
            answer = removeAnswerSeparator(removeHtmlTags(answer));

            StringBuilder combiner = new StringBuilder(title.length() + 1 + body.length() + answer.length());
            combiner.append(title);
            combiner.append(". ");
            combiner.append(body);
            combiner.append(answer);

            Annotation document = new Annotation(combiner.toString());

            // run all Annotators on this text
            pipeline.annotate(document);

            // these are all the sentences in this document
            // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                // traversing the words in the current sentence
                // a CoreLabel is a CoreMap with additional token-specific methods
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                    String word = token.word().toLowerCase();

                    if (word.length() == 1) {
                        //skip punctuations
                        continue;
                    }

                    if (stopWords.contains(word)) {
                        //skip if word is a stop word.
                        continue;
                    }

                    String lemma = token.lemma().toLowerCase();

                    if (stopWords.contains(lemma)) {
                        //skip if lemma is a stop word.
                        continue;
                    }

                    if (lemma.matches(URL_MATCHER)) {
                        //skip if lemma is a url.
                        continue;
                    }

//                    if(isNumeric(lemma)){
//                        continue;
//                    }
//
//                    if(isDate(lemma)){
//                        continue;
//                    }

                    Integer count = bow.get(lemma);

                    if (count == null) {
                        bow.put(lemma, 1);
                    } else {
                        bow.put(lemma, count + 1);
                    }
                }

                //Update document frequency
                for (String word : bow.keySet()) {
                    Integer documentFreq = wordsDocumentFreqMap.get(word);

                    if (documentFreq == null) {
                        wordsDocumentFreqMap.put(word, 1);
                    } else {
                        wordsDocumentFreqMap.put(word, documentFreq + 1);
                    }
                }
            }

            questionsBow.add(bow);
            docCount++;
        }

        documentCount = docCount;
        br.close();

        //Remove infrequent words
        for (Iterator<Map.Entry<String, Integer>> it = wordsDocumentFreqMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            if (entry.getValue() < SIGNIFICANCE_THRESHOLD) {
                it.remove();
            }
        }

        return questionsBow;
    }

    public ArrayList<Map<String, Integer>> testSetAnalysis(String testSetFilePath) throws IOException {

        //List containing bag of words for each question.
        ArrayList<Map<String, Integer>> questionsBow = new ArrayList<>();

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        BufferedReader br = new BufferedReader(new FileReader(testSetFilePath));

        String sCurrentLine = br.readLine();

        if (sCurrentLine == null) {
            throw new IOException("File is empty");
        }

        while ((sCurrentLine = br.readLine()) != null) {

            HashMap<String, Integer> bow = new HashMap<>();

            String[] fields = sCurrentLine.split(",");


            //Preprocessing for text analysis
            String title = fields[TITLE_INDEX];
            String body = fields[BODY_INDEX];

            //title
            title = URLDecoder.decode(title, "UTF8");
            title = title.substring(1, title.length() - 1);

            //body
            body = URLDecoder.decode(body, "UTF8");
            body = body.substring(1, body.length() - 1);
            body = removeHtmlTags(body);

            StringBuilder combiner = new StringBuilder(title.length() + 1 + body.length());
            combiner.append(title);
            combiner.append(". ");
            combiner.append(body);

            Annotation document = new Annotation(combiner.toString());

            // run all Annotators on this text
            pipeline.annotate(document);

            // these are all the sentences in this document
            // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                // traversing the words in the current sentence
                // a CoreLabel is a CoreMap with additional token-specific methods
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                    String lemma = token.lemma().toLowerCase();

                    //Is it in the vocabulary or not?
                    if (wordsDocumentFreqMap.containsKey(lemma)) {

                        Integer count = bow.get(lemma);

                        if (count == null) {
                            bow.put(lemma, 1);
                        } else {
                            bow.put(lemma, count + 1);
                        }
                    }
                }
            }

            questionsBow.add(bow);
        }

        br.close();

        return questionsBow;
    }

    public static void main(String args[]) throws Exception {

        String trainingSetFileName = System.getProperty("trainingSetFile");
        String testSetFileName = System.getProperty("testSetFile");
        String trainingBowFileName = System.getProperty("trainingBowFile");
        String testBowFileName = System.getProperty("testBowFile");

        if (trainingSetFileName == null) {
            throw new Exception("training set file missing: " + trainingSetFileName);
        }

        if (testSetFileName == null) {
            throw new Exception("test set file missing: " + testSetFileName);
        }

        if (trainingBowFileName == null) {
            throw new Exception("training bag of words file missing: " + trainingBowFileName);
        }

        if (testBowFileName == null) {
            throw new Exception("test bag of words file missing: " + testBowFileName);
        }

        String trainingSetFilePath = System.getProperty("user.home") + "/data_files/" + trainingSetFileName + ".csv";
        String testSetFilePath = System.getProperty("user.home") + "/data_files/" + testSetFileName + ".csv";
        String trainingBowFilePath = System.getProperty("user.home") + "/data_files/" + trainingBowFileName + ".csv";
        String testBowFilePath = System.getProperty("user.home") + "/data_files/" + testBowFileName + ".csv";

        BagOfWordsVectorCreator fvc = new BagOfWordsVectorCreator();
        fvc.Create(trainingSetFilePath, testSetFilePath, trainingBowFilePath, testBowFilePath);
    }
}
