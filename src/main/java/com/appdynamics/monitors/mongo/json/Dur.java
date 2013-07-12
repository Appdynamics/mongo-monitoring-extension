
package com.appdynamics.monitors.mongo.json;

public class Dur{
   	private Number commits;
   	private Number commitsInWriteLock;
   	private Number compression;
   	private Number earlyCommits;
   	private Number journaledMB;
   	private TimeMs timeMs;
   	private Number writeToDataFilesMB;

 	public Number getCommits(){
		return this.commits;
	}
	public void setCommits(Number commits){
		this.commits = commits;
	}
 	public Number getCommitsInWriteLock(){
		return this.commitsInWriteLock;
	}
	public void setCommitsInWriteLock(Number commitsInWriteLock){
		this.commitsInWriteLock = commitsInWriteLock;
	}
 	public Number getCompression(){
		return this.compression;
	}
	public void setCompression(Number compression){
		this.compression = compression;
	}
 	public Number getEarlyCommits(){
		return this.earlyCommits;
	}
	public void setEarlyCommits(Number earlyCommits){
		this.earlyCommits = earlyCommits;
	}
 	public Number getJournaledMB(){
		return this.journaledMB;
	}
	public void setJournaledMB(Number journaledMB){
		this.journaledMB = journaledMB;
	}
 	public TimeMs getTimeMs(){
		return this.timeMs;
	}
	public void setTimeMs(TimeMs timeMs){
		this.timeMs = timeMs;
	}
 	public Number getWriteToDataFilesMB(){
		return this.writeToDataFilesMB;
	}
	public void setWriteToDataFilesMB(Number writeToDataFilesMB){
		this.writeToDataFilesMB = writeToDataFilesMB;
	}
}
