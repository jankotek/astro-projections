package org.asterope.geometry;

import static java.lang.Math.*;

/** utlity functions to use with SkyView */
public class Utilities {

    
    /** Create an object of a given class.
     *  The input can be a class in a specified package,
     *  or a full specified class.
     *  We first try to instantiate the object within
     *  the specified package, then try it as a fully
     *  qualified class.
     */
    public static Object newInstance(String cls, String pkg) {

	if (pkg != null) {
	    try {
//		String fullName = pkg+"."+cls;
	        return Class.forName(pkg+"."+cls).newInstance();
	    } catch (Exception e) {
		// OK...  We'll try it without the package prefix
	    }
	}
    try {
        return Class.forName(cls).newInstance();
	} catch (Throwable e) {
		throw new RuntimeException("  Unable to instantiate dynamic class "+cls+" in package "+pkg,e);
	}
    }


        /** Convert a coordinate pair to rade2Vector vectors
     *  @param ra The longitude like coordinate in radians.
     *  @param dec The latitude like coordinate in radians.
     *  @return A double[3] rade2Vector vector corresponding to the coordinates.
     */
    public static double[] rade2Vector(double ra, double dec) {
	    return new double[]{cos(ra)*cos(dec), sin(ra)*cos(dec), sin(dec)};
    }

    /** Convert a coordinate pair to rade2Vector vectors
     *  @param coord The input coordinates
     *  @return A double[3] rade2Vector vector corresponding to the coordinates.
     */
    public static double[] rade2Vector(double[] coord) {
        if(coord.length!=2) throw new IllegalArgumentException("RADE array must have size 2");
        return rade2Vector(coord[0], coord[1]);
    }

    /**
     *  Convert a coordinate pair to rade2Vector vectors.  The user supplies
     *  the array to be filled, assumed to be a vector of length 3.
     *  are created in this version.
     *  @param coord	A double[2] vector of coordinates.
     *  @param unitV    A pre-allocated double[3] rade2Vector vector.  The values
     *                  of this vector will be changed on output.
     */
    public static void rade2Vector(double[] coord, double[] unitV) {
        if(coord.length!=2) throw new IllegalArgumentException("RADE array must have size 2");
        if(coord.length!=3) throw new IllegalArgumentException("Unit vector array must have size 3");
	    unitV[0] = cos(coord[0])*cos(coord[1]);
	    unitV[1] = sin(coord[0])*cos(coord[1]);
	    unitV[2] =               sin(coord[1]);
    }

    /**
     *  Convert a unit vector to the corresponding coordinates.
     *  @param unit	A double[3] normalized spherical vector.
     *  @return         A double[2] coordinate vector with RA&DE.
     */
    public static double[] vector2Rade(double[] unit) {
	    double[] coord = new double[2];
	    vector2Rade(unit, coord);
	    return coord;
    }

    public static double[] vector2Rade(double x, double y, double z) {
	    return vector2Rade(new double[]{x, y, z});
    }

    /**
     * Convert a unit vector to the corresponding coordinates.
     *  @param unit	A double[3] unit vector.
     *  @param coord    A double[2] vector to hold
     *                  the output coordinates.
     */
    public static void vector2Rade(double[] unit, double coord[]) {
        if(unit.length!=3) throw new IllegalArgumentException("Unit vector array must have size 3");
        if(coord.length!=2) throw new IllegalArgumentException("RADE array must have size 2");

        coord[0] = atan2(unit[1], unit[0]);
	    // Ensure that longitudes run from 0 to 2 PI rather than -PI to PI.
	    //
	    if (coord[0] <  0) {
	        coord[0] += 2*PI;
	    }
	    coord[1] = asin(unit[2]);
    }
}
