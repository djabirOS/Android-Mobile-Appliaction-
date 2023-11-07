package com.ostaxi.app.Model;

import com.google.android.gms.maps.model.LatLng;


public class LocationModel {

    private LatLng coordinates;
    private String name = "";


    public LocationModel(LatLng coordinates, String name){
        this.coordinates = coordinates;
        this.name = name;
    }


    public LocationModel(){
    }


    public LatLng getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
