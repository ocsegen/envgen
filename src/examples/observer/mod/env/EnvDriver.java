package env;
import gov.nasa.jpf.jvm.Verify;
import edu.ksu.cis.bandera.Abstraction;

public class EnvDriver {


  public static void main(java.lang.String[] param0){
    System.out.println("@EnvDriverThread: ");
    Subject s = new Subject();
    System.out.println("@EnvDriverThread: ");
    Watcher w1 = new Watcher();
    System.out.println("@EnvDriverThread: ");
    Watcher w2 = new Watcher();
    env.Change Change0 = new env.Change(s, w1, w2);
    Change0.start();
    env.Register Register0 = new env.Register(s, w1, w2);
    Register0.start();
  }
}