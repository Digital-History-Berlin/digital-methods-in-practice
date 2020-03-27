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


package eu.etrap.tracer.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 *
 * @author mbuechler
 */
public class MovingAverageSegmentizerMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            String strCorpusInFile = args[0];
            //String strCorpusInFile = "/home/mbuechler/Development/Medusa/data/corpora/example/example2.txt";
            System.out.println( "strCorpusInFile\t" + strCorpusInFile);

            int intWindowSize = Integer.parseInt( args[1] );
            //int intWindowSize = 10;
            int intShift= 10000000;
            
            String strOutFile = strCorpusInFile.replace(".txt", "-W" + intWindowSize + ".txt");
            System.out.println( "strCorpusOutFile\t" + strOutFile );


            BufferedReader objReader = new BufferedReader( new FileReader( strCorpusInFile ) );
            BufferedWriter objWriter = new BufferedWriter( new FileWriter( strOutFile ) );

            String strLine = null;
            int k=1;
            while( (strLine=objReader.readLine()) != null ){
                String strSplit[] = strLine.split("\t");
                Integer intRUID = Integer.parseInt(strSplit[0]);
                int intWorkID = intRUID / 1000000;
                int intRUIDOnly = intRUID % 1000000;
                
                String strSource = strSplit[3];

                String aryWords[] = strSplit[1].split( " " );

                if( aryWords.length <= intWindowSize){
                    objWriter.write( (intWorkID*intShift+k) + "\t" + strSplit[1] + "\tNULL\t" + intRUID + "=" +strSource + "\n" );
                    k++;
                }else{
                    
                for( int i=0; i<aryWords.length-intWindowSize+1; i++ ){
                    String strReuseUnit = "";

                    for( int j=i; j<i+intWindowSize; j++ ){
                        strReuseUnit += aryWords[j] + " ";
                    }

                    strReuseUnit = strReuseUnit.trim();
                    objWriter.write( (intWorkID*intShift+k) + "\t" + strReuseUnit + "\tNULL\t" + intRUID + "=" +strSource + "\n" );
                    k++;
                }
                }
            }
            objWriter.flush();
            objWriter.close();
            objReader.close();

        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}
