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
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import eu.etrap.tracer.Constants;

/**
 * Created on 07.12.2010 16:57:22 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class LetterLevelPreprocessingImpl extends AbstractPreprocessing  implements Preprocessing {
    // Entfernen der Leerzeichen
    // Tokenisierung auf n letter level

    boolean boolReplaceWhitespaces = false;
    int intNGramSize = 0;

    @Override
    public void init() throws ConfigurationException {
        super.init();

        if( intNGramSize == 0 ){
            throw new ConfigurationException( "Size of n-gram length is not set!" );
        }
    }

    public String execute(String strLine) {
        String strLetterManipulations = processLetterManipulation( strLine );
        String strResult = processLetterPreprocessing( strLetterManipulations );

        return strResult;
    }

    protected String processLetterManipulation(String strLine) {

    // the result string is build in objBuffer
        StringBuilder objBuffer = new StringBuilder();

        ArrayList<int[]> objWhitespaces = getWhitespacePositions(strLine);
        Iterator<int[]> objIter = objWhitespaces.iterator();
        int intStart = 0;
        int intEnd = 0;

        while (objIter.hasNext()) {
            intEnd = ((int[]) objIter.next())[0];

            if (intEnd - intStart > 0) {
                String strWord = strLine.substring(intStart, intEnd);

                // lowercase
                if (boolMakeAllLowerCase) {
                    strWord = processLowerCases( strWord );
                }

                // diachritics
                if (boolRemoveDiachritics) {
                    strWord = this.processRemoveDiachritics( strWord );
                }

                objBuffer.append(strWord.trim() + " " );
                intStart= intEnd;
            }
        }

        return objBuffer.toString();
    }

    protected String processLetterPreprocessing(String strLine) {
        StringBuilder objBuffer = new StringBuilder();

        if (boolReplaceWhitespaces) {
            strLine = removeWhitespaces(strLine);
        }

        for (int i = 0; i < strLine.length() - intNGramSize + 1; i++) {
            String strNGram = strLine.substring(i, i + intNGramSize);
            strNGram = escapeWhitespaces( strNGram );
            objBuffer.append(strNGram).append(" ");
        }

        return objBuffer.toString();
    }

    protected String escapeWhitespaces(String strLine) {
        String strRemovedLine = strLine;

        strRemovedLine = strRemovedLine.replace(" ", Constants.WHITSPACE_ESCAPE);

        return strRemovedLine;
    }

    protected String removeWhitespaces(String strLine) {
        String strRemovedLine = strLine;

        // detect all whitespaces in a line and collect them
        HashSet<String> objSet = new HashSet<String>();
        int intLineLength = strLine.length();

        for (int i = 0; i < intLineLength; i++) {
            if (Character.isWhitespace(strLine.charAt(i))) {
                objSet.add(Character.toString(strLine.charAt(i)));
            }
        }

        // and now remove all collected whitespaces
        Iterator<String> objIter = objSet.iterator();

        while (objIter.hasNext()) {
            String strCharacter = objIter.next();
            strRemovedLine = strRemovedLine.replace(strCharacter, "");
        }

        return strRemovedLine;
    }

    public String getFolderName() throws IOException{
        String stInputFileName = ConfigurationContainer.getSentenceFileName();
        String strDirectory = new File( stInputFileName ).getParentFile().toString();

        String strFolderName = strDirectory + "/TRACER_DATA/01:01-LLP:";

        // add parameters
        strFolderName += "nGram=" + this.intNGramSize + "_";
        strFolderName += "rWS=" + this.boolReplaceWhitespaces + "_";
        strFolderName += "toLC=" + this.boolMakeAllLowerCase+ "_";
        strFolderName += "rDia=" + this.boolRemoveDiachritics;

        return strFolderName;
    }
}
