
package com.appdynamics.monitors.mongo.json;

public class CurrentQueue{
   	private Number readers;
   	private Number total;
   	private Number writers;

 	public Number getReaders(){
		return this.readers;
	}
	public void setReaders(Number readers){
		this.readers = readers;
	}
 	public Number getTotal(){
		return this.total;
	}
	public void setTotal(Number total){
		this.total = total;
	}
 	public Number getWriters(){
		return this.writers;
	}
	public void setWriters(Number writers){
		this.writers = writers;
	}
}
