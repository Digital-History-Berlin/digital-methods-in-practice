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


package eu.etrap.tracer;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 *
 * @author mbuechler
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String strFile = "/tmp/PerseusWorkWise.txt";
        
        try{
            BufferedReader objReader = new BufferedReader( new FileReader(strFile) );
            int sum = 0;
            String strLine = null;
            while( (strLine=objReader.readLine()) != null ){
                String strSplit[] = strLine.split("\t");
                System.out.println( strSplit[0] + "\t" + strSplit[1].trim().split(" ").length );
                sum += strSplit[1].trim().split(" ").length;
            }
            System.out.println("sum=" + sum);
            objReader.close();
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}
