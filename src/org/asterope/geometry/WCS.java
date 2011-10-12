package org.asterope.geometry;



/** A World Coordinate System defines
 *  a translation between celestial and pixel
 *  coordinates.  Note that in many cases
 *  FITS keywords describe the transformations
 *  in the other direction (from pixel to celestial)
 *  but we follow the convention that forward transformations
 *  are from celestial to pixel.
 *  Given a WCS object, wcs,  the pixel-celestial coordinates trasnformation
 *  is simply wcs.inverse();
 */

public class WCS extends Converter {
    
    /** This includes a nominal 'scale' for the WCS. While this
     *  can often be calculated from the transformation, that may sometimes
     *  be difficult.
     */
    private double wcsScale;
    
    /** The coordinate system used in the WCS. */
    private CoordinateSystem csys;
    
    /** The Projection used in the WCS. */
    private Projection        proj;
    
    /** The linear distorter used in the WCS. */
    private Distorter        distort;
    
    /** The scaler used in the WCS. */
    private Scaler           scale;
    



    /** Create a simple WCS given a scaler, CoordinateSystem and Projection.
     */
    public WCS(CoordinateSystem csys, Projection proj, Scaler scale) 
      throws TransformationException {
	 
	this.csys  = csys;
	this.proj  = proj;
	this.scale = scale;
	  
	add(csys.getSphereDistorter());
	add(csys.getRotater());
	add(proj.getRotater());
	add(proj.getProjecter());
	add(proj.getDistorter());
	add(scale);
	setWCSScale(scale);
    }
    
    

    // Accessor methods.
    
    /** Get the CoordinateSystem used in the WCS */
    public CoordinateSystem getCoordinateSystem() {
	return csys;
    }
    
    /** Get the projection used in the WCS */
    public Projection getProjection() {
	return proj;
    }
    
    /** Get the linear scaler used in the projection */
    public Scaler getScaler() {
	return scale;
    }
    
    /** Get the plane distorter used in the projection (or null) */
    public Distorter getDistorter() {
	return distort;
    }
    
    /** Set the scale of the transformation */
    private void setWCSScale(Scaler s) {
	// Use the determinant of the transformation matrix to get the scale
	double[] p   = s.getParams();
        double   det = p[2]*p[5]-p[3]*p[4];
	wcsScale     = 1/Math.sqrt(Math.abs(det));
    }
    
    /** Which axis is the longitude */
    private int lonAxis = -1;
    /** Which axis is the latitude */
    private int latAxis = -1;
    



    /** Get the nominal scale of the WCS.
     */
    public double getScale() {
	return wcsScale;
    }
	
    

}
