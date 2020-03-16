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



package eu.etrap.tracer.featuring;

import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.medusa.controlflow.ControlFlow;
import eu.etrap.medusa.controlflow.DefaultControlFlowImpl;
import de.uni_leipzig.asv.filesort.FileSort;
import eu.etrap.medusa.utils.FileCopy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import eu.etrap.tracer.Constants;

import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 08.12.2010 14:39:51 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractTraining extends ClassConfig {

    /* protected int intNumberOfFeaturesTokens = 0;
    protected int intNumberOfFeaturesTypes = 0;
    protected int intNumberOfSelectedFeaturesTypes = 0;*/
    protected int aryTrainStats[] = null;
    protected String strHierarchy = null;
    protected String strProcessedCorpusFileName = null;
    protected int[] sortOrder = null;
    protected char[] sortTypes = null;
    protected int keyIndex[] = null;
    protected String strTrainingFileName = null;
    protected String strPositionalInvertedListFileName = null;
    protected int intFeatureFrequencyColumn = 0;

    public void init() throws ConfigurationException {
        super.config();

        strHierarchy = "00-00-00-00-00";

        sortOrder = new int[]{2, 0, 1};
        sortTypes = new char[]{'i', 'i', 'I'};
        keyIndex = new int[]{0, 1};

        this.strProcessedCorpusFileName = this.getGlobalProperty("PROCESSED_CORPUS_FILE_NAME");

        aryTrainStats = new int[10];
    }

    public String getFolderName() {
        String strClassName = this.getClass().getName();
        int index = strClassName.lastIndexOf(".");
        String strResult = strClassName.substring(index + 1);
        strResult = getHierarchicalStructure() + "-" + strResult;
        return strResult;
    }

    public String getHierarchicalStructure() {
        return strHierarchy;
    }

    public int[] getTrainingStats() {
        return aryTrainStats;
    }

    protected void doSort(String strInFile, String strOutFile, int[] sortOrder, char[] sortTypes) {
        FileSort sort = new FileSort("\t", sortOrder, sortTypes);
        sort.sort(strInFile, strOutFile);
    }

    // Sort the Medusa default output by feature frquency descendent
    protected void sort(String strInFile, String strOutFile) {
        doSort(strInFile, strOutFile, sortOrder, sortTypes);
    }

    protected void doPreparation() {
        ConfigurationContainer.setSentenceFileName(null);
        setGlobalProperty("SENTENCE_FILE_NAME", strProcessedCorpusFileName);
        setGlobalProperty("TOKENIZER_IMPL", "eu.etrap.medusa.input.DoNothingWordTokenizerImpl");
        setGlobalProperty("enableArrayCache", Boolean.toString(Boolean.TRUE));
        setGlobalProperty("enableRAMHashCache", Boolean.toString(Boolean.TRUE));
    }

    protected void doDedicatedTrain() throws TrainingException {
        ConfigurationContainer.println("Training data by class "
                + this.getClass().getCanonicalName() + " ...");

        try {
            ControlFlow objFlow = new DefaultControlFlowImpl();
            objFlow.init();
            objFlow.start();
            objFlow = null;
        } catch (Throwable e) {
            throw new TrainingException(e);
        }

        ConfigurationContainer.println("\nTraining of corpus by class "
                + this.getClass().getCanonicalName() + " finished.\n");

        strTrainingFileName = ConfigurationContainer.getExportFileName();

        this.aryTrainStats[Constants.NUMBER_OF_REUSE_UNITS] =
                ConfigurationContainer.getCategory("statistics").getIntProperty("SENTENCES", -1);
    }

    public void train() throws TrainingException {
        doPreparation();
        doDedicatedTrain();
    }

    public void writeOutputFile() throws TrainingException {
        try {
            writeFeatsFile(ConfigurationContainer.getWordNumbersCompleteName(),
                    FileManager.getTrainingFeatsFileName(), new int[]{0, 1, 3});

            // Create new Feature ID and store in the fmap file
            writeFMAPFile();

            writeTrainFile();

            cleanup();
        } catch (Exception e) {
            throw new TrainingException(e);
        }
    }

    // needs to be implemented by algorithms
    protected void writeFMAPFile() throws TrainingException {
    }

    // needs to be implemented by algorithms
    protected void writeTrainFile() throws TrainingException {
    }

    protected void countFeatureTokenStats(String strFeatureFileName, int intFeatureFrequencyColumn) throws TrainingException {
        try {
            BufferedReader objReader = new BufferedReader(new FileReader(strFeatureFileName));
            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intFeatureFrequency = Integer.parseInt(strSplit[intFeatureFrequencyColumn].trim());

                countFeatureTokenStats(intFeatureFrequency);
            }

            objReader.close();

        } catch (Exception e) {
            throw new TrainingException(e);
        }
    }

    protected void countFeatureTokenStats(int intFeatureFrequency) {

        this.aryTrainStats[Constants.NUMBER_OF_FEATURE_TYPES]++;
        this.aryTrainStats[Constants.NUMBER_OF_FEATURE_TOKENS] += intFeatureFrequency;
        this.aryTrainStats[Constants.MAX_FEATURE_FREQUENCY] =
                Math.max(aryTrainStats[Constants.MAX_FEATURE_FREQUENCY], intFeatureFrequency);

        switch (intFeatureFrequency) {
            case 1:
                aryTrainStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_1]++;
                break;
            case 2:
                aryTrainStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_2]++;
                break;
            case 3:
                aryTrainStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_3]++;
                break;
            case 4:
                aryTrainStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_4]++;
                break;
            case 5:
                aryTrainStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_5]++;
                break;
            default:
                aryTrainStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_LARGER_THAN_5]++;
        }
    }

    protected void writeFeatsFile(String strSource, String strTarget, int columns[]) throws TrainingException {
        ConfigurationContainer.println("\nWriting feats file from " + strSource + " to "
                + strTarget);

        FileCopy objCopy = new FileCopy();

        try {
            objCopy.copy(strSource, strTarget, columns);
        } catch (Exception e) {
            throw new TrainingException(e);
        }

        ConfigurationContainer.println("Feats file finally written.\n");
    }

    protected void cleanup() {
        String strCorpusFile = ConfigurationContainer.getSentenceFileName();
        File objParentFolder = new File(strCorpusFile).getParentFile();
        String strFileName = new File(strCorpusFile).getName();

        String strListOfFiles[] = objParentFolder.list();

        for (int i = 0; i < strListOfFiles.length; i++) {
            File objFile = new File(strListOfFiles[i]);

            if (objFile.getName().startsWith(strFileName + ".")) {
                if (!( strListOfFiles[i].endsWith(".meta") || strListOfFiles[i].endsWith(".inv") )  ) {
                    new File(objParentFolder.getAbsolutePath() + "/" + strListOfFiles[i]).delete();
                }
            }
        }
    }

    public void doCreateInvertedList() throws TrainingException {
        ConfigurationContainer.println("Generating positional inverted list ...");
        // computing inverted list.
        setGlobalProperty("PARSER_FILTER_IMPL",
                "eu.etrap.medusa.filter.sidx.IDXPositionalInvertedListFilterImpl");
        setGlobalProperty("MEMORY_ALLOCATOR_IMPL",
                "eu.etrap.medusa.config.PositionalInvertedListMemoryAllocatorImpl");
        setGlobalProperty("EXPORTER_IMPL",
                "eu.etrap.medusa.export.PositionalInvertedListFlatFileExporterImpl");

        try {
            ControlFlow objFlow = new DefaultControlFlowImpl();
            objFlow.init();
            objFlow.start();
            objFlow = null;
        } catch (Throwable e) {
            throw new TrainingException(e);
        }
        ConfigurationContainer.println("Positional inverted list created ...");

        strPositionalInvertedListFileName = ConfigurationContainer.getExportFileName();
    }
}
