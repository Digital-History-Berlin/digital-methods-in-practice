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
public class DefaultOutputterMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        String strCorpus = args[0];
        String strScoreFile = args[1];

        HashMap<Integer, String> objID2SentenceMapping = new HashMap<Integer, String>();
        BufferedReader objReaderCorpus = new BufferedReader(new FileReader(strCorpus));
        String strCorpusLine = null;

        while ((strCorpusLine = objReaderCorpus.readLine()) != null) {
            String strSplit[] = strCorpusLine.split("\t");
            Integer objID = new Integer(strSplit[0].trim());
            objID2SentenceMapping.put(objID, strCorpusLine.trim());
        }

        objReaderCorpus.close();

        BufferedReader objReader = new BufferedReader(new FileReader(strScoreFile));
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strScoreFile + ".expanded"));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");

            if (!strLine.trim().equals("")) {
                Integer objID1 = new Integer(strSplit[0]);
                Integer objID2 = new Integer(strSplit[1]);

                String strLine1 = objID2SentenceMapping.get(objID1);
                String strLine2 = objID2SentenceMapping.get(objID2);

                String strSource1 = strLine1.split("\t")[3].trim();
                String strSource2 = strLine2.split("\t")[3].trim();
                
                boolean isSelfReuse = false;
                
                if ( (strSource1 != null) && (strSource2 != null) && 
                        strSource1.trim().equals(strSource2.trim()) ){
                    isSelfReuse = true;
                }

                objWriter.write(strLine + "\t" + strLine1.split("\t")[1]
                        + "\t" + strLine2.split("\t")[1] + "\t" + isSelfReuse + "\n");
            }
        }

        objReader.close();

        objWriter.flush();

        objWriter.close();
    }

}
