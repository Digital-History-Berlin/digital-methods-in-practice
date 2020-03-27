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


package eu.etrap.tracer.featuring.syntactic;

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import eu.etrap.tracer.featuring.AbstractTraining;
import eu.etrap.tracer.featuring.TrainingException;
import eu.etrap.tracer.utils.FileManager;
import bak.pcj.map.ObjectKeyIntMap;
import bak.pcj.map.ObjectKeyIntOpenHashMap;
import bak.pcj.set.IntSet;
import bak.pcj.set.IntOpenHashSet;
import eu.etrap.medusa.utils.FileCopy;
import java.io.File;


/**
 * Created on 16.03.2011 11:00:13 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractSyntacticalTraining extends AbstractTraining {

    protected boolean boolCleanAll = false;
    protected boolean boolisConstantNGramLength = false;
    protected ObjectKeyIntMap objWord2WordID = null;
    protected IntSet objNGramIDs = null;
    protected String strKnownNumbersFileName = null;
    protected String strNGramFileSuffix = null;
    protected int intNgramSize = 0;

    @Override
    public void init() throws ConfigurationException {
        super.init();

        strHierarchy = "01-02-00-00-00";
        objWord2WordID = new ObjectKeyIntOpenHashMap();
        objNGramIDs = new IntOpenHashSet();
        strNGramFileSuffix = "mwu";
        boolisConstantNGramLength = true;
    }

    @Override
    protected void doDedicatedTrain() throws TrainingException {
        // train ngrams as configured
        super.doDedicatedTrain();

        if( boolisConstantNGramLength ){
            countFeatureTokenStats(strTrainingFileName, intFeatureFrequencyColumn);
        }
        
        // merge output from expo file and write *.mwu
        String strNGramOutFile = createNGramList();
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
    }

    protected String createNGramList() throws TrainingException {

        String strNGramOutFile = null;

        try {
            String strInFileName = ConfigurationContainer.getExportFileName();
            strNGramOutFile = ConfigurationContainer.getSentenceFileName();

            strNGramOutFile = strNGramOutFile.replace(".prep", "." + strNGramFileSuffix);

            BufferedReader objReader = new BufferedReader(new FileReader(strInFileName));
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strNGramOutFile));

            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                String strOutLine = "";

                for (int i = 0; i < intNgramSize; i++) {
                    strOutLine += strSplit[i].trim() + " ";
                }

                strOutLine = strOutLine.trim();
                objWriter.write(strOutLine + "\n");
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();

        } catch (Exception e) {
            throw new TrainingException(e);
        }

        return strNGramOutFile;
    }

    @Override
    protected void writeFeatsFile(String strSource, String strTarget, int columns[]) throws TrainingException {
        ConfigurationContainer.println("\nWriting feats file from " + strSource + " to "
                + strTarget);

        try {
            BufferedReader objReader = new BufferedReader(new FileReader(strSource));
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strTarget));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                if (!strSplit[1].trim().contains(" ")) {
                    String strResult = "";

                    for (int i = 0; i < columns.length; i++) {
                        strResult += strSplit[columns[i]] + "\t";
                    }

                    objWriter.write(strResult.trim() + "\n");
                }
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            throw new TrainingException(e);
        }

        ConfigurationContainer.println("Feats file finally written.\n");
    }

    @Override
    protected void writeFMAPFile() throws TrainingException {
        try {
            ConfigurationContainer.println("\nWriting fmap file to "
                    + FileManager.getTrainingFMAPFileName());

            loadWordNumbers();

            BufferedReader objReader = new BufferedReader(new FileReader(ConfigurationContainer.getWordNumbersCompleteName()));
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(FileManager.getTrainingFMAPFileName()));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                if (strSplit[1].trim().contains(" ")) {
                    String strResult = strSplit[0].trim() + "\t";

                    String strWords[] = strSplit[1].trim().split(" ");
                    for (int i = 0; i < strWords.length; i++) {
                        int intWordID = this.objWord2WordID.get(strWords[i].trim());
                        strResult += intWordID + " ";
                    }

                    objWriter.write(strResult.trim() + "\n");
                }
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

                if (!strSplit[1].trim().contains(" ")) {
                    objWord2WordID.put(strSplit[1].trim(), Integer.parseInt(strSplit[0].trim()));
                }
            }

            objReader.close();
        } catch (Exception e) {
            throw new TrainingException(e);
        }

        ConfigurationContainer.println("Word are loaded.\n");
    }

    protected void loadNgramID() throws TrainingException {
        try {
            ConfigurationContainer.println("Loading ngram id's from  "
                    + ConfigurationContainer.getWordNumbersCompleteName());

            BufferedReader objReader = new BufferedReader(new FileReader(FileManager.getTrainingFMAPFileName()));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objNGramIDs.add(Integer.parseInt(strSplit[0].trim()));
            }

            objReader.close();
        } catch (Exception e) {
            throw new TrainingException(e);
        }

        ConfigurationContainer.println("Ngram id's are loaded.\n");
    }

    @Override
    protected void writeTrainFile() throws TrainingException {
        try {
            ConfigurationContainer.println("\nWriting train file to "
                    + FileManager.getTrainingTrainFileName());

            loadNgramID();

            BufferedReader objReader = new BufferedReader(new FileReader(ConfigurationContainer.getExportFileName()));
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(FileManager.getTrainingTrainFileName()));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intID = Integer.parseInt(strSplit[0].trim());

                if (objNGramIDs.contains(intID)) {
                    objWriter.write(strLine.trim() + "\n");
                }
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            throw new TrainingException(e);
        }

        ConfigurationContainer.println("Train file finally written.\n");
    }

    @Override
    protected void cleanup() {

        if (boolCleanAll) {
            new File(ConfigurationContainer.getMWUFileName()).delete();
            new File(ConfigurationContainer.getMWUMapFilename()).delete();

            File objMWUIgnoreFile = new File(ConfigurationContainer.getMWUIgnoreFileName());

            if (objMWUIgnoreFile.length() == 0) {
                objMWUIgnoreFile.delete();
            }

            new File(this.strKnownNumbersFileName).delete();
        }

        super.cleanup();
    }
}
