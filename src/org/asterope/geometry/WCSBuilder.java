package org.asterope.geometry;



/**
 * Helper class to create WCS (World Coordinate System) for Skyview
 */
public class WCSBuilder {

  static final String[] projections = {"Ait","Arc","Stg","Arc","Toa",
          "Csc","Sfl","Tan","Xtn","Car","Sin","Hpx","Zea"};

  /**
   * Reference RA and De, all points are rotated to this position
   */
  public double refRa, refDe;

  /**
   * Canvas width and height in Pixels
   */
  public double width, height;
  /**
   * Projection to be used
   */
  public String projection = "Sin";
  /**
   * xscale 
   */
  public double xscale = 1d;
  /**
   * yscale, is -1 because pixels are numbered opposite way on computer screen 
   */
  public double yscale = -1d;
  
  /**
   * Angular size of pixel 
   */
  public double pixelScale;

  /** anticlockwise rotation*/
  public double rotation = 0;


  public WCS build() throws TransformationException {
    /*
    * coordinate system
    */
    CoordinateSystem coordinateSystem = CoordinateSystem.J2000;

    /*
    * projection
    */
    Projection proj = createProjection(projection, refRa, refDe);

    // Find where the requested center is with respect to
    // the fixed center of this projection.
    double[] coords =  Utilities.rade2Vector(refRa, refDe);

    if(proj == null || proj.getProjecter()==null)
        throw new IllegalArgumentException("projection was not created");

    if(proj.getProjecter().inverse() == null)
        throw new IllegalArgumentException("projection "+projection+" does not support inverse function");

    coords = proj.getRotater().transform(coords);
    coords = proj.getProjecter().transform(coords);
    if(coords.length != 2) throw new InternalError();

    /*
    * scaler
    */
    if(width == 0 || height == 0)
        throw new IllegalArgumentException("Zero width or height");
	  
    if(pixelScale == 0)
        throw new InternalError("zero pixelScale");

    double xs = xscale * pixelScale;
    double ys = yscale * pixelScale;
    if(xs == 0|| ys == 0) throw new InternalError();


    Scaler scaler = new Scaler(0.5 * width + coords[0] / xs,
            0.5 * height - coords[1] / ys,
            -1 / xs, 0,
            0, 1 / ys);

    //add rotation
    Scaler rScaler = new Scaler(0, 0, Math.cos(rotation), Math.sin(rotation),
					-Math.sin(rotation), Math.cos(rotation));
    scaler = rScaler.add(scaler);


    WCS wcs = new WCS(coordinateSystem, proj, scaler);

    return wcs;
  }
  
  public void setPixelScaleFromFOV(double fov){
	pixelScale = fov/Math.sqrt(width * width + height*height);  
  }

  /**
   * construct projection with rotation to reference point,
   * ref. point center of view
   */
  protected Projection createProjection(String projName, double ra, double de) throws TransformationException {
    //is fixed projection? is used without rotation to reference point
    if (Projection.fixedPoint(projName) != null) {
      Projection proj = new Projection(projName);
      proj.setReference(ra, de);
      return proj;
    } else {
      //not fixed projection, needs rotation
      Projection proj = new Projection(projName, new double[]{ra,de});
      //rotate to reference point
      return proj;
    }
  }


}