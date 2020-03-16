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
package eu.etrap.tracer;

import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.controlflow.ControlFlow;
import eu.etrap.medusa.controlflow.DefaultControlFlowImpl;
import eu.etrap.medusa.export.EmptyExportException;
import eu.etrap.medusa.utils.FileCopy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import eu.etrap.tracer.linking.Linking;
import eu.etrap.tracer.linking.LinkingException;
import eu.etrap.tracer.meta.MetaInformation;
import eu.etrap.tracer.meta.MetaInformationBean;
import eu.etrap.tracer.meta.MetaInformationException;
import eu.etrap.tracer.meta.MetaInformationFactory;

import eu.etrap.tracer.preprocessing.Preprocessing;
import eu.etrap.tracer.requirements.DefaultRequirementsControlFlowImpl;
import eu.etrap.tracer.utils.ClassLoader;
import eu.etrap.tracer.preprocessing.PreprocesingIOWrapper;
import eu.etrap.tracer.preprocessing.PreprocessingException;
import eu.etrap.tracer.preprocessing.WordGraphPreparer;
import eu.etrap.tracer.scoring.Scoring;
import eu.etrap.tracer.scoring.ScoringException;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.selection.SelectionProcessor;
import eu.etrap.tracer.featuring.Training;
import eu.etrap.tracer.featuring.TrainingException;
import eu.etrap.tracer.postprocessing.PostprocessingException;
import eu.etrap.tracer.postprocessing.Postprocessing;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 14.12.2010 06:50:06 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public abstract class AbstractTracerFrameWork extends ClassConfig implements TracerFramework {

    MetaInformationBean objPreprocessingBean = null;
    MetaInformationBean objTrainingBean = null;
    MetaInformationBean objSelectionBean = null;

    public void init() throws TracerException {
        try {
            super.config();
            String strOrigCorpus = getGlobalProperty("ORIGINAL_CORPUS");

            if (strOrigCorpus == null || strOrigCorpus.equals("")) {
                this.setGlobalProperty("ORIGINAL_CORPUS", ConfigurationContainer.getSentenceFileName());
                System.out.println("ORIGINAL_CORPUS=" + ConfigurationContainer.getSentenceFileName());
            }
        } catch (ConfigurationException e) {
            throw new TracerException(e);
        }
    }

    public void checkPreRequirements() throws TracerException {
        String strOrigKorpusFile = ConfigurationContainer.getSentenceFileName();

        try {
            ControlFlow objControlFlow = new DefaultRequirementsControlFlowImpl();

            objControlFlow.init();
            objControlFlow.start();
            objControlFlow = null;
        } catch (Throwable e) {
            throw new TracerException(e);
        }

        if (!new File(FileManager.getCharDistFileName()).exists()) {
            ConfigurationContainer.println("\nCreating character distribution ...");

            try {
                doCharacterCount();
            } catch (EmptyExportException e) {
                throw new TracerException(e);
            }

            ConfigurationContainer.println("Character distribution created.\n\n");
        } else {
            ConfigurationContainer.println("Character distribution file "
                    + FileManager.getCharDistFileName() + " is already computed.");
        }

        ConfigurationContainer.println("\nCreating a set of letter n-grams ...");

        int i = 0;

        try {
            for (i = 2; i <= Constants.intMaxNgramLength; i++) {
                if (!new File(FileManager.getCharNGramDistFileName(i)).exists()) {
                    ConfigurationContainer.println("\nProcessing letter n-grams of size " + i + " ...");
                    doNgramCount(i);
                    ConfigurationContainer.println("\nGeneration of letter n-grams of size " + i + " finished.");
                } else {
                    ConfigurationContainer.println("Character distribution file "
                            + FileManager.getCharNGramDistFileName(i) + " is already computed.");
                }
            }
        } catch (EmptyExportException e) {
            ConfigurationContainer.println("\nGeneration of letter n-grams stopped for n=" + i + ". No letter ngrams are found.");
            ConfigurationContainer.setSentenceFileName(strOrigKorpusFile);
        }
        ConfigurationContainer.println("\nGeneration of letter n-grams finished.\n\n");

    }

    public void process() throws TracerException, PreprocessingException, TrainingException, MetaInformationException, SelectionException, LinkingException, ScoringException, PostprocessingException {
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

        // LEVEL 3: do selection
        ConfigurationContainer.println("\n\n\nSTART TO PROCESS LEVEL 3 (SELECTION)");
        try {
            doSelection();
        } catch (ConfigurationException ex) {
            throw new SelectionException(ex);
        }
        ConfigurationContainer.println("\nEND OF PROCESS LEVEL 3 (SELECTION)");

        // LEVEL 4: do linking
        ConfigurationContainer.println("\n\n\nSTART TO PROCESS LEVEL 4 (LINKING)");
        try {
            this.doLinking();
        } catch (ConfigurationException ex) {
            throw new SelectionException(ex);
        }
        ConfigurationContainer.println("\nEND OF PROCESS LEVEL 4 (LINKING)");

        // LEVEL 5: do scoring
        ConfigurationContainer.println("\n\n\nSTART TO PROCESS LEVEL 5 (SCORING)");
        try {
            this.doScoring();
        } catch (ConfigurationException ex) {
            throw new SelectionException(ex);
        }

        ConfigurationContainer.println("\nEND OF PROCESS LEVEL 5 (SCORING)");

        // LEVEL 6: do postprocessing
        String strPostprocessing = getGlobalProperty("POSTPROCESSING_IMPL");

        // optional postprocessing
        if (strPostprocessing != null) {
            ConfigurationContainer.println("\n\n\nSTART TO PROCESS LEVEL 6 (POSTPROCESING)");
            try {
                this.doPostprocessing();
            } catch (ConfigurationException ex) {
                throw new SelectionException(ex);
            }

            ConfigurationContainer.println("\nEND OF PROCESS LEVEL 6 (POSTPROCESSING)");
        }
    }

    protected String doPreprocessing() throws PreprocessingException, TracerException {
        objPreprocessingBean = new MetaInformationBean();
        objPreprocessingBean.strOrigKorpusFile = ConfigurationContainer.getSentenceFileName();

        // Processing Level 1: Pre-processing
        String strLevel1Implementation = this.getGlobalProperty("PREPROCESSING_IMPL");
        ConfigurationContainer.println("\nUsing implementation " + strLevel1Implementation + " for preprocessing.");

        if (strLevel1Implementation == null) {
            throw new PreprocessingException("There is no implementation for \"PREPROCESSING_IMPL\" configured!");
        }

        Preprocessing objPreprocessing = null;

        try {
            objPreprocessing = ClassLoader.loadPreprocessingImpl(strLevel1Implementation);
        } catch (Exception e) {
            throw new PreprocessingException(e);
        }

        String strFolderName = null;

        try {
            strFolderName = objPreprocessing.getFolderName();
            String strFolder[] = strFolderName.split("/");
            objPreprocessingBean.strPreprocessingFolder = strFolder[strFolder.length - 1].trim();
        } catch (Exception e) {
            throw new PreprocessingException(e);
        }

        String strInFile = ConfigurationContainer.getTokenizedSentenceFileName();
        String strOutFile = strFolderName + "/" + new File(strInFile).getName().replace(".txt.tok", ".prep");

        if (!new File(strOutFile).exists()) {

            ConfigurationContainer.println("Storing data in foler " + strFolderName);

            File objDirectory = new File(strFolderName);

            if (!objDirectory.exists()) {
                objDirectory.mkdirs();
            }

            PreprocesingIOWrapper objIOWrapper = new PreprocesingIOWrapper();

            ConfigurationContainer.println("Preprocessing infile=" + strInFile + " to outfile="
                    + strOutFile);

            objIOWrapper.process(strInFile, strOutFile, objPreprocessing);

            objIOWrapper.getMetaInformationBean();
        } else {
            ConfigurationContainer.println("OUTPUT file outfile="
                    + strOutFile + " already exists");
        }

        objPreprocessing = null;

        return strOutFile;
    }

    protected void doTraining() throws TrainingException, MetaInformationException {
        // Processing Level 2: Training
        String strLevel2Implementation = this.getGlobalProperty("TRAINING_IMPL");
        ConfigurationContainer.println("\nUsing implementation " + strLevel2Implementation + " for training.");

        if (strLevel2Implementation == null) {
            throw new TrainingException("There is no implementation for \"TRAINING_IMPL\" configured!");
        }

        Training objTraining = null;

        String strFolderName = null;
        String strInFile = null;
        //String strOutFile = new File(strInFile).getParent() + "/" + strFolderName;
        String strOutFile = null;
        try {
            objTraining = ClassLoader.loadTrainingImpl(strLevel2Implementation);
            strFolderName = objTraining.getFolderName();

            strInFile = this.getGlobalProperty("PROCESSED_CORPUS_FILE_NAME");
            //String strOutFile = new File(strInFile).getParent() + "/" + strFolderName;
            strOutFile = FileManager.getTrainingTrainFileName();
        } catch (ConfigurationException e) {
            throw new TrainingException(e);
        }

        //    String strFolderName = objTraining.getFolderName();
        //  String strInFile = this.getGlobalProperty("PROCESSED_CORPUS_FILE_NAME");
        //String strOutFile = new File(strInFile).getParent() + "/" + strFolderName;
        //  String strOutFile = FileManager.getTrainingTrainFileName();
        this.objTrainingBean = new MetaInformationBean();
        objTrainingBean.strOrigKorpusFile = this.objPreprocessingBean.strOrigKorpusFile;
        objTrainingBean.strPreprocessingFolder = this.objPreprocessingBean.strPreprocessingFolder;
        objTrainingBean.strTrainingFolder = objTraining.getFolderName();

        if (!new File(strOutFile).exists()) {
            ConfigurationContainer.println("Training from infile=" + strInFile + " to outdir="
                    + strOutFile);

            objTraining.train();
            objTraining.writeOutputFile();

            int aryStats[] = objTraining.getTrainingStats();

            MetaInformation objTrainingMI = MetaInformationFactory.createMetaInformationObject(objTrainingBean, Constants.TYPE_TRAINING);
            // counting stats for training techniques
            objTrainingMI.setProperty("NUMBER_OF_REUSE_UNITS", "" + aryStats[Constants.NUMBER_OF_REUSE_UNITS]);
            objTrainingMI.setProperty("NUMBER_OF_FEATURE_TYPES", "" + aryStats[Constants.NUMBER_OF_FEATURE_TYPES]);
            objTrainingMI.setProperty("NUMBER_OF_FEATURE_TOKENS", "" + aryStats[Constants.NUMBER_OF_FEATURE_TOKENS]);
            objTrainingMI.setProperty("MAX_FEATURE_FREQUENCY", "" + aryStats[Constants.MAX_FEATURE_FREQUENCY]);
            objTrainingMI.setProperty("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_1", "" + aryStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_1]);
            objTrainingMI.setProperty("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_2", "" + aryStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_2]);
            objTrainingMI.setProperty("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_3", "" + aryStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_3]);
            objTrainingMI.setProperty("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_4", "" + aryStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_4]);
            objTrainingMI.setProperty("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_5", "" + aryStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_5]);
            objTrainingMI.setProperty("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_LARGER_THAN_5",
                    "" + aryStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_LARGER_THAN_5]);
            objTrainingMI.write();
        } else {
            ConfigurationContainer.println("OUTPUT file outfile="
                    + strOutFile + " already exists");
        }

        objTraining = null;
    }

    protected void doSelection() throws SelectionException, MetaInformationException, ConfigurationException {
        // Processing Level 3: Selection
        String strLevel3Implementations = this.getGlobalProperty("SELECTION_IMPL");

        if (strLevel3Implementations == null) {
            throw new SelectionException("There is no implementation for \"SELECTION_IMPL\" configured!");
        }

        SelectionProcessor objProcessor = new SelectionProcessor();
        try {
            objProcessor.init(strLevel3Implementations);
        } catch (ConfigurationException e) {
            throw new SelectionException(e);
        }

        if (objProcessor.isOpenExistingTask()) {
            ConfigurationContainer.println("\nUsing " + objProcessor.getUsedSelectionImplementations().length + " implementation(s) "
                    + objProcessor.getUsedSelectionImplementationsAsString() + " for selection.");

            objSelectionBean = new MetaInformationBean();
            objSelectionBean.strOrigKorpusFile = this.objTrainingBean.strOrigKorpusFile;
            objSelectionBean.strPreprocessingFolder = this.objTrainingBean.strPreprocessingFolder;
            objSelectionBean.strTrainingFolder = objTrainingBean.strTrainingFolder;

            objProcessor.process(objSelectionBean);
        }

        objProcessor.buildMultipleSelectionFile(objSelectionBean);
        objProcessor = null;
    }

    protected void doLinking() throws LinkingException, MetaInformationException, ConfigurationException {
        // Processing Level 3: Selection
        String strLevel4Implementations = this.getGlobalProperty("LINKING_IMPL");

        if (strLevel4Implementations == null) {
            throw new LinkingException("There is no implementation for \"LINKING_IMPL\" configured!");
        }

        Linking objLinking = ClassLoader.loadLinkingImpl(strLevel4Implementations);

        ConfigurationContainer.println("\nUsing " + strLevel4Implementations + " implementation "
                + " for linking.");
        ConfigurationContainer.println("OUTPUT file is outfile=" + FileManager.getLinkingFileName() + " ... ");

        if (!objLinking.isAlreadyExistent()) {
            objLinking.prepareData();
            objLinking.link();
            objLinking.writeLinkingStats();
            ConfigurationContainer.println("\tDONE!!");
        } else {
            ConfigurationContainer.println(" \tALREADY EXISTENT!!!");
        }

        objLinking = null;
    }

    protected void doScoring() throws ScoringException, MetaInformationException, ConfigurationException {
        // Processing Level 5: Scoring
        String strLevel5Implementations = this.getGlobalProperty("SCORING_IMPL");

        if (strLevel5Implementations == null) {
            throw new ScoringException("There is no implementation for \"SCORING_IMPL\" configured!");
        }

        Scoring objScoring = ClassLoader.loadScoringImpl(strLevel5Implementations);

        ConfigurationContainer.println("\nUsing " + strLevel5Implementations + " implementation "
                + " for scoring.");
        ConfigurationContainer.println("OUTPUT file is outfile=" + FileManager.getScoringFileName() + " ... ");

        if (!objScoring.isAlreadyExistent()) {
            objScoring.prepareData();
            objScoring.score();
            objScoring.writeScoringStats();
            ConfigurationContainer.println("\tDONE!!");
        } else {
            ConfigurationContainer.println(" \tALREADY EXISTENT!!!");
        }

        objScoring = null;
    }

    protected void doPostprocessing() throws PostprocessingException, MetaInformationException, ConfigurationException {
        // Processing Level 6: Postprocessing
        String strLevel6Implementations = this.getGlobalProperty("POSTPROCESSING_IMPL");

        Postprocessing objPostprocesing = ClassLoader.loadPostprocessingImpl(strLevel6Implementations);

        ConfigurationContainer.println("\nUsing " + strLevel6Implementations + " implementation "
                + " for postprocessing.");
        ConfigurationContainer.println("OUTPUT file is outfile=" + FileManager.getPostprocessingFileName() + " ... ");

        if (!objPostprocesing.isAlreadyExistent()) {
            objPostprocesing.postprocess();
            ConfigurationContainer.println("\tDONE!!");
        } else {
            ConfigurationContainer.println(" \tALREADY EXISTENT!!!");
        }

        objPostprocesing = null;
    }

    protected void doCharacterCount() throws TracerException, EmptyExportException {
        String strTempWordsFile = ConfigurationContainer.createTempFile();
        String strTokOrigKorpusFile = ConfigurationContainer.getTokenizedSentenceFileName();
        String strOrigKorpusFile = ConfigurationContainer.getSentenceFileName();

        FileCopy objCopy = new FileCopy();
        try {
            objCopy.copy(strTokOrigKorpusFile, strTempWordsFile);
        } catch (Exception e) {
            throw new TracerException(e);
        }

        ConfigurationContainer.setSentenceFileName(strTempWordsFile);
        this.setGlobalProperty("enableArrayCache", Boolean.toString(Boolean.FALSE));
        this.setGlobalProperty("enableRAMHashCache", Boolean.toString(Boolean.TRUE));
        this.setProperty("dblSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");
        setGlobalProperty("MEMORY_ALLOCATOR_IMPL", "eu.etrap.medusa.config.DefaultMemoryAllocatorImpl");
        setGlobalProperty("EXPORTER_IMPL", "eu.etrap.medusa.export.IDFeatureFlatFileExporterImpl");
        setGlobalProperty("PARSER_FILTER_IMPL", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");
        setGlobalProperty("TOKENIZER_IMPL", "eu.etrap.medusa.input.CharacterTokenizerImpl");

        // Set Thresholds to 0
        this.setProperty("dblSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");
        this.setProperty("intMinimumFrequency", "0", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");
        this.setProperty("dblCutoffSignificanceThreshold", "0.00", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");
        this.setProperty("intCutoffMinimumFrequency", "0.00", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");

        doRun(strTempWordsFile, strTokOrigKorpusFile, FileManager.getCharDistFileNameSuffix());
        ConfigurationContainer.setSentenceFileName(strOrigKorpusFile);
        this.setGlobalProperty("enableArrayCache", Boolean.toString(Boolean.TRUE));
    }

    protected void doCreateInvertedList() throws TracerException {
        try {
            this.setGlobalProperty("enableArrayCache", Boolean.toString(Boolean.FALSE));
            this.setGlobalProperty("enableRAMHashCache", Boolean.toString(Boolean.TRUE));

            String strPreprocessedCorpus = getGlobalProperty("PROCESSED_CORPUS_FILE_NAME");
            String strCorpus = getGlobalProperty("SENTENCE_FILE_NAME");

            setGlobalProperty("SENTENCE_FILE_NAME", strPreprocessedCorpus);
            ConfigurationContainer.setSentenceFileName(strPreprocessedCorpus);

            ControlFlow objFlow = null;

            // computing inverted list.
            setGlobalProperty("PARSER_FILTER_IMPL",
                    "eu.etrap.medusa.filter.sidx.IDXPositionalInvertedListFilterImpl");
            setGlobalProperty("MEMORY_ALLOCATOR_IMPL",
                    "eu.etrap.medusa.config.PositionalInvertedListMemoryAllocatorImpl");
            setGlobalProperty("EXPORTER_IMPL",
                    "eu.etrap.medusa.export.PositionalInvertedListFlatFileExporterImpl");
            ConfigurationContainer.setExportFileName(FileManager.getPositionalInvertedListFileName());

            setGlobalProperty("TOKENIZER_IMPL", "eu.etrap.medusa.input.DoNothingWordTokenizerImpl");

            if (!new File(ConfigurationContainer.getExportFileName()).exists()) {
                ConfigurationContainer.println("\n\nGenerating positional inverted list ...");
                //init();
                objFlow = new DefaultControlFlowImpl();
                objFlow.init();
                objFlow.start();
                objFlow = null;

                ConfigurationContainer.println("Positional inverted list created.");

            } else {
                ConfigurationContainer.println("\n\nInverted positional list "
                        + ConfigurationContainer.getExportFileName() + " is already computed.\n\n");
            }

            ConfigurationContainer.setExportFileName(null);

            this.setGlobalProperty("enableArrayCache", Boolean.toString(Boolean.TRUE));
            setGlobalProperty("SENTENCE_FILE_NAME", strCorpus);
        } catch (Throwable e) {
            throw new TracerException(e);
        }
    }

    protected void doNgramCount(int intNGramSize) throws TracerException, EmptyExportException {
        String strTempWordsFile = ConfigurationContainer.createTempFile();
        String strTokOrigKorpusFile = ConfigurationContainer.getTokenizedSentenceFileName();
        String strOrigKorpusFile = ConfigurationContainer.getSentenceFileName();

        FileCopy objCopy = new FileCopy();
        try {
            objCopy.copy(strTokOrigKorpusFile, strTempWordsFile);
        } catch (Exception e) {
            throw new TracerException(e);
        }

        ConfigurationContainer.setSentenceFileName(strTempWordsFile);

        this.setGlobalProperty("enableArrayCache", Boolean.toString(Boolean.FALSE));
        this.setGlobalProperty("enableRAMHashCache", Boolean.toString(Boolean.TRUE));
        this.setProperty("intNGramSize", Integer.toString(intNGramSize), "eu.etrap.medusa.input.NGramTokenizerImpl");
        this.setProperty("dblSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXTypedTermsFilterImpl");
        setGlobalProperty("MEMORY_ALLOCATOR_IMPL", "eu.etrap.medusa.config.DefaultMemoryAllocatorImpl");
        setGlobalProperty("EXPORTER_IMPL", "eu.etrap.medusa.export.IDFeatureFlatFileExporterImpl");
        setGlobalProperty("PARSER_FILTER_IMPL", "eu.etrap.medusa.filter.sidx.IDXTypedTermsFilterImpl");
        setGlobalProperty("TOKENIZER_IMPL", "eu.etrap.medusa.input.NGramTokenizerImpl");

        // Set Thresholds to 0
        this.setProperty("dblSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXTypedTermsFilterImpl");
        this.setProperty("intMinimumFrequency", "0", "eu.etrap.medusa.filter.sidx.IDXTypedTermsFilterImpl");
        this.setProperty("dblCutoffSignificanceThreshold", "0.00", "eu.etrap.medusa.filter.sidx.IDXTypedTermsFilterImpl");
        this.setProperty("intCutoffMinimumFrequency", "0.00", "eu.etrap.medusa.filter.sidx.IDXTypedTermsFilterImpl");

        doRun(strTempWordsFile, strTokOrigKorpusFile, FileManager.getCharNGramDistFileNameSuffix(intNGramSize));
        ConfigurationContainer.setSentenceFileName(strOrigKorpusFile);
        this.setGlobalProperty("enableArrayCache", Boolean.toString(Boolean.TRUE));
    }

    private void doRun(String strTempWordsFile, String strOrigKorpusFile, String strFileSuffix) throws TracerException, EmptyExportException {

        ControlFlow objFlow = null;
        objFlow = new DefaultControlFlowImpl();
        try {
            objFlow.init();
            objFlow.start();
            objFlow = null;
        } catch (EmptyExportException ex) {
            throw new EmptyExportException(ex);
        } catch (Throwable e) {
            throw new TracerException(e);
        }
        try {
            BufferedReader objReader = new BufferedReader(new FileReader(strTempWordsFile + ".wnc"));
            String strLine = null;
            long longCounter = 0;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                long longFreqValue = Long.parseLong(strSplit[3].trim());
                longCounter += longFreqValue;
            }

            objReader.close();

            objReader = new BufferedReader(new FileReader(strTempWordsFile + ".wnc"));
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOrigKorpusFile + "." + strFileSuffix));
            objWriter.write("0\t%TOTOAL%\t1\t" + longCounter + "\n");
            ConfigurationContainer.println("Copy distribution from "
                    + strTempWordsFile + ".wnc to " + strOrigKorpusFile + "." + strFileSuffix);

            while ((strLine = objReader.readLine()) != null) {
                objWriter.write(strLine + "\n");
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();

            cleanTmp(ConfigurationContainer.getSentenceFileName());
        } catch (Exception ex) {
            throw new TracerException(ex);
        }
    }

    private void cleanTmp(String strRootFile) {

        File objRootFile = new File(strRootFile);
        File objDir = new File(objRootFile.getParent());

        String strFileNames[] = objDir.list();

        for (int i = 0; i < strFileNames.length; i++) {
            String strFile2Delete = objDir.getAbsolutePath() + "/" + strFileNames[i];
            if (strFile2Delete.startsWith(objRootFile.getAbsoluteFile().toString() + ".")) {
                ConfigurationContainer.println("Deleting file " + strFile2Delete);
                new File(strFile2Delete).delete();
            }
        }

    }
}
