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

/**
 *
 * @author mbuechler
 */
public class CleanerMain {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strInFile = "/home/mbuechler/Dissertation/MinutiaeTest/bibel.tagged.csv";

        BufferedReader objReader = new BufferedReader(new FileReader(strInFile));
        BufferedWriter objWriter = new BufferedWriter( new FileWriter(strInFile + ".tags") );
        
        String strLine = null;
        
        while( (strLine=objReader.readLine()) != null ){
            String tabs[] = strLine.split("\t");
            String words[] = tabs[1].trim().split(" ");
            
            String strOutString = "";
            for( int i=0; i<words.length; i++ ){
                String tags[] = words[i].trim().split( "\\|" );
                strOutString += tags[1] + " ";
            }
            objWriter.write( tabs[0] + "\t" + strOutString.trim() 
                    //+ "\t" + tabs[2] + "\t" + tabs[3] 
                    + "\n");
        }
        
        objReader.close();
        objWriter.flush();
        objWriter.close();
    }
}
