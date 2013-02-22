package env;
import gov.nasa.jpf.jvm.Verify;
import edu.ksu.cis.bandera.Abstraction;

public class Vector {
  public static env.Vector TOP = new env.Vector();


  public Vector(){
    // no may se;
  }

  public int size(){
    // no may se;
    return Abstraction.TOP_INT;
  }
}