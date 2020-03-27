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



package eu.etrap.tracer.linking;

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.utils.ClassLoader;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 08.12.2010 13:10:47 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractIntraCorpusLinking extends AbstractLinking {

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strTaxonomyCode = "01";

        this.strRUID2FeatureFile = FileManager.getMultipleSelectionFileName();
        this.strFeature2RUIDFile = FileManager.getMultipleSelectionFileName();

    }

    @Override
    protected void initRUID2Feat() throws ConfigurationException {
        this.objRUID2FeatureConnector = ClassLoader.loadConnectorImpl(this.strRUID2FeatureImplementation);
        objRUID2FeatureConnector.init();
    }

    @Override
    protected void initFeat2RUID() throws ConfigurationException {
        this.objFeature2RUIDConnector = ClassLoader.loadConnectorImpl(this.strFeature2RUIDImplementation);
        this.objFeature2RUIDConnector.init();
    }

    @Override
    public void prepareData() throws LinkingException {
        ConfigurationContainer.println("\tPreparing RUID2Feature connector by implementation in " + strRUID2FeatureImplementation);
        objRUID2FeatureConnector.prepareData(strRUID2FeatureFile);
        this.arySelStats[Constants.NUMBER_OF_FINGERPRINTED_RUID] = objRUID2FeatureConnector.getAllIDs().length;
        ConfigurationContainer.println("\tDONE!\n");

        ConfigurationContainer.println("\tPreparing Feature2RUID connector by implementation in " + strFeature2RUIDImplementation);
        objFeature2RUIDConnector.prepareData(strFeature2RUIDFile);
        this.arySelStats[Constants.NUMBER_OF_FINGERPRINT_FEATURES] = objFeature2RUIDConnector.getAllIDs().length;
        ConfigurationContainer.println("\tDONE!\n");
    }
}
