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


package eu.etrap.tracer.tutorial;

import eu.etrap.tracer.AbstractTracerFrameWork;
import eu.etrap.tracer.TracerException;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.featuring.TrainingException;
import eu.etrap.tracer.linking.LinkingException;
import eu.etrap.tracer.meta.MetaInformationException;
import eu.etrap.tracer.preprocessing.PreprocessingException;
import eu.etrap.tracer.preprocessing.WordGraphPreparer;
import eu.etrap.tracer.scoring.ScoringException;
import eu.etrap.tracer.selection.SelectionException;

/**
 * Created on 14.12.2010 07:11:54 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class L2FrameWorkImpl extends AbstractTracerFrameWork{

       @Override
       public void process() throws TracerException, PreprocessingException, TrainingException, MetaInformationException, SelectionException, LinkingException, ScoringException {
        // processing all pre-requirements
        this.checkPreRequirements();


        // LEVEL 1: do pre-processing
        ConfigurationContainer.println("\n\n\nSTART TO PROCESS LEVEL 1 (PRE-PROCESSING)");

        // e. .g. baseforms, string similar words, synonyms
        WordGraphPreparer objPrep = new WordGraphPreparer();
        objPrep.prepare();
        objPrep = null;

        String strPreprocessedCorpus = doPreprocessing();
        this.setGlobalProperty("PROCESSED_CORPUS_FILE_NAME", strPreprocessedCorpus);
        doCreateInvertedList();

        ConfigurationContainer.println("\nEND OF PROCESS LEVEL 1 (PRE-PROCESSING)");



        // LEVEL 2: do training
        ConfigurationContainer.println("\n\n\nSTART TO PROCESS LEVEL 2 (TRAINING)");
        doTraining();
        ConfigurationContainer.println("\nEND OF PROCESS LEVEL 2 (TRAINING)");
    }
    
}
