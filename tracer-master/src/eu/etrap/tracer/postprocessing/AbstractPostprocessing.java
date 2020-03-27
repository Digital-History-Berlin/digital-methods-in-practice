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
package eu.etrap.tracer.postprocessing;

import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.scoring.ScoringException;
import eu.etrap.tracer.utils.FileManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created on 08.12.2010 13:45:38 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public abstract class AbstractPostprocessing extends ClassConfig implements Postprocessing {

    protected String strTaxonomyCode = null;
    protected int intMode = -1;
    protected int intMinimumFrequency = -1;
    protected long longUsedTime = -1;

    public void init() throws ConfigurationException {
        super.config();

        strTaxonomyCode = "001";
        longUsedTime = 0;
    }

    public void postprocess() throws PostprocessingException {
        long longStartTime = System.currentTimeMillis();

        long longEndTime = System.currentTimeMillis();
        try {
            doPostprocessing();
        } catch (ConfigurationException e) {
            throw new PostprocessingException(e);
        }

        long longUsedTime = longEndTime - longStartTime;
        writePostprocessingStats();
    }

    protected void doPostprocessing() throws PostprocessingException, ConfigurationException {

    }

    public void writePostprocessingStats() throws PostprocessingException {
        try {
            String strPostprocessingMetaFileName = FileManager.getPostprocessingMetaFileName();

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strPostprocessingMetaFileName));

            long longNormalisationFactor = 1000;
            long longTotalTime = this.longUsedTime / longNormalisationFactor;
            objWriter.write("POSTPROCESSING_TIME\t" + longTotalTime + "\n");

            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            throw new PostprocessingException(e);
        }
    }

    @Override
    public boolean isAlreadyExistent() throws ConfigurationException {
        String strSelectionFile = FileManager.getPostprocessingFileName();
        if (new File(strSelectionFile).exists()) {
            return true;
        }

        return false;
    }

    @Override
    public String getFolderName() {
        String strClassName = this.getClass().getName();
        int index = strClassName.lastIndexOf(".");
        String strResult = strClassName.substring(index + 1);
        return strResult + "-mode=" + this.intMode + "_"
                + "minfrequency=" + this.intMinimumFrequency;
    }

    public String getFullFolderName() {
        return this.strTaxonomyCode + "-" + getFolderName();
    }

    public String getTaxonomyCode() {
        return this.strTaxonomyCode;
    }
}
