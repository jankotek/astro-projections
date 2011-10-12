package org.asterope.geometry;


import junit.framework.TestCase;

public class CoordinateSystemTest extends TestCase {

  public void  a(Rotater r, double x, double y, double z, double x2, double y2, double z2){
    double[] res = r.transform(new double[]{x,y,z});
    assertEquals(res[0], x2, 1e-15);
    assertEquals(res[1], y2, 1e-15);
    assertEquals(res[2], z2, 1e-15);
  }


  public void  testEcliptic(){
    Rotater cs =  CoordinateSystem.factory("Ecliptic").getRotater();
    a(cs,1,0,0,0.8843379633847844,-0.46684726194410375,-2.3059594739721634E-5);

  }

  public void testXAxis(){
    Rotater r1 = new Rotater("x",Math.toRadians(90), 0 ,0);
    a(r1, 1,0,0, 1,0,0);
    a(r1, 0,1,0, 0,0,-1);
    a(r1, 0,0,1, 0,1,0);
  }

  public void testYAxis(){
    Rotater r1 = new Rotater("y",Math.toRadians(90), 0 ,0);
    a(r1, 1,0,0, 0,0,1);
    a(r1, 0,1,0, 0,1,0);
    a(r1, 0,0,1, -1,0,0);
  }
  public void  testZAxis(){
    Rotater r1 = new Rotater("z",Math.toRadians(90), 0 ,0);
    a(r1, 1,0,0, 0,-1,0);
    a(r1, 0,1,0, 1,0,0);
    a(r1, 0,0,1, 0,0,1);
  }

  public void  testMultiAxisRotation(){
    Rotater r = new Rotater("xyz",Math.toRadians(48), Math.toRadians(48), Math.toRadians(48));
    a(r, 1,0,0,  0.4477357683661733, -0.4972609476841367, 0.7431448254773942);

    double[] v = Utilities.rade2Vector(1,-1);
    a(r,v[0],v[1],v[2], 0.3400649326435842,-0.8575798632289657,-0.3858919794065476);

  }
}