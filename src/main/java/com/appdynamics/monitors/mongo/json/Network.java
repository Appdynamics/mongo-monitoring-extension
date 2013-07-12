package com.appdynamics.monitors.mongo.json;

public class Network {
	private Number bytesIn;
	private Number bytesOut;
	private Number numRequests;

	public Number getBytesIn() {
		return bytesIn;
	}
	public void setBytesIn(long bytesIn) {
		this.bytesIn = bytesIn;
	}
	public Number getBytesOut() {
		return bytesOut;
	}
	public void setBytesOut(long bytesOut) {
		this.bytesOut = bytesOut;
	}
	public Number getNumRequests() {
		return numRequests;
	}
	public void setNumRequests(int numRequests) {
		this.numRequests = numRequests;
	}
}
