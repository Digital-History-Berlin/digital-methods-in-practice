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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author mbuechler
 */
public class NGramMain {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strTrainFile = "/home/mbuechler/Dissertation/MinutiaeTest/Ngrams/Idioms/TRACER_DATA/01:02-WLP:lem=true_syn=true_ssim=false_redwo=false:ngram=5:LLR=true_toLC=true_rDia=true_w2wl=false:wlt=5/01-02-01-01-03-QuatroGramShinglingTrainingImpl/BIBEL.train";
        
        BufferedReader objReader = new BufferedReader( new FileReader( strTrainFile + ".wordfreq" ) );
        HashMap<Integer, Integer> objFeatFreqs = new HashMap<Integer, Integer>();
        
        String strLine = null;
        while( (strLine=objReader.readLine()) != null ){
            //System.out.println( strLine );
            String strSplit[] = strLine.split("\t");
            objFeatFreqs.put( Integer.parseInt(strSplit[0]), Integer.parseInt(strSplit[1]) );
        }
        
        objReader.close();
        
        objReader = new BufferedReader( new FileReader( strTrainFile.replace("train", "fmap") ) );
        
        strLine = null;
        while( (strLine=objReader.readLine()) != null ){
            
            if( !strLine.contains("115") ){
                String strSplit[] = strLine.split("\t");
            System.out.println( strLine + "\t" + objFeatFreqs.get(Integer.parseInt(strSplit[0])) );
            }
        }
        
        objReader.close();
    }
}
