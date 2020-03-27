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


package eu.etrap.tracer.selection.localglobal.graph;

import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 27.04.2011 12:43:18 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class LocalWeightedContrastiveFeatureRelationSelectionImpl extends AbstractLocalGlobalGraphSelection implements Selection{
    @Override
    public void init() throws ConfigurationException {
        super.init();
        strGlobalSelectorImpl = "eu.etrap.tracer.selection.globalglobal.graph.GlobalWeightedContrastiveFeatureRelationSelectionImpl";
        strFeatureWeightFile = FileManager.getSelectionSortedWeightedContrastiveFeatureRelationFileName();
        strTaxonomyCode = "01-02-01-06-04";
    }
}
