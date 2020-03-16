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
import java.text.DecimalFormat;

/**
 *
 * @author mbuechler
 */
public class WordClassRemoverMain {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strFile = "/home/mbuechler/Dissertation/MinutiaeTest/bibel";
        
       BufferedReader objReader = new BufferedReader( new FileReader(strFile) );
       BufferedWriter objWriter = new BufferedWriter( new FileWriter( strFile + ".csv" ) );
       BufferedWriter objWriter2 = new BufferedWriter( new FileWriter( strFile + ".tagged.csv" ) );
       
       String strLine = null;
       int counter = 1;
       String label = "b1";
       DecimalFormat df = new DecimalFormat("000");
       while( (strLine=objReader.readLine()) != null ){
           String strSplit[] = strLine.split( " " );
           String strOutLine = "";
           
           for( int i=0; i<strSplit.length; i++ ){
               String strSplit2[] = strSplit[i].split("\\|");
               strOutLine += " " + strSplit2[0].trim();
           }
           
           objWriter.write( label + "." + df.format(counter) + "\t" + strOutLine.trim() + "\t" + strOutLine.trim() + "\n" );
           objWriter2.write(label + "." + df.format(counter) + "\t" + strLine.trim() + "\n" );
           counter++;
       }
       
       objWriter.flush();
       objWriter.close();
       
       objWriter2.flush();
       objWriter2.close();
       objReader.close();
    }
}
