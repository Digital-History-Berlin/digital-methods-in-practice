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



package eu.etrap.tracer.linking.connector.ram;

import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.linking.connector.AbstractConnector;

import bak.pcj.list.IntList;
import bak.pcj.list.IntArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import eu.etrap.tracer.linking.LinkingException;

/**
 * Created on 07.05.2011 11:19:44 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractRAMConnector extends AbstractConnector {

    protected int intKeyIndex = -1;
    protected int intValueIndex = -2;
    // this maps ruid and feature id to each other
    private HashMap<Integer, IntList> objIDMapping = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();

        strTaxonomyCode = "01-01-01-00";
        objIDMapping = new HashMap<Integer, IntList>();
    }

    public void prepareData(String strSource) throws LinkingException{
        File objInFile = new File( strSource );

        try {
            BufferedReader objReader = new BufferedReader(new FileReader(objInFile));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intKey = Integer.parseInt(strSplit[this.intKeyIndex]);
                int intValue = Integer.parseInt(strSplit[this.intValueIndex]);

                IntList objValues = null;
                if (this.objIDMapping.containsKey(intKey)) {
                    objValues = this.objIDMapping.get(intKey);
                } else {
                    objValues = new IntArrayList();
                }

                objValues.add(intValue);
                objIDMapping.put(intKey, objValues);
            }

            objReader.close();
        } catch (Exception e) {
            throw new LinkingException(e);
        }

    }

    public void clean() {
        objIDMapping.clear();
    }

    public int[] getData(int id) throws LinkingException{
        return objIDMapping.get(id).toArray();
    }

    public int[] getAllIDs() throws LinkingException{
        int aryResult[] = new int[objIDMapping.size()];
        Iterator<Integer> objIter = objIDMapping.keySet().iterator();

        int index = 0;
        while( objIter.hasNext() ){
            int intDataEntry = objIter.next();
            aryResult[index]=intDataEntry;
            index++;
        }

        Arrays.sort(aryResult);
        return aryResult;
    }
}
