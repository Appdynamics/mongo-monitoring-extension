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

public class Asserts{
   	private Number msg;
   	private Number regular;
   	private Number rollovers;
   	private Number user;
   	private Number warning;

 	public Number getMsg(){
		return this.msg;
	}
	public void setMsg(Number msg){
		this.msg = msg;
	}
 	public Number getRegular(){
		return this.regular;
	}
	public void setRegular(Number regular){
		this.regular = regular;
	}
 	public Number getRollovers(){
		return this.rollovers;
	}
	public void setRollovers(Number rollovers){
		this.rollovers = rollovers;
	}
 	public Number getUser(){
		return this.user;
	}
	public void setUser(Number user){
		this.user = user;
	}
 	public Number getWarning(){
		return this.warning;
	}
	public void setWarning(Number warning){
		this.warning = warning;
	}
}
