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


package eu.etrap.tracer.selection;

/**
 * Created on 08.04.2011 11:09:47 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public interface WordClassWeightsInterface {

    public static int NOUN = 15;
    public static int VERB = 14;
    public static int PARTICIPLE = 13;
    public static int ADJECTIVE = 12;
    public static int ADVERB = 11;
    public static int PRONOUN = 10;
    public static int NUMERAL = 9;
    public static int INTERJECTION = 8;
    public static int PARTICLE = 7;
    public static int PREPOSITION = 6;
    public static int CONJUNCTION = 5;
    public static int ARTICLE = 4;
    public static int PUNCTUATION = 3;
    public static int EXCLAMATION = 2;
    public static int DEFAULT = 0;

    public static Character TAG_NOUN = 'n';
    public static Character TAG_VERB = 'v';
    public static Character TAG_PARTICIPLE = 't';
    public static Character TAG_ADJECTIVE = 'a';
    public static Character TAG_ADVERB = 'd';
    public static Character TAG_PRONOUN = 'p';
    public static Character TAG_NUMERAL = 'm';
    public static Character TAG_INTERJECTION = 'i';
    public static Character TAG_PARTICLE = 'g';
    public static Character TAG_PREPOSITION = 'r';
    public static Character TAG_CONJUNCTION = 'c';
    public static Character TAG_ARTICLE = 'l';
    public static Character TAG_PUNCTUATION = 'u';
    public static Character TAG_EXCLAMATION = 'e';
    public static Character TAG_UNSET = '-';
    public static Character TAG_UNKOWN = '_';
}