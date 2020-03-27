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



package eu.etrap.tracer.linking.connector.cooccaccess;

import de.uni_leipzig.asv.coocc.BinFileConstructor;
import de.uni_leipzig.asv.coocc.BinFileMultCol;
import de.uni_leipzig.asv.filesort.FileSort;
import eu.etrap.medusa.config.ConfigurationException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import bak.pcj.set.IntSet;
import bak.pcj.set.IntOpenHashSet;
import eu.etrap.medusa.config.ConfigurationContainer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import eu.etrap.tracer.linking.LinkingException;
import eu.etrap.tracer.linking.connector.AbstractConnector;

/**
 * Created on 07.05.2011 11:24:09 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractCooccaccessConnector extends AbstractConnector {

    protected BinFileMultCol objDataStore = null;
    protected IntSet objUniqueListOfIDs = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strTaxonomyCode = "01-02-02-00";
    }

    public void prepareData(String strSource) throws LinkingException {
        String strSortedFile = strSource + ".sorted";

        if (!new File(strSortedFile).exists()) {
            ConfigurationContainer.println("\tSort file " + strSource + " to "
                    + strSortedFile);
            int sortOrder[] = new int[]{0, 1};
            char sortTypes[] = new char[]{'i', 'i'};
            FileSort sort = new FileSort("\t", sortOrder, sortTypes);
            sort.sort(strSource, strSortedFile);
        } else {
            ConfigurationContainer.println("\t" + strSource + " is ALREADY sorted in "
                    + strSortedFile);
        }

        //BinFileMultColPreparer col = new BinFileMultColPreparer( strSortedFile, 2);
        objDataStore = BinFileConstructor.getBinFileMultCol(strSortedFile, 2, false);

        fillUniqueListOfIDS(strSource);
    }

    protected void fillUniqueListOfIDS(String strSource) throws LinkingException {
        try {
            objUniqueListOfIDs = new IntOpenHashSet();
            BufferedReader objReader = new BufferedReader(new FileReader(strSource));
            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objUniqueListOfIDs.add(Integer.parseInt(strSplit[0]));
            }

            objReader.close();
        } catch (Exception e) {
            throw new LinkingException(e);
        }
    }

    public void clean() {
        objUniqueListOfIDs.clear();
        this.objDataStore = null;
    }

    public int[] getData(int id) throws LinkingException {
        List objList = objDataStore.getData(id);
        int intSize = objList.size();
        int aryResult[] = new int[intSize];

        Iterator objIter = objList.iterator();

        int index = 0;
        while (objIter.hasNext()) {
            Integer objData[] = (Integer[]) objIter.next();
            aryResult[index] = objData[0];
            index++;
        }

        return aryResult;
    }

    public int[] getAllIDs() throws LinkingException {
        int aryResult[] = objUniqueListOfIDs.toArray();
        Arrays.sort(aryResult);
        return aryResult;
    }
}
