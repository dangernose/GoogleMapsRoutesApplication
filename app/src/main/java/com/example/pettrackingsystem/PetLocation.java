package com.example.pettrackingsystem;

public class PetLocation {
    Integer ID;
    Double Lat;
    Double Lng;
    String date;
    String time;

    public PetLocation(Integer ID, Double lat, Double lng, String date, String time) {
        this.ID = ID;
        Lat = lat;
        Lng = lng;
        this.date = date;
        this.time = time;
    }

    public Double getLat() {
        return Lat;
    }

    public void setLat(Double lat) {
        Lat = lat;
    }

    public Double getLng() {
        return Lng;
    }

    public void setLng(Double lng) {
        Lng = lng;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
