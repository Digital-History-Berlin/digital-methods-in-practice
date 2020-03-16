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
import eu.etrap.medusa.config.ConfigurationException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import eu.etrap.tracer.selection.Selection;

/**
 * Created on 08.12.2010 13:59:57 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class LocalWinnowingSelectorImpl extends AbstractLocalSelection implements Selection {

    int intWinnowingWindowSize = 0;

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strTaxonomyCode = "01-02-02-01-02";
        transformFeatureDensity2WinnowingWindow();
    }

    protected void transformFeatureDensity2WinnowingWindow() {
        double dblWinnowingWindow = 2 / dblFeatureDensity - 1.0;
        intWinnowingWindowSize = (int) Math.round(Math.ceil(dblWinnowingWindow));
        
        // TODO: CHECK this output
        /*ConfigurationContainer.println("\tTransformed feature density fd="
                + this.formatFeatureDensity(dblFeatureDensity) + " to a winnowing window of "
                + intWinnowingWindowSize);*/
    }

    @Override
    protected LinkedHashSet<String> doSelect(String strRUID, LinkedHashSet<String> objDataEntries) {

        if (objDataEntries.size() < intWinnowingWindowSize) {
            LinkedHashSet<String> objSelectedDataEntries = this.processSmallerReuseUnitesThanWinnowingSize(objDataEntries);
            return this.processSmallerReuseUnitesThanWinnowingSize(objDataEntries);
        }

        Object strData[] = objDataEntries.toArray();

        LinkedHashSet<String> objSelectedDataEntries = new LinkedHashSet<String>();

        for (int i = 0; i <= strData.length - intWinnowingWindowSize; i++) {

            int intWindowMax = 0;
            int intWindowMaxIndex = 0;
            for (int j = i; j < i + this.intWinnowingWindowSize; j++) {
                if (j < objDataEntries.size()) {
                    String strSplit[] = ((String) strData[j]).split("\t");
                    int intFeatureID = Integer.parseInt(strSplit[0].trim());

                    if (intFeatureID >= intWindowMax) {
                        intWindowMax = intFeatureID;
                        intWindowMaxIndex = j;
                    }
                }
            }
            objSelectedDataEntries.add((String) strData[intWindowMaxIndex]);

        }

        return objSelectedDataEntries;
    }

    protected LinkedHashSet<String> processSmallerReuseUnitesThanWinnowingSize(LinkedHashSet<String> objDataEntries) {

        LinkedHashSet<String> objSelectedDataEntries = new LinkedHashSet<String>();

        Iterator<String> objIter = objDataEntries.iterator();

        int intMaxWordID = 0;
        String strMaxFeatData = null;

        while (objIter.hasNext()) {
            String strData = objIter.next();
            String strSplit[] = strData.split("\t");
            int intCurrentWordID = Integer.parseInt(strSplit[0]);

            if (intCurrentWordID >= intMaxWordID) {
                intMaxWordID = intCurrentWordID;
                strMaxFeatData = strData;
            }
        }

        objSelectedDataEntries.add(strMaxFeatData);

        return objSelectedDataEntries;
    }
}
