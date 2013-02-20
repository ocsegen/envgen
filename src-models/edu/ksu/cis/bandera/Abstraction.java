/** 
 * Simple stub for the Abstraction class. 
 * Used for compilation of examples only.
 *
 */
package edu.ksu.cis.bandera;

import gov.nasa.jpf.jvm.Verify;

public class Abstraction {

	public static int TOP_INT = 0;

	public static boolean TOP_BOOL = false;

	public static long TOP_LONG = 0;

	public static short TOP_SHORT = 0;

	public static double TOP_DOUBLE = 0;
	
	public static char TOP_CHAR = 'a';
	
	public static byte TOP_BYTE = 0;

	public static String TOP_STRING = "TOP";

	public static float TOP_FLOAT = 0;

	public static Object TOP_OBJ = new Object();

	public static Object getTopObject(String type) {
		return null;
	}
	
	public static int getTopInt(){
		return 0;
	}
	
	public static boolean getTopBool(){
		return false;
	}
	
	public static long getTopLong(){
		return 0;
	}
	
	public static short getTopShort(){
		return 0;
	}

	public static double getTopDouble(){
		return 0;
	}
	
	public static float getTopFloat(){
		return 0;
	}
	
	public static String getTopString(){
		return "top";
	}
	public static String getChoiceString(){
		if (Verify.randomBool())
			return "abc";
		else
			return "efg";
	}
	
	public static String getSymbolicString(){
		if (Verify.randomBool())
			return "symbolic1";
		else
			return "symbolic2";
	}
}
