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



package eu.etrap.tracer.meta;

import java.util.TreeMap;
import eu.etrap.tracer.Constants;

/**
 * Created on 28.03.2011 12:07:39 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class MetaInformationFactory {

    private static TreeMap<String, MetaInformation> objAlreadyLoadedMetaInformation = null;
    private static boolean isInitialized = false;

    private static void init() {
        if (!isInitialized) {
            isInitialized = true;
            objAlreadyLoadedMetaInformation = new TreeMap<String, MetaInformation>();
        }

    }

    public static MetaInformation createMetaInformationObject(MetaInformationBean objMIBean, int MetaInformationType)
            throws MetaInformationException {

        init();

        switch (MetaInformationType) {
            case Constants.TYPE_REQUIREMENTS:
                objMIBean.objMetaInformation = createRequirementsMetaInformation(objMIBean, MetaInformationType);
                return objMIBean.objMetaInformation;

            case Constants.TYPE_PREPROCESING:
                objMIBean.objMetaInformation = createPreprocesingMetaInformation(objMIBean, MetaInformationType);
                return objMIBean.objMetaInformation;

            case Constants.TYPE_TRAINING:
                objMIBean.objMetaInformation = createTrainingMetaInformation(objMIBean, MetaInformationType);
                return objMIBean.objMetaInformation;

            default:
                throw new MetaInformationException("Unsuported constant "
                        + MetaInformationType + " as Constants.TYPE_*");
        }
    }

    private static MetaInformation createRequirementsMetaInformation(MetaInformationBean objMIBean, int MetaInformationType)
            throws MetaInformationException {
        String strKey = MetaInformationType + "\t" + objMIBean.strOrigKorpusFile;
        MetaInformation objMeta = objAlreadyLoadedMetaInformation.get(strKey);

        if (objMeta == null) {
            objMeta = new RequirementsMetaInformationHandler();
            objMeta.init(objMIBean);
            objAlreadyLoadedMetaInformation.put(strKey, objMeta);
        }

        return objMeta;
    }

    private static MetaInformation createPreprocesingMetaInformation(MetaInformationBean objMIBean, int MetaInformationType)
            throws MetaInformationException {
        String strKey = MetaInformationType + "\t" + objMIBean.strOrigKorpusFile
                + "\t" + objMIBean.strPreprocessingFolder;
        MetaInformation objMeta = objAlreadyLoadedMetaInformation.get(strKey);

        if (objMeta == null) {
            objMeta = new PreprocessingMetaInformationHandler();
            objMeta.init(objMIBean);
            objAlreadyLoadedMetaInformation.put(strKey, objMeta);
        }

        return objMeta;
    }

    private static MetaInformation createTrainingMetaInformation(MetaInformationBean objMIBean, int MetaInformationType)
            throws MetaInformationException {
        String strKey = MetaInformationType + "\t" + objMIBean.strOrigKorpusFile
                + "\t" + objMIBean.strPreprocessingFolder
                + "\t" + objMIBean.strTrainingFolder;
        MetaInformation objMeta = objAlreadyLoadedMetaInformation.get(strKey);

        if (objMeta == null) {
            objMeta = new TrainingMetaInformationHandler();
            objMeta.init(objMIBean);
            objAlreadyLoadedMetaInformation.put(strKey, objMeta);
        }

        return objMeta;
    }

    private static MetaInformation createSelectionMetaInformation(MetaInformationBean objMIBean, int MetaInformationType)
            throws MetaInformationException {
        String strKey = MetaInformationType + "\t" + objMIBean.strOrigKorpusFile
                + "\t" + objMIBean.strPreprocessingFolder
                + "\t" + objMIBean.strTrainingFolder
                + "\t" + objMIBean.strSelectionFolder;
        MetaInformation objMeta = objAlreadyLoadedMetaInformation.get(strKey);

        if (objMeta == null) {
            objMeta = new TrainingMetaInformationHandler();
            objMeta.init(objMIBean);
            objAlreadyLoadedMetaInformation.put(strKey, objMeta);
        }

        return objMeta;
    }
}
