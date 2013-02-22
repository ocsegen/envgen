package env;
import gov.nasa.jpf.jvm.Verify;
import edu.ksu.cis.bandera.Abstraction;

public class Register extends java.lang.Thread {
  public Subject s;
  public Watcher w1;
  public Watcher w2;


  public Register(Subject param0, Watcher param1, Watcher param2){
    s=param0;
    w1=param1;
    w2=param2;
  }

  public void run(){
    for(int i=0;i<2;++i){
      int choice2=Verify.random(1);
      switch(choice2){
          case 0:
                System.out.println("@EnvDriverThread: s.deleteObserver[TOP]");
                s.deleteObserver(((Watcher)Verify.randomObject("Watcher")));
                break;
          case 1:
                System.out.println("@EnvDriverThread: s.addObserver[TOP]");
                s.addObserver(((Watcher)Verify.randomObject("Watcher")));
                break;
          }
    }
  }
}