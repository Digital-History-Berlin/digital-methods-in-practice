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

/**
 *
 * @author mbuechler
 */
public class FeatureIDCheckerMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String strFileName = "/home/mbuechler/Development/Traces/data/corpora/example/TRACER_DATA/01:02-WLP:lem=false_syn=false_ssim=false_redwo=false:ngram=4:LLR=true_toLC=false_rDia=false_w2wl=false:wlt=0/01-02-01-01-01-BiGramShinglingTrainingImpl/example.train.wordfreq.desc.sorted";
            BufferedReader objReader = new BufferedReader(new FileReader(strFileName));

            String strLine = objReader.readLine();

            int intFeatIDOld = Integer.parseInt(strLine.split("\t")[0].trim());
            int FeatIDNew = 0;
            while ((strLine = objReader.readLine()) != null) {
                FeatIDNew = Integer.parseInt(strLine.split("\t")[0].trim());

                if (FeatIDNew - intFeatIDOld != 1) {
                    System.out.println("ERROR between IDs " + intFeatIDOld
                            + " and " + FeatIDNew);
                }

                intFeatIDOld = FeatIDNew;

            }

            objReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
