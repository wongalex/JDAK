package provCalc;

import java.util.LinkedList;

public class FileCluster {

    // Corresponds with the index of the array of BagAdts.
    public int index;
    
    // Corresponds with the fileName specified by the BagAdt.
    public String fileName;
    
    // Corresponds with the URI specified by the BagAdt, if applicable.
    public String uri;
    
    // Holds all the indexes of the close sources to the news.
    public LinkedList<Integer> closeSource;
    
    // Used to help stop a provenance chain (e.g. A->B->C->A).
    public boolean visited = false;
    
    // Holds the index to the BagAdt that is most related with less words.
    public int closestLessWordsIndex;
    
    // Holds the index to the BagAdt that is most related with less words.
    public int closestMoreWordsIndex;
}
