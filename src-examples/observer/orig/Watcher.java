import java.util.Observer;
import java.util.Observable;
public class Watcher implements Observer{
	public boolean registered = false;
        /**
          * @observable 
          *   INVOKE begin (this, Subject subject): subject == o;
          */              
	public void update(Observable o, Object arg) {
		
		 // a recently unregistered Observer will be
	     //   wrongly notified when it doesn't care
		
		assert registered;
	}
}


