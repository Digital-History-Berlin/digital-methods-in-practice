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



package eu.etrap.tracer.phd.cleaning.evaluation.bible;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author mbuechler
 */
public class FMeasureMaxDetectorMain {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strFile1 = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/data/KJV_WBS-01-02-01-01-02-TriGramShinglingTrainingImpl-lem=true_syn=true_ssim=false.csv";
        strFile1 = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/data/BBE_YLT-01-02-01-01-02-TriGramShinglingTrainingImpl-lem=true_syn=true_ssim=false.csv";

        String strFolderName = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/data/";

        File objFiles[] = new File(strFolderName).listFiles();

        DecimalFormat df = new DecimalFormat("0.00");
        for (int i = 0; i < objFiles.length; i++) {
            String fileName[] = objFiles[i].getName().split("-");
            String strBibleVersions[] = fileName[0].split("_");

            if (!strBibleVersions[0].trim().equals(strBibleVersions[1].trim())
                    && objFiles[i].getName().contains("KJV_")) {


                BufferedReader objReader = new BufferedReader(new FileReader(objFiles[i]));
                String strLine = null;

                double fmax = 0.0;
                String t = "";

                while ((strLine = objReader.readLine()) != null) {
                    String strSplit[] = strLine.split("\t");
                    double f = Double.parseDouble(strSplit[9]);

                    if (f >= fmax) {
                        fmax = f;
                        t = strSplit[0];
                    }
                }

                if (objFiles[i].getName().contains("Word") && objFiles[i].getName().contains("lem=true_syn=true_ssim=false") ) {
                    System.out.println( objFiles[i] + 
                    "\t(" + df.format(Double.parseDouble(t)) + ", " + df.format(fmax) + ")" 
                );
                }
                
                objReader.close();
            }
        }
    }
}
