
package com.appdynamics.monitors.mongo.json;

public class Admin{
   	private TimeAcquiringMicros timeAcquiringMicros;
   	private TimeLockedMicros timeLockedMicros;

 	public TimeAcquiringMicros getTimeAcquiringMicros(){
		return this.timeAcquiringMicros;
	}
	public void setTimeAcquiringMicros(TimeAcquiringMicros timeAcquiringMicros){
		this.timeAcquiringMicros = timeAcquiringMicros;
	}
 	public TimeLockedMicros getTimeLockedMicros(){
		return this.timeLockedMicros;
	}
	public void setTimeLockedMicros(TimeLockedMicros timeLockedMicros){
		this.timeLockedMicros = timeLockedMicros;
	}
}
