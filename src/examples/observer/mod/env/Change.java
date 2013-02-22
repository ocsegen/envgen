package env;
import gov.nasa.jpf.jvm.Verify;
import edu.ksu.cis.bandera.Abstraction;

public class Change extends java.lang.Thread {
  public Subject s;
  public Watcher w1;
  public Watcher w2;


  public Change(Subject param0, Watcher param1, Watcher param2){
    s=param0;
    w1=param1;
    w2=param2;
  }

  public void run(){
    for(int i=0;i<2;++i){
      System.out.println("@EnvDriverThread: s.changeState[]");
      s.changeState();
    }
  }
}