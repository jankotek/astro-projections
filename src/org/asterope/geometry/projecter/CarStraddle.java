package org.asterope.geometry.projecter;

import org.asterope.geometry.Projecter;

/** Handle the Straddling of the Cartesion
 *  projection when a figure extends accross the Lon=180 line.
 */
class CarStraddle extends Straddle {
   
   Projecter proj;
   
   boolean doClip;
   double clipXMin, clipXMax, clipYMin, clipYMax;
   
   CarStraddle(Projecter input) {
       this.proj = input;
   }
   
   CarStraddle(Car inProj) {
       this((Projecter)inProj);
       doClip = true;
       clipXMin = -Math.PI;
       clipXMax =  Math.PI;
       clipYMin = -Math.PI/2;
       clipYMax =  Math.PI/2;
   }
   
   boolean straddle(double[][] vertices) {
   
   	boolean pos  = false;
	boolean neg  = false;
	boolean both = false;
	int n = vertices[0].length;
//	boolean debug = Math.abs(vertices[0][0]) > 179*Math.PI/180;
	
	// First check to see if we have both positive and negative
	// x values.  If not there is no issue.
	for (int i=0; i<n; i += 1) {
	    if (vertices[0][i] > 0) {
		if (neg) {
		    both = true;
		    break;
		}
		pos = true;
	    } else if (vertices[0][i] < 0) {
		if (pos) {
		    both = true;
		    break;
		}
		neg = true;
	    }
	}
	
	if (both) {
	    double[][] tvert = new double[2][n];
	    for (int i=0; i<n; i += 1) {
		if (vertices[0][i] >= 0) {
		    tvert[0][i] = vertices[0][i];
		    tvert[1][i] = vertices[1][i];
		} else {
		    double[] shadow = proj.shadowPoint(vertices[0][i],vertices[1][i]);
		    tvert[0][i] = shadow[0];
		    tvert[1][i] = shadow[1];
		}
	    }
	    double noStraddle   = testArea(vertices);
	    double haveStraddle = testArea(tvert);
	    
	    // Retain a slight preference for not straddling.
	    return haveStraddle < 0.9*noStraddle;
	}
	return false;
    }
   
    static double testArea(double[][] inputs) {
	return Clip.convexArea(inputs[0].length, inputs[0], inputs[1]);
    }
   
    /** This addresses issues that can occur when
     *  a point is exactly on the boundary.
     *  Here the boundary is assumed symmetric in X.
     */
    void fixShadow(double x, double y, double[] shadow) {
	if ( (x > 0 && shadow[0] > 0) || (x < 0 && shadow[0] < 0)) {
	    shadow[0] = - shadow[0];
	}
    }

   
    double[][][] straddleComponents(double[][] inputs) {

   	int n = inputs[0].length;
	// We'll have two output areas.
	double[][][] areas = new double[2][2][n];
	
	for (int i=0; i<n; i += 1) {
	    double x = inputs[0][i];
	    double y = inputs[1][i];
	    double[] shad = proj.shadowPoint(x,y);
	    
	    fixShadow(x,y,shad);
	    
	    if (x < 0) {
		areas[0][0][i] = shad[0];
		areas[0][1][i] = shad[1];
		areas[1][0][i] = x;
		areas[1][1][i] = y;
	    } else {
		areas[1][0][i] = shad[0];
		areas[1][1][i] = shad[1];
		areas[0][0][i] = x;
		areas[0][1][i] = y;
	    }
	}
	
	if (doClip) {

	    for (int i=0; i<areas.length; i += 1) {
		
		double[] xi = areas[i][0];
		double[] yi = areas[i][1];
		
		double[] xo = new double[12];
		double[] yo = new double[12];
	    
	        int np = Clip.rectClip(n, xi, yi, xo, yo,
			   clipXMin, clipYMin, clipXMax, clipYMax);
	        double[] xv = new double[np];
	        double[] yv = new double[np];
	        System.arraycopy(xo, 0, xv, 0, np);
	        System.arraycopy(yo, 0, yv, 0, np);
		areas[i][0] = xv;
		areas[i][1] = yv;
	    }
	}
	
	return areas;
    }


/**
 * The class implements a fast flux conserving resampling
 * based on the Sutherland-Hodgman clipping algorithm.
 * Incomplete class, cut out from Skyview package to satisfy CarStraddle dependency
 *
 */
    static class Clip{

    /** Calculate the area of a convex polygon.
     * This function calculates the area of a convex polygon
     * by deconvolving the polygon into triangles and summing
     * the areas of the consituents.  The user provides the
     * coordinates of the vertices of the polygon in sequence
     * along the circumference (in either direction and starting
     * at any point).
     *
     * Only distinct vertices should be given, i.e., the
     * first vertex should not be repeated at the end of the list.     *
     * @param	n	The number of vertices in the polygon.
     * @param   x	The x coordinates of the vertices
     * @param   y	The y coordinates of teh vertices
     * @return		The area of the polygon.
     */
    public static double convexArea(int n, double[] x, double[] y) {

	double area = 0;

	for(int i=1; i<n-1; i += 1) {

	    area += triangleArea(x[0],y[0], x[i], y[i], x[i+1], y[i+1]);
	}

	return area;
    }

    /** Calculate the area of an arbitrary triangle.
     *  Use the vector formula
     *     A = 1/2 sqrt(X^2 Y^2 - (X-Y)^2)
     *  where X and Y are vectors describing two sides
     *  of the triangle.
     *
     *  @param x0	x-coordinate of first vertex
     *  @param y0       y-coordinate of first vertex
     *  @param x1       x-coordinate of second vertex
     *  @param y1       y-coordinate of second vertex
     *  @param x2       x-coordinate of third vertex
     *  @param y2       y-coordinate of third vertex
     *
     *  @return         Area of the triangle.
     */

    private static double triangleArea(double x0, double y0,
				      double x1, double y1,
				      double x2, double y2) {

	// Convert vertices to vectors.
	double a = x0-x1;
	double b = y0-y1;
	double e = x0-x2;
	double f = y0-y2;

	double area=  (a*a+b*b)*(e*e+f*f) - (a*e+b*f)*(a*e+b*f);
	if (area <= 0) {
	    return 0; // Roundoff presumably!
	} else {
	    return Math.sqrt(area)/2;
	}
    }

        /** Clip a polygon to a half-plane bounded by a vertical line.
     *  Users can flip the input axis order to clip by a horizontal line.
     *  This is the central operation in the Sutherland-Hodgeman algorithm.
     *
     *  This function uses pre-allocated arrays for
     *  output so that no new objects are generated
     *  during a call.
     *
     *  @param 	n	Number of vertices in the polygon
     *  @param  x	X coordinates of the vertices
     *  @param  y	Y coordinates of the vertices
     *  @param  nx	New X coordinates
     *  @param  ny      New Y coordinates
     *  @param  val	Value at which clipping is to occur.
     *  @param  dir     Direction for which data is to be
     *                  clipped.  true-> clip below val, false->clip above val.
     *
     *  @return         The number of new vertices.
     *
     */
    private static int lineClip(int n,
			       double[] x, double[] y,
			       double[] nx, double[] ny,
                               double val, boolean dir) {

	int	nout=0;

	// Need to handle first segment specially
	// since we don't want to duplicate vertices.
	boolean last = inPlane(x[n-1], val, dir);

	for (int i=0; i < n; i += 1) {

	    if (last) {

		if (inPlane(x[i], val, dir)) {
		    // Both endpoints in, just add the new point
		    nx[nout] = x[i];
		    ny[nout] = y[i];
		    nout    += 1;
		} else {
		    double ycross;
		    // Moved out of the clip region, add the point we moved out
		    if (i == 0) {
		        ycross = y[n-1] + (y[0]-y[n-1])*(val-x[n-1])/(x[0]-x[n-1]);
		    } else {
		        ycross = y[i-1] + (y[i]-y[i-1])*(val-x[i-1])/(x[i]-x[i-1]);
		    }
		    nx[nout] = val;
		    ny[nout] = ycross;
		    nout    += 1;
		    last     = false;
		}

	    } else {

		if (inPlane(x[i], val, dir)) {
		    // Moved into the clip region.  Add the point
		    // we moved in, and the end point.
		    double ycross;
		    if (i == 0) {
		        ycross = y[n-1] + (y[0]-y[n-1])*(val-x[n-1])/(x[i]-x[n-1]);
		    } else {
		        ycross = y[i-1] + (y[i]-y[i-1])*(val-x[i-1])/(x[i]-x[i-1]);
		    }
		    nx[nout]  = val;
		    ny[nout] = ycross;
		    nout += 1;

		    nx[nout] = x[i];
		    ny[nout] = y[i];
		    nout += 1;
		    last     = true;

		} else {
		    // Segment entirely clipped.
		}
	    }
	}
	return nout;
    }

        /**
     * Is the test value on the on the proper side of a line.
     *
     * @param test	Value to be tested
     * @param divider	Critical value
     * @param direction True if values greater than divider are 'in'
     *                  False if smaller values are 'in'.
     * @return          Is the value on the desired side of the divider?
     */
    private static boolean inPlane(double test, double divider, boolean direction) {

        // Note that since we always include
	// points on the dividing line as 'in'.  Not sure
	// if this is important though...

	if (direction) {
	    return test >= divider;
	} else {
	    return test <= divider;
	}
    }

    /** Clip a polygon by a non-rotated rectangle.
     *
     *  This uses a simplified version of the Sutherland-Hodgeman polygon
     *  clipping method.  We assume that the region to be clipped is
     *  convex.  This implies that we will not need to worry about
     *  the clipping breaking the input region into multiple
     *  disconnected areas.
     *    [Proof: Suppose the resulting region is not convex.  Then
     *     there is a line between two points in the region that
     *     crosses the boundary of the clipped region.  However the
     *     clipped boundaries are all lines from one of the two
     *     figures we are intersecting.  This would imply that
     *     this line crosses one of the boundaries in the original
     *     image.  Hence either the original polygon or the clipping
     *     region would need to be non-convex.]
     *
     *  Private arrays are used for intermediate results to minimize
     *  allocation costs.
     *
     *  @param n	Number of vertices in the polygon.
     *  @param x	X values of vertices
     *  @param y        Y values of vertices
     *  @param nx	X values of clipped polygon
     *  @param ny       Y values of clipped polygon
     *
     *  @param          minX Minimum X-value
     *  @param		minY Minimum Y-value
     *  @param          maxX MAximum X-value
     *  @param          maxY Maximum Y-value
     *
     *  @return		Number of vertices in clipped polygon.
     */
    public static int rectClip(int n, double[] x, double[] y, double[] nx, double[] ny,
                               double minX, double minY, double maxX, double maxY) {




    // Intermediate storage used by rectClip.
    // The maximum number of vertices we will get if we start with
    // a convex quadrilateral is 12, but we use larger
    // arrays in case this routine is used is some other context.
    // If we were good we'd be checking this when we add points in
    // the clipping process.

    //JAN KOTEK: This use to be field so instance was cached, but I prefer thread safety
    //TODO cache those instances?
    double[] rcX0 = new double[12];
    double[] rcX1 = new double[12];
    double[] rcY0 = new double[12];
    double[] rcY1 = new double[12];

	int nCurr;

	// lineClip is called four times, once for each constraint.
	// Note the inversion of order of the arguments when
	// clipping vertically.
	//

	nCurr = lineClip(n, x, y, rcX0, rcY0, minX, true);

	if (nCurr > 0) {
	    nCurr = lineClip(nCurr, rcX0, rcY0, rcX1, rcY1, maxX, false);

	    if (nCurr > 0) {
		nCurr = lineClip(nCurr, rcY1, rcX1, rcY0, rcX0, minY, true);

		if (nCurr > 0) {
		    nCurr = lineClip(nCurr, rcY0, rcX0, ny, nx, maxY, false);
		}
	    }
	}

	// We don't need to worry that we might not have set the output arrays.
        // If nCurr == 0, then it doesn't matter that
	// we haven't set nx and ny.  And if it is then we've gone
	// all the way and they are already set.

	return nCurr;
    }
    }



}
