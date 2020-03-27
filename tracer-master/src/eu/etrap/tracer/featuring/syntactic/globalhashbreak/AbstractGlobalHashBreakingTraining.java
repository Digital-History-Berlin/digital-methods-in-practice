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



package eu.etrap.tracer.featuring.syntactic.globalhashbreak;

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.medusa.utils.FileCopy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import eu.etrap.tracer.featuring.TrainingException;
import eu.etrap.tracer.featuring.syntactic.AbstractSyntacticalTraining;
import eu.etrap.tracer.utils.FileManager;
import bak.pcj.map.ObjectKeyIntMap;
import bak.pcj.map.ObjectKeyIntOpenHashMap;
import eu.etrap.tracer.Constants;

/**
 * Created on 24.03.2011 20:31:59 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractGlobalHashBreakingTraining extends AbstractSyntacticalTraining {

    protected int intModP = 0;
    protected ObjectKeyIntMap objNgrams = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();

        strHierarchy = "01-02-02-01-00";
        objNgrams = new ObjectKeyIntOpenHashMap();

        this.boolisConstantNGramLength = false;

        // IGNORE: just necessary for some precomputations. 
        // UNIMPORTANT for the real job of this training.
        intNgramSize = 10;
    }

    @Override
    protected void doPreparation() {
        super.doPreparation();

        setGlobalProperty("MEMORY_ALLOCATOR_IMPL",
                "eu.etrap.medusa.config.NGramMemoryAllocatorImpl");
        this.setProperty("intNumberOfWords", "" + intNgramSize,
                "eu.etrap.medusa.config.NGramMemoryAllocatorImpl");

        setGlobalProperty("EXPORTER_IMPL",
                "eu.etrap.medusa.export.NGramFlatFileExporterImpl");

        this.setProperty("intNumberOfWords", "" + intNgramSize,
                "eu.etrap.medusa.export.NGramFlatFileExporterImpl");

        setGlobalProperty("PARSER_FILTER_IMPL",
                "eu.etrap.medusa.filter.sidx.IDXRightDecoGramHashBreakingFilterImpl");


        this.setProperty("dblSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXRightDecoGramHashBreakingFilterImpl");
        this.setProperty("intMinimumFrequency", "2", "eu.etrap.medusa.filter.sidx.IDXRightDecoGramHashBreakingFilterImpl");
        this.setProperty("dblCutoffSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IIDXRightDecoGramHashBreakingFilterImpl");
        this.setProperty("intCutoffMinimumFrequency", "2", "eu.etrap.medusa.filter.sidx.IDXRightDecoGramHashBreakingFilterImpl");

        this.setGlobalProperty("SIGNIFICANCE_IMPL", "eu.etrap.medusa.significance.FrequencySignificanceImpl");
        this.setGlobalProperty("CUTOFF_SIGNIFICANCE_IMPL", "eu.etrap.medusa.significance.FrequencySignificanceImpl");

        // in order to get integer values as significance measure
        this.setProperty("intAccuracy", "0", "eu.etrap.medusa.export.NGramFlatFileExporterImpl");
        this.setProperty("boolExportFrequency", "false", "eu.etrap.medusa.export.NGramFlatFileExporterImpl");
        this.setProperty("boolReplaceWordNumbers", "false", "eu.etrap.medusa.export.NGramFlatFileExporterImpl");
    }

    @Override
    protected String createNGramList() throws TrainingException {
        String strNGramOutFile = this.strProcessedCorpusFileName.replace(".prep", "." + this.strNGramFileSuffix);

        try {
            // read word list for global rank as hash code
            BufferedReader objReader = new BufferedReader(new FileReader(ConfigurationContainer.getWordNumbersCompleteName()));
            ObjectKeyIntMap objWord2ID = new ObjectKeyIntOpenHashMap();
            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objWord2ID.put(strSplit[1].trim(), Integer.parseInt(strSplit[0].trim()));
            }

            objReader.close();


            objReader = new BufferedReader(new FileReader(this.strProcessedCorpusFileName));

            // read prep file
            strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
    
                // split it into words
                String strWords[] = strSplit[1].trim().split(" ");

                int intStartPos = 0;
                for (int i = 0; i < strWords.length; i++) {
                    int intHashCode = objWord2ID.get(strWords[i]);

                    if (intHashCode % intModP == 0) {
                        String strHashBreak = "";
                        for (int j = intStartPos; j <= i; j++) {
                            strHashBreak += strWords[j] + " ";
                        }

                        strHashBreak = strHashBreak.trim();
                        int intFreq = 0;

                        if (objNgrams.containsKey(strHashBreak)) {
                            intFreq = objNgrams.get(strHashBreak);
                        }

                        intFreq++;

                        objNgrams.put(strHashBreak, intFreq);
                        intStartPos = i + 1;
                    }
                }

                if (strWords.length - intStartPos != 0) {
                    String strHashBreak = "";
                    for (int j = intStartPos; j < strWords.length; j++) {
                        strHashBreak += strWords[j] + " ";
                    }

                    strHashBreak = strHashBreak.trim();
                    int intFreq = 0;

                    if (objNgrams.containsKey(strHashBreak)) {
                        intFreq = objNgrams.get(strHashBreak);
                    }

                    intFreq++;

                    objNgrams.put(strHashBreak, intFreq);

                }
            }

            objReader.close();

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strNGramOutFile));

            Iterator<String> objIterator = objNgrams.keySet().iterator();
            while (objIterator.hasNext()) {
                String strNGram = objIterator.next();
                objWriter.write(strNGram + "\n");

                int intFeatureFrequency = this.objNgrams.get( strNGram );
                countFeatureTokenStats(intFeatureFrequency);
            }

            objWriter.flush();
            objWriter.close();


            FileCopy objCopy = new FileCopy();
            strKnownNumbersFileName =
                    ConfigurationContainer.getSentenceFileName().replace(".prep", ".kn");

            try {
                objCopy.copy(ConfigurationContainer.getWordNumbersCompleteName(),
                        strKnownNumbersFileName, new int[]{0, 1});
            } catch (Exception e) {
                throw new TrainingException(e);
            }

            boolCleanAll = false;
            // cleanup
            this.cleanup();

            // run inverted list
            String strOldKnownNumbers = ConfigurationContainer.getKnownWordNumbersName();
            ConfigurationContainer.setKnownWordNumbersFileName(strKnownNumbersFileName);
            ConfigurationContainer.setMWUFileName(strNGramOutFile);
            ConfigurationContainer.setMWUMapFilename(strNGramOutFile + ".map");
            this.doCreateInvertedList();
            ConfigurationContainer.setKnownWordNumbersFileName(strOldKnownNumbers);

            boolCleanAll = true;
            //System.exit(0);
        } catch (Exception e) {
            throw new TrainingException(e);
        }

        return strNGramOutFile;
    }

    @Override
    protected void writeFMAPFile() throws TrainingException {
        try {
            ConfigurationContainer.println("\nWriting fmap file to "
                    + FileManager.getTrainingFMAPFileName());

            loadWordNumbers();

            BufferedReader objReader = new BufferedReader(new FileReader(ConfigurationContainer.getMWUFileName()));
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(FileManager.getTrainingFMAPFileName()));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strResult = this.objWord2WordID.get(strLine.trim()) + "\t";

                String strWords[] = strLine.trim().split(" ");
                for (int i = 0; i < strWords.length; i++) {
                    int intWordID = this.objWord2WordID.get(strWords[i].trim());
                    strResult += intWordID + " ";
                }

                objWriter.write(strResult.trim() + "\n");

                // counting features
                this.aryTrainStats[Constants.NUMBER_OF_FEATURE_TYPES]++;
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            throw new TrainingException(e);
        }

        ConfigurationContainer.println("Fmap file finally written.\n");
    }

    protected void loadWordNumbers() throws TrainingException {
        try {
            ConfigurationContainer.println("Loading word numbers from "
                    + ConfigurationContainer.getWordNumbersCompleteName());

            BufferedReader objReader = new BufferedReader(new FileReader(ConfigurationContainer.getWordNumbersCompleteName()));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objWord2WordID.put(strSplit[1].trim(), Integer.parseInt(strSplit[0].trim()));
            }

            objReader.close();
        } catch (Exception e) {
            throw new TrainingException(e);
        }

        ConfigurationContainer.println("Word are loaded.\n");
    }
}
