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

import bak.pcj.IntIterator;
import bak.pcj.list.IntList;
import bak.pcj.list.IntArrayList;
import eu.etrap.medusa.config.ConfigurationException;
import java.util.ArrayList;
import eu.etrap.tracer.scoring.Scoring;
import eu.etrap.tracer.scoring.WordClassWeightStore;

/**
 * Created on 07.12.2010 16:57:22 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class SelectedFeatureWordClass2WeightedOverlapImpl extends AbstractSymmetricSelectedFeatureScoring implements Scoring {

    private WordClassWeightStore objStore = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strTaxonomyCode = "02-02-01-01-02-03";

        objStore = new WordClassWeightStore();

        try {
            objStore.init();
        } catch (Exception ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    protected double weight(ArrayList<Integer> objFingerprint1, ArrayList<Integer> objFingerprint2) {

        // computer overlap
        double dblResult = 0;
        IntList objWIDs = new IntArrayList();

        IntList objOverlap = this.overlap(objFingerprint1, objFingerprint2);

        IntIterator objIter = objOverlap.iterator();
        boolean containsVerb = true;
        
        while (objIter.hasNext()) {
            int intFeatureID = objIter.next();

            // weight feature
            double dblFeatureWeight = objStore.getFeatureWeight(intFeatureID);

            dblResult += dblFeatureWeight;

            // bestimme die Menge der Wörter/Wort-ID's
            String strFeats = objStore.getFMAP4FID(intFeatureID);
            String aryWords[] = strFeats.split(" ");

            for (int i = 0; i < aryWords.length; i++) {
                int intWID = Integer.parseInt(aryWords[i]);
                objWIDs.add( intWID );
                boolean isVerb = objStore.isVerb(intWID);

                if( isVerb ){
                   containsVerb = true;
                }

            }

            // TODO: READ FEATS
            // TODO: GET POS WORD POS TAG
        }

        boolean boolScoreDown= false;

        if( objOverlap.size() < 2 ){
            boolScoreDown=true;
        }

        double dblLocalityScore = (double)objWIDs.size()/(double)(objOverlap.size()+1);
        if( dblLocalityScore > 1.3 ){
            boolScoreDown=true;
        }

        if( containsVerb == false ){
            boolScoreDown=true;
        }

        if( boolScoreDown ){
           dblResult = dblResult * (-1);
        }

        return dblResult;
    }
}
