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
