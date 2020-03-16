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



package eu.etrap.tracer.meta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 28.03.2011 09:19:04 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public abstract class AbstractMetaInformationHandler {

    protected ArrayList<String> objKeys = null;
    protected HashMap<String, String> objProperties = null;
    protected String strLevelMetaInformationFileName = null;
    protected String strOriginalKorpusFileName = null;
    protected boolean isInitialized = false;
    protected MetaInformationBean objBean = null;

    public void init( MetaInformationBean objMIBean) throws MetaInformationException {
        objKeys = new ArrayList<String>();
        objProperties = new HashMap<String, String>();

        if (new File(strLevelMetaInformationFileName).exists()) {
            loadProperties();
        }

        isInitialized = true;
    }

    private void loadProperties() throws MetaInformationException {
        try {
            BufferedReader objReader = new BufferedReader(new FileReader(strLevelMetaInformationFileName));
            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                String strKey = strSplit[0].trim();
                String strValue = strSplit[1].trim();

                objKeys.add(strKey);
                objProperties.put(strKey, strValue);
            }

            objReader.close();
        } catch (Exception e) {
            throw new MetaInformationException(e);
        }
    }

    public void setProperty(String strKey, String strValue) {

        if (!objProperties.containsKey(strKey)) {
            objKeys.add(strKey);
        }

        objProperties.put(strKey, strValue);
    }

    public String getProperty(String strKey) {
        return objProperties.get(strKey);
    }

    public void write() throws MetaInformationException {

        try {
            System.out.println( "Writing meta file to " + strLevelMetaInformationFileName);
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strLevelMetaInformationFileName));
            String strLine = null;

            int intNumberOfFeatures = objKeys.size();

            for (int i = 0; i < intNumberOfFeatures; i++) {
                String strKey = objKeys.get(i);
                String strValue = this.objProperties.get(strKey);

                objWriter.write(strKey + "\t" + strValue + "\n");
            }

            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            throw new MetaInformationException(e);
        }
    }

    public void close() throws MetaInformationException {
        objKeys = null;
        objProperties = null;
        isInitialized = false;
    }
}
