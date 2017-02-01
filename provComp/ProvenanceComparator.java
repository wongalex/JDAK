package provComp;

import java.text.DecimalFormat;

import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Compares two provenance graphs. The result of the comparison is the precision
 * and recall between RDF statements contained in the two provenance graphs. The
 * graphs must be contained within two files and the two files must be
 * interpretable as RDF graphs. Please see the java doc for the compare function
 * for the definitions for precision and recall as each relate to the presence
 * or absence of statements in both of the graphs.
 *
 * @author <a href="mailto:davisdb1@u.washington.edu">Del Davis</a>
 * @version 0.1
 * @since 0.1
 * @see #compare
 */
public class ProvenanceComparator {

    private static final boolean DEBUG = false;
    private static final boolean DEBUGTHIS = false;
    private Model rpModel;
    private Model gtModel;
    private boolean readyToCompare;

    /**
     * Runs a simple precision and recall calculation on two provenance rdf
     * files and prints the precision and recall to the console
     *
     * @param args - 1st argument: recovered provenance filename (or URI) ......
     * - 2nd argument: groundtruth provenance filename (or URI)
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            ProvenanceComparator pc = new ProvenanceComparator(args[0], args[1]);
            if (pc.readyToCompare) {
                printResults(pc.compare());
            } else {
                error(2);
                usage();
            } // not properly initialized
        } else { // wrong amount of arguments
            error(1);
            usage();
        }
    }

    /**
     * parses the results from a P&R calculation to the console
     *
     * @param results
     */
    private static void printResults(String results) {
        System.out.println(results);
        // System.out.println("Precision = " +
        // results.split(";")[0].split("="));
        // System.out.println("Recall = " + results.split(";")[1].split("="));
    }

    /**
     * Prints the usage to the console
     */
    private static void usage() {
        System.out.println("Usage: java -jar ProvenanceComparator.jar "
                + "<recovered provenance filename> "
                + "<groundtruth provenance filename>");
    }

    /**
     * prints a coded error message to the screen
     *
     * @param errorNum - the code for the error
     */
    private static void error(int errorNum) {
        switch (errorNum) {
            case 1:
                System.out.println("Incorrect amount of arguments specified");
                break;
            case 2:
                System.out
                        .println("Files do not exist or contain unparseable RDF data");
                break;
            default:
                System.out.println("Unknown error occurred");
                break;
        }
    }

    /**
     * Constructs a new provenance comparator
     *
     * @param recoveredProvFilePath - The location of a file to load into an rdf
     * model that represents the recovered provenance
     * @param groundTruthFilePath - The location of a file to load into an rdf
     * model that represents the ground truth provenance
     */
    public ProvenanceComparator(String recoveredURI, String groundTruthURI) {
        System.out.println("Recovered File Name: " + recoveredURI
                + "\nGround Truth File Name:" + groundTruthURI);
        init(recoveredURI, groundTruthURI);
    }

    /**
     * Used to reset and reload models (reconstruct this ProvenanceComparator)
     *
     * @param recoveredProvURI
     * @param groundTruthURI
     * @return Whether or not the models were loaded properly
     */
    public boolean init(String recoveredProvURI, String groundTruthURI) {
        reset();
        rpModel = RDFDataMgr.loadModel(recoveredProvURI);
        gtModel = RDFDataMgr.loadModel(groundTruthURI);
        readyToCompare = rpModel != null && gtModel != null;
        return readyToCompare;
    }

    /**
     * Indicates whether the initialization (during construction or subsequent
     * initialization) was successful
     *
     * @return Whether or not this comparator is ready to compare
     */
    public boolean ready() {
        return readyToCompare;
    }

    private void reset() {
        rpModel = gtModel = null;
        readyToCompare = false;
    }

    /**
     * <h2>Compares the two models for precision and recall.</h2>
     *
     * <p>
     * Precision calculation:
     * </p>
     *
     * <p>
     * A <i>True Positive</i><b>(TP)</b> is detected when: A statement is found
     * in the ground truth and that statement is also found in the recovered
     * provenance
     * </p>
     *
     * <p>
     * A <i>False Positive</i><b>(FP)</b> is detected when: A statement is found
     * in the recovered provenance, but it was not found in the ground truth
     * </p>
     *
     * <p>
     * A <i>False Negative</i><b>(FN)</b> is detected when: A statement is found
     * in the ground truth, but it was not found in the recovered provenance
     * </p>
     * <p>
     * Precision = TP / (TP + FP)<br>
     * Recall = TP / (TP + FN)
     * </p>
     *
     * @return A string representation of the precision and recall results of
     * the form:
     * <p>
     * &lt;calcType1&gt;=&lt;calcValue1&gt;;&lt;calcTypeN&gt;=&lt;
     * calcValueN&gt;
     * </p>
     */
    public String compare() {
        // default values for P&R... indicate ttl files weren't loaded
        double precision = -1, recall = -1;
        // true positive, false positive, false negative
        double tp = 0, fp = 0, fn = 0;
        // default... indicates that one or both RDF files weren't loaded
        String result = "Unknown";

        if (readyToCompare) {
            // all the statements in the ground truth
            StmtIterator gtIter = gtModel.listStatements();

            // iterate through all the ground truth statements
            while (gtIter.hasNext()) {
                // get the next statement
                Statement statement = gtIter.next();
                /* Get Statement Parts for Debugging and Ignore-Flags */
                RDFNode object = statement.getObject();
                RDFNode subject = statement.getSubject();
                RDFNode predicate = statement.getPredicate();

                /* Basic Debugging Statements */
                if (DEBUG) {
                    if (subject != null) {
                        System.out.println("\nSubject: " + subject.toString());
                    } else {
                        System.out.println("\nSubject: is null!");
                    }
                    if (predicate != null) {
                        System.out.println("\nPredicate: " + predicate.toString());
                    } else {
                        System.out.println("\nPredicate: is null!");
                    }
                    if (object != null) {
                        System.out.println("\nObject: " + object.toString());
                    } else {
                        System.out.println("\nObject: is null!");
                    }
                }

                /* Set Ignore Flags */
                boolean isResourceDefinition = object != null && object.toString().equals("http://www.w3.org/ns/prov#Entity");
                boolean isLabel = predicate != null && predicate.toString().equals("http://www.w3.org/2000/01/rdf-schema#label");

                /* More Specific Debugging Statements */
                if (DEBUG) {
                    System.out.println("\nIs a Resource Definition?: " + isResourceDefinition);
                    System.out.println("Because object is null?... " + (object == null));
                    System.out.println("Because object.toString().equals(\"http://www.w3.org/ns/prov#Entity\") is true?... " + object.toString().equals("http://www.w3.org/ns/prov#Entity"));
                    System.out.println("\nIs a Label: " + isLabel);
                    System.out.println("Because predicate is null?... " + predicate);
                    System.out.println("Because predicate.toString().equals(\"http://www.w3.org/2000/01/rdf-schema#label\") is true?... " + predicate.toString().equals("http://www.w3.org/2000/01/rdf-schema#label"));
                    System.out.println("\nHere is the whole statement: \n" + statement.toString());
                    System.out.println("---------------------------------------------------------\n");
                }

                // statement is a correlation between prov elements
                if (!isResourceDefinition && !isLabel) {
                    if (DEBUG) {
                        System.out.println("\nFound one that is not a label...");
                    }
                    // Statement disposableStatement = ResourceFactory.createStatement(ResourceFactory.createResource("#" + subject.toString().split("#")[1]), ResourceFactory.createProperty(predicate.toString()), object);
                    // System.out.println(disposableStatement.toString());
                    // statement exists in the recovered prov					
                    StmtIterator si = rpModel.listStatements();
                    Statement siCurStmnt;
                    RDFNode siObject;// = statement.getObject();				
                    RDFNode siSubject;// = statement.getSubject();
                    RDFNode siPredicate;// = statement.getPredicate();
                    boolean same;
                    boolean found = false;

                    while (si.hasNext() && !found) {
                        same = true; // assume they are the same
                        siCurStmnt = si.next();
                        siObject = siCurStmnt.getObject();
                        siSubject = siCurStmnt.getSubject();
                        siPredicate = siCurStmnt.getPredicate();
                        String rpCurSubjectString = siSubject.toString();
                        String gtCurSubjectString = subject.toString();
                        String rpCurPredicateString = siPredicate.toString();
                        String gtCurPredicateString = predicate.toString();
                        String rpCurObjectString = siObject.toString();
                        String gtCurObjectString = object.toString();

                        if (DEBUGTHIS && gtCurPredicateString.equals(rpCurPredicateString) && gtCurSubjectString.equals(rpCurSubjectString)) {
                            System.out.println("\nrpCurSubjectString: " + rpCurSubjectString.split("#")[1]);
                            System.out.println("gtCurSubjectString: " + gtCurSubjectString.split("#")[1]);
                            System.out.println("\nrpCurPredicateString: " + rpCurPredicateString);
                            System.out.println("gtCurPredicateString: " + gtCurPredicateString);
                            System.out.println("\nrpCurObjectString: " + rpCurObjectString);
                            System.out.println("gtCurObjectString: " + gtCurObjectString);
                        }

                        same = rpCurPredicateString.equals(gtCurPredicateString);
                        if (same) {
                            if (rpCurSubjectString.split("#").length >= 2) {
                                same = rpCurSubjectString.split("#")[1]
                                        .equals(gtCurSubjectString.split("#")[1]);
                            } else {
                                same = false;
                            }
                        }
                        if (same) {
                            same = rpCurObjectString.equals(gtCurObjectString);
                        }
                        if (same) {
                            found = true;
                            tp++;
                            break;
                        }
                    }

                    if (!found) {
                        fn++;
                    }
//                                    if (rpModel.contains(disposableStatement)) {
//                                            // is a true positive						
//                                            tp++;
//                                    } else { // statement doesn't exist in the recovered prov
//                                            // is a false negative
//                                            fn++;
//                                    }
                }
            }

            // all the statements in recovered prov
            StmtIterator rpIter = rpModel.listStatements();

            // iterate through the recovered statements
            while (rpIter.hasNext()) {
                Statement s = rpIter.next();
//				RDFNode o = s.getObject();
//				RDFNode predicate = s.getPredicate();
//				// if the statement doesn't exist in the ground truth
//				if (o != null
//						&& !o.toString().equals(
//								"http://www.w3.org/ns/prov#Entity")
//						&& !predicate.toString()
//								.equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                if (!gtModel.contains(s)) {
                    // this is a false positive
                    fp++;
                }
//				}
            }

            // specific P&R calculation values
            System.out.println("tp: " + tp);
            System.out.println("fp: " + fp);
            System.out.println("fn: " + fn);

            // calculate the precision
            precision = (tp / (tp + fp)) * 100;
            // calculate the recall
            recall = (tp / (tp + fn)) * 100;

            // construct the overall result
            result = "Prec=" + (new DecimalFormat("#.##").format(precision))
                    + "%;Rec=" + (new DecimalFormat("#.##").format(recall))
                    + "%";
        }
        return result;
    }
}
