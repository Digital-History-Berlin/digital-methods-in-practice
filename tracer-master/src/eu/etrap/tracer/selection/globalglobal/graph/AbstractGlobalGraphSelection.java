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


package eu.etrap.tracer.selection.globalglobal.graph;

import bak.pcj.IntIterator;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.selection.globalglobal.AbstractGlobalSelection;
import eu.etrap.tracer.utils.FileManager;
import bak.pcj.set.IntSet;
import bak.pcj.set.IntOpenHashSet;
import de.uni_leipzig.asv.filesort.FileSort;
import eu.etrap.medusa.config.ConfigurationContainer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import eu.etrap.tracer.selection.Selection;

/**
 * Created on 18.04.2011 19:49:05 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractGlobalGraphSelection extends AbstractGlobalSelection implements Selection {

    protected TreeSet<String> objFeatureCooccurrenceFrequencies = null;
    protected HashMap<Integer, IntSet> objCooccurrencesAsFeatures = null;
    protected String strOutFileName = null;
    protected int intScaleFactor = 0;

    @Override
    public void init() throws ConfigurationException {
        super.init();
        objFeatureCooccurrenceFrequencies = new TreeSet<String>();
        objCooccurrencesAsFeatures = new HashMap<Integer, IntSet>();
    }

    @Override
    public void weightFeatures() throws SelectionException {

        // sort train file
        sortTrainingFile();

        try {
            // load features sentence by sentence
            // compute in-memory all feature co-occurrences
            loadTrainFile();
            
            if (!new File(strSortedOutFile).exists()) {
                BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFileName));
                String strOldKey = "";

                Iterator<String> objIter = objFeatureCooccurrenceFrequencies.iterator();
                while (objIter.hasNext()) {
                    String strData = objIter.next();
                    String aryKeys[] = strData.split("\t");
                    String strKey = aryKeys[0] + "\t" + aryKeys[1];

                    if (!strKey.equals(strOldKey)) {
                        // COMPUTE SCORE
                        int intFeatCoccFreq = objFeatureCooccurrenceFrequencies.subSet(strKey + "\t", strKey + "\tZ").size();
                        IntSet objFeatures1 = objCooccurrencesAsFeatures.get(Integer.parseInt(aryKeys[0]));
                        IntSet objFeatures2 = objCooccurrencesAsFeatures.get(Integer.parseInt(aryKeys[1]));

                        // add features itself in order to get max dice sim of 1.0
                        objFeatures1.add(Integer.parseInt(aryKeys[0]));
                        objFeatures2.add(Integer.parseInt(aryKeys[1]));

                        int intOverlap = computeOverlap(objFeatures1, objFeatures2, intFeatCoccFreq);
                        double dblFeatureWeight = doWeightFeatures(objFeatures1, objFeatures2, intFeatCoccFreq, intOverlap);

                        int intScaledFeatureWeight = (int) ((double) intScaleFactor * dblFeatureWeight);

                        // WRITE SCORE
                        objWriter.write(aryKeys[0] + " " + aryKeys[1] + "\t"
                                + intScaledFeatureWeight + "\n");

                        strOldKey = strKey;
                    }
                }

                objWriter.flush();
                objWriter.close();

                // sort weighted file
                int sortOrder[] = new int[]{1, 0};
                char sortTypes[] = new char[]{'s', 'I'};
                FileSort sort = new FileSort("\t", sortOrder, sortTypes);
                sort.sort(strOutFileName, strSortedOutFile);
            }
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    protected double doWeightFeatures(IntSet objFeatures1, IntSet objFeatures2, int intFeatCoccFreq, int intOverlap) {
        // compute dice similarity
        double dblDice = ((double) (2 * intOverlap)) / ((double) objFeatures1.size() + (double) objFeatures2.size());
        return dblDice;
    }

    protected int computeOverlap(IntSet objFeatures1, IntSet objFeatures2, int intFeatCoccFreq) {
        int intOverlap = 0;

        IntSet objSmallerSet = null;
        IntSet objBiggerSet = null;

        if (objFeatures1.size() >= objFeatures2.size()) {
            objBiggerSet = objFeatures1;
            objSmallerSet = objFeatures2;
        } else {
            objBiggerSet = objFeatures2;
            objSmallerSet = objFeatures1;
        }

        IntIterator objIterator = objSmallerSet.iterator();
        while (objIterator.hasNext()) {
            int intFeature = objIterator.next();

            if (objBiggerSet.contains(intFeature)) {
                intOverlap++;
            }
        }

        return intOverlap;
    }

    protected void loadTrainFile() throws ConfigurationException, FileNotFoundException, IOException {
        HashSet<String> objDataEntries = new HashSet<String>();

        String strSortedFileName = FileManager.getTrainingSortedTrainFileName();
        ConfigurationContainer.print("\tReading sorted train file from " + strSortedFileName);
        BufferedReader objReader = new BufferedReader(new FileReader(strSortedFileName));

        String strLine = null;
        String strOldRUID = null;
        while ((strLine = objReader.readLine()) != null) {
            intTotalNumberOfFeatures++;
            String strRUID = strLine.split("\t")[1];

            if (strOldRUID == null) {
                strOldRUID = strRUID;
            }

            if (strRUID.equals(strOldRUID)) {
                objDataEntries.add(strLine);
            } else {
                computeFeatureCooccurrences(objDataEntries);
                computeFeatureCooccurrencesFrequencies(objDataEntries, strOldRUID);
                strOldRUID = strRUID;
                objDataEntries.clear();
                objDataEntries.add(strLine);
            }
        }

        computeFeatureCooccurrences(objDataEntries);
        computeFeatureCooccurrencesFrequencies(objDataEntries, strOldRUID);
        objReader.close();
        ConfigurationContainer.println(" DONE!");
    }

    protected void computeFeatureCooccurrencesFrequencies(HashSet<String> objDataEntries, String strRUID) {
        // a) feat1 tab teat 2 --> freq
        int aryFeatureIDs[] = new int[objDataEntries.size()];
        Iterator<String> objIter = objDataEntries.iterator();
        int index = 0;

        while (objIter.hasNext()) {
            String strData = objIter.next();
            String strSplit[] = strData.split("\t");
            aryFeatureIDs[index] = Integer.parseInt(strSplit[0].trim());
            index++;

        }

        for (int i = 0; i < aryFeatureIDs.length; i++) {
            for (int j = 0; j < aryFeatureIDs.length; j++) {
                if (i != j && aryFeatureIDs[i] < aryFeatureIDs[j]) {
                    String strKey = aryFeatureIDs[i] + "\t" + aryFeatureIDs[j]
                            + "\t" + strRUID;
                    objFeatureCooccurrenceFrequencies.add(strKey);
                }
            }
        }
    }

    protected void computeFeatureCooccurrences(HashSet<String> objDataEntries) {
        // b) feat --> co-occurrences
        int aryFeatureIDs[] = new int[objDataEntries.size()];
        Iterator<String> objIter = objDataEntries.iterator();
        int index = 0;
        while (objIter.hasNext()) {
            String strData = objIter.next();
            String strSplit[] = strData.split("\t");
            aryFeatureIDs[index] = Integer.parseInt(strSplit[0].trim());
            index++;

        }

        for (int i = 0; i < aryFeatureIDs.length; i++) {
            for (int j = 0; j < aryFeatureIDs.length; j++) {
                if (i != j) {
                    IntSet objFeatures = null;

                    if (objCooccurrencesAsFeatures.containsKey(aryFeatureIDs[i])) {
                        objFeatures = objCooccurrencesAsFeatures.get(aryFeatureIDs[i]);
                    } else {
                        objFeatures = new IntOpenHashSet();
                    }

                    objFeatures.add(aryFeatureIDs[j]);
                    objCooccurrencesAsFeatures.put(aryFeatureIDs[i], objFeatures);
                }
            }
        }
    }

    @Override
    public void select() throws SelectionException {
        objCooccurrencesAsFeatures.clear();
        objCooccurrencesAsFeatures = null;

        // compute number of selected features
        computeNumberOfSelectedTokens();

        // read weighted features in memory
        HashSet<String> objSelectedFeatures = new HashSet<String>();

        try {
            String strSelectedOutFileName = FileManager.getSelectionFileName(this.getClass().getName());

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strSelectedOutFileName));
            ConfigurationContainer.print("\tSelecting features to outfile" + strSelectedOutFileName);

            BufferedReader objReader = new BufferedReader(new FileReader(strSortedOutFile));
            String strLine = null;

            ConfigurationContainer.println();

            while (intNumberOfAlreadySelectedTokens < intNumberOfSelectedTokens && (strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                String strKey = strSplit[0].replace(" ", "\t");
                SortedSet<String> objSubset = objFeatureCooccurrenceFrequencies.subSet(strKey + "\t", strKey + "\tZ");
                Iterator<String> objIter = objSubset.iterator();

                while (objIter.hasNext()) {
                    String strDataEntry = objIter.next();
                    String aryFeatures[] = strDataEntry.split("\t");
                    objSelectedFeatures.add(aryFeatures[0] + "\t" + aryFeatures[2]);
                    objSelectedFeatures.add(aryFeatures[1] + "\t" + aryFeatures[2]);
                    intNumberOfAlreadySelectedTokens = objSelectedFeatures.size();
                }
            }

            objReader.close();

            Iterator<String> objIter = objSelectedFeatures.iterator();
            while (objIter.hasNext()) {
                String strData = objIter.next();
                objWriter.write(strData + "\n");
            }


            objWriter.flush();
            objWriter.close();
            ConfigurationContainer.println(" DONE! ");

            ConfigurationContainer.println("\t" + intNumberOfAlreadySelectedTokens + " feature tokens are selected out of "
                    + intTotalNumberOfFeatures + " by a configured feature density of "
                    + formatFeatureDensity(((double) intNumberOfAlreadySelectedTokens / (double) intTotalNumberOfFeatures)) + ".");

            objFeatureCooccurrenceFrequencies.clear();
            objFeatureCooccurrenceFrequencies = null;
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }
}
