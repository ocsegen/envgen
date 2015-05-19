# 2. Installation #


## Requirements ##
  * Java 1.7
  * ant
  * Soot 2.5.0
    * Packaged with the tool
  * Mercurial, to check out the code
  * Eclipse
    * Optional, the tool can run on the command line, using ant


## Building and Running Examples ##

Check out the code: hg clone https://code.google.com/p/envgen/

To build the code

  * Using ant, in the root dir run the following commands
    * ant clean
    * ant
    * ant gen-observer-driver-re
      * generates drivers from regular expressions
    * ant gen-observer-stubs-se
      * generates stubs using side-effects analysis

  * Using eclipse
    * Build Project
    * Run Configuration: EnvGen-observer-driver-re
      * generates drivers from regular expressions
    * Run Configuration: EnvGen-observer-stubs-se
      * generates stubs using side-effects analysis