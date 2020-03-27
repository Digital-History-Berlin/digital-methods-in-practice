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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;




/**
 *
 * @author mbuechler
 */
public class DiffAnalMain {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strFile = "/home/mbuechler/Dissertation/MinutiaeTest/2013-01-07-CSV/BIBEL.removed.txt";

        BufferedReader objReader = new BufferedReader(new FileReader(strFile));
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFile + ".dist"));
        String strLine = null;

        HashMap<String, Integer> objData = new HashMap<String, Integer>();

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");

            String strWord = strSplit[1];

            String strWords[] = strWord.split(" ");

            for (int i = 0; i < strWords.length; i++) {
                if (!strWords[i].trim().equals("")) {

                    int freq = 0;
                    if (objData.containsKey(strWords[i].trim())) {
                        freq = objData.get(strWords[i].trim());
                    }

                    freq++;

                    objData.put(strWords[i].trim(), freq);

                }
            }
        }


        objReader.close();

        Iterator<String> objIter = new TreeSet(objData.keySet()).iterator();
        while (objIter.hasNext()) {
            String key = objIter.next();
            if (key.trim().endsWith("v") ) {
                System.out.println(key + "\t" + objData.get(key));
            }

            objWriter.write(key + "\t" + objData.get(key) + "\n");
        }

        objWriter.flush();
        objWriter.close();
    }
}
