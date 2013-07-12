package com.appdynamics.monitors.mongo.json;

public class Mem {
	private Number bits;
	private Number resident;
	private Number virtual;
	private Number mapped;
	private Number mappedWithJournal;
	
	public Number getBits() {
		return bits;
	}
	public void setBits(Number bits) {
		this.bits = bits;
	}
	public Number getResident() {
		return resident;
	}
	public void setResident(Number resident) {
		this.resident = resident;
	}
	public Number getVirtual() {
		return virtual;
	}
	public void setVirtual(Number virtual) {
		this.virtual = virtual;
	}
	public Number getMapped() {
		return mapped;
	}
	public void setMapped(Number mapped) {
		this.mapped = mapped;
	}
	public Number getMappedWithJournal() {
		return mappedWithJournal;
	}
	public void setMappedWithJournal(Number mappedWithJournal) {
		this.mappedWithJournal = mappedWithJournal;
	}
	
}
