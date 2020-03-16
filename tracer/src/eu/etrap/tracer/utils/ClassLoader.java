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


package eu.etrap.tracer.utils;

import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.linking.Linking;
import eu.etrap.tracer.linking.connector.Connector;
import eu.etrap.tracer.preprocessing.Preprocessing;
import eu.etrap.tracer.scoring.Scoring;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.featuring.Training;
import eu.etrap.tracer.postprocessing.Postprocessing;

/**
 * Created on 27.12.2010 10:11:09 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class ClassLoader {

    public static Preprocessing loadPreprocessingImpl(String CLASS_IMPL) throws ConfigurationException {

        Class classImpl = null;
        Preprocessing objPreprocess = null;

        try {
            classImpl = Class.forName(CLASS_IMPL);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("ClassNotFoundException: " + e.getMessage(), e);
        }

        try {
            objPreprocess = (Preprocessing) classImpl.newInstance();
        } catch (InstantiationException e) {
            throw new ConfigurationException("InstantiationException: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("IllegalAccessException: " + e.getMessage(), e);
        }

        objPreprocess.init();

        return objPreprocess;
    }

    public static Training loadTrainingImpl(String CLASS_IMPL) throws ConfigurationException {

        Class classImpl = null;
        Training objTraining = null;

        try {
            classImpl = Class.forName(CLASS_IMPL);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("ClassNotFoundException: " + e.getMessage(), e);
        }

        try {
            objTraining = (Training) classImpl.newInstance();
        } catch (InstantiationException e) {
            throw new ConfigurationException("InstantiationException: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("IllegalAccessException: " + e.getMessage(), e);
        }

        objTraining.init();

        return objTraining;
    }

    public static Selection loadSelectionImpl(String CLASS_IMPL) throws ConfigurationException {

        Class classImpl = null;
        Selection objSelection = null;

        try {
            classImpl = Class.forName(CLASS_IMPL);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("ClassNotFoundException: " + e.getMessage(), e);
        }

        try {
            objSelection = (Selection) classImpl.newInstance();
        } catch (InstantiationException e) {
            throw new ConfigurationException("InstantiationException: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("IllegalAccessException: " + e.getMessage(), e);
        }

        objSelection.init();

        return objSelection;
    }

    public static Linking loadLinkingImpl(String LINKING_IMPL) throws ConfigurationException {

        Class classImpl = null;
        Linking objLinking = null;

        try {
            classImpl = Class.forName(LINKING_IMPL);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("ClassNotFoundException: " + e.getMessage(), e);
        }

        try {
            objLinking = (Linking) classImpl.newInstance();
        } catch (InstantiationException e) {
            throw new ConfigurationException("InstantiationException: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("IllegalAccessException: " + e.getMessage(), e);
        }

        objLinking.init();

        return objLinking;
    }

    public static Connector loadConnectorImpl(String CONNECTOR_IMPL) throws ConfigurationException {

        Class classImpl = null;
        Connector objConnector = null;

        try {
            classImpl = Class.forName(CONNECTOR_IMPL);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("ClassNotFoundException: " + e.getMessage(), e);
        }

        try {
            objConnector = (Connector) classImpl.newInstance();
        } catch (InstantiationException e) {
            throw new ConfigurationException("InstantiationException: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("IllegalAccessException: " + e.getMessage(), e);
        }

        return objConnector;
    }

    public static Scoring loadScoringImpl(String SCORING_IMPL) throws ConfigurationException {

        Class classImpl = null;
        Scoring objScoring = null;

        try {
            classImpl = Class.forName(SCORING_IMPL);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("ClassNotFoundException: " + e.getMessage(), e);
        }

        try {
            objScoring = (Scoring) classImpl.newInstance();
        } catch (InstantiationException e) {
            throw new ConfigurationException("InstantiationException: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("IllegalAccessException: " + e.getMessage(), e);
        }

        objScoring.init();

        return objScoring;
    }
    
        public static Postprocessing loadPostprocessingImpl(String POSTPROCESSING_IMPL) throws ConfigurationException {

        Class classImpl = null;
        Postprocessing objPostprocessing = null;

        try {
            classImpl = Class.forName(POSTPROCESSING_IMPL);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("ClassNotFoundException: " + e.getMessage(), e);
        }

        try {
            objPostprocessing = (Postprocessing) classImpl.newInstance();
        } catch (InstantiationException e) {
            throw new ConfigurationException("InstantiationException: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("IllegalAccessException: " + e.getMessage(), e);
        }

        objPostprocessing.init();

        return objPostprocessing;
    }
}
