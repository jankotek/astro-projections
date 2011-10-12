package org.asterope.geometry;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class WCSBuilderTest extends TestCase {
    
   private final double D2R = Math.PI / 180d;

   public void testCreateProjection()  throws Exception {

        testProjection("Sin",D2R * 0, D2R * 0);
        testProjection("Sin",D2R * 90, D2R * 45);
        testProjection("Tan",D2R * 90, D2R * 45);
        testProjection("Car",D2R * 0, D2R * 0);
        //testProjection("Car",D2R * 13, D2R * 47); 
        //TODO more projections tests
    }

    protected void testProjection(final String name, final double ra, final double de) throws Exception {
    	System.out.println(name);
    	WCSBuilder wcs = new WCSBuilder();
        final Projection p = wcs.createProjection(name, ra,de);
        testCenterPoint(ra, de, p);        
        testThreadSafe(name, ra, de, p);
    }

    /**
     * At previous version of Skyview projecter was producing wierd results when accesed from more threads.
     * Now (July 2010) it seems to be fixed.  
     */
	private void testThreadSafe(final String name, final double ra, final double de, final Projection p) throws InterruptedException,
			Exception {
		
		final int threadNum = 10;
        final List<Thread> threads = new ArrayList<Thread>();
        final List<Exception> excep = new ArrayList<Exception>();
        for(int i = 0; i<threadNum; i++){
        	Thread t = new Thread(){
        		@Override public void run() {
        			try{
        				while(!isInterrupted()){
        		        	final double ra2 = ra + Math.random()*10*D2R;
        		        	final double de2 = de + Math.random()*10*D2R;

        					testPoint(name,ra2 , de2, p);
        				}
        			}catch (Exception e){
        				excep.add(e);
        			}
        		}
        	};
        	threads.add(t);
        	t.setDaemon(true);
        	t.start();
        }
        //wait 1/5 second and shutdown threads
        Thread.sleep(200);
        for(Thread t:threads){
        	t.interrupt();
        }
        //rethrow any exceptions
        for(Exception e:excep)
        	throw e;
	}

	private void testCenterPoint(final double ra, final double de, final Projection p) {
		//try to project center
		double[] testPoint = Utilities.rade2Vector(ra, de);
		if(p.getRotater()==null)
			return; //if there is no rotater, projection have defined other central point
		testPoint =p.getRotater().transform(testPoint);		
        testPoint = p.getProjecter().transform(testPoint);

        assertTrue(testPoint.length == 2);
        //should be projected to zero
        assertTrue(Math.abs(testPoint[0])<1e-8);
        assertTrue(Math.abs(testPoint[1])<1e-8);
	}

    /**
     * Test that single RA DE point is projected and deprojected to same position 
     */
	private void testPoint(String name, double ra, double de, final Projection p) {
		double[] testPoint = Utilities.rade2Vector(ra, de);
		
		if(p.getRotater()!=null)
			testPoint =p.getRotater().transform(testPoint);
        testPoint = p.getProjecter().transform(testPoint);

        assertTrue(testPoint.length == 2);
        
        //inverse of projection
        testPoint = p.getProjecter().inverse().transform(testPoint);
        if(p.getRotater()!=null)
        	testPoint = p.getRotater().inverse().transform(testPoint);
        
        //check coordinates agree
        double[] testPoint2 = Utilities.rade2Vector(ra, de);
        assertEquals(testPoint2[0], testPoint[0],1e-7);
        assertEquals(testPoint2[1], testPoint[1],1e-7);
	}


    public void testBuild() throws TransformationException {
        WCSBuilder wcsBuilder = new WCSBuilder();
        wcsBuilder.refRa = D2R * 10;
        wcsBuilder.refDe = D2R * 10;
        wcsBuilder.width = 800;
        wcsBuilder.height = 600;
        wcsBuilder.projection = "Sin";
        wcsBuilder.pixelScale = 1/ (D2R * 16 * 800);

        WCS wcs= wcsBuilder.build();

        double[] ref = Utilities.rade2Vector(wcsBuilder.refRa, wcsBuilder.refDe);

        double[] v2 = wcs.transform(ref);
        assertEquals(v2[0], 400d);
        assertEquals(v2[1], 300d);

    }
}