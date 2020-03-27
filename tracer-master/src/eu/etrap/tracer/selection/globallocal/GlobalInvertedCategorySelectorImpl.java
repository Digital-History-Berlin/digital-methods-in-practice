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


package eu.etrap.tracer.selection.globallocal;

import bak.pcj.IntIterator;
import de.uni_leipzig.asv.filesort.FileSort;
import bak.pcj.set.IntSet;
import bak.pcj.set.IntOpenHashSet;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.selection.globalglobal.AbstractGlobalSelection;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 11.12.2010 15:59:47 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class GlobalInvertedCategorySelectorImpl extends AbstractGlobalSelection implements Selection {

    protected String strCategoryFileName = null;
    protected TreeSet<String> objClassifiedData = null;
    protected HashMap<String, Integer> objCategoriesDistribution = null;
    protected int intNumberOfRUU = 0;
    protected double dblMinimumFraction = 0;
    protected HashMap<Integer, IntSet> objInvertedList = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();
        objClassifiedData = new TreeSet<String>();
        objCategoriesDistribution = new HashMap<String, Integer>();
        objInvertedList = new HashMap<Integer, IntSet>();
        strSortedOutFile = FileManager.getSelectionSortedICFFileName();
        strTaxonomyCode = "01-01-02-02-01";
    }

    @Override
    protected void doWeightFeatures() throws SelectionException {
        try {
            ConfigurationContainer.print("\tLoading category distribution ...");
            loadFile(strCategoryFileName);

            ConfigurationContainer.println(" DONE. Loaded " + objClassifiedData.size()
                    + " categorized re-use units by " + objCategoriesDistribution.size()
                    + " categories.");

            detectMaxNumberOfCategories();
            ConfigurationContainer.println("\tSelected " + objCategoriesDistribution.size()
                    + " categories.");

            loadInvertedList();

            String strFilame = FileManager.getSelectionICFFileName();

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFilame));
            IntIterator objIter = this.objFeatureDistribution.keySet().iterator();

            int intScaleFactor = 100;

            while (objIter.hasNext()) {
                int intFeatID = objIter.next();

                if (intFeatID > 0) {
                    double dblNumberOfRelevantCategories = (double) this.objCategoriesDistribution.size();
                    double dblNumberOfObservedCategories = computeNumberOfObservedCategories(intFeatID);

                    double icf = Math.log(Math.max(1, dblNumberOfRelevantCategories / dblNumberOfObservedCategories)) / Math.log(2);
                    int intScaledFactor = (int) (icf * (double) intScaleFactor);

                    objWriter.write(intFeatID + "\t" + intScaledFactor + "\n");
                }
            }

            objWriter.flush();
            objWriter.close();

            int sortOrder[] = new int[]{1, 0};
            char sortTypes[] = new char[]{'i', 'I'};
            FileSort sort = new FileSort("\t", sortOrder, sortTypes);
            sort.sort(strFilame, strSortedOutFile);

        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    protected void detectMaxNumberOfCategories() throws FileNotFoundException, IOException {
        int intMinCatFreq = (int) Math.round(dblMinimumFraction * this.intNumberOfRUU);
        ConfigurationContainer.println("\tIgnoring all categories that contain less than "
                + intMinCatFreq + " re-use units.");

        HashMap<String, Integer> objCleanedCategoriesDistribution = new HashMap<String, Integer>();

        Iterator<String> objIter = this.objCategoriesDistribution.keySet().iterator();
        while (objIter.hasNext()) {
            String strKey = objIter.next();
            int intCategoryFreq = objCategoriesDistribution.get(strKey);

            if (intCategoryFreq >= intMinCatFreq) {
                objCleanedCategoriesDistribution.put(strKey, intCategoryFreq);
            }
        }

        objCategoriesDistribution = objCleanedCategoriesDistribution;
    }

    protected void loadFile(String strFileName) throws FileNotFoundException, IOException {
        IntSet objRUUIDs = new IntOpenHashSet();
        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
        String strLine = null;
        int counter = 0;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");

            String strClass = strSplit[1].trim();
            objClassifiedData.add(strLine.trim());

            objRUUIDs.add(Integer.parseInt(strSplit[0].trim()));

            int intCatFrequency = 0;

            if (objCategoriesDistribution.containsKey(strClass)) {
                intCatFrequency = objCategoriesDistribution.get(strClass);
            }

            intCatFrequency++;


            objCategoriesDistribution.put(strClass, intCatFrequency);
        }

        objReader.close();

        intNumberOfRUU = objRUUIDs.size();
        objRUUIDs.clear();
        objRUUIDs = null;
    }

    protected int detectScaleFactor(int intTotalNumberOfFeatures) {
        double dblLog10 = Math.log10(intTotalNumberOfFeatures);
        int intNUmberOfDigits = (int) Math.round(Math.ceil(dblLog10));
        return (int) Math.round(Math.pow(10, intNUmberOfDigits));
    }

    @Override
    protected void buildByteArray() throws SelectionException {
        super.buildByteArray();

        try {
            BufferedReader objReader = new BufferedReader(new FileReader(FileManager.getSelectionSortedICFFileName()));
            String strLine = null;

            while (intNumberOfAlreadySelectedTokens < this.intNumberOfSelectedTokens) {
                strLine = objReader.readLine();
                int intFeatID = Integer.parseInt(strLine.split("\t")[0].trim());

                int intFeatFreq = objFeatureDistribution.get(intFeatID);
                if (intFeatFreq > 0) {
                    setSelectedFeature(intFeatID);
                    intNumberOfAlreadySelectedTokens += intFeatFreq;
                }
            }

            objReader.close();
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    protected void loadInvertedList() throws IOException, ConfigurationException {
        String strTrainFile = FileManager.getTrainingTrainFileName();
        BufferedReader objReader =
                new BufferedReader(new FileReader(strTrainFile));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            int intFeatureID = Integer.parseInt(strSplit[0].trim());
            int intRUID = Integer.parseInt(strSplit[1].trim());


            IntSet obRUIDs = null;
            if (this.objInvertedList.containsKey(intFeatureID)) {
                obRUIDs = objInvertedList.get(intFeatureID);
            } else {
                obRUIDs = new IntOpenHashSet();
            }

            obRUIDs.add(intRUID);
            objInvertedList.put(intFeatureID, obRUIDs);
        }

        objReader.close();
    }

    protected int computeNumberOfObservedCategories(int intFeatID) {
        IntSet objRUIDs = this.objInvertedList.get(intFeatID);
        IntIterator objIter = objRUIDs.iterator();
        HashSet<String> objObservedClasses = new HashSet<String>();

        while (objIter.hasNext()) {
            int intRUID = objIter.next();
            SortedSet<String> objClassData = this.objClassifiedData.subSet(intRUID + "\t", intRUID + "\tZ");

            Iterator<String> objIter2 = objClassData.iterator();
            while (objIter2.hasNext()) {
                String strData = objIter2.next();
                String strSplit[] = strData.split("\t");
                objObservedClasses.add(strSplit[1].trim());
            }

        }

        return objObservedClasses.size();
    }
}
