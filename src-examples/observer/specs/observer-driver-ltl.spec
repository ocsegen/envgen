environment{
  setup{
  //spec for main
   Subject s; Watcher w1; Watcher w2;
  }


  driver-assumptions{
    ltl{
      
      Change: [] changeState() #

      Register: [] (addObserver() | deleteObserver()) #
      
      //CleanUp: join() # 
    }
  }
 
}
