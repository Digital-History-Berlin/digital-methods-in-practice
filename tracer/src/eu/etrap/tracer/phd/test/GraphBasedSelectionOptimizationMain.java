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


package eu.etrap.tracer.phd.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author mbuechler
 */
public class GraphBasedSelectionOptimizationMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String strInFile = "/home/mbuechler/Development/Traces/data/corpora/example/TRACER_DATA/01:02-WLP:lem=false_syn=false_ssim=false_redwo=false:ngram=4:LLR=true_toLC=false_rDia=false_w2wl=false:wlt=0/01-02-01-01-01-BiGramShinglingTrainingImpl";
            strInFile += "/test/example.train.graph.wgd.sorted";
            HashSet<String> objOrigData = new HashSet<String>();

            BufferedReader objReader = new BufferedReader(new FileReader(strInFile));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objOrigData.add(strSplit[0].trim());
            }

            objReader.close();
            System.out.println("1 objOrigData.size()=" + objOrigData.size());

            strInFile = "/home/mbuechler/Development/Traces/data/corpora/example/TRACER_DATA/01:02-WLP:lem=false_syn=false_ssim=false_redwo=false:ngram=4:LLR=true_toLC=false_rDia=false_w2wl=false:wlt=0/01-02-01-01-01-BiGramShinglingTrainingImpl";
            strInFile += "/example.train.graph.wgd.sorted";

            objReader = new BufferedReader(new FileReader(strInFile));


            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                if (objOrigData.contains(strSplit[0])) {
                    objOrigData.remove(strSplit[0]);
                } else {
                    System.out.println("ERROR for " + strLine + "\t" + strSplit[0]);
                }
            }

            objReader.close();
            System.out.println("2 objOrigData.size()=" + objOrigData.size());

            Iterator<String> objIter = objOrigData.iterator();
            while (objIter.hasNext()) {
                String strData = objIter.next();
                String strSplit[] = strData.split("\t");
                int intWordID1 = Integer.parseInt(strSplit[0]);
                int intWordID2 = Integer.parseInt(strSplit[1]);

                if (intWordID2 < intWordID1) {
                    System.out.println("ERROR Single data=" + strData);
                }
            }

        } catch (Exception e) {
        }
    }
}
