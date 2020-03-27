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



package eu.etrap.tracer.postprocessing;

import bak.pcj.map.IntKeyIntMap;
import bak.pcj.map.IntKeyIntOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;


/**
 *MovingWindowResultsMergerMain
 * @author mbuechler
 */
public class MovingWindowResultsMergerMain {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strCorpusFileName = args[0];
        String strScoreFilename = args[1];
        int intWindowSize = Integer.parseInt(args[2]);
        
        BufferedReader objReader = new BufferedReader( new FileReader(strCorpusFileName) );
        
        String strLine = null;
        
        IntKeyIntMap objMap = new IntKeyIntOpenHashMap();
        
        while( (strLine=objReader.readLine() ) != null ){
            String strSplit[] = strLine.split( "\t" );
            
            int intRUID = Integer.parseInt( strSplit[3].split("=")[0] );
            
            objMap.put(Integer.parseInt( strSplit[0] ), intRUID);
        }
        
        objReader.close();
        
        
        objReader = new BufferedReader( new FileReader(strScoreFilename) );
        
        strLine = null;
        
        HashMap<String,double[]> objNormData = new HashMap<String, double[]>();
        
        while( (strLine=objReader.readLine() ) != null ){
            String strSplit[] = strLine.split( "\t" );
            
            int intID1 = Integer.parseInt( strSplit[0] );
            int intID2 = Integer.parseInt( strSplit[1] );
            
            String strKey = objMap.get(intID1) + "\t" + objMap.get(intID2);
            
            double aryValue[] = new double[2];
            
            if ( objNormData.containsKey(strKey) ){
                aryValue = objNormData.get(strKey);
            }
            
            double dblOverlap = Double.parseDouble(strSplit[2]);
            double dblScore = Double.parseDouble(strSplit[3]);
            
            aryValue[0] = Math.max(aryValue[0], dblOverlap);
            aryValue[1] = Math.max(aryValue[1], dblScore);
            
            objNormData.put( strKey, aryValue );
        }
        
        objReader.close();
        
        BufferedWriter objWriter = new BufferedWriter( new FileWriter( strScoreFilename.replace(".score", "-norm.score") ) );
        
        Iterator<String> objIter = objNormData.keySet().iterator();
        
        while( objIter.hasNext() ){
            String strKey = objIter.next();
            double aryValues[]  = objNormData.get( strKey );
            
            objWriter.write( strKey + "\t" + aryValues[0] + "\t" + aryValues[1] + "\n" );
        }
        
        
        objWriter.flush();
        objWriter.close();
    }
    
}
