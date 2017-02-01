/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package provtool;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
/**
 *
 * @author jwoodrin
 */
public class BagAdt {
        // the stored distances between this and others
    public int[] bagDistances;
    public List<Double> topics = new ArrayList<>();
    public String fileExtension = "";
    public String fileName;
    public String contents; // the processed contents of the file
    public String uri;
    public int processedWrdCnt;
    public int unProcessedWrdCnt;  
    public boolean source;
    public Hashtable<String, Integer> wordTracker = new Hashtable<String, Integer>();        
}
