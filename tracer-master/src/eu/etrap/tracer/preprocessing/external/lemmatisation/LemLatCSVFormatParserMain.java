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
package eu.etrap.tracer.preprocessing.external.lemmatisation;

import eu.etrap.medusa.config.ConfigurationContainer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created on 26.07.2017 14:18:42 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class LemLatCSVFormatParserMain {

    protected static HashMap<String,String> objPoSMapping = null;
    protected static TreeSet<String> objInflBaseMapping = null;
    protected static TreeSet<String> objBaseForms = null;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strDataDirectory = "data/corpora/orosius/";
        String strMappingFile = "data/pos-mappings/lat-lemlat-mapping.csv";
        String strFileSuffix = ".csv";      
        
        strDataDirectory = args[0];
        strMappingFile = args[1];
        strFileSuffix = args[2];
        
        objPoSMapping = new HashMap<String, String>();
        objInflBaseMapping = new TreeSet<String>();
        objBaseForms = new TreeSet<String>();
        
        readMapping( strMappingFile );
        
        File obDataDirectory = new File(strDataDirectory);
        File objFiles[] = obDataDirectory.listFiles();
        Arrays.sort(objFiles);

        ConfigurationContainer.println("\t" + objFiles.length + " file(s) to process in folder " + strDataDirectory + " ...\n");
        
        
        for(int i=0; i<objFiles.length; i++){
            
            if(objFiles[i].toString().endsWith(strFileSuffix)){
            
            System.out.println( "Processing file name: " + objFiles[i] );
            BufferedReader objReader = new BufferedReader(new FileReader( objFiles[i] ));
            
            String strLine = null;
            while( (strLine=objReader.readLine()) != null ){
                //System.out.println(strLine);
                String strSplit[] = strLine.split(",");
                String strInflectedWord = strSplit[0].trim();
                String strBaseForm = strSplit[2].trim();
                String strLemLatPoSTag = getPoSTagFromLemLatMorphCode( strSplit[6].trim() );
                String strTracerPoSTag = objPoSMapping.get(strLemLatPoSTag);
                objInflBaseMapping.add( strInflectedWord + "\t" + strBaseForm.replaceAll("\"", "").replaceAll("'", "") + "\t" + strTracerPoSTag);
                objBaseForms.add(strBaseForm.replaceAll("\"", "").replaceAll("'", "") + "\t" + strTracerPoSTag);
            }
            
            objReader.close();
            }
        }
        
        writeTreeSetOnDisc(objInflBaseMapping, strDataDirectory + "lemma-mapping.list");
        writeTreeSetOnDisc(objBaseForms, strDataDirectory + "lemma.lemma-list");
    }
    
    protected static String getPoSTagFromLemLatMorphCode( String strMorphCode ){
        String strPoS = "";
        
        Iterator<String> objIter = objPoSMapping.keySet().iterator();
        
        while( objIter.hasNext() ){
            String strLemLatTag = objIter.next().trim();
            
            if( strMorphCode.startsWith(strLemLatTag) && strPoS.length() < strLemLatTag.length() ){
                strPoS = strLemLatTag;
            }
        }
        
        return strPoS;
    }
    
    protected static void readMapping( String strFileName ) throws FileNotFoundException, IOException{
    
        BufferedReader objReader = new BufferedReader(new FileReader( strFileName ) );
        String strLine = null;
        
        while( (strLine=objReader.readLine()) != null ){
            String strSplit[] = strLine.split("\t");
            objPoSMapping.put( strSplit[1].trim(), strSplit[0].trim());
        }
        
        objReader.close();
    }
    
    protected static String getPoSTag( String StrLemLatCode ){
    
           return null;
    }
    
    protected static void writeTreeSetOnDisc(TreeSet<String> objData, String strFileName) throws IOException{
        System.out.println("Writing file " + strFileName);
        
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFileName));
        
        Iterator<String> objIterator = objData.iterator();
        while( objIterator.hasNext() ){
            objWriter.write(objIterator.next().trim() + "\n");
        }
        
        objWriter.flush();
        objWriter.close();
    }
}
