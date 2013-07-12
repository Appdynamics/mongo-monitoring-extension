
package com.appdynamics.monitors.mongo.json;

public class Cursors{
   	private Number clientCursors_size;
   	private Number timedOut;
   	private Number totalOpen;

 	public Number getClientCursors_size(){
		return this.clientCursors_size;
	}
	public void setClientCursors_size(Number clientCursors_size){
		this.clientCursors_size = clientCursors_size;
	}
 	public Number getTimedOut(){
		return this.timedOut;
	}
	public void setTimedOut(Number timedOut){
		this.timedOut = timedOut;
	}
 	public Number getTotalOpen(){
		return this.totalOpen;
	}
	public void setTotalOpen(Number totalOpen){
		this.totalOpen = totalOpen;
	}
}
