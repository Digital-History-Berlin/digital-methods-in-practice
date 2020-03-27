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

import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationException;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created on 27.12.2010 14:18:42 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractPreprocessing extends ClassConfig {

    protected boolean boolRemoveDiachritics = false;
    protected boolean boolMakeAllLowerCase = false;
    protected String LETTER_SHAVER_MAPPING_FILE_NAME = null;
    protected HashMap<String,String> objCharMapping = null;
    protected int countStats[] = null;

    public void init() throws ConfigurationException {
        super.config();

        if( !new File(LETTER_SHAVER_MAPPING_FILE_NAME).exists() ){
            throw new ConfigurationException( "File " +LETTER_SHAVER_MAPPING_FILE_NAME +
                    " not found. It was set by property LETTER_SHAVER_MAPPING_FILE_NAME");
        }

        // cf interface 'Constants' for pre processing stats
        countStats = new int[9];

        if (boolRemoveDiachritics) {
            loadLetterShavingMapping();
        }
    }

    private void loadLetterShavingMapping() throws ConfigurationException{
        objCharMapping = new HashMap<String,String>();
            try {
                BufferedReader objReader = new BufferedReader(new FileReader(LETTER_SHAVER_MAPPING_FILE_NAME));

                String strLine = null;

                while ((strLine = objReader.readLine()) != null) {
                    if (!(strLine.startsWith("###") && strLine.endsWith("###"))) {
                        String strSplit[] = strLine.split("\t");

                        String strKey = "";
                        String strValue = "";

                        if (strSplit.length == 2) {
                            strKey = strSplit[0];
                            strValue = strSplit[1];
                        } else {
                            if (strSplit.length == 1) {
                                strKey = strSplit[0].trim();
                            } else {
                                throw new ConfigurationException("Wrong column format in line \"" + strLine + "\"");
                            }
                        }

                        objCharMapping.put(strKey, strValue);
                    }
                }

                objReader.close();

            } catch (Exception e) {
                throw new ConfigurationException(e);
            }
    }

    protected String processLowerCases(String strWord) {
        return strWord.toLowerCase();
    }

    protected String processRemoveDiachritics(String strWord) {

        int intWordLength = strWord.length();
        StringBuilder objBuffer = new StringBuilder();
        
        for( int i=0; i<intWordLength; i++ ){
            String strKey = strWord.substring(i, i+1);
            String strReplaceValue = objCharMapping.get(strKey);

            if( strReplaceValue == null ){
                strReplaceValue = strKey;
            }

            objBuffer.append(strReplaceValue);
        }

        String strProcessedWord = objBuffer.toString();
        
        return strProcessedWord;
    }

    /*
     * Copied from Medusa's AbstractWordTokenizer
     */
    protected ArrayList<int[]> getWhitespacePositions(String strCurLine) {
        ArrayList<int[]> objTokens = new ArrayList<int[]>(32);

        int intEndPos = strCurLine.length();
        for (int i = 0; i < intEndPos; i++) {
            if (Character.isWhitespace(strCurLine.charAt(i))) {
                int intPos[] = new int[1];
                intPos[0] = i;
                objTokens.add(intPos);
            }
        }

        int intPos[] = new int[1];
        intPos[0] = strCurLine.length();
        objTokens.add(intPos);
        return objTokens;
    }

    public int[] getPreprocessingStats(){
        return countStats;
    }
}
