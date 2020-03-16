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


package eu.etrap.tracer.phd.cleaning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author mbuechler
 */
public class PrepareDBDumpLemmatisationMain {

    /**
     * This class cleans the database dump having several base forms to one word
     * form. See e. g. http://www.perseus.tufts.edu/hopper/morph?l=%E1%BC%88%CE%BC%E1%BD%B0&amp;la=greek
     *
     * The result is that in the first column every word form occurrs only once.
     * In order to do that a simple arg max f(x) approach is chosen that selects
     * the base form that has the highest frequency given by all linked word forms.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            String strInFile = "/home/mbuechler/PHD/Daten/Lemmatisation/TLG-LEMMA.txt.tagged";
            String strOutFile = strInFile + ".cleaned";


            // read mapping word form --> base forms from file
            BufferedReader objReader = new BufferedReader( new FileReader( strInFile ) );
            BufferedWriter objWriter = new BufferedWriter( new FileWriter( strOutFile ) );

            String strLine = "";
            HashMap<String, HashSet<String>> objMapping = new HashMap<String, HashSet<String>>();
            HashMap<String, Long> objBaseformFreqs = new HashMap<String,Long>();
            
            while( (strLine=objReader.readLine()) != null ){
                String strSplit[] = strLine.split( "\t" );
                String strWordForm = strSplit[0].trim();
                String strBaseForm = strSplit[3].trim();

                HashSet<String> objBaseForms = objMapping.get( strWordForm );

                if( objBaseForms == null ){
                    objBaseForms = new HashSet<String>();
                }

                objBaseForms.add(strBaseForm);

                objMapping.put( strWordForm, objBaseForms);
            }

            objReader.close();

            System.out.println( "Size=" + objMapping.size() );


            // read word form freqs
            strInFile = "/home/mbuechler/PHD/Daten/Encoding/TLG-WORDS.txt";
            objReader = new BufferedReader( new FileReader( strInFile ) );
             
            strLine = "";
            HashMap<String, Long> objWordformFrequency = new HashMap<String, Long>();

            while( (strLine=objReader.readLine()) != null ){
                String strSplit[] = strLine.split( "\t" );
                String strWordForm = strSplit[1].trim();
                Long objLongFreq = new Long( strSplit[2].trim() );

                objWordformFrequency.put( strWordForm, objLongFreq);
            }

            objReader.close();

            System.out.println( "Size=" + objWordformFrequency.size() );

            // compute virtual base form frequencies
            Iterator<String> objIter = objMapping.keySet().iterator();

            while( objIter.hasNext() ){
                String strWordForm = objIter.next();
                Long objWordFreq = objWordformFrequency.get( strWordForm );
                HashSet<String> objBaseforms = objMapping.get( strWordForm );

                Iterator<String> objBaseformIter = objBaseforms.iterator();

                while( objBaseformIter.hasNext() ){
                    String strBaseform = objBaseformIter.next();

                    long longFreqValue = 0;
                    if (objBaseformFreqs.containsKey(strBaseform)){
                        longFreqValue = objBaseformFreqs.get(strBaseform);
                    }

                    longFreqValue += objWordFreq;
                    objBaseformFreqs.put(strBaseform, longFreqValue);
                }
            }

            System.out.println( "Size=" + objBaseformFreqs.size() );

            // iterate and write final file
            Iterator<String> objWordFormIter = objMapping.keySet().iterator();

            while( objWordFormIter.hasNext() ){
                String strWordForm = objWordFormIter.next();
                HashSet<String> objBaseforms = objMapping.get( strWordForm );
                
                long longMaxBaseformFreq = 0;
                String strMostProbableBaseform = null;

                Iterator<String> objBaseformIter = objBaseforms.iterator();
                while( objBaseformIter.hasNext() ){
                    String strBaseForm = objBaseformIter.next();
                    long longBaseFormFreq = objBaseformFreqs.get(strBaseForm);

                    if( longBaseFormFreq > longMaxBaseformFreq ){
                        longMaxBaseformFreq = longBaseFormFreq;
                        strMostProbableBaseform = strBaseForm;
                    }
                }

                objWriter.write( strWordForm + "\t" + strMostProbableBaseform + "\t" + longMaxBaseformFreq + "\n" );
            }

            objWriter.flush();
            objWriter.close();
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

}
