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


package eu.etrap.tracer.scoring.feature.selected.symmetric;

import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.scoring.Scoring;
import java.util.ArrayList;


/**
 * Created on 07.12.2010 16:57:22 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class SelectedFeatureResemblanceSimilarityImpl extends AbstractSymmetricSelectedFeatureScoring implements Scoring {

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strTaxonomyCode = "02-02-01-01-01-02";
    }

    @Override
    protected double weight(ArrayList<Integer> objFingerprint1, ArrayList<Integer> objFingerprint2) {
        // computer overlap
        double dblResult = 0;
        double dblOverlap = super.weight(objFingerprint1, objFingerprint2);
        dblResult += dblOverlap;
        dblResult /= ((double) objFingerprint1.size() + (double) objFingerprint2.size()
                - dblOverlap);

        return dblResult;
    }
}
