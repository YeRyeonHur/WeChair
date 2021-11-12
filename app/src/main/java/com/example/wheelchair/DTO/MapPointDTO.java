package com.example.wheelchair.DTO;

public class MapPointDTO {
    private double latitude, longitude;
    private String name;
    private int type; //type=0 화장실 type=1버스정류장

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
    }
}
