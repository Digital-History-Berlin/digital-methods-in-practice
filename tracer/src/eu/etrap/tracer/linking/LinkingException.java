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





package eu.etrap.tracer.linking;

/**
 * Created on 07.12.2010 17:32:25 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class LinkingException extends Exception {

    /**
     * Creates a new instance of <code>LinkingException</code> without detail message.
     */
    public LinkingException() {
    }


    /**
     * Constructs an instance of <code>LinkingException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public LinkingException(String msg) {
        super(msg);
    }

    public LinkingException(Exception e) {
        super(e);
    }
}