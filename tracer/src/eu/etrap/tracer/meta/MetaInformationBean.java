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

/**
 * Created on 28.03.2011 13:20:12 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class MetaInformationBean {

    public String strOrigKorpusFile = null;
    public String strPreprocessingFolder = null;
    public String strTrainingFolder = null;
    public String strSelectionFolder = null;
    public String strLinkingFolder = null;
    public String strScoringFolder = null;
    public String strPostprocessingFolder = null;

    public MetaInformation objMetaInformation = null;

    
    public String serializeToKey() {
        String strKey = strOrigKorpusFile;

        if (strPreprocessingFolder == null) {
            return strKey;
        }
        strKey += "\t" + strPreprocessingFolder;

        if (strTrainingFolder == null) {
            return strKey;
        }
        strKey += "\t" + strTrainingFolder;

        if (strSelectionFolder == null) {
            return strKey;
        }
        strKey += "\t" + strSelectionFolder;

        if (strLinkingFolder == null) {
            return strKey;
        }
        strKey += "\t" + strLinkingFolder;

        if (strScoringFolder == null) {
            return strKey;
        }
        strKey += "\t" + strScoringFolder;

        if (strPostprocessingFolder == null) {
            return strKey;
        }
        strKey += "\t" + strPostprocessingFolder;

        return strKey;
    }
}
