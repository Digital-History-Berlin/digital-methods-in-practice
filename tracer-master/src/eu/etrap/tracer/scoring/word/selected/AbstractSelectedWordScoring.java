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


package eu.etrap.tracer.scoring.word.selected;

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import eu.etrap.tracer.scoring.ScoringException;
import eu.etrap.tracer.scoring.word.AbstractWordScoring;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 28.05.2011 11:10:18 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractSelectedWordScoring extends AbstractWordScoring {

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strTaxonomyCode = "01-02-00-00-00-00";
        this.strFingerpringFile = FileManager.getMultipleSelectionFileName();
    }

    @Override
    public void prepareData() throws ScoringException {
        try {
            ConfigurationContainer.println( "\tPreparing data from " + strFingerpringFile + " ..." );
            
            HashMap<Integer, String> objFMAP = loadFMAP();

            BufferedReader objReader =
                    new BufferedReader(new FileReader(this.strFingerpringFile));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intRUID = Integer.parseInt(strSplit[1].trim());
                int intFID = Integer.parseInt(strSplit[0].trim());

                ArrayList<Integer> objRUID2Fingerprint = new ArrayList<Integer>();

                if (objFingerprints.containsKey(intRUID)) {
                    objRUID2Fingerprint = objFingerprints.get(intRUID);
                }

                String strFMAPIDs = objFMAP.get( intFID );
                String strFMAPSplit[] = strFMAPIDs.split(" ");

                for( int i=0; i<strFMAPSplit.length; i++ ){
                    objRUID2Fingerprint.add(new Integer(strFMAPSplit[i]) );
                }

                objFingerprints.put(intRUID, objRUID2Fingerprint);
            }

            objReader.close();

            objFMAP.clear();
            objFMAP = null;
            cleanFingerprints();
        } catch (Exception e) {
            throw new ScoringException(e);
        }
    }

    protected void cleanFingerprints(){
        Iterator<Integer> objIter = objFingerprints.keySet().iterator();

        while( objIter.hasNext() ){
                Integer objRUID = objIter.next();
                ArrayList<Integer> objFingerprint = objFingerprints.get( objRUID );
                HashSet<Integer> objUniqeFeatures = new HashSet<Integer>(objFingerprint);
                objFingerprint.clear();
                objFingerprint.addAll(objUniqeFeatures);
        }
    }

    protected HashMap<Integer, String> loadFMAP() throws FileNotFoundException, IOException, ConfigurationException {
        HashMap<Integer, String> objFMAP = new HashMap<Integer, String>();

        String strFileName = FileManager.getTrainingFMAPFileName();
        ConfigurationContainer.print( "\t\tLoading fmap file " + strFileName + " ..." );
        
        BufferedReader objReader =
                new BufferedReader(new FileReader(strFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            String strRUID = strSplit[0].trim();
            String strFMAP = strSplit[1].trim();
            objFMAP.put(new Integer(strRUID),strFMAP);
        }

        objReader.close();

        ConfigurationContainer.println( " DONE!" );

        return objFMAP;
    }
}
