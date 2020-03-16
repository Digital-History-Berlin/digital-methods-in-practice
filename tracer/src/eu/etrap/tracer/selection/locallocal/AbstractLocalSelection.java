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


package eu.etrap.tracer.selection.locallocal;

import eu.etrap.medusa.config.ConfigurationContainer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import eu.etrap.tracer.selection.AbstractSelection;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 04.04.2011 10:33:13 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractLocalSelection extends AbstractSelection {

    @Override
    public void weightFeatures() throws SelectionException {
        sortTrainingFile();
    }

    @Override
    public void select() throws SelectionException {
        try {
            int intNumberOfSelectedTokens = 0;
            int intTotalNumberOfFeatures = 0;

            String strOutFileName = FileManager.getSelectionFileName(this.getClass().getName());
            BufferedWriter objWriter =
                    new BufferedWriter(new FileWriter(strOutFileName));

            LinkedHashSet<String> objDataEntries = new LinkedHashSet<String>();

            String strSortedFileName = FileManager.getTrainingSortedTrainFileName();
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
                    LinkedHashSet<String> objSelectedData = doSelect(strOldRUID, objDataEntries);
                    Iterator<String> objIter = objSelectedData.iterator();

                    while (objIter.hasNext()) {
                        //String strSplit[] = objIter.next().trim().split("\t");
                        String strResult = objIter.next();
                        objWriter.write(strResult.trim() + "\n");
                        intNumberOfSelectedTokens++;
                    }

                    strOldRUID = strRUID;
                    objDataEntries.clear();
                    objDataEntries.add(strLine);
                }
            }

            HashSet<String> objSelectedData = doSelect(strOldRUID, objDataEntries);
            Iterator<String> objIter = objSelectedData.iterator();

            while (objIter.hasNext()) {
                String strResult = objIter.next();
                objWriter.write( strResult + "\n");
                intNumberOfSelectedTokens++;
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();

            ConfigurationContainer.println("\t" + intNumberOfSelectedTokens + " feature tokens are selected out of "
                    + intTotalNumberOfFeatures + " by a configured feature density of "
                    + formatFeatureDensity((double) intNumberOfSelectedTokens / (double) intTotalNumberOfFeatures)
                    + " (configured: " + formatFeatureDensity(dblFeatureDensity) + ").");
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    // will be overwritten
    protected LinkedHashSet<String> doSelect(String strRUID, LinkedHashSet<String> objDataEntries) {
        return null;
    }

    protected int getNumberOfSelectedFeatures(int intTotalNumberOfFeatures) {
        return Math.max(1, (int) Math.round(this.dblFeatureDensity * intTotalNumberOfFeatures));
    }

    protected String formatFeatureDensity(double dblFeatureDensity) {
        DecimalFormat objFeatureDensityFormat = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.UK));
        return objFeatureDensityFormat.format(new Double(dblFeatureDensity));
    }
}
