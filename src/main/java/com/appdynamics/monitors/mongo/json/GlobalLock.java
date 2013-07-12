
package com.appdynamics.monitors.mongo.json;

public class GlobalLock{
   	private ActiveClients activeClients;
   	private CurrentQueue currentQueue;
   	private Number lockTime;
   	private Number totalTime;

 	public ActiveClients getActiveClients(){
		return this.activeClients;
	}
	public void setActiveClients(ActiveClients activeClients){
		this.activeClients = activeClients;
	}
 	public CurrentQueue getCurrentQueue(){
		return this.currentQueue;
	}
	public void setCurrentQueue(CurrentQueue currentQueue){
		this.currentQueue = currentQueue;
	}
 	public Number getLockTime(){
		return this.lockTime;
	}
	public void setLockTime(Number lockTime){
		this.lockTime = lockTime;
	}
 	public Number getTotalTime(){
		return this.totalTime;
	}
	public void setTotalTime(Number totalTime){
		this.totalTime = totalTime;
	}
}
