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


package eu.etrap.tracer.requirements;

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.controlflow.AbstractControlFlow;
import eu.etrap.medusa.controlflow.ControlFlow;
import eu.etrap.medusa.controlflow.DefaultControlFlowImpl;
import eu.etrap.medusa.utils.FileCopy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import eu.etrap.tracer.TracerException;
import eu.etrap.tracer.utils.FileManager;
import eu.etrap.tracer.meta.MetaInformation;
import eu.etrap.tracer.meta.MetaInformationFactory;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.meta.MetaInformationBean;

/**
 * Created on 07.12.2010 16:57:22 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class DefaultRequirementsControlFlowImpl extends AbstractControlFlow {

    private double dblStringSimThreshold = 0.0;
    private int intSimWordsCounter = 0;
    private int intMinWordLength = 0;

    /** Creates a new instance of DefaultControlFlowImpl. */
    public DefaultRequirementsControlFlowImpl() {
    }

    @Override
    public void start() throws Throwable {
        intSimWordsCounter = 0;

        if (intMinWordLength < 0) {
            intMinWordLength = 5;
        }

        String strCorpusFile = ConfigurationContainer.getSentenceFileName();
        ControlFlow objFlow = null;

        // computing inverted list.
        setGlobalProperty("PARSER_FILTER_IMPL",
                "eu.etrap.medusa.filter.sidx.IDXPositionalInvertedListFilterImpl");
        setGlobalProperty("MEMORY_ALLOCATOR_IMPL",
                "eu.etrap.medusa.config.PositionalInvertedListMemoryAllocatorImpl");
        setGlobalProperty("EXPORTER_IMPL",
                "eu.etrap.medusa.export.PositionalInvertedListFlatFileExporterImpl");
        ConfigurationContainer.setExportFileName(FileManager.getPositionalInvertedListFileName());

        if (!new File(ConfigurationContainer.getExportFileName()).exists()) {
            ConfigurationContainer.println("Generating positional inverted list ...");
            init();
            objFlow = new DefaultControlFlowImpl();
            objFlow.init();
            objFlow.start();
            objFlow = null;


            ConfigurationContainer.println("Positional inverted list created.");

            MetaInformationBean objMIBean = new MetaInformationBean();
            objMIBean.strOrigKorpusFile = strCorpusFile;

            MetaInformation objMetaInformation =
                    MetaInformationFactory.createMetaInformationObject(objMIBean, Constants.TYPE_REQUIREMENTS);
            objMetaInformation.setProperty("SENTENCES", ConfigurationContainer.getCategory("statistics").getProperty("SENTENCES"));
            objMetaInformation.setProperty("WORD_TYPES", ConfigurationContainer.getCategory("statistics").getProperty("WORD_TYPES"));
            objMetaInformation.setProperty("WORD_TOKENS", ConfigurationContainer.getCategory("statistics").getProperty("WORD_TOKENS"));
            objMetaInformation.setProperty("BOW_WORD_TOKENS", ConfigurationContainer.getCategory("statistics").getProperty("BOW_WORD_TOKENS"));
            objMetaInformation.setProperty("SOURCES", ConfigurationContainer.getCategory("statistics").getProperty("SOURCES"));
            objMetaInformation.setProperty("SENTENCES", ConfigurationContainer.getCategory("statistics").getProperty("SENTENCES"));
            objMetaInformation.write();
        } else {
            ConfigurationContainer.println("Inverted positional list "
                    + ConfigurationContainer.getExportFileName() + " is already computed.\n\n");
        }

        ConfigurationContainer.setExportFileName(null);

        String strOrigKorpusFile = ConfigurationContainer.getSentenceFileName();


        if (!new File(FileManager.getStringSimilarWordFileName()).exists()) {
            ConfigurationContainer.println("\n\nComputing string similarity ...");

            // copy already computed words to tmp for further processing
            String strTempWordsFile = ConfigurationContainer.createTempFile();

            FileCopy objCopy = new FileCopy();
            objCopy.copy(ConfigurationContainer.getWordNumbersCompleteName(), strTempWordsFile, new int[]{0, 1});

            ConfigurationContainer.setKnownWordNumbersFileName(ConfigurationContainer.getWordNumbersCompleteName());
            ConfigurationContainer.setSentenceFileName(strTempWordsFile);

            trainLetterNGrams("2", strOrigKorpusFile);
            cleanTmp(strTempWordsFile);

            ConfigurationContainer.setSentenceFileName(strOrigKorpusFile);

            MetaInformationBean objMIBean = new MetaInformationBean();
            objMIBean.strOrigKorpusFile = strCorpusFile;

            MetaInformation objMetaInformation =
                    MetaInformationFactory.createMetaInformationObject(objMIBean, Constants.TYPE_REQUIREMENTS);
            objMetaInformation.setProperty("SSIM_THRESHOLD", Double.toString(this.dblStringSimThreshold));
            objMetaInformation.setProperty("SSIM_EDGES", Integer.toString(intSimWordsCounter));
            objMetaInformation.write();

            ConfigurationContainer.println("String similarity computed.\n\n");
        } else {
            ConfigurationContainer.println("String similarity file "
                    + FileManager.getStringSimilarWordFileName() + " is already computed.\n\n");
        }

        cleanupFileSystem();
    }

    private void trainLetterNGrams(String strNgram, String strOrigCorpus) throws TracerException {
        try {
            longStart = System.currentTimeMillis();

            this.setGlobalProperty("enableArrayCache",
                    Boolean.toString(Boolean.FALSE));
            this.setGlobalProperty("enableRAMHashCache",
                    Boolean.toString(Boolean.TRUE));
            this.setProperty("intNGramSize", strNgram, "eu.etrap.medusa.input.NGramTokenizerImpl");
            this.setProperty("dblSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");
            setGlobalProperty("MEMORY_ALLOCATOR_IMPL",
                    "eu.etrap.medusa.config.DefaultMemoryAllocatorImpl");
            setGlobalProperty("EXPORTER_IMPL",
                    "eu.etrap.medusa.export.IDFeatureFlatFileExporterImpl");
            setGlobalProperty("PARSER_FILTER_IMPL",
                    "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");
            setGlobalProperty("TOKENIZER_IMPL", "eu.etrap.medusa.input.NGramTokenizerImpl");

            // Set Thresholds to 0
            this.setProperty("dblSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");
            this.setProperty("intMinimumFrequency", "0", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");
            this.setProperty("dblCutoffSignificanceThreshold", "0.00", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");
            this.setProperty("intCutoffMinimumFrequency", "0.00", "eu.etrap.medusa.filter.sidx.IDXIDFeaturingFilterImpl");

            this.setProperty("boolReplaceWordNumbers", "true", "eu.etrap.medusa.export.IDFeatureFlatFileExporterImpl");

            ControlFlow objFlow = null;
            objFlow = new DefaultControlFlowImpl();
            objFlow.init();
            objFlow.start();
            objFlow = null;

            computeSimilarity(ConfigurationContainer.getExportFileName(), strOrigCorpus);
             
            this.setGlobalProperty("enableArrayCache",
                    Boolean.toString(Boolean.TRUE));
        } catch (Throwable e) {
            throw new TracerException(e);
        }
    }

    private HashSet<Integer> selectFeatureIDs(String strTrainingsFile) throws FileNotFoundException, IOException {
        HashSet<Integer> objSelectedFeatureIDs = new HashSet<Integer>();
        BufferedReader objReader = new BufferedReader(new FileReader(strTrainingsFile));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            objSelectedFeatureIDs.add(new Integer(strLine.split("\t")[1]));
        }

        objReader.close();

        return objSelectedFeatureIDs;
    }

    private void cleanTmp(String strRootFile) {

        File objRootFile = new File(strRootFile);
        File objDir = new File(objRootFile.getParent());

        String strFileNames[] = objDir.list();

        for (int i = 0; i < strFileNames.length; i++) {
            String strFile2Delete = objDir.getAbsolutePath() + "/" + strFileNames[i];
            if (strFile2Delete.startsWith(objRootFile.getAbsoluteFile().toString())) {
                ConfigurationContainer.println("Deleting file " + strFile2Delete);
                new File(strFile2Delete).delete();
            }
        }

    }

    private void computeSimilarity(String strTrainFile, String strOrigCorpus) throws IOException {
        // read word --> feature
        // feature --> word
        // read wordliste
        TreeSet<String> objWordIDs = new TreeSet<String>();
        HashMap<String, HashSet<String>> objWord2Feature = new HashMap<String, HashSet<String>>();
        HashMap<String, HashSet<String>> objFeature2Word = new HashMap<String, HashSet<String>>();

        BufferedReader objReader = new BufferedReader(new FileReader(strTrainFile));
        String strLine = null;

        // read all necessary data from training file
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objWordIDs.add(strSplit[0].trim());

            // adding features2word
            HashSet<String> objKeys = null;
            if (objWord2Feature.containsKey(strSplit[0].trim())) {
                objKeys = objWord2Feature.get(strSplit[0].trim());
            } else {
                objKeys = new HashSet<String>();
            }
            objKeys.add(strSplit[1].trim());
            objWord2Feature.put(strSplit[0].trim(), objKeys);

            objKeys = null;
            if (objFeature2Word.containsKey(strSplit[1].trim())) {
                objKeys = objFeature2Word.get(strSplit[1].trim());
            } else {
                objKeys = new HashSet<String>();
            }
            objKeys.add(strSplit[0].trim());
            objFeature2Word.put(strSplit[1].trim(), objKeys);
        }

        // compute sim
        Iterator<String> objWordIDIter = objWordIDs.iterator();
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOrigCorpus + ".ssim"));

        while (objWordIDIter.hasNext()) {
            String objCurWordID = objWordIDIter.next();
            HashSet<String> objListofCandidates = new HashSet<String>();

            HashSet<String> objCurWordsFeatures = objWord2Feature.get(objCurWordID);
            Iterator<String> objCurWordFeatureIter = objCurWordsFeatures.iterator();

            while (objCurWordFeatureIter.hasNext()) {
                String objWordFeature = objCurWordFeatureIter.next();
                HashSet<String> objWordsReg2Feature = objFeature2Word.get(objWordFeature);
                objListofCandidates.addAll(objWordsReg2Feature);
            }

            Iterator<String> objCandidateIter = objListofCandidates.iterator();

            while (objCandidateIter.hasNext()) {

                String objCandidateID = objCandidateIter.next();

                if (!objCurWordID.equals(objCandidateID)) {
                    HashSet<String> objCandidateFeatures = objWord2Feature.get(objCandidateID);

                    HashSet<String> objSmallerSet = objCurWordsFeatures;
                    HashSet<String> objLargerSet = objCandidateFeatures;

                    if (objSmallerSet.size() > objLargerSet.size()) {
                        objSmallerSet = objCandidateFeatures;
                        objLargerSet = objCurWordsFeatures;
                    }

                    Iterator<String> objFeatureIter = objSmallerSet.iterator();
                    int intOverlap = 0;
                    while (objFeatureIter.hasNext()) {
                        String objFeature = objFeatureIter.next();

                        if (objLargerSet.contains(objFeature)) {
                            intOverlap++;
                        }
                    }

                    double dblDice =
                            2 * (double) intOverlap / ((double) objSmallerSet.size() + (double) objLargerSet.size());

                    if (dblDice >= dblStringSimThreshold
                            && objCandidateID.length() >= intMinWordLength
                            && objCurWordID.length() >= intMinWordLength) {
                        objWriter.write(objCurWordID + "\t" + objCandidateID
                                + "\t" + intOverlap + "\t" + dblDice + "\n");
                        intSimWordsCounter++;
                    }
                }
            }
        }

        objWriter.flush();
        objWriter.close();
    }

    protected boolean checkFile(String strFileName) {
        if (strFileName != null && new java.io.File(strFileName).exists()
                && new java.io.File(strFileName).length() > 0) {
            return true;
        }
        return false;
    }

    protected boolean delete(String strFileName) {
        return new File(strFileName).delete();
    }

    protected void cleanupFileSystem() {
        delete(ConfigurationContainer.getExternalWordFrequenciesFileName());
        delete(ConfigurationContainer.getExternalWordNumbersFileName());
        delete(ConfigurationContainer.getBOWWordFrequenciesFileName());
        delete(ConfigurationContainer.getIDXFileName());
        delete(ConfigurationContainer.getParaSFileName());
        delete(ConfigurationContainer.getParserFilterComponentFileName());
        delete(ConfigurationContainer.getPersistentHashFileName());
        delete(ConfigurationContainer.getSentenceFileNameForDB());
        delete(ConfigurationContainer.getSourceFileName());
        delete(ConfigurationContainer.getTokenizedSentenceFileNameForDB());
        delete(ConfigurationContainer.getWSWNFileName());
        delete(ConfigurationContainer.getWordFrequenciesFileName());
        delete(ConfigurationContainer.getWordNumbersFileName());

        //delete(ConfigurationContainer.getSentenceFileName() + ".eu.etrap.medusa.filter.sidx.IDXTypedTermsFilterImpl.hash.lgl2.expo");
        //delete(ConfigurationContainer.getSentenceFileName() + ".eu.etrap.medusa.filter.sidx.IDXTypedTermsFilterImpl.letter_ngram.2.expo");
        delete(ConfigurationContainer.getSentenceFileName() + ".eu.etrap.medusa.filter.sidx.IDXPositionalInvertedListFilterImpl.hash");
        //delete(ConfigurationContainer.getSentenceFileName() + ".eu.etrap.medusa.filter.sidx.IDXTypedTermsFilterImpl.letter_ngram.2.expo.wnc");
    }
}
