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


package eu.etrap.tracer.selection.localglobal.graph;

import bak.pcj.map.ObjectKeyIntMap;
import bak.pcj.map.ObjectKeyIntOpenHashMap;
import eu.etrap.medusa.config.ConfigurationContainer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import eu.etrap.tracer.selection.localglobal.AbstractLocalGlobalSelection;

/**
 * Created on 27.04.2011 13:09:32 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractLocalGlobalGraphSelection extends AbstractLocalGlobalSelection {

    protected ObjectKeyIntMap objFeatureWeights = null;

    @Override
    protected void loadFeatureWeights() throws FileNotFoundException, IOException {
        ConfigurationContainer.print("\tLoading feature weights from file "
                + strFeatureWeightFile + " ... ");
        objFeatureWeights = new ObjectKeyIntOpenHashMap();
        BufferedReader objReader = new BufferedReader(new FileReader(strFeatureWeightFile));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objFeatureWeights.put(strSplit[0].trim(), Integer.parseInt(strSplit[1].trim()));
        }

        objReader.close();
        ConfigurationContainer.println("DONE! " + objFeatureWeights.size() + " feature weights loaded.");
    }

    @Override
    protected HashMap<Integer, HashSet<String>> createFeatureWeights2FeatureIDs(LinkedHashSet<String> objDataEntries) {
        HashMap<Integer, HashSet<String>> objWeights2FeatID = new HashMap<Integer, HashSet<String>>();

        if( objDataEntries.size() == 1 ){
            HashSet<String> objData = new HashSet<String>();
            Iterator<String> objIter = objDataEntries.iterator();
            while( objIter.hasNext() ){
                String strData = objIter.next();
                objData.add(strData);
            }

            objWeights2FeatID.put( 100, objData);

            return objWeights2FeatID;
        }

        int aryFeatID[] = new int[objDataEntries.size()];
        int index = 0;
        Iterator<String> objIter = objDataEntries.iterator();

        while (objIter.hasNext()) {
            String strData = objIter.next();
            String strSplit[] = strData.split("\t");
            aryFeatID[index] = Integer.parseInt(strSplit[0]);
            index++;
        }

        // detect features
        HashSet<String> objFeatures = new HashSet<String>();
        for (int i = 0; i < aryFeatID.length; i++) {
            for (int j = 0; j < aryFeatID.length; j++) {
                if (i != j) {
                    objFeatures.add(aryFeatID[i] + " " + aryFeatID[j]);
                }
            }
        }

        TreeSet<String> objSortedDataEntries = new TreeSet<String>(objDataEntries);

        Iterator<String> objFeatureIter = objFeatures.iterator();
        while (objFeatureIter.hasNext()) {
            String strFeature = objFeatureIter.next();
            int intFeatWeight = this.objFeatureWeights.get(strFeature);

            HashSet<String> objFeaturesWithSameWeight = null;
            if (objWeights2FeatID.containsKey(intFeatWeight)) {
                objFeaturesWithSameWeight = objWeights2FeatID.get(intFeatWeight);
            } else {
                objFeaturesWithSameWeight = new HashSet<String>();
            }

            String strSplitedFeats[] = strFeature.split(" ");

            for (int i = 0; i < strSplitedFeats.length; i++) {
                SortedSet<String> objData = objSortedDataEntries.subSet(strSplitedFeats[i] + "\t", strSplitedFeats[i] + "\tZ");

                Iterator<String> objIter2 = objData.iterator();

                while (objIter2.hasNext()) {
                    String strData = objIter2.next();
                    objFeaturesWithSameWeight.add(strData);
                }
            }
            objWeights2FeatID.put(intFeatWeight, objFeaturesWithSameWeight);
        }

        return objWeights2FeatID;
    }
}
