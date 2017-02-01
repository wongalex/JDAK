/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package provtool;

import fileProcessor.FileProcessor;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import provCalc.ProvCalculation;
import provComp.ProvenanceComparator;
import provRec.Construct;

/**
 *
 * @author jwoodrin
 */
public class MainRoutine {

    public void Run(int type) {

        switch (type) {
            case 1:
                Dset1();
                break;
            case 2:
                GenDataPreProc();
                break;
            case 3:
                GenDataPostProc();
                break;
            default:
                break;
        }
    }

    public void Dset1() {
        try {
            // run jasons module
            FileProcessor fileProc = new FileProcessor();
            fileProc.processStopWords();
            BagAdt[] bags = null;

            bags = fileProc.runDataSet1();

            // run alex code
            ProvCalculation provCalc = new ProvCalculation();
            List<List<BagAdt>> bgs = provCalc.create(bags);

            // run kriti code
            Construct construct = new Construct(true);
            construct.construct(bgs, "recovered.ttl");

            // run del
            String groundTruth = "human_groundtruth.ttl";
            //String groundTruth = "data\\data_set_1\\human-generated-dev\\human_groundtruth.ttl";
            ProvenanceComparator provComp = new ProvenanceComparator("recovered.ttl", groundTruth);
            String output = provComp.compare();
            System.out.println(output);
        } catch (Exception e) {
        }
    }

    public void GenDataPreProc() {
        FileProcessor fileProc = new FileProcessor();
        fileProc.processStopWords();
        fileProc.preProcess();
    }

    public void GenDataPostProc() {
        // get a fileprocessor
        FileProcessor fileProc = new FileProcessor();
        // call postprocess
        BagAdt[] bags = fileProc.postProcess();
        // call calculate (generic)
        ProvCalculation provCalc = new ProvCalculation();
        List<List<BagAdt>> bgs = provCalc.createGeneric(bags);
        // call construct (generic)
        Construct construct = new Construct(false);
        construct.constructGeneric(bgs, "recovered.ttl");
        // call compare
        String groundTruth = "groundtruth.ttl";
        ProvenanceComparator provComp = new ProvenanceComparator("recovered.ttl", groundTruth);
        String output = provComp.compare();
        System.out.println(output);
    }
    /*
     public void Dset2() {
     try{
     // run jasons module
     FileProcessor fileProc = new FileProcessor();
     fileProc.processStopWords();
     BagAdt[] bags = fileProc.runGenericDataSet();
     // run alex code
     ProvCalculation provCalc = new ProvCalculation();
     List<List<BagAdt>> bgs = provCalc.create(bags);
     // run kriti code
     Construct construct = new Construct(false);
     construct.construct(bgs, "recovered.ttl");
     // run del
     String groundTruth = "groundtruth.ttl";
     //String groundTruth = ".\\data\\data_set_2\\machine-generated-dev\\groundtruth.ttl";
     ProvenanceComparator provComp = new ProvenanceComparator("recovered.ttl", groundTruth);
     String output = provComp.compare();
     System.out.println(output);
   
     }catch(Exception e){}
     }
     */
}
