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

import bak.pcj.set.IntOpenHashSet;
import bak.pcj.set.IntSet;
import eu.etrap.medusa.config.ConfigurationException;
import java.util.Random;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.selection.SelectionException;

/**
 * Created on 08.12.2010 13:59:44 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class Global0ModPSelectorImpl extends AbstractGlobalSelection implements Selection {

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strTaxonomyCode = "01-01-01-02-02";
    }

    @Override
    protected void buildByteArray() throws SelectionException {
        super.buildByteArray();


        int intAccuracy = (int) Math.round(Math.pow(10, getAccuracy(this.dblFeatureDensity)));
        int intNumberOfSelectedClasses = (int) Math.round(this.dblFeatureDensity * intAccuracy);

        IntSet objSelected = new IntOpenHashSet();
        Random objRandom = new Random(0);

        while (objSelected.size() < intNumberOfSelectedClasses) {
            objSelected.add(objRandom.nextInt(intAccuracy));
        }

        for (int i = this.intMinFeatID; i <= this.intMaxFeatID; i++) {
            int intFeatFreq = objFeatureDistribution.get(i);

            if (objSelected.contains(i % intAccuracy) && (intFeatFreq > 0)) {
                setSelectedFeature(i);
                intNumberOfAlreadySelectedTokens += intFeatFreq;
            }
        }
    }
}
