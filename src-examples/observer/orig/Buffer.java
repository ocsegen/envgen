import java.util.*;
/**
 * Implements a set of watchers.  Uses Vector implementation.
 */
public class Buffer extends Vector{

    
    /**
     * Adds <code>w</code> if the set does not contain
     * an identical watcher.
     */
    public void register(Watcher w){
    	synchronized(w){
    		if(!contains(w)){
    			w.registered = true;
    			super.addElement(w);
    		}
    	}
    }

    public boolean isEmpty(){
        return super.isEmpty();
    }
    /**
     * Checks whether this set contains the watcher <code>w</code> 
     * based on method <code>equals</code> of class <code>Watcher</code>.
     */
    public boolean contains(Watcher w){
	Watcher temp;
	for(int i = 0; i < size(); ++i){
	    temp = elementAtIndex(i);
	    if(temp.equals(w))
		return true;
	}
	return false;
    }
    
    /**
     * Copies elements of <code>this</code> set
     * into <code>dest</code>.
     */
    public void copy(Buffer dest){
	if(dest == null)
	    dest = new Buffer();

	for(int i = 0; i<size(); ++i){
	    Watcher w = elementAtIndex(i);
	    dest.addElement(w);
	}
    }


    public  Watcher elementAtIndex(int index){
	return (Watcher)super.elementAt(index);
    }


    public void unregister(Watcher w){
    	synchronized(w){
    	if(super.removeElement(w))
			w.registered = false;
    	}
	
    }
    



    public Watcher removeFirst(){
        Watcher result = elementAtIndex(0);
        unregister(result);
        return result;
    }
}
