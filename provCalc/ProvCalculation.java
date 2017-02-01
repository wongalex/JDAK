package provCalc;

import java.util.*;
import provtool.BagAdt;

//-----------------------------------------------------------------------------
// The following lists the different cases for finding the 2 Files with 
// closest distances for recovering provenance for machine generated data.
//  1. No Files meet distance threshold.
//      a. Current File will not be included in Prov.
//  2. Only 1 File meets distance threshold.
//      a. Find if the unProcessedWrdCnt is > or < of current File
//         and set appropriately.
//      b. What if the unProcessedWrdCnt is equal??? <-- need to deal with
//         (currently setup so current File comes after.)
//  3. 2+ Files meet distance threshold.
//      a. One File has a lesser unProcessedWrdCnt than
//         the current File and the other has a greater unProcessedWrdCnt than 
//         the current File. Just set them appropriately.
//      b. What if the unProcessedWrdCnt is equal??? <-- need to deal with
//         (currently setup so current File comes after.)
//----------------------------------------------------------------------------

public class ProvCalculation 
{

    // What will hold our passed input from module 1, pre-processing.
    private BagAdt[] bowArray;

    // Will hold fileName, uri, and what comes before and after the file,
    // if applicable.
    private Vector<FileCluster> fileRelations;

    // Holds all the indexes of the close sources to the news article.
    private LinkedList<Integer> closeSource;

    // Variable for holding our distance threshold. Default value is
    // 200, but can be changed with the use of the constructor or the
    // changeThreshold() method. An initial value is set because the
    // machine generated data uses a static distanceThreshold whereas
    // the human generated data uses a dynamic distanceThreshold.
    private double distanceThreshold = 200;

//----------------------------------------------------------------------------
// changeThreshold()
// Changes the distance threshold if it wasn't set during the Constructor.
    public void changeThreshold(int newThreshold) 
    {
        distanceThreshold = newThreshold;
    }

//----------------------------------------------------------------------------
// create()
// Creates the BagAdt with the order of provenance for human generated data.   
    public List<List<BagAdt>> create(BagAdt[] inputArray) 
    {
        try 
        {
            bowArray = inputArray;
            fileRelations = new Vector<FileCluster>();

            // provBag will be used to keep track of the order of
            // provenance of the BadAdts.
            List<List<BagAdt>> provBag = new ArrayList<List<BagAdt>>();
            
            // Outer loop to get through every Bag of Words in the BoW array.
            for (int i = 0; i < bowArray.length; i++) 
            {
                // Making sure it's a news article and not a source.
                if (bowArray[i].source == false) 
                {
                    closeSource = new LinkedList<Integer>();
                    FileCluster fileRelate = new FileCluster();
                    fileRelate.fileName = bowArray[i].fileName;
                    fileRelate.index = i;
                    if (bowArray[i].uri != null) 
                    {
                        fileRelate.uri = bowArray[i].uri;
                    }
                    // Used for determining the distance threshold
                    // for this news article.
                    int distanceSum = 0;
                    int sourceCount = 0;
                    
                    // Each news article will have a different
                    // distanceThreshold which is dependent on its distance
                    // to all other distances from sources. We sum up the
                    // all the source distances and divide by the number
                    // of sources to get an average distance.
                    for (int k = 0; k < bowArray.length; k++) 
                    {
                        if (bowArray[k].source == true) 
                        {
                            distanceSum += bowArray[i].bagDistances[k];
                            sourceCount++;
                        }
                    }
                    // We set a multiplier of 0.94 to get a number slightly
                    // below the average distanceThreshold in order to
                    // eliminate many false positives.
                    distanceThreshold = (distanceSum / sourceCount) * (0.94);
                    
                    // Inner loop used for the distance array in each BoW index.
                    for (int j = 0; j < bowArray.length; j++) 
                    {
                        // Making sure its a source and not a news article. 
                        if (bowArray[j].source == true) 
                        {
                            // We make sure that the distance is not pointing to itself
                            // with -1 and that the distance is below the
                            // distanceThreshold.
                            if (bowArray[i].bagDistances[j] != -1
                                    && bowArray[i].bagDistances[j] > 1
                                    && bowArray[i].bagDistances[j] < distanceThreshold) 
                            {
                                // Adding all sources that meet the dynamic
                                // distanceThreshold.
                                closeSource.add(j);
                            }
                        }
                    }
                    fileRelate.closeSource = this.closeSource;
                    fileRelations.add(fileRelate);
                }
                // Resetting the distanceThreshold for the next news article.
                distanceThreshold = 0;
            }

            // Reordering the BagAdts for provenance
            for (int i = 0; i < fileRelations.size(); i++) 
            {
                FileCluster current = fileRelations.get(i);
                BagAdt currentBag = bowArray[current.index];
                
                // Check to see if we have already visited this BagAdt.
                // If we have, that means that it was already used in a
                // provenance chain, and we don't need to duplicate the 
                // chain.
                if (current.closeSource != null && current.closeSource.peek() != null) 
                {
                    List<BagAdt> oneProvBag = new ArrayList<BagAdt>();
                    // The first BagAdt in this List signifies the news article.
                    oneProvBag.add(currentBag);
                    
                    // We iterate through the closeSource, adding the BagAdt
                    // corresponding with the index specified in the closeSource.
                    // These BagAdts are potential primary sources to the
                    // news article.
                    while (current.closeSource.size() != 0) 
                    {
                        currentBag = bowArray[current.closeSource.peek()];
                        oneProvBag.add(currentBag);
                        current.closeSource.remove();
                    }
                    provBag.add(oneProvBag);
                }
            }
            return provBag;
        } 
        catch (Exception e) 
        {
            System.out.println(e);
        }
        return null;
    }
//----------------------------------------------------------------------------
// createGeneric()
// Creates the BagAdt with the order of provenance for machine generated data.   
    public List<List<BagAdt>> createGeneric(BagAdt[] inputArray) 
    {
        bowArray = inputArray;
        fileRelations = new Vector<FileCluster>();

        for (int i = 0; i < inputArray.length; i++) 
        {
            fileRelations.add(new FileCluster());
        }

        List<List<BagAdt>> provBag = new ArrayList<List<BagAdt>>();
        // Outer loop to get through every Bag of Words in the BoW array.
        for (int i = 0; i < bowArray.length; i++) 
        {
            // Index with the closest distance of the current
            // iteration of the outer for loop.
            int closestLessWordsIndex = -1;

            // Index with the second closest distance of the current
            // iteration of the outer for loop.
            int closestMoreWordsIndex = -1;

            fileRelations.get(i).fileName = bowArray[i].fileName;
            if (bowArray[i].uri != null) 
            {
                fileRelations.get(i).uri = bowArray[i].uri;
            }

            // Inner loop used for the distance array in each BoW index.
            for (int j = 0; j < bowArray.length; j++) 
            {
                // We make sure that the distance is not pointing to itself
                // with -1 and that the distance is below the
                // distanceThreshold.
                if (bowArray[i].bagDistances[j] != -1
                        && bowArray[i].bagDistances[j] > 1
                        && bowArray[i].bagDistances[j] < distanceThreshold) 
                {
                    // We then check to see if the unProcessedWrdCnt is less
                    // than or equal to the current File's unProcessedWrdCnt.
                    // That way we can determine whether the File
                    // came before or after the current File. For
                    // now, we are assuming that an equal unProcessedWrdCnt
                    // would mean that the File came before the current
                    // File. This will not be true in all cases, which
                    // will throw off our PnR numbers.
                    if (bowArray[j].unProcessedWrdCnt
                            <= bowArray[i].unProcessedWrdCnt) 
                    {
                        // Check if closestLessWordsIndex is NULL, meaning
                        // there hasn't been any input that has been below
                        // the threshold, or if the distance is less than 
                        // the current closest. We save the current index 
                        // to this variable if either case is true.
                        if (closestLessWordsIndex == -1) 
                        {
                            closestLessWordsIndex = j;
                        } 
                        else if (bowArray[i].bagDistances[j]
                                < bowArray[i].bagDistances[closestLessWordsIndex]) 
                        {
                            closestLessWordsIndex = j;
                        }
                    } 
                    else if (bowArray[j].unProcessedWrdCnt
                            > bowArray[i].unProcessedWrdCnt) 
                    {
                        // Check if closestLessWordsIndex is NULL, meaning
                        // there hasn't been any input that has been below
                        // the threshold, or if the distance is less than 
                        // the current closest. We save the current index 
                        // to this variable if either case is true.
                        if (closestMoreWordsIndex == -1) 
                        {
                            closestMoreWordsIndex = j;
                        } 
                        else if (bowArray[i].bagDistances[j]
                                < bowArray[i].bagDistances[closestMoreWordsIndex]) 
                        {
                            closestMoreWordsIndex = j;
                        }
                    }
                }
            }
            fileRelations.get(i).closestLessWordsIndex = closestLessWordsIndex;
            fileRelations.get(i).closestMoreWordsIndex = closestMoreWordsIndex;
        }

        // Reordering the BagAdts for provenance
        for (int i = 0; i < fileRelations.size(); i++) 
        {

            FileCluster current = fileRelations.get(i);
            BagAdt currentBag = bowArray[i];
            // Check to see if we have already visited this BagAdt.
            // If we have, that means that it was already used in a
            // provenance chain, and we don't need to duplicate the 
            // chain.
            if (current.visited == false) 
            {
                current.visited = true;
                if (current.closestMoreWordsIndex != -1) 
                {
                    List<BagAdt> oneProvBag = new ArrayList<BagAdt>();
                    oneProvBag.add(currentBag);
                    currentBag = bowArray[current.closestMoreWordsIndex];
                    current = fileRelations.get(current.closestMoreWordsIndex);
                    current.visited = true;
                    oneProvBag.add(currentBag);
                    provBag.add(oneProvBag);
                }
            }
        }
        return provBag;
    }
}
