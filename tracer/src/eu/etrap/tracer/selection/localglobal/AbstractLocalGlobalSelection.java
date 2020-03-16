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


package eu.etrap.tracer.selection.localglobal;

import eu.etrap.medusa.config.ConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.selection.locallocal.AbstractLocalSelection;
import bak.pcj.map.IntKeyIntMap;
import bak.pcj.map.IntKeyIntOpenHashMap;
import eu.etrap.medusa.config.ConfigurationContainer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.utils.ClassLoader;

/**
 * Created on 16.04.2011 19:17:01 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractLocalGlobalSelection extends AbstractLocalSelection {

    protected String strGlobalSelectorImpl = null;
    protected String strFeatureWeightFile = null;
    protected IntKeyIntMap objFeatureWeights = null;

    @Override
    public void weightFeatures() throws SelectionException {
        sortTrainingFile();

        try {
            computeGlobalKnowledge();
            loadFeatureWeights();
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    protected void loadFeatureWeights() throws FileNotFoundException, IOException {
        ConfigurationContainer.print("\tLoading feature weights from file "
                + strFeatureWeightFile + " ... ");
        objFeatureWeights = new IntKeyIntOpenHashMap();
        BufferedReader objReader = new BufferedReader(new FileReader(strFeatureWeightFile));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objFeatureWeights.put(Integer.parseInt(strSplit[0].trim()), Integer.parseInt(strSplit[1].trim()));
        }

        objReader.close();
        ConfigurationContainer.println("DONE! " + objFeatureWeights.size() + " feature weights loaded.");
    }

    protected void computeGlobalKnowledge() throws ConfigurationException, SelectionException {
        Selection objSelection = ClassLoader.loadSelectionImpl(strGlobalSelectorImpl);
        objSelection.init();
        objSelection.weightFeatures();
        objSelection = null;
    }

    @Override
    protected LinkedHashSet<String> doSelect(String strRUID, LinkedHashSet<String> objDataEntries) {
        int intNumberofSelectedFeatures = getNumberOfSelectedFeatures(objDataEntries.size());

        int intNumberOfAlreadySelectedFeatures = 0;

        // local weighting
        HashMap<Integer, HashSet<String>> objWeights2FeatureID = createFeatureWeights2FeatureIDs(objDataEntries);
        int arySortedWeights[] = sortWeights(objWeights2FeatureID);

        LinkedHashSet<String> objSelectedDataEntries = new LinkedHashSet<String>();
        
        int size = arySortedWeights.length;
        size--;
        int counter = 0;
        while (intNumberOfAlreadySelectedFeatures < intNumberofSelectedFeatures) {
            int intCurrWeight = arySortedWeights[size - counter];

            HashSet<String> objWeightedData = objWeights2FeatureID.get(intCurrWeight);
            Iterator<String> objIter = objWeightedData.iterator();
            while (intNumberOfAlreadySelectedFeatures < intNumberofSelectedFeatures
                    && objIter.hasNext()) {
                String strData = objIter.next();
                objSelectedDataEntries.add(strData);
                intNumberOfAlreadySelectedFeatures++;
            }

            counter++;
        }

        return objSelectedDataEntries;
    }

    protected HashMap<Integer, HashSet<String>> createFeatureWeights2FeatureIDs(LinkedHashSet<String> objDataEntries) {
        HashMap<Integer, HashSet<String>> objWeights2FeatID = new HashMap<Integer, HashSet<String>>();

        Iterator<String> objIter = objDataEntries.iterator();
        while (objIter.hasNext()) {
            String strData = objIter.next();
            String strSplit[] = strData.split("\t");
            int intFeatID = Integer.parseInt(strSplit[0]);
            int intFeatWeight = this.objFeatureWeights.get(intFeatID);

            HashSet<String> objFeaturesWithSameWeight = null;
            if (objWeights2FeatID.containsKey(intFeatWeight)) {
                objFeaturesWithSameWeight = objWeights2FeatID.get(intFeatWeight);
            } else {
                objFeaturesWithSameWeight = new HashSet<String>();
            }
            objFeaturesWithSameWeight.add(strData);
            objWeights2FeatID.put(intFeatWeight, objFeaturesWithSameWeight);
        }

        return objWeights2FeatID;
    }

    protected int[] sortWeights(HashMap<Integer, HashSet<String>> objWeights2FeatureID) {
        int aryWeights[] = new int[objWeights2FeatureID.size()];
        int counter = 0;

        Iterator<Integer> objIter = objWeights2FeatureID.keySet().iterator();
        while (objIter.hasNext()) {
            int intWeight = objIter.next();
            aryWeights[counter] = intWeight;
            counter++;
        }

        Arrays.sort(aryWeights);

        return aryWeights;
    }
}
