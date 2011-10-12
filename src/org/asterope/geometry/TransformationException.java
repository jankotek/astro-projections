package org.asterope.geometry;

/** This class is thrown when an error
 *  occurs relating to transformations among
 *  frames.
 */
public class TransformationException extends Exception {
    public TransformationException(){
    }

    static final long serialVersionUID = 1L;

    public TransformationException(String msg) {
	super(msg);
    }
}
