
package com.appdynamics.monitors.mongo.json;

public class TimeMs{
   	private Number dt;
   	private Number prepLogBuffer;
   	private Number remapPrivateView;
   	private Number writeToDataFiles;
   	private Number writeToJournal;

 	public Number getDt(){
		return this.dt;
	}
	public void setDt(Number dt){
		this.dt = dt;
	}
 	public Number getPrepLogBuffer(){
		return this.prepLogBuffer;
	}
	public void setPrepLogBuffer(Number prepLogBuffer){
		this.prepLogBuffer = prepLogBuffer;
	}
 	public Number getRemapPrivateView(){
		return this.remapPrivateView;
	}
	public void setRemapPrivateView(Number remapPrivateView){
		this.remapPrivateView = remapPrivateView;
	}
 	public Number getWriteToDataFiles(){
		return this.writeToDataFiles;
	}
	public void setWriteToDataFiles(Number writeToDataFiles){
		this.writeToDataFiles = writeToDataFiles;
	}
 	public Number getWriteToJournal(){
		return this.writeToJournal;
	}
	public void setWriteToJournal(Number writeToJournal){
		this.writeToJournal = writeToJournal;
	}
}
