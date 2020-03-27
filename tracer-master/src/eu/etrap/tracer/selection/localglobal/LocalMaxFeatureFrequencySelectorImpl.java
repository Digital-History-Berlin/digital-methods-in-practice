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

import bak.pcj.map.IntKeyIntOpenHashMap;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 08.12.2010 13:58:26 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class LocalMaxFeatureFrequencySelectorImpl extends AbstractLocalGlobalSelection implements Selection {

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strGlobalSelectorImpl = "eu.etrap.tracer.selection.globalglobal.GlobalMaxFeatureFrequencySelectorImpl";
        strFeatureWeightFile = FileManager.getSelectionAscSortedWordFrequencyFileName();
        strTaxonomyCode = "01-02-01-01-01";
    }

    @Override
        protected void loadFeatureWeights() throws FileNotFoundException, IOException {
        ConfigurationContainer.print("\tLoading feature weights from file "
                + strFeatureWeightFile + " ... ");
        objFeatureWeights = new IntKeyIntOpenHashMap();
        BufferedReader objReader = new BufferedReader(new FileReader(strFeatureWeightFile));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objFeatureWeights.put(Integer.parseInt(strSplit[0].trim()), 1000000000 - Integer.parseInt(strSplit[1].trim()));
        }

        objReader.close();
        ConfigurationContainer.println("DONE! " + objFeatureWeights.size() + " feature weights loaded.");
    }
}
