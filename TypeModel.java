package com.ostaxi.app.Model;

import android.graphics.drawable.Drawable;



public class TypeModel {

    String name, id,description;
    Drawable image;
    int people;


    public TypeModel(String id, String name, Drawable image, int people, String description){
        this.id = id;
        this.name = name;
        this.image = image;
        this.people = people;
        this.description =description;
    }

    public String getId() {
        return id;
    }

    public Drawable getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public int getPeople() {
        return people;
    }

    public String getDescription() {
        return description;
    }


}
