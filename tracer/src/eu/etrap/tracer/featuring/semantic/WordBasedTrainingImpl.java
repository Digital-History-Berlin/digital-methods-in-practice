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


package eu.etrap.tracer.featuring.semantic;

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.medusa.utils.FileCopy;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.featuring.Training;
import eu.etrap.tracer.featuring.TrainingException;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 08.12.2010 14:42:38 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class WordBasedTrainingImpl extends AbstractSemanticTraining implements Training {

    @Override
    public void init() throws ConfigurationException {
        super.init();

        strHierarchy = "01-01-01-00-00";
    }

    @Override
    public void doDedicatedTrain() throws TrainingException {
        doCreateInvertedList();

        this.aryTrainStats[Constants.NUMBER_OF_REUSE_UNITS] =
                ConfigurationContainer.getCategory("statistics").getIntProperty("SENTENCES", -1);

        countFeatureTokenStats(ConfigurationContainer.getWordFrequenciesFileName(), 1);
    }

    @Override
    protected void writeFMAPFile() throws TrainingException {
        try {
            ConfigurationContainer.println("Writing fmap file  " + FileManager.getTrainingFMAPFileName()
                    + " by using data from " + ConfigurationContainer.getExportFileName());

            FileCopy objCopy = new FileCopy();
            int intNumberOfLines = objCopy.copy(ConfigurationContainer.getExportFileName(),
                    FileManager.getTrainingFMAPFileName(), new int[]{0, 0});

            ConfigurationContainer.println("fmap file written.\n");
        } catch (Exception e) {
            throw new TrainingException(e);
        }
    }

    @Override
    protected void writeTrainFile() throws TrainingException {
        try {
            ConfigurationContainer.println("Writing train file  " + FileManager.getTrainingFMAPFileName()
                    + " by using data from " + ConfigurationContainer.getExportFileName());

            FileCopy objCopy = new FileCopy();

            objCopy.copy(ConfigurationContainer.getExportFileName(),
                    FileManager.getTrainingTrainFileName(), new int[]{0, 1, 2});

            ConfigurationContainer.println("Train file written.\n");
        } catch (Exception e) {
            throw new TrainingException(e);
        }
    }
}