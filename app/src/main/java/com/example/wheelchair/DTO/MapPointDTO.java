package com.example.wheelchair.DTO;

public class MapPointDTO {
    private double latitude, longitude;
    private int estbDate, wfcltId;
    private String faclTyCd;
    public int getWfcltId() {
        return wfcltId;
    }

    public void setWfcltId(int wfcltId) {
        this.wfcltId = wfcltId;
    }

    public void setfaclTyCd(String faclTyCd){ this.faclTyCd = faclTyCd;}
    public int getEstbDate() {
        return estbDate;
    }
    public String getFaclTyCd(){ return faclTyCd;}
    public void setEstbDate(int estbDate) {
        this.estbDate = estbDate;
    }

    private String name;
    private int type; //type=0 화장실 type=1 버스정류장 2 = 음식점

    public MapPointDTO (){
        super();
    }

    public MapPointDTO(double latitude, double longitude, String name, int type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }//
}
