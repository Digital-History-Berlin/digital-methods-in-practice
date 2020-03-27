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
import java.io.BufferedWriter;
import java.io.FileWriter;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 15.04.2011 16:18:22 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractFeatureFrequencySelection extends AbstractGlobalSelection {

    protected boolean isAscendingSortOrder = false;

    @Override
    protected void doWeightFeatures() throws SelectionException {
        try {
            ConfigurationContainer.println("\tWeighting features by word frequency ...");

            String strFileName = FileManager.getSelectionWordFrequencyFileName();

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFileName));
            IntIterator objFeatIter = this.objFeatureDistribution.keySet().iterator();

            while (objFeatIter.hasNext()) {
                int intFeatID = objFeatIter.next();

                if (intFeatID > 0) {
                    int intFeatureFrequency = objFeatureDistribution.get(intFeatID);
                    objWriter.write(intFeatID + "\t" + intFeatureFrequency + "\n");
                }
            }

            objWriter.flush();
            objWriter.close();

            int sortOrder[] = new int[]{1, 0};
            char sortTypes[] = getSortOrder();
            FileSort sort = new FileSort("\t", sortOrder, sortTypes);
            sort.sort(strFileName, strSortedOutFile);
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    protected char[] getSortOrder() {

        if (this.isAscendingSortOrder) {
            return new char[]{'I', 'i'};
        }

        return new char[]{'i', 'I'};
    }
}
