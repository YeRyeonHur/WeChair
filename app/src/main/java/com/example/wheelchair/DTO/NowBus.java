package com.example.wheelchair.DTO;

public class NowBus {
    private String busNum;
    private int time;
    private String busType;

    public NowBus(String busNum, int time, String busType){
        this.busNum = busNum;
        this.time = time;
        this.busType = busType;
    }

    public String getBusNum() {
        return busNum;
    }

    public void setBusNum(String busNum) {
        this.busNum = busNum;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

}
