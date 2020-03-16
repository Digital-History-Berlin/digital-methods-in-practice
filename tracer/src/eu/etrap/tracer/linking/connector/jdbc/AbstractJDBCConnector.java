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



package eu.etrap.tracer.linking.connector.jdbc;

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.linking.connector.AbstractConnector;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import eu.etrap.tracer.linking.LinkingException;

/**
 * Created on 07.05.2011 11:26:24 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractJDBCConnector extends AbstractConnector {

    protected Connection objConnection = null;
    protected PreparedStatement objGetDataStatement = null;
    protected PreparedStatement objAllIDsStatement = null;
    protected String sqlGetDataStatement = null;
    protected String sqlAllIDsStatement = null;
    protected String strDBConnectionURL = null;
    protected String strDBUserName = null;
    protected String strDBPassword = null;

    @Override
    public void init() throws ConfigurationException {
        try {
            super.init();
            strTaxonomyCode = "01-02-01-00";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            objConnection = DriverManager.getConnection(strDBConnectionURL, strDBUserName, strDBPassword);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public void prepareData(String strSource) throws LinkingException {
        try {
            objGetDataStatement = objConnection.prepareStatement(sqlGetDataStatement);
            objAllIDsStatement = objConnection.prepareStatement(sqlAllIDsStatement);
        } catch (Exception e) {
            throw new LinkingException(e);
        }
    }

    public void clean() {
        try {
            objGetDataStatement = null;
            objAllIDsStatement = null;
            objConnection.close();
            objConnection = null;
        } catch (Exception e) {
            ConfigurationContainer.println("Troubles while closing db connection: " + e.getMessage());
        }

    }

    public int[] getData(int id) throws LinkingException {
        int aryResult[] = null;

        try {
            objGetDataStatement.setInt(1, id);
            aryResult = fetchData(objGetDataStatement);
        } catch (Exception e) {
            throw new LinkingException(e);
        }

        return aryResult;
    }

    public int[] getAllIDs() throws LinkingException{
        int aryResult[] = null;

        try {
            aryResult = fetchData(objAllIDsStatement);
        } catch (Exception e) {
            throw new LinkingException(e);
        }

        return aryResult;
    }

    protected int[] fetchData(PreparedStatement obStatement) throws SQLException {
        int aryResult[] = null;

        ResultSet objResultSet = obStatement.executeQuery();
        int intSize = getResultSize(objResultSet);

        aryResult = new int[intSize];

        int index = 0;
        while (objResultSet.next()) {
            aryResult[index] = objResultSet.getInt(1);
            index++;
        }

        return aryResult;
    }

    protected int getResultSize(ResultSet objDBResultSet) throws SQLException {
        objDBResultSet.last();
        int numRows = objDBResultSet.getRow();
        objDBResultSet.beforeFirst();
        return numRows;
    }
}
