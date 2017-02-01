package provRec;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import provtool.BagAdt;

/**
 * Constructs a provenance graph in terms of RDF statements. The graph is
 * written in turtle notation.
 * @author Kriti
 */
public class Construct {

    boolean humanGeneratedData = false;
    String predicate = "prov:wasDerivedFrom";
    String aProvEntity = "a prov:Entity";

    public Construct(boolean humanGeneratedData) {
        if (humanGeneratedData) {
            // changed from "prov:wasPrimarySource"
            predicate = "prov:hadPrimarySource";
        }
    }

    /**
     * Constructs a provenance graph in terms of RDF statements. The graph is
     * written in turtle notation.
     *
     * @param provMap - A mapping of resources to each other in terms of
     * resource wasDerivedFrom otherResource. Relationships are currently
     * derived from left (most present data created) to right (previously
     * created data that contributed to the existence of the data on the left)
     * @param outputFileName
     */
    public void construct(List<List<BagAdt>> provMap, String outputFileName) {

        String fileName = outputFileName;//"out.ttl";//C:/Users/kriti gupta/Desktop/out.ttl

        try {
            // prepare to print to a file
            PrintWriter outputStream = new PrintWriter(fileName);
            // print the standard w3prov prefixes
            outputStream.println(" @prefix	prov:	<http://www.w3.org/ns/prov#> .");
            outputStream.println(" @prefix	rdfs:	<http://www.w3.org/2000/01/rdf-schema#> .");
            outputStream.println(" @prefix	xsd:	<http://www.w3.org/2001/XMLSchema#> .\n");
            outputStream.flush();

            // for the entire map
            for (int i = 0, im = provMap.size(); i < im; i++) {
                // get the next graph in the map
                List<BagAdt> currentGraph = provMap.get(i);
                // for all of the bags in the graph
                for (int j = 0, jm = currentGraph.size(); j < jm; j++) {
                    // define first one
                    if(j == 0){
                        outputStream.println("<" + currentGraph.get(j).uri + "> " + aProvEntity + " .\n");
                        outputStream.flush();
                    } else {
                        // define the next bag as well...
                        // <someURI> a prov:Entity .
                        outputStream.println("<" + currentGraph.get(j).uri + "> " + aProvEntity + " .\n");
                        outputStream.flush();
                        // state the relationship... 
                        // tuple <someURI> prefixedPredicate <someOtherURI> .
                        outputStream.println("<#" + currentGraph.get(0).uri + "> " + predicate + "\n <" + currentGraph.get(j).uri + "> .\n");
                        outputStream.flush();
                    }
                }
            }          
            
            // clean up
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
        /**
     * Constructs a provenance graph in terms of RDF statements. The graph is
     * written in turtle notation.
     *
     * @param provMap - A mapping of resources to each other in terms of
     * resource wasDerivedFrom otherResource. Relationships are currently
     * derived from left (most present data created) to right (previously
     * created data that contributed to the existence of the data on the left)
     * @param outputFileName
     */
    public void constructGeneric(List<List<BagAdt>> provMap, String outputFileName) {

        String fileName = outputFileName;//"out.ttl";//C:/Users/kriti gupta/Desktop/out.ttl

        try {
            // prepare to print to a file
            PrintWriter outputStream = new PrintWriter(fileName);
            // print the standard w3prov prefixes
            outputStream.println(" @prefix	prov:	<http://www.w3.org/ns/prov#> .");
            outputStream.println(" @prefix	rdfs:	<http://www.w3.org/2000/01/rdf-schema#> .");
            outputStream.println(" @prefix	xsd:	<http://www.w3.org/2001/XMLSchema#> .\n");
            outputStream.flush();

            // for the entire map
            for (int i = 0, im = provMap.size(); i < im; i++) {
                // get the next graph in the map
                List<BagAdt> currentGraph = provMap.get(i);
                // for all of the bags in the graph
              //  for (int j = 0, jm = currentGraph.size(); j < jm; j++) {
                    /*
                    
                    if(j == 0){
                        outputStream.println("<" + currentGraph.get(j).uri + "> " + aProvEntity + " .\n");
                        outputStream.flush();
                    } else {
                        // define the next bag as well...
                        // <someURI> a prov:Entity .
                        outputStream.println("<" + currentGraph.get(j).uri + "> " + aProvEntity + " .\n");
                        outputStream.flush();
                        // state the relationship... 
                        // tuple <someURI> prefixedPredicate <someOtherURI> .
                        outputStream.println("<#" + currentGraph.get(0).uri + "> " + predicate + "\n <" + currentGraph.get(j).uri + "> .\n");
                        outputStream.flush();
                    }
                    */
                    // for the first bag
                  //  if (j == 0) {
                        // define it... 
                        // <someURI> a prov:Entity .
                        outputStream.println("<" + currentGraph.get(0).uri + "> " + aProvEntity + " .\n");
                        outputStream.flush();
                        
                   // } else {
                        // define the next bag as well... if its not the last iteration
                        // <someURI> a prov:Entity .
                        //if(j == jm - 1){
                            outputStream.println("<" + currentGraph.get(0 + 1).uri + "> " + aProvEntity + " .\n");
                            outputStream.flush();
                        //}
                        // state the relationship... 
                        // tuple <someURI> prefixedPredicate <someOtherURI> .
                        outputStream.println("<" + currentGraph.get(0).uri + "> " + predicate + "\n <" + currentGraph.get(0 + 1).uri + "> .\n");
                        outputStream.flush();
                    //}
             //   }
            }
            // clean up
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
