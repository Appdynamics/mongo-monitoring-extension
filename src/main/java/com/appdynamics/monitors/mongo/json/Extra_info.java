
package com.appdynamics.monitors.mongo.json;

public class Extra_info{
   	private String note;
   	private Number page_faults;

 	public String getNote(){
		return this.note;
	}
	public void setNote(String note){
		this.note = note;
	}
 	public Number getPage_faults(){
		return this.page_faults;
	}
	public void setPage_faults(Number page_faults){
		this.page_faults = page_faults;
	}
}
