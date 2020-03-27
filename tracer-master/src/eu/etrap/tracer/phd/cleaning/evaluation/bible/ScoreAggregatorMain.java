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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author mbuechler
 */
public class ScoreAggregatorMain {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws IOException {
        long aryData[][] = new long[101][2];

        String strFileName = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/MotivationCorrelationRundTRC/ScoreDist.trigram.out";
        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));

        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            int index = (int) Math.round(Double.parseDouble(strSplit[0]) * 100);
            aryData[index][0] = Long.parseLong(strSplit[1]);
            aryData[index][1] = Long.parseLong(strSplit[2]);
        }

        objReader.close();
        
        for( int i=aryData.length-2; i>=0; i--  ){
            aryData[i][0]= aryData[i][0] + aryData[i+1][0];
            aryData[i][1]= aryData[i][1] + aryData[i+1][1];
        }
        
        BufferedWriter objWriter = new BufferedWriter( new FileWriter( strFileName + ".cum" ) );
        
        for( int i=0; i< aryData.length; i++ ){
            objWriter.write( ((double)i/100.0) + "\t" + aryData[i][0] + "\t" + aryData[i][1] + "\n" );
        }
        
        objWriter.flush();
        objWriter.close();
    }
}
