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
package eu.etrap.tracer.postprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created on 06.08.2017 10:52:29 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class SelfReuseRemoverMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        String strScoreFile = args[0];

        
        HashMap<Integer, String> objID2SentenceMapping = new HashMap<Integer,String>();
        BufferedReader objReaderCorpus = new BufferedReader(new FileReader(strScoreFile));
        

        
        BufferedReader objReader = new BufferedReader(new FileReader(strScoreFile));
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strScoreFile + ".selected"));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");

            if (!strLine.trim().equals("")) {
                int intRUID1 = Integer.parseInt(strSplit[0]);
                int intRUID2 = Integer.parseInt(strSplit[1]);

                if (intRUID1 != intRUID2) {
                    objWriter.write(strLine + "\n");
                }
            }
        }

        objReader.close();
        objReaderCorpus.close();

        objWriter.flush();

        objWriter.close();
    }

}
