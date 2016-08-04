package com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Seat {

    private Integer id;

    private Integer seatClass;

    private String seatArea;

    private String seatLocation;

    private String seatCompartmentType;

    private boolean upperLevel;

    private Integer wagonID;

    private Double price;

    /*
     * TODO: Brauchen wir das im Konstruktor?
     */
    private Integer lockEventID;

    /*
     * Der Konstruktor hat den Preis nicht, weil wir den erst nachträglich hinzufügen (nachdem wir beim Preisdienst
     * waren)
     * 
     * Einen zweiten Konstruktor zu bauen verwirrt JSON bei implizieter Konvertierung
     * 
     * Den hier brauchen wir für auslesen aus der Datenbank, also nehmen wir erstmal den, würde ich vorschlagen
     */
    public Seat(@JsonProperty("id") int id, @JsonProperty("seatClass") int seatClass,
            @JsonProperty("seatArea") String seatArea, @JsonProperty("seatLocation") String seatLocation,
            @JsonProperty("seatCompartmentType") String seatCompartmentType,
            @JsonProperty("upperLevel") boolean upperLevel, @JsonProperty("wagonID") int wagonID) {

        this.setId(id);
        this.setSeatClass(seatClass);
        this.setSeatArea(seatArea);
        this.setSeatLocation(seatLocation);
        this.setSeatSection(seatCompartmentType);
        this.setUpperLevel(upperLevel);
        this.setWagonID(wagonID);
    }

    public int getSeatClass() {
        return seatClass;
    }

    @JsonProperty("seatClass")
    public void setSeatClass(int seatClass) {
        this.seatClass = seatClass;
    }

    public String getSeatArea() {
        return seatArea;
    }

    @JsonProperty("seatArea")
    public void setSeatArea(String seatArea) {
        this.seatArea = seatArea;
    }

    public String getSeatLocation() {
        return seatLocation;
    }

    @JsonProperty("seatLocation")
    public void setSeatLocation(String seatLocation) {
        this.seatLocation = seatLocation;
    }

    public String getSeatSection() {
        return seatCompartmentType;
    }

    @JsonProperty("seatCompartmentType")
    public void setSeatSection(String seatSection) {
        this.seatCompartmentType = seatSection;
    }

    public boolean isUpperLevel() {
        return upperLevel;
    }

    @JsonProperty("upperLevel")
    public void setUpperLevel(boolean upperLevel) {
        this.upperLevel = upperLevel;
    }

    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWagonID() {
        return wagonID;
    }

    @JsonProperty("wagonID")
    public void setWagonID(Integer wagonID) {
        this.wagonID = wagonID;
    }

    public Double getPrice() {
        return price;
    }

    @JsonProperty("price")
    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getLockEventID() {
        return lockEventID;
    }

    @JsonProperty("lockEventID")
    public void setLockEventID(Integer lockEventID) {
        this.lockEventID = lockEventID;
    }

}
