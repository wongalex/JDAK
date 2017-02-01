/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileProcessor;

import java.io.BufferedReader;
import provtool.BagAdt;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.StrictMath.abs;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author jwoodrin
 */
public class FileProcessor {

    //   public BagAdt[] bags;
    int size;
    int completed = 0;
    Hashtable<String, String> stopWords = new Hashtable<String, String>();
    Hashtable<String, String> approvedFileTypes = new Hashtable<String, String>();
    String preprocessedPath = ".\\data\\preprocessed\\";

    /**
     * routine for ds1
     */
    public BagAdt[] runDataSet1() {
        readFileFormats();
        String path = ".\\data\\data_set_1\\human-generated-dev\\rawdata\\";
        List<String> fileNames = getDataSetFileNames(path);
        List<String> dataSources = getDataOneSetSources();

        Vector<BagAdt> bags = new Vector<>();

        // process sources
        for (String file : dataSources) {

            BagGenerator bg = new BagGenerator();
            bg.dataSetType = 1;
            bg.bagList = bags;
            bg.bag.fileName = file;
            bg.bag.uri = file;
            // DEBUG: the string contents of file look good for all dataSources
            //System.out.println(file);
            bg.fileType = "uri";
            bg.path = "";
            bg.bag.source = true;
            bg.mainThread = this;
            bg.stopWords = stopWords;

            // start the bag generator
            bg.run();

        }

        // process news files
        for (String file : fileNames) {

            BagGenerator bg = new BagGenerator();
            bg.dataSetType = 1;
            bg.bagList = bags;
            bg.bag.fileName = file;
            bg.bag.uri = file;
            // DEBUG: the string contents of file look good for all news files
            //System.out.println(file);
            bg.fileType = "file";
            bg.path = path;
            bg.bag.source = false;
            bg.mainThread = this;
            bg.stopWords = stopWords;
            // start the bag generator
            bg.run();

        }
        // calculate distances        
        BagAdt[] bgs = formatBagList(bags);
        CalculateDistances(bgs);
        return bgs;
    }

    /* OLD
     public BagAdt[] runGenericDataSet() {
     readFileFormats();
     // path to data
     // C:\Users\jwoodrin\Documents\NetBeansProjects\ProvTool\data\data_set_2\machine-generated-dev\rawdata
     String path = ".\\data\\data_set_2\\machine-generated-dev\\rawdata\\";
     List<String> fileNames = getDataSetFileNames(path);

     fileNames = filterFileTypes(fileNames);

     Vector<BagAdt> bagList = new Vector<>();

     List<BagGenerator> threads = new ArrayList<BagGenerator>();

     //  // process news files
     for (String file : fileNames) {

     BagGenerator bg = new BagGenerator();
     bg.dataSetType = 2;
     bg.bagList = bagList;
     bg.bag.fileName = file;
     bg.bag.uri = file;
     bg.fileType = "file";
     bg.path = path;
     bg.mainThread = this;
     bg.stopWords = stopWords;
     threads.add(bg);
     // start the thread
     bg.run();

     }
 
     // calculate distances        
     BagAdt[] bags = formatBagList(bagList);
     CalculateDistances(bags);
     return bags;

     }*/
    public BagAdt[] postProcess() {
        /* Read in all topic model output files */
        String path = ".\\data\\TopicModelingOutput\\";
        List<String> fileNames = getDataSetFileNames(path);
        String origFileDir = ".\\data\\preprocessed\\";
        List<BagAdt> bags = new ArrayList<>();

        /* Loop through all entries in each output file */
        for (String file : fileNames) {
            try {
                // read lines
                String filePath = path + file;
                Charset charset = Charset.defaultCharset();
                List<String> fileLines = Files.readAllLines((new File(filePath)).toPath(), charset);

                // for all lines
                for (String line : fileLines) {
                    // note: split on bar | 
                    String[] lineParts = line.split("\\|");

                    // not right size? wrong info... so skip it
                    if (lineParts.length != 2) {
                        continue;
                    }
                    String[] ext = lineParts[0].split("\\.");
                    if (ext.length < 2) {
                        continue;
                    }
                    String fileType = ext[1].toLowerCase();
                    String origFileLoc = origFileDir + fileType + "\\" + lineParts[0];
                    // read in file and get the word count

                    // element 1 is filename... put it in bag
                    BagAdt bag = new BagAdt();
                    bag.contents = this.readFileContents(origFileLoc);
                    bag.fileName = lineParts[0];
                    bag.uri = lineParts[0];
                    // get word count
                    BagGenerator bg = new BagGenerator();
                    bag.processedWrdCnt = bg.countWords(bag.contents);
                    bag.unProcessedWrdCnt = bag.processedWrdCnt;
                    bag.source = false;
                    bag.fileExtension = ext[1];

                    // split element 2 on space
                    String[] topicPercents = lineParts[1].split("\\s+");

                    // element 2 is all topic correlations
                    // for each topic
                    for (String topicPercent : topicPercents) {

                        // put into arraylist
                        try {
                            bag.topics.add(Double.parseDouble(topicPercent));
                        } catch (Exception e) {
                            System.out.println(e + "\nTopic Correlation could not be parsed for: " + lineParts[0]);
                        }
                    }

                    bags.add(bag);
                }
            } catch (Exception e) {
            }
        }
        // return our results
        BagAdt[] bgs = new BagAdt[bags.size()];
        bags.toArray(bgs);//new BagAdt[bagList.size()];
        // calculate distances
        calculateDistancesGeneric(bgs);
        return bgs;
    }

    public void calculateDistancesGeneric(BagAdt[] bagsToCalc) {
        List<Integer> barriers = new ArrayList<>();
        String currentType = null;
        int count = 0;
        // allocate distance array
        for (BagAdt bag : bagsToCalc) {
            bag.bagDistances = new int[bagsToCalc.length];
            if (!bag.fileExtension.equals(currentType)) {
                barriers.add(count);
                currentType = bag.fileExtension;
            }
            // set all
            for (int i = 0, im = bag.bagDistances.length; i < im; i++) {
                // to default value
                bag.bagDistances[i] = -1;
            }
            count++;
        }

        for (int i = 0; i < barriers.size(); i++) {
            // process each sub array
            int min = barriers.get(i);
            int max;
            if (i == barriers.size() - 1) {
                max = bagsToCalc.length - 1;
            } else {
                max = barriers.get(i + 1) - 1;
            }
            // step through our sub-array values
            for (int j = min; j < max; j++) {
                // the current file
                BagAdt currentBag = bagsToCalc[j];
                // for all other files
                for (int k = min; k < max; k++) {
                    if (k == j) {
                        continue;
                    }

                    currentBag.bagDistances[k] = topicComparison(currentBag.topics, bagsToCalc[k].topics);
                }
            }
        }
    }

    private int topicComparison(List<Double> currentTopics, List<Double> otherTopics) {
        int toReturn = -1;
        Double averageDistance = 0.0;
        Double totalDistances = 0.0;
        Double difference = 0.0;
        int count = 0;

        if (currentTopics.size() == otherTopics.size()) {
            // for all the topics in the current bag
            for (int i = 0; i < currentTopics.size(); i++) {

                // if both bags are above 75% in the topic
                if (currentTopics.get(i) > 0.80 && otherTopics.get(i) > 0.80) {
                    count++;
                    // find distance: abs(curTopic[iteration] - otherTopic[iteration])
                    difference = Math.abs(currentTopics.get(i) - otherTopics.get(i));
                // add to the running average distance
                    // TODO THINK ABOUT THIS,,, ^^^ next line of code up and if statment
                    totalDistances += difference;
                }

            }
            averageDistance = totalDistances / count;

            // return final average distance
            toReturn = (int) (averageDistance * 1000.0);

        } else {
            System.out.println("Error occured during distance calculation: amount of topics is different!");
            toReturn = -1;
        }
        return toReturn;
    }

    public void preProcess() {

        readFileFormats();
        // wipe out all pre-processed sub directories
        cleanPreProcessedFiles(preprocessedPath);
        // create directory if it doesn't exist

        // path to data
        // C:\Users\jwoodrin\Documents\NetBeansProjects\ProvTool\data\data_set_2\machine-generated-dev\rawdata
        String path = ".\\data\\data_set_2\\machine-generated-dev\\rawdata\\";
        List<String> fileNames = getDataSetFileNames(path);

        fileNames = filterFileTypes(fileNames);

        for (String file : fileNames) {

            BagGenerator bg = new BagGenerator();
            bg.dataSetType = 2;
            //        bg.bagList = bagList;
            bg.bag.fileName = file;
            bg.bag.uri = file;
            bg.fileType = "file";
            bg.path = path;
            bg.mainThread = this;
            bg.stopWords = stopWords;
            //    threads.add(bg);
            // start the thread
            bg.preProcessor();

        }
    }

    private void cleanPreProcessedFiles(String path) {
        // wipe out everything in the path directory
        deleteFolder(new File(path));
        // recreate the preprocessed directory
        new File(path).mkdir();

    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private void readFileFormats() {
        try {
            String file = ".\\fileformats\\file_formats.txt";
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                approvedFileTypes.put(line, line);
            }
            br.close();
        } catch (Exception e) {
        }
    }

    private List<String> filterFileTypes(List<String> files) {

        List<String> filteredFiles = new ArrayList<String>();
        for (String file : files) {
            String[] fileType = file.split("\\.");
            if (fileType.length > 1) {
                for (String type : fileType) {
                    Boolean approved = false;
                    approved = approvedFileTypes.contains(type);
                    if (approved) {
                        filteredFiles.add(file);
                    }
                }
            }
        }
        return filteredFiles;

    }

    private BagAdt[] formatBagList(Vector<BagAdt> bagList) {
        BagAdt[] bags = new BagAdt[bagList.size()];
        bagList.toArray(bags);//new BagAdt[bagList.size()];
//        int cnt = 0;
//        //for (BagAdt bag : bags) {
//        for(BagAdt bag : bagList){
//            //bags[cnt] = bagList.elementAt(cnt);
//            bags[cnt] = bag;
//            cnt++;
//        }

        return bags;
    }

    private void CalculateDistances(BagAdt[] bags) {
        // inner bag list

        for (int i = 0; i < bags.length; i++) {
            bags[i].bagDistances = new int[bags.length];
            // outter bag list
            for (int j = 0; j < bags.length; j++) {
                if (i != j) {
                    BagAdt fromBag = bags[i];
                    BagAdt toBag = bags[j];
                    bags[i].bagDistances[j] = calculateDistance(fromBag, toBag);

                } else {
                    bags[i].bagDistances[i] = -1;
                }

            }
        }
    }

    private int calculateDistance(BagAdt bag1, BagAdt bag2) {
        int distance = 0;
        Set<String> words1 = bag1.wordTracker.keySet();

        Set<String> words2 = bag2.wordTracker.keySet();

        // iterate through our lists and compare
        for (String word1 : words1) {
            Boolean found = false;
            for (String word2 : words2) {
                if (word1.equals(word2)) {
                    int bag1WrdCnt = bag1.wordTracker.get(word1);
                    int bag2WrdCnt = bag2.wordTracker.get(word1);
                    distance += abs(bag1WrdCnt - bag2WrdCnt);
                    found = true;
                }
            }
            if (!found) {
                int bag1WrdCnt = bag1.wordTracker.get(word1);
                distance += bag1WrdCnt;
            }
        }
        return distance;
    }

    public void processStopWords() {
        try {
            String file = ".\\stopwords\\stop_generic.txt";
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                line = line.toLowerCase();
                // TODO: STEM THE STOP WORD HERE
                stopWords.put(line, line);
            }
            br.close();
        } catch (Exception e) {
        }

    }

    /**
     * reads all the news files from the file system
     *
     * @param fileNames
     * @return
     */
    private List<String> readNews(List<String> fileNames) {
        Document doc = null;
        List<String> newsText = new ArrayList<String>();

        for (String title : fileNames) {

            String path = ".\\data\\data_set_1\\human-generated-dev\\rawdata\\" + title;
            try {
                String text = new String(Files.readAllBytes(Paths.get("file")), StandardCharsets.UTF_8);
                doc = Jsoup.parse(text);
                newsText.add(doc.text());
            } catch (IOException ex) {
                Logger.getLogger(FileProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return newsText;
    }

    public String readFileContents(String path) {
        String fileContent = "";
        try {
            Path filePath = new File(path).toPath();
            fileContent = new String(Files.readAllBytes(filePath), "UTF-8");
        } catch (Exception e) {
            System.out.println(e);
        }
        return fileContent;
    }

    /**
     * called from threads to read in each file
     *
     * @param path
     * @return
     */
    public String readHtmlFileContents(String path) {

        Document doc = null;
        String contents = "";

        try {
            String text = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            doc = Jsoup.parse(text);
            contents = doc.text();
        } catch (IOException ex) {
            Logger.getLogger(FileProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return contents;
    }

    public String readRemoteSite(String uri) {
        String contents = "";
        Document doc = null;
        try {
            doc = Jsoup.connect(uri).get(); // reads the remote site
            contents = doc.text();

        } catch (IOException ex) {
            //System.out.println("Error: " + uri + " " + ex);
            //Logger.getLogger(FileProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return contents;
    }

    /**
     * reads all the remote html sources for the human generated articles
     *
     * @param dataSources
     * @return
     */
    private List<String> readSources(List<String> dataSources) {

        Document doc = null;
        String text;
        ArrayList<String> sourceText = new ArrayList<String>();
        for (String source : dataSources) {
            try {
                doc = Jsoup.connect(source).get();
                text = doc.text();
                sourceText.add(text);

            } catch (IOException ex) {
                Logger.getLogger(FileProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return sourceText;
    }

    /**
     *
     * @return
     */
    public List<String> getDataSetFileNames(String path) {

        List<String> results = new ArrayList<String>();
        File[] files = new File(path).listFiles();
        //del added
        String fileName = "";
        for (File file : files) {
            if (file.isFile()) {
                fileName = file.getName();
                if (!fileName.equals("human_sources.txt")) {
                    results.add(file.getName());
                }
            }
        }
        return results;
    }

    public List<String> getDataOneSetSources() {
        try {
            //   Path filePath = new File("C:\\Users\\jwoodrin\\Documents\\NetBeansProjects\\ProvTool\\data\\data_set_1\\human-generated-dev\\rawdata\\human_sources.txt").toPath();
            Path filePath = new File(".\\data\\data_set_1\\human-generated-dev\\rawdata\\human_sources.txt").toPath();
            Charset charset = Charset.defaultCharset();
            List<String> stringList = Files.readAllLines(filePath, charset);
            // DEBUG: These all look good
            //for(String s : stringList)
            //System.out.println(s);
            return stringList;
        } catch (Exception e) {
        }
        return null;
    }

}
