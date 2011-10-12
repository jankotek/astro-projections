package org.asterope.geometry;

/** This class deprojects a point from a projection plane
 *  onto thecelestial sphere.
 */
public abstract class Deprojecter extends Transformer {
    
    
    /** What is the output dimensionality of a deprojecter? */
    protected int getOutputDimension() {
	return 3;
    }
    
    /** What is the input dimensionality of a deprojecter? */
    protected int getInputDimension() {
	return 2;
    }
}
