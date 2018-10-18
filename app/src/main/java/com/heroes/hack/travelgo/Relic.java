package com.heroes.hack.travelgo;

/**
 * Created by Angelika Iskra on 12.10.2018.
 */
public class Relic {

    private int id;
    private int exp;
    private String identification;
    private String datingOfObj;
    private String placeName;
    private String districtName;
    private String voivodeshipName;

    private Double latitude;
    private Double longitude;

    public Relic(int id, String identification, String datingOfObj, String placeName, String districtName, String voivodeshipName, Double latitude, Double longtitude, int exp) {
        this.id = id;
        this.identification = identification;
        this.datingOfObj = datingOfObj;
        this.placeName = placeName;
        this.districtName = districtName;
        this.voivodeshipName = voivodeshipName;
        this.latitude = latitude;
        this.longitude = longtitude;
        this.exp = exp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getDatingOfObj() {
        return datingOfObj;
    }

    public void setDatingOfObj(String datingOfObj) {
        this.datingOfObj = datingOfObj;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getVoivodeshipName() {
        return voivodeshipName;
    }

    public void setVoivodeshipName(String voivodeshipName) {
        this.voivodeshipName = voivodeshipName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }
}
