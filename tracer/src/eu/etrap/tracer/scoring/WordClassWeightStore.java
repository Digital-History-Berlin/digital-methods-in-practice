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


package eu.etrap.tracer.scoring;

import eu.etrap.medusa.config.ConfigurationException;
import java.io.FileNotFoundException;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.selection.globallocal.GlobalWordClassSelectorImpl;

import bak.pcj.map.IntKeyIntMap;
import bak.pcj.map.IntKeyIntOpenHashMap;
import eu.etrap.medusa.config.ConfigurationContainer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import eu.etrap.tracer.preprocessing.Preprocessing;
import eu.etrap.tracer.preprocessing.WordLevelPreprocessingImpl;
import eu.etrap.tracer.selection.WordClassWeightsInterface;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 13.10.2011 14:12:07 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class WordClassWeightStore {

    private IntKeyIntMap objWeightedFeatures = null;
    private HashMap<Integer, String> objFMAP = null;
    private HashMap<String, String> objWordClass = null;
    private HashMap<Integer, String> objFEATS = null;

    public void init() throws SelectionException, ConfigurationException, FileNotFoundException {
        Selection objFeatureWeighting = new GlobalWordClassSelectorImpl();

        if (!objFeatureWeighting.isAlreadyExistent()) {
            objFeatureWeighting.init();
            objFeatureWeighting.weightFeatures();
        }

        objWeightedFeatures = new IntKeyIntOpenHashMap();

        String strFileName = FileManager.getSelectionSortedWordClassFileName();

        try {
            BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objWeightedFeatures.put(Integer.parseInt(strSplit[0]), Integer.parseInt(strSplit[1]));
            }

            objReader.close();


            ConfigurationContainer.println("\t\tWeighted Features loaded: " + objWeightedFeatures.size());

            objFMAP = new HashMap<Integer, String>();

            String strFMAPFileName = FileManager.getTrainingFMAPFileName();
            objReader = new BufferedReader(new FileReader(strFMAPFileName));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objFMAP.put(new Integer(strSplit[0]), strSplit[1].trim());
            }

            objReader.close();



            // read word classes
            Preprocessing objPreprocess = new WordLevelPreprocessingImpl();
            objPreprocess.init();

            objWordClass = new HashMap<String, String>();
            String strWord2LemFile = ConfigurationContainer.getGeneralCategory().getProperty("BASEFORM_FILE_NAME");

            objReader = new BufferedReader(new FileReader(strWord2LemFile));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                if (strSplit.length == 3) {
                    String strWord = objPreprocess.execute(strSplit[0].trim());
                    String strMorphCode = strSplit[2].trim();
                    Character strWordClass = strMorphCode.charAt(0);

                    if (strWordClass == WordClassWeightsInterface.TAG_VERB) {
                        objWordClass.put(strWord.trim(), strWordClass.toString());
                    }
                }else{
                    ConfigurationContainer.println( "\t\tINGORE: " + strLine);
                }
            }

            objReader.close();
            ConfigurationContainer.println("Verbs loaded: " + objWordClass.size());



            // read feats
            objFEATS = new HashMap<Integer, String>();
            String strFEATSFile = FileManager.getTrainingFeatsFileName();
            objReader = new BufferedReader(new FileReader(strFEATSFile));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objFEATS.put(Integer.parseInt(strSplit[0]), strSplit[1]);
            }

            objReader.close();


        } catch (Exception e) {

            throw new ConfigurationException(e);
        }
    }

    public int getFeatureWeight(int fid) {
        return objWeightedFeatures.get(fid);
    }

    public String getFMAP4FID(int fid) {
        return objFMAP.get(fid);
    }

    public boolean isVerb(int wid) {
        boolean containsVerb = false;

        String strWord = objFEATS.get(wid);

        if (objWordClass.containsKey(strWord)) {
            return true;
        }

        return containsVerb;
    }
}
