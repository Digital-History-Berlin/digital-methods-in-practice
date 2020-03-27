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
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 11.12.2010 15:59:47 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class GlobalSelfInformationSelectorImpl extends AbstractGlobalSelection implements Selection {

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strSortedOutFile = FileManager.getSelectionSortedSelfInformationFileName();
        strTaxonomyCode = "01-01-01-04-01";
    }

    @Override
    protected void doWeightFeatures() throws SelectionException {
        try {
            String strEntropyFileName = FileManager.getSelectionSelfInformationFileName();

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strEntropyFileName));
            IntIterator objIter = this.objFeatureDistribution.keySet().iterator();

            int intScaleFactor = detectScaleFactor(this.intTotalNumberOfFeatures);

            while (objIter.hasNext()) {
                int intFeatID = objIter.next();

                if (intFeatID > 0) {
                    int intFeatFreq = objFeatureDistribution.get(intFeatID);

                    double p = (double) intFeatFreq / (double) this.intTotalNumberOfFeatures;
                    double entropy = -1 * Math.log(p) / Math.log(2);
                    int intScaledFactor = (int) (entropy * (double) intScaleFactor);

                    objWriter.write(intFeatID + "\t" + intScaledFactor + "\n");
                }
            }

            objWriter.flush();
            objWriter.close();

            int sortOrder[] = new int[]{1, 0};
            char sortTypes[] = new char[]{'I', 'I'};
            FileSort sort = new FileSort("\t", sortOrder, sortTypes);
            sort.sort(strEntropyFileName, strSortedOutFile);

        } catch (Exception e) {
            throw new SelectionException(e);
        }
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
            BufferedReader objReader = new BufferedReader(new FileReader(FileManager.getSelectionSortedSelfInformationFileName()));
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
