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
package eu.etrap.tracer.utils;

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.File;
import java.text.DecimalFormat;
import eu.etrap.tracer.linking.Linking;
import eu.etrap.tracer.scoring.Scoring;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.featuring.Training;
import eu.etrap.tracer.postprocessing.Postprocessing;

/**
 * Created on 01.03.2011 09:17:27 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class FileManager {

    public static String getStringSimilarWordFileName() {
        return FileManager.getOriginalCorpusFileName() + ".ssim";
    }

    public static String getTracesHome() {
        return "TRACER_DATA";
    }

    public static String getCleanedStringSimilarWordFileName() {
        return FileManager.getStringSimilarWordFileName() + "."
                + FileManager.getPreparedFileNameSuffix();
    }

    public static String getSynonymWordFileName() {
        return ConfigurationContainer.getGeneralCategory().getProperty("SYNONYMS_FILE_NAME");
    }

    // containing only these word forms - out of a bigger list - that really
    // are part of this corpus
    public static String getReducedSynonymFileName() {
        return ConfigurationContainer.getSentenceFileName() + ".sim";
    }

    public static String getCleanedSynonymWordFileName() {
        return FileManager.getReducedSynonymFileName() + "."
                + FileManager.getPreparedFileNameSuffix();
    }

    public static String getOriginalCorpusFileName() {
        return ConfigurationContainer.getGeneralCategory().getProperty("ORIGINAL_CORPUS");
    }

    public static String getOriginalTokenizedCorpusFileName() {
        return ConfigurationContainer.getGeneralCategory().getProperty("ORIGINAL_CORPUS") + ".tok";
    }

    public static String getCharDistFileNameSuffix() {
        return "dist.char";
    }

    public static String getCharDistFileName() {
        return FileManager.getOriginalTokenizedCorpusFileName()
                + "." + FileManager.getCharDistFileNameSuffix();
    }

    public static String getCharNGramDistFileNameSuffix(int intNGramSize) {
        return "dist.letter." + new DecimalFormat("00").format(intNGramSize) + "gram";
    }

    public static String getCharNGramDistFileName(int intNGramSize) {
        return FileManager.getOriginalTokenizedCorpusFileName()
                + "." + FileManager.getCharNGramDistFileNameSuffix(intNGramSize);
    }

    public static String getLengthReducedWordsFileName(int intNGramSize, boolean weigthByLogLikelihoodRatio) {
        return FileManager.getOriginalTokenizedCorpusFileName()
                + "." + FileManager.getLengthReducedWordsFileNameSuffix(intNGramSize, weigthByLogLikelihoodRatio);
    }

    public static String getCleanedLengthReducedWordsFileName(int intNGramSize, boolean weigthByLogLikelihoodRatio) {
        return FileManager.getLengthReducedWordsFileName(intNGramSize, weigthByLogLikelihoodRatio)
                + "." + FileManager.getPreparedFileNameSuffix();
    }

    public static String getLengthReducedWordsFileNameSuffix(int intNGramSize, boolean weigthByLogLikelihoodRatio) {
        return "red.words." + new DecimalFormat("00").format(intNGramSize) + "." + weigthByLogLikelihoodRatio;
    }

    public static String getPositionalInvertedListFileName() {
        return ConfigurationContainer.getSentenceFileName() + ".inv";
    }

    public static String getPreparedFileNameSuffix() {
        return "prep";
    }

    // containing only these word forms - out of a bigger list - that really
    // are part of this corpus
    public static String getReducedBaseformFileName() {
        return FileManager.getOriginalTokenizedCorpusFileName() + ".base";
    }

    // removed entries having more than one baseform
    // an edge weighting algorithm is used for that
    public static String getCleanedBaseformFileName() {
        return FileManager.getReducedBaseformFileName() + "."
                + FileManager.getPreparedFileNameSuffix();
    }

    public static String getTrainingHomeFolder() throws ConfigurationException {
        File objFile = new File(ConfigurationContainer.getGeneralCategory().getProperty("PROCESSED_CORPUS_FILE_NAME"));
        String strFolderName = objFile.getParent();

        Training objTraining
                = ClassLoader.loadTrainingImpl(ConfigurationContainer.getGeneralCategory().getProperty("TRAINING_IMPL"));

        String strOutputFolder = strFolderName + "/" + objTraining.getFolderName();
        objTraining = null;

        File objOutputFolder = new File(strOutputFolder);

        if (!objOutputFolder.exists()) {
            objOutputFolder.mkdirs();
        }

        return strOutputFolder;
    }

    public static String getCorpusFileName() {
        File objFile = new File(ConfigurationContainer.getGeneralCategory().getProperty("PROCESSED_CORPUS_FILE_NAME"));
        return objFile.getName().replace(".prep", "");

    }

    public static String getTrainingFeatsFileName() throws ConfigurationException {
        String strFolderName = getTrainingHomeFolder();
        String strCorpusName = getCorpusFileName();

        return strFolderName + "/" + strCorpusName + ".feats";
    }

    public static String getTrainingSortedTrainFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".sorted";
    }

    public static String getTrainingTrainFileName() throws ConfigurationException {
        String strFolderName = getTrainingHomeFolder();
        String strCorpusName = getCorpusFileName();

        return strFolderName + "/" + strCorpusName + ".train";
    }

    public static String getTrainingFMAPFileName() throws ConfigurationException {
        String strFolderName = getTrainingHomeFolder();
        String strCorpusName = getCorpusFileName();

        return strFolderName + "/" + strCorpusName + ".fmap";
    }

    public static String getTrainingMetaFileName() throws ConfigurationException {
        String strFolderName = getTrainingHomeFolder();
        String strCorpusName = getCorpusFileName();

        return strFolderName + "/" + strCorpusName + ".meta";
    }

    public static String getSelectionEntropyFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".entropy";
    }

    public static String getSelectionSortedEntropyFileName() throws ConfigurationException {
        return getSelectionEntropyFileName() + ".sorted";
    }

    public static String getSelectionSelfInformationFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".selinf";
    }

    public static String getSelectionSortedSelfInformationFileName() throws ConfigurationException {
        return getSelectionSelfInformationFileName() + ".sorted";
    }

    public static String getSelectionIDFFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".idf";
    }

    public static String getSelectionSortedIDFFileName() throws ConfigurationException {
        return getSelectionIDFFileName() + ".sorted";
    }

    public static String getSelectionFrequencyClassFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".fcl";
    }

    public static String getSelectionSortedFrequencyClassFileName() throws ConfigurationException {
        return getSelectionFrequencyClassFileName() + ".sorted";
    }

    public static String getSelectionWordClassFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".pos";
    }

    public static String getSelectionSortedWordClassFileName() throws ConfigurationException {
        return getSelectionWordClassFileName() + ".sorted";
    }

    public static String getSelectionICFFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".icf";
    }

    public static String getSelectionSortedICFFileName() throws ConfigurationException {
        return getSelectionICFFileName() + ".sorted";
    }

    public static String getSelectionFFICFFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".fficf";
    }

    public static String getSelectionSortedFFICFFileName() throws ConfigurationException {
        return getSelectionFFICFFileName() + ".sorted";
    }

    public static String getSelectionKullbackLeiblerDivergenceFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".kld";
    }

    public static String getSelectionSortedKullbackLeiblerDivergenceFileName() throws ConfigurationException {
        return getSelectionKullbackLeiblerDivergenceFileName() + ".sorted";
    }

    public static String getSelectionLogLikelihoodRatioFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".llr";
    }

    public static String getSelectionSortedLogLikelihoodRatioFileName() throws ConfigurationException {
        return getSelectionLogLikelihoodRatioFileName() + ".sorted";
    }

    public static String getSelectionFeatureRedundancyFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".red";
    }

    public static String getSelectionSortedFeatureRedundancyFileName() throws ConfigurationException {
        return getSelectionFeatureRedundancyFileName() + ".sorted";
    }

    public static String getSelectionWordLengthFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".wole";
    }

    public static String getSelectionSortedWordLengthFileName() throws ConfigurationException {
        return getSelectionWordLengthFileName() + ".sorted";
    }

    public static String getSelectionWordFrequencyFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".wordfreq";
    }

    public static String getSelectionAscSortedWordFrequencyFileName() throws ConfigurationException {
        return getSelectionWordFrequencyFileName() + ".asc.sorted";
    }

    public static String getSelectionDescSortedWordFrequencyFileName() throws ConfigurationException {
        return getSelectionWordFrequencyFileName() + ".desc.sorted";
    }

    public static String getSelectionWordGraphDependenciesFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".graph.wgd";
    }

    public static String getSelectionSortedWordGraphDependenciesFileName() throws ConfigurationException {
        return getSelectionWordGraphDependenciesFileName() + ".sorted";
    }

    public static String getSelectionContrastiveFeatureRelationFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".graph.cfr";
    }

    public static String getSelectionSortedContrastiveFeatureRelationFileName() throws ConfigurationException {
        return getSelectionContrastiveFeatureRelationFileName() + ".sorted";
    }

    public static String getSelectionWeightedWordGraphDependenciesFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".graph.wwgd";
    }

    public static String getSelectionSortedWeightedWordGraphDependenciesFileName() throws ConfigurationException {
        return getSelectionWeightedWordGraphDependenciesFileName() + ".sorted";
    }

    public static String getSelectionWeightedContrastiveFeatureRelationFileName() throws ConfigurationException {
        return getTrainingTrainFileName() + ".graph.wcfr";
    }

    public static String getSelectionSortedWeightedContrastiveFeatureRelationFileName() throws ConfigurationException {
        return getSelectionWeightedContrastiveFeatureRelationFileName() + ".sorted";
    }

    public static String getSelectionFileName(String strClassName) throws ConfigurationException {
        String strTrainFile = getTrainingTrainFileName();
        String strParentFolder = new File(strTrainFile).getParent();
        String strTrainFileName = new File(strTrainFile).getName();

        String strOutDir = strParentFolder + "/"
                + ClassLoader.loadSelectionImpl(strClassName).getFullFolderName();

        if (!new File(strOutDir).exists()) {
            new File(strOutDir).mkdirs();
        }

        String strOutFileName = strOutDir + "/" + strTrainFileName.replace(".train", ".sel");

        return strOutFileName;
    }

    public static String getMultipleSelectionFileName() throws ConfigurationException {
        String strTrainFile = getTrainingTrainFileName();

        String strOutDir = determineSelectionOutDir(strTrainFile);

        if (!new File(strOutDir).exists()) {
            new File(strOutDir).mkdirs();
        }

        String strTrainFileName = new File(strTrainFile).getName();
        String strOutFileName = strOutDir + "/" + strTrainFileName.replace(".train", ".sel");

        return strOutFileName;
    }

    protected static String determineSelectionOutDir(String strTrainingInFile) throws ConfigurationException {
        String strTraingDir = new File(strTrainingInFile).getParent();

        String strSelections = ConfigurationContainer.getGeneralCategory().getProperty("SELECTION_IMPL");
        String strSelectionImplementations[] = strSelections.split(",");
        String strTaxonomyCode[] = new String[strSelectionImplementations.length];

        String strSelectionFolderName = "";
        for (int i = 0; i < strSelectionImplementations.length; i++) {
            Selection objSelection = ClassLoader.loadSelectionImpl(strSelectionImplementations[i].trim());
            objSelection.init();
            strSelectionFolderName += objSelection.getFolderName() + "-";
            strTaxonomyCode[i] = objSelection.getTaxonomyCode();
        }

        String strResultString = strTraingDir + "/"
                + getMultipleSelectionTaxonomyCode(strTaxonomyCode) + "-"
                + strSelectionFolderName.substring(0, strSelectionFolderName.length() - 1);

        return strResultString;
    }

    protected static String getMultipleSelectionTaxonomyCode(String strTaxonomyCode[]) {
        String strReturnTaxCode[] = new String[]{"02", "00", "00", "00", "00"};

        for (int i = 1; i < 5; i++) {
            String strLevelCode = strTaxonomyCode[0].split("-")[i];
            boolean isTheSameLevelCode = true;

            for (int j = 1; j < strTaxonomyCode.length; j++) {
                String strTmpLevelCode = strTaxonomyCode[j].split("-")[i];

                if (!strLevelCode.equals(strTmpLevelCode)) {
                    isTheSameLevelCode = false;
                }
            }

            if (isTheSameLevelCode == true) {
                strReturnTaxCode[i] = strLevelCode;
            } else {
                break;
            }
        }

        String strResultCode = "";
        for (int i = 0; i < 5; i++) {
            strResultCode += strReturnTaxCode[i] + "-";
        }
        strResultCode = strResultCode.substring(0, strResultCode.length() - 1);

        return strResultCode;

    }

    public static String getLinkingFileName() throws ConfigurationException {
        String strResult = FileManager.getMultipleSelectionFileName();

        String strParentFolder = new File(strResult).getParent();

        String strLinkingImpl = ConfigurationContainer.getGeneralCategory().getProperty("LINKING_IMPL");
        Linking objLinking = ClassLoader.loadLinkingImpl(strLinkingImpl);
        String strLinkingFolderName = objLinking.getFullFolderName();

        String strOutDir = strParentFolder + "/" + strLinkingFolderName;

        if (!new File(strOutDir).exists()) {
            new File(strOutDir).mkdirs();
        }

        strResult = strOutDir + "/" + objLinking.getFolderName() + ".link";

        return strResult;
    }

    public static String getLinkingMetaFileName() throws ConfigurationException {
        String strResult = FileManager.getLinkingFileName();
        return strResult.replace(".link", ".meta");
    }

    public static String getScoringFileName() throws ConfigurationException {
        String strResult = FileManager.getLinkingFileName();

        String strParentFolder = new File(strResult).getParent();
        String strScoringFileName = new File(strResult).getName();

        String strLinkingImpl = ConfigurationContainer.getGeneralCategory().getProperty("SCORING_IMPL");
        Scoring objScoring = ClassLoader.loadScoringImpl(strLinkingImpl);
        objScoring.init();
        String strScoringFolderName = objScoring.getFullFolderName();

        String strOutDir = strParentFolder + "/" + strScoringFolderName;

        if (!new File(strOutDir).exists()) {
            new File(strOutDir).mkdirs();
        }

        strResult = strOutDir + "/" + strScoringFileName.replace(".link", ".score");

        return strResult;
    }

    public static String getScoringMetaFileName() throws ConfigurationException {
        String strResult = getScoringFileName();
        strResult = strResult.replace(".score", ".meta");

        return strResult;
    }

    public static String getPostprocessingFolderName() throws ConfigurationException {
        String strResult = FileManager.getScoringFileName();

        String strParentFolder = new File(strResult).getParent();
        
        String strPostprocessingImpl = ConfigurationContainer.getGeneralCategory().getProperty("POSTPROCESSING_IMPL");
        Postprocessing objPostprocesing = ClassLoader.loadPostprocessingImpl(strPostprocessingImpl);
        objPostprocesing.init();

        String strPostprocessingFolderName = objPostprocesing.getFullFolderName();

        String strOutDir = strParentFolder + "/" + strPostprocessingFolderName;

        if (!new File(strOutDir).exists()) {
            new File(strOutDir).mkdirs();
        }

        
        return strOutDir;
    }

    public static String getPostprocessingFileName() throws ConfigurationException {
        String strResult = FileManager.getScoringFileName();

        String strParentFolder = new File(strResult).getParent();
        String strPostprocessingFileName = new File(strResult).getName();

        String strPostprocessingImpl = ConfigurationContainer.getGeneralCategory().getProperty("POSTPROCESSING_IMPL");
        Postprocessing objPostprocesing = ClassLoader.loadPostprocessingImpl(strPostprocessingImpl);
        objPostprocesing.init();

        String strPostprocessingFolderName = objPostprocesing.getFullFolderName();

        String strOutDir = strParentFolder + "/" + strPostprocessingFolderName;

        if (!new File(strOutDir).exists()) {
            new File(strOutDir).mkdirs();
        }

        strResult = strOutDir + "/" + strPostprocessingFileName.replace(".score", ".pproc");

        return strResult;
    }

    public static String getPostprocessingMetaFileName() throws ConfigurationException {
        String strResult = getPostprocessingFileName();
        strResult = strResult.replace(".pproc", ".meta");

        return strResult;
    }

    public static String getTRVHTMLpackageFolderName() throws ConfigurationException {
        return "build/trv/html";
    }
}
