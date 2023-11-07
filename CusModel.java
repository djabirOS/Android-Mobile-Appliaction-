package com.ostaxi.app.Model;

import com.google.firebase.database.DataSnapshot;

import java.text.DecimalFormat;


public class CusModel {


    private String id = "", name = "", phone = "", profileImage = "default", notificationKey = "";
    private float ratingsAvg = 0;


    public CusModel(String id, String name, String phone, String profileImage, String notificationKey, float ratingsAvg) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.profileImage = profileImage;
        this.notificationKey = notificationKey;
        this.ratingsAvg = ratingsAvg;
    }

    public CusModel(String id) {
        this.id = id;
    }


    public CusModel() {
    }



    public void setData(DataSnapshot dataSnapshot) {
        id = dataSnapshot.getKey();
        if (dataSnapshot.child("name").getValue() != null) {
            name = dataSnapshot.child("name").getValue().toString();
        }
        if (dataSnapshot.child("phone").getValue() != null) {
            phone = dataSnapshot.child("phone").getValue().toString();
        }
        if (dataSnapshot.child("profileImageUrl").getValue() != null) {
            profileImage = dataSnapshot.child("profileImageUrl").getValue().toString();
        }
        if(dataSnapshot.child("notificationKey").getValue()!=null){
            notificationKey = dataSnapshot.child("notificationKey").getValue().toString();
        }
        int ratingSum = 0;
        float ratingsTotal = 0;
        for (DataSnapshot child : dataSnapshot.child("rating").getChildren()) {
            ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
            ratingsTotal++;
        }
        if (ratingsTotal != 0) {
            ratingsAvg = ratingSum / ratingsTotal;
        }
    }



    public String getRatingString(){
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(ratingsAvg);
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getNotificationKey() {
        return notificationKey;
    }
}
