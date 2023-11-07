package com.ostaxi.app.Model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.location.Location;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ostaxi.app.SendNotification;
import com.ostaxi.app.Utils;
import com.ostaxi.app.R;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;


public class RideModel implements Cloneable{


    DatabaseReference rideRef;
    private double duration, distance, estimatedPrice;
    private Double ridePrice = 0.0;
    private float calculatedRideDistance, timePassed = 0;
    private Long timestamp;
    private int rating = 0, cancelledType = 0, state;
    private DriverModel mDriver;
    private CusModel mCustomer;
    Activity activity;
    Boolean ended = false, customerPaid = false, driverPaid = false, cancelled = false;
    private String id;
    private float rideDistance = 0;
    private LocationModel pickup, current, destination;
    private String  requestService = "type_1", car = "--", cancelledReason;


    public RideModel(DriverModel mDriver, CusModel mCustomer, Activity activity, Boolean ended, Boolean customerPaid, Boolean driverPaid, Boolean cancelled, String id, float rideDistance, LocationModel pickup, LocationModel current, LocationModel destination, String requestService, String car, String cancelledReason, DatabaseReference rideRef, double duration, double distance, double estimatedPrice, Double ridePrice, float calculatedRideDistance, float timePassed, Long timestamp, int rating, int cancelledType, int state) {
        this.mDriver = mDriver;
        this.mCustomer = mCustomer;
        this.activity = activity;
        this.ended = ended;
        this.customerPaid = customerPaid;
        this.driverPaid = driverPaid;
        this.cancelled = cancelled;
        this.id = id;
        this.rideDistance = rideDistance;
        this.pickup = pickup;
        this.current = current;
        this.destination = destination;
        this.requestService = requestService;
        this.car = car;
        this.cancelledReason = cancelledReason;
        this.rideRef = rideRef;
        this.duration = duration;
        this.distance = distance;
        this.estimatedPrice = estimatedPrice;
        this.ridePrice = ridePrice;
        this.calculatedRideDistance = calculatedRideDistance;
        this.timePassed = timePassed;
        this.timestamp = timestamp;
        this.rating = rating;
        this.cancelledType = cancelledType;
        this.state = state;
    }

    public RideModel(Activity activity, String id){
        this.id = id;
        this.activity = activity;
    }

    public RideModel(){}


    public int checkRide(){
        if (current == null) {
            Toast.makeText(activity.getApplicationContext(), "Can't get location", Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (destination == null) {
            Toast.makeText(activity.getApplicationContext(), "Please pick a destination", Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (pickup == null) {
            Toast.makeText(activity.getApplicationContext(), "Please pick a pickup point", Toast.LENGTH_SHORT).show();
            return -1;
        }

        return 0;
    }



    public void setData(DataSnapshot dataSnapshot){
        id = dataSnapshot.getKey();

        pickup = new LocationModel();
        destination = new LocationModel();

        if(dataSnapshot.child("pickup").child("name").getValue()!=null){
            pickup.setName(dataSnapshot.child("pickup").child("name").getValue().toString());
        }
        if(dataSnapshot.child("destination").child("name").getValue()!=null){
            destination.setName(dataSnapshot.child("destination").child("name").getValue().toString());
        }
        if(dataSnapshot.child("pickup").child("lat").getValue()!=null && dataSnapshot.child("pickup").child("lng").getValue()!=null){
            pickup.setCoordinates(
                    new LatLng(Double.parseDouble(dataSnapshot.child("pickup").child("lat").getValue().toString()),
                                Double.parseDouble(dataSnapshot.child("pickup").child("lng").getValue().toString())));
        }
        if(dataSnapshot.child("destination").child("lat").getValue()!=null && dataSnapshot.child("destination").child("lng").getValue()!=null){
            destination.setCoordinates(
                    new LatLng(Double.parseDouble(dataSnapshot.child("destination").child("lat").getValue().toString()),
                                Double.parseDouble(dataSnapshot.child("destination").child("lng").getValue().toString())));
        }


        if(dataSnapshot.child("service").getValue() != null){
            requestService = dataSnapshot.child("service").getValue().toString();
        }
        if(dataSnapshot.child("customerId").getValue() != null){
            mCustomer = new CusModel(dataSnapshot.child("customerId").getValue().toString());
        }
        if(dataSnapshot.child("driverId").getValue() != null){
            mDriver = new DriverModel(dataSnapshot.child("driverId").getValue().toString());
        }
        if(dataSnapshot.child("ended").getValue() != null){
            ended = Boolean.parseBoolean(dataSnapshot.child("ended").getValue().toString());
        }
        if(dataSnapshot.child("cancelled").getValue() != null){
            cancelled = Boolean.valueOf(dataSnapshot.child("cancelled").getValue().toString());
        }
        if(dataSnapshot.child("cancelled_info").child("type").getValue() != null){
            cancelled = true;
            cancelledType = Integer.parseInt(dataSnapshot.child("cancelled_info").child("type").getValue().toString());
        }
        if(dataSnapshot.child("cancelled_info").child("reason").getValue() != null){
            cancelledReason = dataSnapshot.child("cancelled_info").child("reason").getValue().toString();
        }
        if(dataSnapshot.child("state").getValue() != null){
            state = Integer.parseInt(dataSnapshot.child("state").getValue().toString());
        }
        if(dataSnapshot.child("timestamp").getValue() != null){
            timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
        }
        if(dataSnapshot.child("price").getValue() != null){
            ridePrice = Double.valueOf(dataSnapshot.child("price").getValue().toString());
        }
        if(dataSnapshot.child("car").getValue() != null){
            car = dataSnapshot.child("car").getValue().toString();
        }
        if(dataSnapshot.child("customerPaid").getValue() != null){
            customerPaid = true;
        }
        if(dataSnapshot.child("driverPaidOut").getValue() != null){
            driverPaid = true;
        }
        if(dataSnapshot.child("rating").getValue() != null){
            rating = Integer.parseInt(dataSnapshot.child("rating").getValue().toString());
        }

        if(dataSnapshot.child("calculated_duration").getValue() != null){
            duration = Double.parseDouble(dataSnapshot.child("calculated_duration").getValue().toString());
        }
        if(dataSnapshot.child("calculated_distance").getValue() != null){
            distance = Double.parseDouble(dataSnapshot.child("calculated_distance").getValue().toString());
        }
        if(dataSnapshot.child("calculated_Price").getValue() != null){
            estimatedPrice = Double.parseDouble(dataSnapshot.child("calculated_Price").getValue().toString());
        }

        rideRef = FirebaseDatabase.getInstance().getReference().child("ride_info").child(this.id);

        //Calculate the ride distance, by calculating the direct distance between pickup and destination
        if(this.pickup.getCoordinates() != null && this.destination.getCoordinates() != null){
            Location loc1 = new Location("");
            loc1.setLatitude(this.pickup.getCoordinates().latitude);
            loc1.setLongitude(this.pickup.getCoordinates().longitude);

            Location loc2 = new Location("");
            loc2.setLatitude(this.destination.getCoordinates().latitude);
            loc2.setLongitude(this.destination.getCoordinates().longitude);

            calculatedRideDistance = loc1.distanceTo(loc2) / 1000;
        }
    }


    public void postRideInfo(){


        Log.e("poopp", "postRideInfo: "+"po" );
        rideRef = FirebaseDatabase.getInstance().getReference().child("ride_info");

        id =  rideRef.push().getKey();
        DatabaseReference requestLocation = FirebaseDatabase.getInstance().getReference().child("customer_requests");

        GeoFire geoFire = new GeoFire(requestLocation);
        geoFire.setLocation(id,new GeoLocation(pickup.getCoordinates().latitude, pickup.getCoordinates().longitude));

        String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        HashMap map = new HashMap();
        map.put("service", requestService);
        map.put("state", 0);
        map.put("customerId", customerId);
        map.put("ended", false);

        map.put("calculated_duration", duration);
        map.put("calculated_distance", distance);
        map.put("calculated_Price", Utils.rideCostEstimate(distance, duration));
        map.put("rating_calculated", false);
        map.put("rating", -1);
        map.put("creation_timestamp", ServerValue.TIMESTAMP);
        map.put("destination/name", destination.getName());
        map.put("destination/lat", destination.getCoordinates().latitude);
        map.put("destination/lng", destination.getCoordinates().longitude);
        map.put("pickup/name", pickup.getName());
        map.put("pickup/lat", pickup.getCoordinates().latitude);
        map.put("pickup/lng", pickup.getCoordinates().longitude);

        rideRef.child(id).updateChildren(map);
        rideRef = rideRef.child(id);


    }


    public void recordRide(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ride_info").child(id);

        HashMap map = new HashMap();
        map.put("ended", true);
        map.put("state", 3);
        map.put("distance", rideDistance);
        map.put("timestamp", ServerValue.TIMESTAMP);

        ref.updateChildren(map);

        new SendNotification("Make sure to rate the driver and pay for the ride","ride ended", mCustomer.getNotificationKey());
    }


    public void pickedCustomer(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ride_info").child(id);

        HashMap map = new HashMap();
        map.put("state", 2);
        map.put("timestamp_picked_customer", ServerValue.TIMESTAMP);

        ref.updateChildren(map);
    }



    public String getDate() {
        if(timestamp == null){return "--";}
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("MM-dd-yyyy, hh:mm", cal).toString();
        return date;
    }


    public void cancelRide(){
        if(id == null){return;}
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ride_info").child(id);

        HashMap map = new HashMap();
        map.put("state", -1);
        map.put("cancelled", true);

        ref.updateChildren(map);
    }


    public void confirmDriver(){
        HashMap map = new HashMap();
        map.put("state", 1);
        map.put("driverId", FirebaseAuth.getInstance().getCurrentUser().getUid());

        this.rideRef.updateChildren(map);
    }


    public void showDialog(Activity activity) {
        try {
            RideModel mTempRide = (RideModel) this.clone();
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_ride_review);

            Button mConfirm = dialog.findViewById(R.id.confirm);
            RatingBar mRate = dialog.findViewById(R.id.rate);
            TextView mName = dialog.findViewById(R.id.name);;
            ImageView mImage = dialog.findViewById(R.id.image);

            mName.setText(mTempRide.getDriver().getNameDash());

            if (!mDriver.getProfileImage().equals("default"))
                Glide.with(activity).load(mDriver.getProfileImage()).apply(RequestOptions.circleCropTransform()).into(mImage);

            mConfirm.setOnClickListener(view -> {
                if (mRate.getNumStars() == 0) {
                    return;
                }
                mTempRide.getRideRef().child("rating").setValue(mRate.getRating());
                dialog.dismiss();





                final Dialog dialog2 = new Dialog(activity);
                dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog2.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
                dialog2.setCancelable(false);
                dialog2.setContentView(R.layout.dialog_payment);

                Button mConfirm2 = dialog2.findViewById(R.id.pay);
                TextView amount = dialog2.findViewById(R.id.amount);;

                amount.setText("£"+String.valueOf(distance*2));


                mConfirm2.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View view) {
                             DatabaseReference pp = FirebaseDatabase.getInstance().getReference().child("ride_info").child(id).child("price");

                             pp.setValue(String.valueOf(distance*2));
                             DatabaseReference aa = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(mDriver.getId());
                             aa.addListenerForSingleValueEvent(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot snapshot) {

                                     if(snapshot.hasChild("payout_amount")){


                                         String payout = snapshot.child("payout_amount").getValue().toString();

                                         float p = Float.parseFloat(payout);

                                         float pp = (float) (p+(distance*2));

                                         aa.child("payout_amount").setValue(String.valueOf(pp));


                                     }else{

                                         aa.child("payout_amount").setValue(String.valueOf(distance*2));

                                     }
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError error) {

                                 }
                             });
                             aa.child("payout_amount");

                             dialog2.dismiss();

                         }
                     });



                dialog2.show();


            });
            dialog.show();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }


    public DriverModel getDriver() {
        return mDriver;
    }
    public void setDriver(DriverModel mDriver) {
        this.mDriver = mDriver;
    }

    public CusModel getCustomer() {
        return mCustomer;
    }
    public void setCustomer(CusModel mCustomer) {
        this.mCustomer = mCustomer;
    }

    public LocationModel getPickup() {
        return pickup;
    }
    public void setPickup(LocationModel pickup) {
        this.pickup = pickup;
    }

    public LocationModel getCurrent() {
        return current;
    }
    public void setCurrent(LocationModel current) {
        this.current = current;
    }

    public LocationModel getDestination() {
        return destination;
    }
    public void setDestination(LocationModel destination) {
        this.destination = destination;
    }

    public String getRequestService() {
        return requestService;
    }
    public void setRequestService(String requestService) {
        this.requestService = requestService;
    }

    public float getRideDistance() {
        return rideDistance;
    }

    public void setRideDistance(float rideDistance) {
        this.rideDistance = rideDistance;
    }

    public void setTimePassed(float timePassed) {
        this.timePassed = timePassed;
    }

    public String getId() {
        return id;
    }

    public Boolean getEnded() {
        return ended;
    }

    public int getCancelledType() {
        return cancelledType;
    }

    public String getCancelledReason() {
        return cancelledReason;
    }

    public Double getRidePrice() {
        return ridePrice;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Boolean getCustomerPaid() {
        return customerPaid;
    }

    public String getCar() {
        return car;
    }

    public int getRating() {
        return rating;
    }

    @SuppressLint("DefaultLocale")
    public String getPriceString(){
        return String.format("%.2f", ridePrice);
    }

    public Boolean getCancelled() {
        return cancelled;
    }

    public DatabaseReference getRideRef() {
        return rideRef;
    }

    public String getCalculatedRideDistance() {
        String s = String.valueOf(calculatedRideDistance);
        String s1 = s.substring(s.indexOf(".")+2).trim();
        return s.replace(s1, "") + " km";
    }

    public float getTimePassed() {
        return timePassed;
    }

    public int getCalculatedTime(){
        return (int) calculatedRideDistance * 100 / 60;
    }

    public int getState() {
        return state;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    public double getDistance() {
        return distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getDurationString(){
        int days = (int) duration / 86400;
        int hours = ((int) duration - days * 86400) / 3600;
        int minutes = ((int) duration - days * 86400 - hours * 3600) / 60;
        int seconds = (int) duration - days * 86400 - hours * 3600 - minutes * 60;

        return hours + " hour " + minutes + "min";
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }
}
