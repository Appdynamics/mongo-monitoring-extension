/**
 * Copyright 2013 AppDynamics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
