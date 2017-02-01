/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package provtool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProvTool Project Purpose: Construct Provenance from challenge data sets.
 *
 * @author Team #2
 */
public class ProvTool {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Application Logic
        System.out.println("//******************");
        System.out.println("Welcome To ProvTool");
        System.out.println("//******************");
        MainRoutine mainRoutine = new MainRoutine();
        System.out.println("Please Make a processing selection");
        System.out.println("");
        System.out.println("1. Data Set 1: Human Generated");
        System.out.println("2. Generic Dataset: Preprocessing");
        System.out.println("3. Generic Dataset: Postprocessing");
        String s = null;
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

        try {
            s = bufferRead.readLine();
        } catch (IOException ex) {
            Logger.getLogger(ProvTool.class.getName()).log(Level.SEVERE, null, ex);
        }

        switch (s) {
            case "1":
                mainRoutine.Run(1);
                break;
            case "2":
                mainRoutine.Run(2);
                break;
            case "3":
                mainRoutine.Run(3);
                break;
            default:
                return;
        }
    }

}
