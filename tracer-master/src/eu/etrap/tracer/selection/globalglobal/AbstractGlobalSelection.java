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

import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import eu.etrap.tracer.selection.AbstractSelection;
import bak.pcj.map.IntKeyIntMap;
import bak.pcj.map.IntKeyIntOpenHashMap;
import bak.pcj.set.IntSet;
import bak.pcj.set.IntOpenHashSet;
import eu.etrap.medusa.config.ConfigurationContainer;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import eu.etrap.tracer.selection.FeatureDistributionStore;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.utils.FileManager;
import eu.etrap.tracer.Constants;

/**
 * Created on 04.04.2011 10:32:37 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractGlobalSelection extends AbstractSelection {

    protected int intTotalNumberOfFeatures = 0;
    protected IntKeyIntMap objFeatureDistribution = null;
    protected int intNumberOfSelectedTokens = 0;
    protected byte arySelectedFeatureID[] = null;
    protected int intMinFeatID = 0;
    protected int intMaxFeatID = 0;
    protected int intNumberOfAlreadySelectedTokens = 0;
    protected String strSortedOutFile = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strFeatureDistStoreID = "GlobalFeatureFrequencyDistribution";

        // This feature will be counted within loadFeatureFrequencyDistribution()
        intTotalNumberOfFeatures = 0;

        intMinFeatID = 2100000000;
        intMaxFeatID = 0;

        strTaxonomyCode = "01-01-00-00-00";
    }

    @Override
    public void weightFeatures() throws SelectionException {
        try {
            loadFeatureFrequencyDistribution();
        } catch (Exception e) {
            throw new SelectionException(e);
        }

        if (!new File(strSortedOutFile).exists()) {
            doWeightFeatures();
        }
    }

    protected void writeByteArrayToDisc() throws SelectionException {
        try {
            String strSelectionFile = FileManager.getSelectionFileName(this.getClass().getName());

            String strOutFileName = strSelectionFile.replace(".sel", ".bin");

            BufferedOutputStream objStream =
                    new BufferedOutputStream(new FileOutputStream(strOutFileName));

            objStream.write(arySelectedFeatureID, 0, arySelectedFeatureID.length);
            objStream.flush();
            objStream.close();
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    protected void buildByteArray() throws SelectionException {
        int intNumberOfBytes = 0;
        int intNeededBytes = this.intMaxFeatID + 1;

        if (intNeededBytes % 8 == 0) {
            intNumberOfBytes = intNeededBytes / 8;
        } else {
            intNumberOfBytes = intNeededBytes / 8;
            intNumberOfBytes++;
        }

        arySelectedFeatureID = new byte[intNumberOfBytes];
    }

    protected void doWeightFeatures() throws SelectionException {
        // do nothing iff only feature frequency is used
    }

    protected void computeNumberOfSelectedTokens() {
        intNumberOfSelectedTokens = (int) Math.round(dblFeatureDensity * (double) intTotalNumberOfFeatures);
        ConfigurationContainer.println("\t" + intNumberOfSelectedTokens + " feature tokens should be selected out of "
                + intTotalNumberOfFeatures + " by a configured feature density of " + formatFeatureDensity(dblFeatureDensity));
    }

    protected void loadFeatureFrequencyDistribution() throws IOException, ConfigurationException {

        if (!FeatureDistributionStore.containsDistribution(strFeatureDistStoreID)) {
            objFeatureDistribution = new IntKeyIntOpenHashMap();
            String strTrainFile = FileManager.getTrainingTrainFileName();
            ConfigurationContainer.println("\tReading train file " + strTrainFile);

            BufferedReader objReader =
                    new BufferedReader(new FileReader(strTrainFile));

            String strLine = null;

            IntSet objReuseUnits = new IntOpenHashSet();

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intFeatureID = Integer.parseInt(strSplit[0].trim());
                objReuseUnits.add(Integer.parseInt(strSplit[1].trim()));

                int intFeatureFrequency = 0;

                if (objFeatureDistribution.containsKey(intFeatureID)) {
                    intFeatureFrequency = objFeatureDistribution.get(intFeatureID);
                }

                intTotalNumberOfFeatures++;
                intFeatureFrequency++;

                objFeatureDistribution.put(intFeatureID, intFeatureFrequency);

                this.intMinFeatID = Math.min(intFeatureID, intMinFeatID);
                this.intMaxFeatID = Math.max(intFeatureID, intMaxFeatID);
            }

            objReader.close();

            objFeatureDistribution.put(Constants.NUMBER_OF_STORED_TOKENS, intTotalNumberOfFeatures);
            objFeatureDistribution.put(Constants.MINIMUM_FEATURE_ID, intMinFeatID);
            objFeatureDistribution.put(Constants.MAXIMUM_FEATURE_ID, intMaxFeatID);
            objFeatureDistribution.put(Constants.NUMBER_OF_REUSE_UNITS, objReuseUnits.size());

            objReuseUnits.clear();

            FeatureDistributionStore.setDistribution(strFeatureDistStoreID, objFeatureDistribution);
            ConfigurationContainer.println("\tGlobal feature distribution stored by key " + strFeatureDistStoreID);
        } else {
            objFeatureDistribution = FeatureDistributionStore.getDistribution(strFeatureDistStoreID);
            intTotalNumberOfFeatures = objFeatureDistribution.get(Constants.NUMBER_OF_STORED_TOKENS);
            intMinFeatID = objFeatureDistribution.get(Constants.MINIMUM_FEATURE_ID);
            intMaxFeatID = objFeatureDistribution.get(Constants.MAXIMUM_FEATURE_ID);
            ConfigurationContainer.println("\tAlready loaded global feature distribution is used by the key " + strFeatureDistStoreID);
        }

        ConfigurationContainer.println("\tFeature IDs range from " + intMinFeatID
                + " to " + intMaxFeatID + ".");
    }

    protected void setSelectedFeature(int intFeatureID) {
        int intPosByte = intFeatureID / 8;
        int intPosBit = intFeatureID % 8;
        this.arySelectedFeatureID[intPosByte] = (byte) (arySelectedFeatureID[intPosByte] | (1 << (intPosBit)));
    }

    protected boolean isAlreadySelectedWord(int intFeatureID) {

        int intPosByte = intFeatureID / 8;
        int intPosBit = intFeatureID % 8;

        if ((arySelectedFeatureID[intPosByte] & (1 << intPosBit)) == (1 << intPosBit)) {
            return true;
        }

        return false;
    }

    protected int getAccuracy(double dblFDValue) {
        String strFeatureDensityAsString = Double.toString(dblFDValue);
        int intPositionOfSeparator = strFeatureDensityAsString.indexOf(".") + 1;

        int intAccuracy = strFeatureDensityAsString.length() - intPositionOfSeparator;

        return Math.min(intAccuracy, Constants.OMODP_MAX_ACCURACY);
    }

    protected String formatFeatureDensity(double dblFeatureDensity) {
        DecimalFormat objFeatureDensityFormat = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.UK));
        return objFeatureDensityFormat.format(new Double(dblFeatureDensity));
    }

    @Override
    public void select() throws SelectionException {
        computeNumberOfSelectedTokens();
        buildByteArray();

        ConfigurationContainer.println("\t" + intNumberOfAlreadySelectedTokens + " feature tokens are selected out of "
                + intTotalNumberOfFeatures + " by a configured feature density of "
                + formatFeatureDensity(((double) intNumberOfAlreadySelectedTokens / (double) intTotalNumberOfFeatures)) + ".");

        writeByteArrayToDisc();

        try {
            String strTrainFile = FileManager.getTrainingTrainFileName();
            String strOutFileName = FileManager.getSelectionFileName(this.getClass().getName());

            BufferedReader objReader = new BufferedReader(new FileReader(strTrainFile));

            BufferedWriter objWriter =
                    new BufferedWriter(new FileWriter(strOutFileName));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intFeatureID = Integer.parseInt(strSplit[0]);

                if (isAlreadySelectedWord(intFeatureID)) {
                    objWriter.write(strSplit[0] + "\t" + strSplit[1] + "\n");
                }
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }
}
