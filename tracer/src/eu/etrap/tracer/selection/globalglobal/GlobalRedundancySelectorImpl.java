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


package eu.etrap.tracer.selection.globalglobal;

import bak.pcj.IntIterator;
import de.uni_leipzig.asv.filesort.FileSort;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 11.12.2010 15:59:47 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class GlobalRedundancySelectorImpl extends AbstractGlobalSelection implements Selection {

    int intTotalNumberOfWords = 0;
    HashMap<String, Long> objLetterDistribution = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();

        objLetterDistribution = new HashMap<String, Long>();
        strSortedOutFile = FileManager.getSelectionSortedFeatureRedundancyFileName();
        strTaxonomyCode = "01-01-01-04-03";
    }

    @Override
    protected void doWeightFeatures() throws SelectionException {
        try {
            String strFileName = FileManager.getSelectionFeatureRedundancyFileName();
            ConfigurationContainer.println("\tWeighting features by feature redundancy ...");

            if (!new File(strFileName).exists()) {
                double dblLetterEntropy = computeLetterEntropy();

                HashMap<Integer, String> objFeatsMapping = loadFeatsFile();
                HashMap<Integer, String> objFMAPMapping = loadFMAPFile();


                BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFileName));
                IntIterator objFeatIter = this.objFeatureDistribution.keySet().iterator();

                double intScaleFactor = 1000;

                while (objFeatIter.hasNext()) {
                    int intFeatID = objFeatIter.next();

                    if (intFeatID > 0) {
                        int intFeatureFrequency = objFeatureDistribution.get(intFeatID);
                        String strFeatureKey = objFMAPMapping.get(intFeatID);
                        String aryFeatureKey[] = strFeatureKey.split(" ");

                        String strFeature = "";
                        for (int i = 0; i < aryFeatureKey.length; i++) {
                            String strWord = objFeatsMapping.get(new Integer(aryFeatureKey[i].trim()));
                            strFeature += strWord + " ";
                        }

                        int intFeatureLength = strFeature.trim().length();

                        double dblLengthInBit = intFeatureLength * dblLetterEntropy;

                        double dblSelfInformation = -1 * Math.log((double) intFeatureFrequency / (double) intTotalNumberOfFeatures) / Math.log(2);
                        double dblDiff = dblLengthInBit - dblSelfInformation;
                        //double dblRedundancy = (double) intFeatureFrequency * dblDiff;
                        double dblRedundancy = dblDiff;

                        int intScaledFactor = (int) (dblRedundancy * (double) intScaleFactor);

                        objWriter.write(intFeatID + "\t" + intScaledFactor + "\n");
                    }
                }

                objWriter.flush();
                objWriter.close();

                int sortOrder[] = new int[]{1, 0};
                char sortTypes[] = new char[]{'i', 'I'};
                FileSort sort = new FileSort("\t", sortOrder, sortTypes);
                sort.sort(strFileName, strSortedOutFile);
                ConfigurationContainer.println(" DONE!");
            } else {
                ConfigurationContainer.println(" ALREADY COMPUTED!");
            }
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    protected double computeLetterEntropy() throws FileNotFoundException, IOException, ConfigurationException {
        double dblTotalLetterEntropy = 0.0;

        ConfigurationContainer.print("\tReading letter distribution from "
                + FileManager.getCharDistFileName() + " ... ");
        BufferedReader objReader = new BufferedReader(new FileReader(FileManager.getCharDistFileName()));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            String strLetter = strSplit[1].trim();
            Long objLetterFreq = new Long(strSplit[3].trim());
            objLetterDistribution.put(strLetter, objLetterFreq);
        }
        objReader.close();

        // move %TOTAL% to Constants
        long intTotalNumberOfLetters = objLetterDistribution.get("%TOTOAL%");
        Iterator<String> objIter = objLetterDistribution.keySet().iterator();

        while (objIter.hasNext()) {
            String strLetter = objIter.next();
            double dblLetterProb = (double) objLetterDistribution.get(strLetter) / (double) intTotalNumberOfLetters;
            double dblLetterEntropy = dblLetterProb * Math.log(dblLetterProb) / Math.log(2);
            dblTotalLetterEntropy += dblLetterEntropy;
        }

        dblTotalLetterEntropy *= -1;
        ConfigurationContainer.println("DONE. Letter entropy is " + dblTotalLetterEntropy);
        return dblTotalLetterEntropy;
    }

    protected HashMap<Integer, String> loadFMAPFile() throws FileNotFoundException, IOException, ConfigurationException {
        return loadFile(FileManager.getTrainingFMAPFileName(), 1);
    }

    protected HashMap<Integer, String> loadFeatsFile() throws FileNotFoundException, IOException, ConfigurationException {
        return loadFile(FileManager.getTrainingFeatsFileName(), 1);
    }

    protected HashMap<Integer, String> loadFile(String strFileName, int intColumn) throws FileNotFoundException, IOException {
        HashMap<Integer, String> objMap = new HashMap<Integer, String>();

        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objMap.put(new Integer(strSplit[0].trim()), strSplit[intColumn].trim());
        }

        objReader.close();

        return objMap;
    }

    @Override
    protected void buildByteArray() throws SelectionException {
        super.buildByteArray();

        try {
            BufferedReader objReader = new BufferedReader(new FileReader(FileManager.getSelectionSortedFeatureRedundancyFileName()));
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
}
