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


package eu.etrap.tracer.selection;

import bak.pcj.map.IntKeyIntMap;
import java.util.HashMap;

/**
 * Created on 04.04.2011 14:14:15 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class FeatureDistributionStore {

    static HashMap<String, IntKeyIntMap> objStore = null;

    static private void init() {
        if (objStore == null) {
            objStore = new HashMap<String, IntKeyIntMap>();
        }
    }

    static public void setDistribution(String strDistributionKey, IntKeyIntMap objDist) {
        init();
        objStore.put(strDistributionKey, objDist);
    }

    static public boolean containsDistribution(String strDistributionKey) {
        init();
        return objStore.containsKey(strDistributionKey);
    }

    static public IntKeyIntMap getDistribution(String strDistributionKey) {
        init();
        return objStore.get(strDistributionKey);
    }

    static public void removeDistribution(String strDistributionKey) {
        init();
        objStore.remove(strDistributionKey);
    }

    static public void clear() {
        objStore = null;
        init();
    }
}
