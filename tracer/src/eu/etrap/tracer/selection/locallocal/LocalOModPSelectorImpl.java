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

import eu.etrap.medusa.config.ConfigurationException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import eu.etrap.tracer.selection.Selection;

/**
 * Created on 08.12.2010 13:59:44 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class LocalOModPSelectorImpl extends AbstractLocalSelection implements Selection {

    protected Random objRandom = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();
        objRandom = new Random(0);
        strTaxonomyCode = "01-02-02-01-01";
    }

    @Override
    protected LinkedHashSet<String> doSelect(String strRUID, LinkedHashSet<String> objDataEntries) {
        int intNumberofSelectedFeatures = getNumberOfSelectedFeatures(objDataEntries.size());

        LinkedHashSet<String> objSelectedDataEntries = new LinkedHashSet<String>();

        double dblInvFeatureDensity = 1/this.dblFeatureDensity;
        int intP = (int)Math.round( Math.ceil(dblInvFeatureDensity) );

        // make 0 mod p
        Iterator<String> objIter = objDataEntries.iterator();
        while( objIter.hasNext() ){
             String strDataSet = objIter.next();
             String strSplit[] = strDataSet.split("\t");
             String strPosition = strSplit[2].trim();
             int intPos = Integer.parseInt( strPosition );

             if( intPos % intP == 0 ){
                objSelectedDataEntries.add( strDataSet );
             }
        }

        // fill randomly datasets to the threshold
        Object strData[] = objDataEntries.toArray();
        while (objSelectedDataEntries.size() < intNumberofSelectedFeatures) {
            int intPos = objRandom.nextInt(strData.length);
            objSelectedDataEntries.add((String) strData[intPos]);
        }
        
        return objSelectedDataEntries;
    }
}
