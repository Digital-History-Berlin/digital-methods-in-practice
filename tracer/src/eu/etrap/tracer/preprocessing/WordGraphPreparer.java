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


package eu.etrap.tracer.preprocessing;

import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.File;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.TracerException;
import eu.etrap.tracer.preprocessing.graph.BaseFormWordGraphPreprocessingImpl;
import eu.etrap.tracer.preprocessing.graph.LengthReducedWordGraphPreprocessingImpl;
import eu.etrap.tracer.preprocessing.graph.StringSimilarWordGraphPreprocessingImpl;
import eu.etrap.tracer.preprocessing.graph.SynonymWordGraphPreprocessingImpl;
import eu.etrap.tracer.preprocessing.graph.WordGraphPreprocessing;

import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 04.03.2011 12:30:52 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class WordGraphPreparer extends ClassConfig {

    public void init() throws ConfigurationException {
        super.config();
    }

    public void prepare() throws TracerException {


        // todo: entscheide, ob die impl aus WordLevel kommt
        // todo: lade dann alle boolProperties
        String strPreprocessingImpl = this.getGlobalProperty("PREPROCESSING_IMPL");
        
        boolean boolLemmatisation = Boolean.parseBoolean(getProperty("boolLemmatisation", strPreprocessingImpl));
        boolean boolReplaceSynonyms = Boolean.parseBoolean(getProperty("boolReplaceSynonyms", strPreprocessingImpl));
        boolean boolReplaceStringSimilarWords = Boolean.parseBoolean(getProperty("boolReplaceStringSimilarWords", strPreprocessingImpl));
        boolean boolReplaceByReducedString = Boolean.parseBoolean(getProperty("boolReplaceByReducedString", strPreprocessingImpl));

        if (boolLemmatisation && !new File(FileManager.getCleanedBaseformFileName()).exists()) {
            prepareBaseformGraph();
        }

        if (boolReplaceStringSimilarWords && !new File(FileManager.getCleanedStringSimilarWordFileName()).exists()) {
            prepareStringSimilarityGraph();
        }

        if (boolReplaceSynonyms && !new File(FileManager.getCleanedSynonymWordFileName()).exists()) {
            prepareSynonymGraph();
        }


        if (boolReplaceByReducedString) {

            for (int i = 2; i <= Constants.intMaxNgramLength; i++) {
                if (!new File(FileManager.getCleanedLengthReducedWordsFileName(i, true)).exists()) {
                    prepareLengthReducedWordGraph(i, true);
                }

                if (!new File(FileManager.getCleanedLengthReducedWordsFileName(i, false)).exists()) {
                    prepareLengthReducedWordGraph(i, false);
                }
            }
        }
    }

    private void prepareBaseformGraph() throws TracerException {
        ConfigurationContainer.println("\nPreprocessing baseform graph in "
                + ConfigurationContainer.getGeneralCategory().getProperty("BASEFORM_FILE_NAME") + " ...");

        WordGraphPreprocessing objPrepare = new BaseFormWordGraphPreprocessingImpl();
        try {
            objPrepare.init();
        } catch (ConfigurationException e) {
            throw new TracerException(e);
        }

        objPrepare.preprocessing();

        ConfigurationContainer.println("Preprocessing baseform graph is finished and stored in " + FileManager.getCleanedBaseformFileName() + " ...\n");
    }

    private void prepareStringSimilarityGraph() throws TracerException {
        ConfigurationContainer.println("\nPreprocessing string similarity graph in "
                + FileManager.getStringSimilarWordFileName() + " ...");

        WordGraphPreprocessing objPrepare = new StringSimilarWordGraphPreprocessingImpl();
        try {
            objPrepare.init();
        } catch (ConfigurationException e) {
            throw new TracerException(e);
        }

        objPrepare.preprocessing();

        ConfigurationContainer.println("Preprocessing string similarity graph is finished and stored in "
                + FileManager.getCleanedStringSimilarWordFileName() + " ...\n");
    }

    private void prepareSynonymGraph() throws TracerException {
        ConfigurationContainer.println("\nPreprocessing synonym graph in "
                + FileManager.getSynonymWordFileName() + " ...");

        WordGraphPreprocessing objPrepare = new SynonymWordGraphPreprocessingImpl();

        try {
            objPrepare.init();
        } catch (ConfigurationException e) {
            throw new TracerException(e);
        }

        objPrepare.preprocessing();

        ConfigurationContainer.println("Preprocessing synonym graph is finished and stored in "
                + FileManager.getCleanedSynonymWordFileName() + " ...\n");
    }

    private void prepareLengthReducedWordGraph(int intNGramSize, boolean weigthByLogLikelihoodRatio) throws TracerException {
        ConfigurationContainer.println("Preprocessing length reduced graph with ngram size " + intNGramSize
                + " and weigthByLogLikelihoodRatio=" + weigthByLogLikelihoodRatio + " ...");

        this.setProperty("intNGramSize", "" + intNGramSize,
                "eu.etrap.tracer.preprocessing.graphs.LengthReducedWordGraphPreprocessingImpl");
        this.setProperty("weigthByLogLikelihoodRatio", "" + weigthByLogLikelihoodRatio,
                "eu.etrap.tracer.preprocessing.graphs.LengthReducedWordGraphPreprocessingImpl");

        WordGraphPreprocessing objPrepare = new LengthReducedWordGraphPreprocessingImpl();

        try {
            objPrepare.init();
        } catch (ConfigurationException e) {
            throw new TracerException(e);
        }

        objPrepare.preprocessing();

        ConfigurationContainer.println("Preprocessing length reduced graph is finished and stored in "
                + FileManager.getCleanedLengthReducedWordsFileName(intNGramSize, weigthByLogLikelihoodRatio) + " ...\n");
    }
}
