/**
 *  Simple stub for the actual Verify class.
 *  Used for compilation of examples only.
 *  JPF picks up the actual Verify class, when running.
 */
package gov.nasa.jpf.jvm;

public class Verify{
	 /**
	   * Returns a random number between 0 and max inclusive.
	   */
	  public static int random (int max) {
	    // this is only executed when not running JPF
	    //return random.nextInt(max + 1);
		  return 0;
	  }

	  /**
	   * Returns a random boolean value, true or false. Note this gets
	   * handled by the native peer, and is just here to enable running
	   * instrumented applications w/o JPF
	   */
	  public static boolean randomBool () {
	    // this is only executed when not running JPF
	    //return random.nextBoolean();
		  return false;
	  }
	  
	  /**
	   * Returns a random boolean value, true or false. Note this gets
	   * handled by the native peer, and is just here to enable running
	   * instrumented applications w/o JPF
	   */
	  public static boolean getBoolean () {
	    // this is only executed when not running JPF
	    //return random.nextBoolean();
		  return false;
	  }

	  // Backwards compatibility START
	  public static Object randomObject (String type) {
	    return null;
	  }

	  public static Object randomReachable(String type, Object obj){
		  return null;
	  }
	  public static void assertTrue(boolean b){
		  
	  }

	  	
	  public static void beginAtomic(){
		  
	  }
	  
	  public static void endAtomic(){
		  
	  }
}
