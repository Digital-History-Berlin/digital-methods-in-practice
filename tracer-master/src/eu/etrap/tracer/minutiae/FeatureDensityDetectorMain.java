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
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author mbuechler
 */
public class FeatureDensityDetectorMain {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strInFile = "/home/mbuechler/Dissertation/MinutiaeTest/2013-01-07-CSV/BIBEL.txt";

        BufferedReader objReader = new BufferedReader(new FileReader(strInFile));
        System.out.println("Processing file " + strInFile);
        HashMap<String, Integer> objData = new HashMap<String, Integer>();
        HashMap<String, Integer> objDataAll = new HashMap<String, Integer>();

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            //System.out.println( strLine );
            String strSplit[] = strLine.split("\t");
            String user = strSplit[3].trim().split(" ")[1];
            String line = strSplit[1].trim();

            String strLineSplit[] = line.split(" ");

            int freq = 0;
            if (objDataAll.containsKey(user)) {
                freq = objDataAll.get(user);
            }
            
            freq += strLineSplit.length;
            objDataAll.put( user, freq);
            
            int selected = 0;
            for( int i=0; i<strLineSplit.length; i++ ){
                if( !strLineSplit[i].trim().equals( "XXX|X" ) ){
                    selected++;
                }
            }
            
            freq = 0;
            if (objData.containsKey(user)) {
                freq = objData.get(user);
            }
            
            freq += selected;
            
            objData.put( user, freq);
            
        }
        objReader.close();
        
        
        Iterator<String> objIter = new TreeSet( objDataAll.keySet()).iterator();
        
        int selectedAll = 0;
        int allAll = 0;
        
        BufferedWriter objWriter = new BufferedWriter( new FileWriter(strInFile + ".fd") );
        
        while( objIter.hasNext() ){
            String user = objIter.next();
            
            int selected = objData.get(user);
            int all = objDataAll.get(user);
            
            selectedAll += selected;
            allAll += all;
            
            double dblFD = (double)selected/(double)all;
            objWriter.write( user + "\t" + selected + "\t" + all + "\t" + dblFD + "\n" );
        }
        objWriter.flush();
        objWriter.close();
        
        System.out.println( "FD=" + selectedAll + "\t" + allAll + "\t" + (double)selectedAll/(double)allAll );
    }
}
