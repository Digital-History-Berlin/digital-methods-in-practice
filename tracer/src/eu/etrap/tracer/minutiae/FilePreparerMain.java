/*
 * The Tracer project is a result of Marco Büchler's PhD in computer science about
 * 'Computational Aspects of the Historical Text Re-use and Knowledge Transfer'.
 * Tracer includes a six level architecture consisting of
 *      - Preprocessing (eu.etrap.tracer.preprocessing)
 *      - Training (eu.etrap.tracer.featuring)
 *      - Selection (eu.etrap.tracer.selection)
 *      - Linking (eu.etrap.tracer.linking)
 *      - Scoring (eu.etrap.tracer.scoring)
 *      - Postprocessing (eu.etrap.tracer.postprocessing).
 *
 * Tracer is provided both by open source and open access for non-commercial use.
 *
 * If you have any questions, please, send me an e-mail to mbuechler@etrap.eu.
 *
 * Marco Büchler
 * eTRAP Research Group 
 * Institute for Computer Science
 * Georg-August-Universiy Göttingen
 * Papendiek 16
 * 37073 Göttingen, Germany
 * web: http://www.etrap.eu
 *
 * This project has begun at the Leipzig e-Humanities Research Group at Leipzig
 * University, Germany is now continued as part of the eTRAP research group 
 * (http://www.etrap.eu).
 *
 */



package eu.etrap.tracer.minutiae;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author mbuechler
 */
public class FilePreparerMain {

    private static HashMap<String, String> objData = new HashMap<String, String>();

    /**
     * @param args the command line arguments
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws IOException {

        String strDataDir = "/home/mbuechler/Dissertation/MinutiaeTest/2013-01-07-CSV/";
        String version = "bibel";
        //version = "mittelalter";

        load("/home/mbuechler/Dissertation/MinutiaeTest/" + version + ".tagged.csv");
        System.out.println("Size=" + objData.size());

        File objFiles[] = new File(strDataDir).listFiles();
        Arrays.sort(objFiles);

        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strDataDir + version.toUpperCase() + ".txt"));
        BufferedWriter objWriter2 = new BufferedWriter(new FileWriter(strDataDir + version.toUpperCase() + ".removed.txt"));

        int counter = 0;
        int line = 0;
        for (int i = 0; i < objFiles.length; i++) {
            if (objFiles[i].getName().contains(version)) {
                counter++;

                String strLine = null;
                BufferedReader objReader = new BufferedReader(new FileReader(objFiles[i]));
                System.out.println(counter + "\tProcessing file " + objFiles[i]);

                String user = objFiles[i].getName().replace(version + "_", "").replace(".csv", "");

                while ((strLine = objReader.readLine()) != null) {
                    line++;
                    String strSplit[] = strLine.split("\t");

                    String strUnmodified = objData.get(strSplit[0].replace("\"", "").trim());

                    String strUnmodSplit[] = strUnmodified.trim().split(" ");
                    String strModSplit[] = strSplit[2].replace("\"", "").trim().split(" ");

                    int begin = 0;
                    HashSet<Integer> objIndex = new HashSet<Integer>();

                    for (int a = 0; a < strUnmodSplit.length; a++) {

                        for (int b = begin; b < strModSplit.length; b++) {
                            String strWords[] = strUnmodSplit[a].split("\\|");
                            
                            //System.out.println( strModSplit[b] + " vs. " + strUnmodSplit[a] + ": " + strModSplit[b].trim().equals(strWords[0].trim()));
                            
                            if (strModSplit[b].equals(strWords[0])) {
                                /*System.out.println( strModSplit[b] + " \t" + strWords[0] + "\t"
                                + strUnmodSplit[a]);*/
                                objIndex.add(a);
                                begin = b;
                                break;
                            }
                        }
                    }

                    //System.out.println( objIndex.size()  + "\t"+ objIndex );
                    String strOutString = "";
                    String strOutRemovedString = "";

                    for (int a = 0; a < strUnmodSplit.length; a++) {
                        if (objIndex.contains(a)) {
                            strOutString += strUnmodSplit[a];
                        } else {
                            strOutString += "XXX|X";
                            strOutRemovedString += strUnmodSplit[a] + " ";
                        }
                        strOutString += " ";
                    }

                    objWriter.write(line + "\t" + strOutString.trim() + "\tNULL\t" + strSplit[0].replace("\"", "").trim() + " " + user + "\n");
                    objWriter2.write( line + "\t" + strOutRemovedString.trim() + "\tNULL\t" + strSplit[0].replace("\"", "").trim() + " " + user + "\n" );
                }

                objReader.close();
            }
        }

        objWriter.flush();
        objWriter.close();
        objWriter2.flush();
        objWriter2.close();
    }

    private static void load(String strFile) throws FileNotFoundException, IOException {

        String strLine = null;
        BufferedReader objReader = new BufferedReader(new FileReader(strFile));


        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objData.put(strSplit[0].trim(), strSplit[1].trim());
        }

        objReader.close();
    }
}
