environment{

  driver-assumptions{
    re{
      
      Main: Subject s; Watcher w1; Watcher w2  #
            
      Change: (changeState())^{2} #

      Register: (addObserver() | deleteObserver())^{2} #
      
    }
  }
 
}
