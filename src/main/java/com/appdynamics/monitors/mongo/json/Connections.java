
package com.appdynamics.monitors.mongo.json;

public class Connections{
   	private Number available;
   	private Number current;

 	public Number getAvailable(){
		return this.available;
	}
	public void setAvailable(Number available){
		this.available = available;
	}
 	public Number getCurrent(){
		return this.current;
	}
	public void setCurrent(Number current){
		this.current = current;
	}
}
