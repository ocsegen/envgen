package env;
import java.util.Observable;

public class Subject extends Observable{

    public void changeState() {
	setChanged();
	notifyObservers();
    }
    
    // copied from Observable
    protected boolean changed = false;
    protected Buffer obs;
    public Subject() {
	obs = new Buffer();
    }
    
    /**
     * @observable 
     *   RETURN end (this, Watcher watcher): watcher == o;
     */              
    public synchronized void addObserver(Watcher o) {
	
	    obs.register(o);
	
    }

    /**
     * @observable
     *   INVOKE begin (this, Watcher watcher): watcher == o;
     */              
    public synchronized void deleteObserver(Watcher o) {
	obs.unregister(o);
    }

    public synchronized boolean hasChanged() {
	return changed;
    }
    public void notifyObservers() {
	notifyObservers(null);
    }
    
    /* COMMENT FROM java.util.Observer
     *
     * We don't want the Observer doing callbacks  
     * into arbitrary code while holding its own Monitor.
     * The code where we extract each Observable from
     * the Vector and store the state of the Observer
     * needs synchronization, but notifying observers
     * does not (should not).  The worst result of any
     * potential race-condition here is that:
     * 1) a newly-added Observer will miss a
     *   notification in progress
     * 2) a recently unregistered Observer will be
     *   wrongly notified when it doesn't care
     */
    
    /**
     * @observable
     *   INVOKE begin (this);
     *   RETURN end (this);
     * @assert
     *   LOCATION[point] inWindow : false;
     */              
    //   LOCATION[point] inWindow (this, Watcher w): w == cw;
    public void notifyObservers(Object arg) {
	Watcher cw;
	
	Buffer localBuffer = new Buffer();
	synchronized (this) {
	    if (!changed)
		return;
	    obs.copy(localBuffer);
	    changed = false;
	}
	if (obs.size() != localBuffer.size()) {
point:            cw = null;
	}
	while(!localBuffer.isEmpty()){
	    cw = localBuffer.removeFirst();
	    cw.update(this, arg);
	}  
    }
    protected synchronized void setChanged() {
	changed = true;
    }
}


