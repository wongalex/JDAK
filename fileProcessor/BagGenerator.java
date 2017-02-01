/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileProcessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import provtool.BagAdt;

/**
 *
 * @author jwoodrin
 */
public class BagGenerator {

    BagAdt bag = new BagAdt();
    FileProcessor mainThread;
    int dataSetType;
    Vector<BagAdt> bagList = null;
    String fileType; // uri or file
    String path;

    Hashtable<String, String> stopWords;

    //   Hashtable<String, Integer> wordTracker = new Hashtable<String, Integer>();
    // main routine
    public void preProcessor() {
        String ft = "";
        try {
            ft = checkFileType();
            switch (ft) {
                case "html":
                    bag.contents = mainThread.readHtmlFileContents(path + bag.fileName);
                    break;
                case "":
                    break;
                default:
                    bag.contents = mainThread.readFileContents(path + bag.fileName);
                    break;
            }
            processWords();

            // write to staged file directory
            stageFile(ft);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void run() {
        try {

            if (fileType.equals("uri")) {
                bag.contents = mainThread.readRemoteSite(bag.uri);
            } else if (fileType.equals("file")) {
                String ft = checkFileType();
                switch (ft) {
                    case "html":
                        bag.contents = mainThread.readHtmlFileContents(path + bag.fileName);
                        break;
                    default:
                        bag.contents = mainThread.readFileContents(path + bag.fileName);
                        break;
                }
            }
            bag.unProcessedWrdCnt = countWords(bag.contents);
            processWords();
            bag.processedWrdCnt = getProcesedWordCount();
            // add the the vector of bags
            bagList.add(bag);
        } catch (Exception e) {
        }
    }

    public String checkFileType() {
        String[] s = bag.fileName.split("\\.");
        String type = "";
        if (s.length > 1) {
            type = s[1];
        }
        return type;
    }

    private int getProcesedWordCount() {

        ArrayList<Integer> arr = new ArrayList<Integer>(bag.wordTracker.values());
        int sum = 0;
        for (Integer cnt : arr) {
            sum += cnt;
        }
        return sum;
    }

    public int countWords(String contents) {
        int count = 0;
        String[] words = contents.split(" ");
        count = words.length;
        return count;
    }

    private void processWords() {
        // change all words to lowercase
        bag.contents = bag.contents.toLowerCase();

        // remove all {word} 
        bag.contents = bag.contents.replaceAll("\\{[^\\}]+\\}", " ");
        // remove /someword 
        bag.contents = bag.contents.replaceAll("\\\\[^\\s}]+\\s", " ");

        // remove all punctuation (have to do this before removing possessive nouns)
        bag.contents = bag.contents.replaceAll("\\p{P}\\s+", " ");

        // remove special characters
        bag.contents = bag.contents.replaceAll("[?!@#$%^&*-_+=)(}{`~|'\"]", " ");

        //bag.contents = (new StringBuilder()).append(bag.contents.split("\\s+")).toString();
        // remove two character or less words (now the s on the end of the possessive nouns is removed)
        bag.contents = bag.contents.replaceAll("\\b\\w{1,2}\\b\\s?", "");

//        // stem all words
//        String[] unstemmedWords = bag.contents.split("\\s+");
//        char[] unstemmedWord;
//        String stemmedWords = "";
//        Stemmer stemmer = new Stemmer();
//        for(String word : unstemmedWords){
//        	stemmer.init();
//        	unstemmedWord = word.toCharArray();
//        	stemmer.add(unstemmedWord, unstemmedWord.length);
//        	stemmer.stem();
//        	stemmedWords = (new StringBuilder()).append(stemmedWords).append(stemmer.toString()).append(" ").toString();;
//        }
//        bag.contents = stemmedWords;
        // remove stop words
        ArrayList<String> words1 = new ArrayList<String>(stopWords.keySet());
        for (String s : words1) {
            //if(s.length() > 2){
            bag.contents = bag.contents.replaceAll(" " + s + " ", " ");
            //}
        }
        /*		
         $line =~ s/\W/ /g;        # remove all punctuation 
         $line =~ s/\b\w\b/ /g;    # remove single chars
         $line =~ s/\b\w\w\b/ /g;  # remove two char words
         */

        // now count words (and amount of instances of unique words) after preprocessing
        String[] words = bag.contents.split("\\s+");
        for (String word : words) {
            // check for stopwords
            Boolean isStopword = stopWords.contains(word);
            if (!isStopword) {
                // check to see if this word has already been added, and if so increment the counter
                Boolean alreadyAdded = bag.wordTracker.containsKey(word);
                if (alreadyAdded) {
                    int wordCnt = bag.wordTracker.get(word);
                    bag.wordTracker.put(word, wordCnt + 1);
                } // if not, add it to the list of words
                else {
                    bag.wordTracker.put(word, 1);
                }
            }
        }
        //     bag.contents = "";

    }

    private void stageFile(String fileType) {
        Writer writer = null;
        String path = mainThread.preprocessedPath;
        String fullPath = path + fileType + "\\";
        File f = new File(fullPath);
        // check to see if a directory with name fileType exists
        if (!f.exists()) {
            // then create it
            f.mkdir();
        }
        try {

            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fullPath + bag.fileName), "utf-8"));
            writer.write(bag.contents);
        } catch (IOException ex) {
            // report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
//        // set our working directory path in relation to our context root
//        String path = mainThread.preprocessedPath;
//        String fullPath = path + fileType + "\\";
//        File f = new File(fullPath);
//        // check to see if a directory with name fileType exists
//        if(!f.exists()){
//            // then create it
//            f.mkdir();
//        } 
//        
//        try{
//            File newFile = new File(fullPath + bag.fileName);
//            // write file and contents to the appropriate directory
//            PrintWriter pw = new PrintWriter(fullPath + bag.fileName);
//            pw.print(bag.contents);
//            pw.flush();
//            pw.close();
//        }catch(Exception e){}
    }
}
