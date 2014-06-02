package env;
import gov.nasa.jpf.jvm.Verify;
import edu.ksu.cis.bandera.Abstraction;

public class Buffer extends env.Vector {
  public static env.Buffer TOP = new env.Buffer();


  public Buffer(){
    // no may se;
  }

  public void register(Watcher param0){
    // begin may se;
    if(Verify.randomBool()){
      param0.registered=true;
    }
    // end may se;
  }

  public void unregister(Watcher param0){
    // begin may se;
    if(Verify.randomBool()){
      param0.registered=false;
    }
    // end may se;
  }

  public void copy(env.Buffer param0){
    // no may se;
  }

  public boolean isEmpty(){
    // no may se;
    return Abstraction.TOP_BOOL;
  }

  public Watcher removeFirst(){
    // no may se;
    return ((Watcher)Verify.randomObject("Watcher"));
  }
}