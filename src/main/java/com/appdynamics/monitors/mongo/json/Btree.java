
package com.appdynamics.monitors.mongo.json;

public class Btree{
   	private Number accesses;
   	private Number hits;
   	private Number missRatio;
   	private Number misses;
   	private Number resets;

 	public Number getAccesses(){
		return this.accesses;
	}
	public void setAccesses(Number accesses){
		this.accesses = accesses;
	}
 	public Number getHits(){
		return this.hits;
	}
	public void setHits(Number hits){
		this.hits = hits;
	}
 	public Number getMissRatio(){
		return this.missRatio;
	}
	public void setMissRatio(Number missRatio){
		this.missRatio = missRatio;
	}
 	public Number getMisses(){
		return this.misses;
	}
	public void setMisses(Number misses){
		this.misses = misses;
	}
 	public Number getResets(){
		return this.resets;
	}
	public void setResets(Number resets){
		this.resets = resets;
	}
}
