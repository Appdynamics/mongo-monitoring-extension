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

public class ServerStats{
    private String host;
    private String version;
    private String process;
    private Number pid;
    private Number uptime;
    private Number uptimeMillis;
    private Number uptimeEstimate;
    private LocalTime localTime;

   	private Asserts asserts;
   	private BackgroundFlushing backgroundFlushing;
   	private Connections connections;
   	private Cursors cursors;
   	private Dur dur;
   	private Extra_info extra_info;
   	private GlobalLock globalLock;
   	private IndexCounters indexCounters;

    private Network network;
    private Opcounters opcounters;
    private OpcountersRepl opcountersRepl;
    private boolean writeBacksQueued;
   	private Mem mem;

   	private Number ok;

   	private String serverUsed;


 	public Asserts getAsserts(){
		return this.asserts;
	}
	public void setAsserts(Asserts asserts){
		this.asserts = asserts;
	}
 	public BackgroundFlushing getBackgroundFlushing(){
		return this.backgroundFlushing;
	}
	public void setBackgroundFlushing(BackgroundFlushing backgroundFlushing){
		this.backgroundFlushing = backgroundFlushing;
	}
 	public Connections getConnections(){
		return this.connections;
	}
	public void setConnections(Connections connections){
		this.connections = connections;
	}
 	public Cursors getCursors(){
		return this.cursors;
	}
	public void setCursors(Cursors cursors){
		this.cursors = cursors;
	}
 	public Dur getDur(){
		return this.dur;
	}
	public void setDur(Dur dur){
		this.dur = dur;
	}
 	public Extra_info getExtra_info(){
		return this.extra_info;
	}
	public void setExtra_info(Extra_info extra_info){
		this.extra_info = extra_info;
	}
 	public GlobalLock getGlobalLock(){
		return this.globalLock;
	}
	public void setGlobalLock(GlobalLock globalLock){
		this.globalLock = globalLock;
	}
 	public String getHost(){
		return this.host;
	}
	public void setHost(String host){
		this.host = host;
	}
 	public IndexCounters getIndexCounters(){
		return this.indexCounters;
	}
	public void setIndexCounters(IndexCounters indexCounters){
		this.indexCounters = indexCounters;
	}
 	public LocalTime getLocalTime(){
		return this.localTime;
	}
	public void setLocalTime(LocalTime localTime){
		this.localTime = localTime;
	}
 	public Mem getMem(){
		return this.mem;
	}
	public void setMem(Mem mem){
		this.mem = mem;
	}
 	public Network getNetwork(){
		return this.network;
	}
	public void setNetwork(Network network){
		this.network = network;
	}
 	public Number getOk(){
		return this.ok;
	}
	public void setOk(Number ok){
		this.ok = ok;
	}
 	public Opcounters getOpcounters(){
		return this.opcounters;
	}
	public void setOpcounters(Opcounters opcounters){
		this.opcounters = opcounters;
	}
 	public Number getPid(){
		return this.pid;
	}
	public void setPid(Number pid){
		this.pid = pid;
	}
 	public String getProcess(){
		return this.process;
	}
	public void setProcess(String process){
		this.process = process;
	}
 	public String getServerUsed(){
		return this.serverUsed;
	}
	public void setServerUsed(String serverUsed){
		this.serverUsed = serverUsed;
	}
 	public Number getUptime(){
		return this.uptime;
	}
	public void setUptime(Number uptime){
		this.uptime = uptime;
	}
 	public Number getUptimeEstimate(){
		return this.uptimeEstimate;
	}
	public void setUptimeEstimate(Number uptimeEstimate){
		this.uptimeEstimate = uptimeEstimate;
	}
 	public Number getUptimeMillis(){
		return this.uptimeMillis;
	}
	public void setUptimeMillis(Number uptimeMillis){
		this.uptimeMillis = uptimeMillis;
	}
 	public String getVersion(){
		return this.version;
	}
	public void setVersion(String version){
		this.version = version;
	}
 	public boolean getWriteBacksQueued(){
		return this.writeBacksQueued;
	}
	public void setWriteBacksQueued(boolean writeBacksQueued){
		this.writeBacksQueued = writeBacksQueued;
	}

    public OpcountersRepl getOpcountersRepl() {
        return opcountersRepl;
    }

    public void setOpcountersRepl(OpcountersRepl opcountersRepl) {
        this.opcountersRepl = opcountersRepl;
    }
}
