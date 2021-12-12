package com.example.wheelchair.DTO;

public class MapPointDTO {
    private double latitude, longitude;
    private int estbDate;
    private String wfcltId;//busStation에서 node id
    private String faclTyCd;//busStation 이면 CODE = BUS
    private boolean infoFlag = false;
    private String name;
    private int type; //type=0 화장실 type=1 버스정류장 2 = 음식점

    public int getNodeNm() {
        return nodeNm;
    }

    public void setNodeNm(int nodeNm) {
        this.nodeNm = nodeNm;
    }

    private int nodeNm;//bus station number

    public boolean[] getInfo() {
        return info;
    }

    public void setInfo(boolean[] info) {
        this.info = info;
        infoFlag = true;
    }

    public boolean hasInfo() {
        return infoFlag;
    }

    private boolean info[];
    //계단, 승강설비, 장애인전용 주차구역, 주출입구 접근로, 주출입구 높이차이 제거

    public String getWfcltId() {
        return wfcltId;
    }


    public void setWfcltId(String wfcltId) {
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