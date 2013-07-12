
package com.appdynamics.monitors.mongo.json;

public class ServerStats{
   	private Asserts asserts;
   	private BackgroundFlushing backgroundFlushing;
   	private Connections connections;
   	private Cursors cursors;
   	private Dur dur;
   	private Extra_info extra_info;
   	private GlobalLock globalLock;
   	private String host;
   	private IndexCounters indexCounters;
   	private LocalTime localTime;
   	private Mem mem;
   	private Network network;
   	private Number ok;
   	private Opcounters opcounters;
   	private Number pid;
   	private String process;
   	private String serverUsed;
   	private Number uptime;
   	private Number uptimeEstimate;
   	private Number uptimeMillis;
   	private String version;
   	private boolean writeBacksQueued;

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
}
