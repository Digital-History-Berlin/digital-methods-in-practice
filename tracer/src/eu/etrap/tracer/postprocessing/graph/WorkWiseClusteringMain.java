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


package eu.etrap.tracer.postprocessing.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;

/**
 *
 * @author mbuechler
 */
public class WorkWiseClusteringMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String strFile = args[0];
            BufferedReader objReader = new BufferedReader(new FileReader(strFile));

            String strLine = null;

            int size = 1000;
            int aryOverlap[][] = new int[size][size];
            double aryScore[][] = new double[size][size];

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int key1 = Integer.parseInt(strSplit[0]) / 1000000;
                int key2 = Integer.parseInt(strSplit[1]) / 1000000;
                int overlap = (int) Math.round(Double.parseDouble(strSplit[2]));
                double score = Double.parseDouble(strSplit[3]) / 1000;

                aryOverlap[key1][key2] += overlap;
                aryScore[key1][key2] += score;
            }

            objReader.close();
            DecimalFormat df = new DecimalFormat( "0");
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                     if( aryOverlap[i][j] != 0 && (i!=j)){
                         System.out.println( i + "\t" + j + "\t" +
                                 aryOverlap[i][j] + "\t" +
                                 df.format( aryScore[i][j]) );
                     }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}