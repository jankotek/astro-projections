package org.asterope.geometry.csys;

import org.asterope.geometry.Component;
import org.asterope.geometry.CoordinateSystem;
import org.asterope.geometry.Rotater;
import org.asterope.geometry.SphereDistorter;


/** This class implements Besselian coordinate systems.
 *  These systems are not simple rotations from the reference
 *  coordinate frame.  These coordinate systems are implemented
 *  such that the rotation matrix is appropriate for J2000 coordinates
 *  but the rectify and derectify function perform transformation
 *  from/to Besselian coordinates.  The transformations do
 *  not use any proper motion or distance information supplied
 *  by the user.  The methods in this class are based on P. Wallaces
 *  SLA library substantially modified for use within Java and SkyView.
 *
 *  From Wiki:
 *
    A Besselian epoch, named after the German mathematician and astronomer
 Friedrich Bessel (1784–1846), is an epoch that is based on a Besselian y
 ear of 365.242198781 days, which is a tropical year measured at the point
 where the Sun's longitude is exactly 280°. Since 1984, Besselian
 equinoxes/epochs have been superseded by Julian equinoxes/epochs.
 The current standard equinox/epoch is J2000.0, which is a Julian equinox/epoch.

 Besselian equinoxes/epochs are calculated according to:

 B = 1900.0 + (Julian date − 2415020.31352) / 365.242198781
 The previous standard equinox/epoch was B1950.0, a Besselian equinox/epoch.

 Since the right ascension and declination of stars are constantly changing
 due to precession, astronomers always specify these with reference to
 a particular equinox. Historically used Besselian equinoxes include
 B1875.0, B1900.0, B1925.0 and B1950.0. The official constellation boundaries
 were defined in 1930 using B1875.0.
 *
 */
public class Besselian extends CoordinateSystem
  implements Component {

    private double epoch;
    
    /** These two work arrays mean that this class is not
     *  thread safe.  If multiple threads are to be used each thread
     *  needs its own coordinate system object.
     */
    /** Get a CoordinateSystem of a given epoch.
     *  @param epoch The epoch as a calendar year (possibly fractional).
     */    
    public Besselian(double epoch) {
	this.epoch     = epoch;
    }
    
    /** This coordinate system is not just a rotation away from the reference frame.*/
    public boolean isRotation() {
	return false;
    }
    
    public String getName() {
	return "B"+epoch;
    }
    
    public String getDescription() {
	return "A Beseelian (FK4 based) equatorial coordinate system.  Dynamic terms are not included.";
    }
    
    
    public Rotater getRotater() {
	return precession(epoch);
    }
    
    public SphereDistorter getSphereDistorter() {
	return new org.asterope.geometry.spheredistorter.Besselian();
    }

    /**
     * Calculate the Besselian Precession between 1950 and the given epoch.
     */

    private Rotater precession(double epoch) {

        double DAS2R = 4.8481368110953599358991410235794797595635330237270e-6;
   
        //  Interval between basic epoch B1850.0 and beginning epoch in TC */
        double bigt  = ( 1950 - 1850 ) / 100.0;
   
        // Interval over which precession required, in tropical centuries */
        double t = (epoch - 1950 ) / 100.0;
 
        //  Euler angles */
        double tas2r = t * DAS2R;
        double w     = 2303.5548 + ( 1.39720 + 0.000059 * bigt ) * bigt;
        double zeta  = (w + ( 0.30242 - 0.000269 * bigt + 0.017996 * t ) * t ) * tas2r;
        double z     = (w + ( 1.09478 + 0.000387 * bigt + 0.018324 * t ) * t ) * tas2r;
        double theta = ( 2005.1125 + ( - 0.85294 - 0.000365* bigt ) * bigt +       
                       ( - 0.42647 - 0.000365 * bigt - 0.041802 * t ) * t ) * tas2r;
 
        return  new Rotater( "ZYZ", -zeta, theta, -z);
    }
}
