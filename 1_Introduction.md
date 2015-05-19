# 1. Introduction #

**OCSEGen** is a tool for closing open components and systems with their execution context.

The tool is implemented on top of [Soot: Java Optimization Framework](http://www.sable.mcgill.ca/soot/) and uses its many features:
  * loading of Java classes into Jimple intermediate representation
  * method control flow graphs
  * fixed point computation to perform data flow analysis


## Project Layout ##

  * src/main
    * main java sources
  * src/models
    * stubs for modeling primitives such as jpf.Verify
    * needed for compilation of generated code
  * src/examples
    * observer/orig
      * original code of the observer example
    * observer/mod
      * generated code of the observer example
  * lib
    * soot libraries needed by the tool
  * configs
    * configuration files to specify the tool mode
    * generate drivers or stubs
    * use specifications or code analysis
    * specify unit under analysis
  * specs
    * specification files
  * scripts
    * xml scripts to run code analysis and generation for examples
  * eclipse
    * launch configs to run the tool in eclipse


Here is the outline of wiki:
  * [1. Introduction](1_Introduction.md)
  * [2. Installation](2_Installation.md)
  * [3. User Guide](3_UserGuide.md)
  * [4. Developer Guide](4_DeveloperGuide.md)
