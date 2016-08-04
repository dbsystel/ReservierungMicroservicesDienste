package com.db.systel.bachelorproject2016.pricingservice.domainmodel;

public class Seat {

	private int seatClass;
	private String seatArea;
	private String seatLocation;
	private String seatCompartmentType;
	private boolean upperLevel;

	public Seat(int seatClass, String seatArea, String seatLocation, String seatCompartmentType, boolean upperLevel) {
		this.setSeatClass(seatClass);
		this.setSeatArea(seatArea);
		this.setSeatLocation(seatLocation);
		this.setSeatSection(seatCompartmentType);
		this.setUpperLevel(upperLevel);
	}

	public int getSeatClass() {
		return seatClass;
	}

	public void setSeatClass(int seatClass) {
		this.seatClass = seatClass;
	}

	public String getSeatArea() {
		return seatArea;
	}

	public void setSeatArea(String seatArea) {
		this.seatArea = seatArea;
	}

	public String getSeatLocation() {
		return seatLocation;
	}

	public void setSeatLocation(String seatLocation) {
		this.seatLocation = seatLocation;
	}

	public String getSeatSection() {
		return seatCompartmentType;
	}

	public void setSeatSection(String seatSection) {
		this.seatCompartmentType = seatSection;
	}

	public boolean isUpperLevel() {
		return upperLevel;
	}

	public void setUpperLevel(boolean upperLevel) {
		this.upperLevel = upperLevel;
	}
	
	
}
