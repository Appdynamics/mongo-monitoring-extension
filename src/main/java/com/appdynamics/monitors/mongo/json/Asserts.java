
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
