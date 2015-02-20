package edu.ksu.cis.envgen;

import java.util.Properties;

import edu.ksu.cis.envgen.applinfo.ModuleInfo;
/** Basic building block of the envgen modules */

public abstract class EnvGenModule {
	/** Unit classes. */
	protected ModuleInfo unit;
	
	/** Env classes */
	protected ModuleInfo env;
	
	protected ApplInfo applInfo; 

	protected Properties properties;
	
	public ModuleInfo getUnit(){
		return unit;
	}
	
	public ModuleInfo getEnv(){
		return env;
	}
	
	public ApplInfo getApplInfo(){
		return applInfo;
	}
	
	public Properties getProperties(){
		return properties;
	}
	
	public void setUnit(ModuleInfo unit){
		this.unit = unit;
	}
	
	public void setEnv(ModuleInfo env){
		this.env = env;
	}
	
	public void setApplInfo(ApplInfo applInfo){
		this.applInfo = applInfo;
	}
	
	public void setProperties(Properties properties){
		this.properties = properties;
	}
	
	public abstract void setOptions(Properties properties);
}
