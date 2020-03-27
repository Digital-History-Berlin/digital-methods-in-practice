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


package eu.etrap.tracer.selection.globalglobal.graph;

import bak.pcj.set.IntSet;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 18.04.2011 12:54:28 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class GlobalWeightedWordGraphDependenciesSelectionImpl extends AbstractGlobalGraphSelection{
    @Override
    public void init() throws ConfigurationException {
        super.init();
        strOutFileName = FileManager.getSelectionWeightedWordGraphDependenciesFileName();
        this.strSortedOutFile = FileManager.getSelectionSortedWeightedWordGraphDependenciesFileName();
        intScaleFactor = Constants.WORD_DEPENDENCY_GRAPH_SCALEFACTOR;
        strTaxonomyCode = "01-01-01-06-02";
    }

    @Override
    protected double doWeightFeatures(IntSet objFeatures1, IntSet objFeatures2, int intFeatCoccFreq, int intOverlap) {
        // compute graph based similarity
        double dblSim = super.doWeightFeatures(objFeatures1, objFeatures2, intFeatCoccFreq, intOverlap);
        double dblScore = dblSim*intOverlap;
        return dblScore;
    }
}
