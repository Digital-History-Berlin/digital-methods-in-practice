/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.etrap.tracer.utils;

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
public class IDStripperMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strCorpusFile = args[0];
        
        BufferedReader objReader = new BufferedReader( new FileReader(strCorpusFile) );
        BufferedWriter objWriter = new BufferedWriter( new FileWriter( strCorpusFile + ".stripped" ) );
        
        String strLine = null;
        while((strLine=objReader.readLine()) != null ){
            String strSplit[] = strLine.split("\t");
            String strMetaDataField = strSplit[3].trim();
            String strWork = strMetaDataField.split("=")[1].trim();
            objWriter.write( strSplit[0] + "\t" + strSplit[1] + "\t" + 
                    strSplit[2] + "\t" + strWork + "\n" );
        }
        
        objReader.close();
        objWriter.flush();
        objWriter.close();
    }    
}
