package com.ostaxi.app.maps;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.ncorti.slidetoact.SlideToActView;
import com.ostaxi.app.Adapters.RequestAdapter;
import com.ostaxi.app.DriverSettingsActivity;
import com.ostaxi.app.TripActivity;
import com.ostaxi.app.SplashActivity;
import com.ostaxi.app.Model.DriverModel;
import com.ostaxi.app.Model.RideModel;
import com.ostaxi.app.DriverPayoutActivity;
import com.ostaxi.app.R;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapDriverActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, DirectionCallback {

    private TextView mCustomerName;
    DatabaseReference mUser;
    RideModel mCurrentRide;
    Marker pickupMarker, destinationMarker;
    DriverModel mDriver = new DriverModel();
    TextView mUsername, mLogout;
    private ValueEventListener driveHasEndedRefListener;
    private RequestAdapter cardRequestAdapter;
    List<RideModel> requestList = new ArrayList<>();
    View mBottomSheet;
    BottomSheetBehavior<View> mBottomSheetBehavior;
    GeoQuery geoQuery;
    int MAX_SEARCH_DISTANCE = 20;
    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private SlideToActView mRideStatus;
    private Switch mWorkingSwitch;
    private LinearLayout mCustomerInfo, mBringUpBottomLayout;
    boolean started = false;
    boolean zoomUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver);

        Toolbar toolbar = findViewById(R.id.toolbar);


        polylines = new ArrayList<>();


        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mUser = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(FirebaseAuth.getInstance().getUid());
        mCustomerInfo = findViewById(R.id.customerInfo);

        mBringUpBottomLayout = findViewById(R.id.bringUpBottomLayout);

        mCustomerName = findViewById(R.id.name);
        mUsername = navigationView.getHeaderView(0).findViewById(R.id.usernameDrawer);
        FloatingActionButton mMaps = findViewById(R.id.openMaps);
        FloatingActionButton mCall = findViewById(R.id.phone);
        ImageView mCancel = findViewById(R.id.cancel);
        mRideStatus = findViewById(R.id.rideStatus);
        mLogout = findViewById(R.id.logout);

        mWorkingSwitch = findViewById(R.id.workingSwitch);

        mLogout.setOnClickListener(v -> logOut());

        mWorkingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!mDriver.getActive()) {
                Toast.makeText(MapDriverActivity.this, R.string.not_approved, Toast.LENGTH_LONG).show();
                mWorkingSwitch.setChecked(false);
                return;
            }
            if (isChecked) {
                connectDriver();
            } else {
                disconnectDriver();
            }
        });

        mRideStatus.setOnSlideCompleteListener(v -> {
            switch (mCurrentRide.getState()) {
                case 1:
                    if (mCurrentRide == null) {
                        return;
                    }
                    mCurrentRide.pickedCustomer();
                    break;
                case 2:
                    if (mCurrentRide != null)
                        mCurrentRide.recordRide();
                    break;
            }
        });

        mMaps.setOnClickListener(view -> {
            if (mCurrentRide.getState() == 1) {
                openMaps(mCurrentRide.getPickup().getCoordinates().latitude, mCurrentRide.getPickup().getCoordinates().longitude);
            } else {
                openMaps(mCurrentRide.getDestination().getCoordinates().latitude, mCurrentRide.getDestination().getCoordinates().longitude);
            }
        });

        mCall.setOnClickListener(view -> {
            if (mCurrentRide == null) {
                Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.driver_no_phone), Snackbar.LENGTH_LONG).show();
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mCurrentRide.getCustomer().getPhone()));
                startActivity(intent);
            } else {
                Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.no_phone_call_permissions), Snackbar.LENGTH_LONG).show();
            }
        });

        mCancel.setOnClickListener(v -> {
            mCurrentRide.cancelRide();
            endRide();
        });
        ImageView mDrawerButton = findViewById(R.id.drawerButton);
        mDrawerButton.setOnClickListener(v -> drawer.openDrawer(Gravity.LEFT));

        mBringUpBottomLayout = findViewById(R.id.bringUpBottomLayout);
        mBringUpBottomLayout.setOnClickListener(v -> {
            if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (mCurrentRide == null) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        Log.e("asdasda", "onDataChange: "+"asdasdasd" );

        getUserData();
        //TODO getAssignedCustomer();
        initializeRequestCardSwipe();
        isRequestInProgress();

        ViewTreeObserver vto = mBringUpBottomLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(this::initializeBottomLayout);

    }

    private void openMaps(double latitude, double longitude) {
        try {
            String url = "https://waze.com/ul?ll=" + latitude + "," + longitude + "&navigate=yes";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=" + latitude + "," + longitude));
            startActivity(intent);
        }
    }



    private void initializeRequestCardSwipe() {
        cardRequestAdapter = new RequestAdapter(getApplicationContext(), R.layout.item_request, requestList);

        final SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);

        flingContainer.setAdapter(cardRequestAdapter);

        //Handling swipe of cards
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                requestList.remove(0);
                cardRequestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                RideModel mRide = (RideModel) dataObject;
                requestList.remove(mRide);
                cardRequestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                RideModel mRide = (RideModel) dataObject;

                if (mRide.getDriver() == null) {

                    try {
                        mCurrentRide = (RideModel) mRide.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    mCurrentRide.confirmDriver();
                    requestListener();
                }

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });

    }


    private void initializeBottomLayout() {
        mBottomSheet = findViewById(R.id.bottomSheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setPeekHeight(mBringUpBottomLayout.getHeight());


        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (mCurrentRide == null) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }


    private void getUserData() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mDriver.setData(dataSnapshot);

                    mUsername.setText(mDriver.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("driversWorking").child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    connectDriver();
                } else {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    disconnectDriver();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void isRequestInProgress() {

        Log.e("asdasda", "onDataChange: "+"asdasdasd" );

        FirebaseDatabase.getInstance().getReference().child("ride_info").orderByChild("driverId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }else{

                    getRequestsAround();

                }

                for(DataSnapshot mData : dataSnapshot.getChildren()){
                    mCurrentRide = new RideModel();
                    mCurrentRide.setData(mData);

                    if (mCurrentRide.getCancelled() || mCurrentRide.getEnded()) {
                        endRide();
                        return;
                    }


                    Log.e("asdasd88888a", "onDataChange: "+"asdasdasd" );

                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    requestListener();
                }

            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        });
    }


    private void checkRequestState() {
        switch (mCurrentRide.getState()) {
            case 1:
                destinationMarker = mMap.addMarker(new MarkerOptions().position(mCurrentRide.getDestination().getCoordinates()).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio_filled)));
                pickupMarker = mMap.addMarker(new MarkerOptions().position(mCurrentRide.getPickup().getCoordinates()).title("Pickup").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radio)));

                mRideStatus.setText(getResources().getString(R.string.picked_customer));
                mRideStatus.resetSlider();

                mCustomerName.setText(mCurrentRide.getDestination().getName());

                getAssignedCustomerInfo();

                requestList.clear();
                cardRequestAdapter.notifyDataSetChanged();
                erasePolylines();
                getRouteToMarker(mCurrentRide.getPickup().getCoordinates());
                break;
            case 2:
                erasePolylines();
                if (mCurrentRide.getDestination().getCoordinates().latitude != 0.0 && mCurrentRide.getDestination().getCoordinates().longitude != 0.0) {
                    getRouteToMarker(mCurrentRide.getDestination().getCoordinates());
                }
                mRideStatus.setText(getResources().getString(R.string.drive_complete));
                mRideStatus.resetSlider();
                break;
            default:
                endRide();

        }
    }


    private void getRequestsAround() {
        if (mLastLocation == null) {
            return;
        }


        Log.e("kokok", "getRequestsAround: " );
        DatabaseReference requestLocation = FirebaseDatabase.getInstance().getReference().child("customer_requests");

        GeoFire geoFire = new GeoFire(requestLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), MAX_SEARCH_DISTANCE);
        //geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {


                Log.e("asdd", "onKeyEntered: +" );

                if(!mWorkingSwitch.isChecked()){
                    //return;
                }



                if (mCurrentRide == null) {
                    for (RideModel mRideIt : requestList) {
                        if (mRideIt.getId().equals(key)) {
                            return;
                        }
                    }

                    getRequestInfo(key);

                }else{
                    requestList.clear();
                }
            }

            @Override
            public void onKeyExited(String key) {

                Log.e("asdd", "onKeyEntered:333 +" );

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

                Log.e("asdd", "onKeyEntered:111 +" );

            }

            @Override
            public void onGeoQueryReady() {

                Log.e("asdd", "onKeyEntered22: +" );

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(getApplicationContext(),"asdasd",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getRequestInfo(String key) {


        Log.e("uuuu", "getRequestInfo: "+key );
        FirebaseDatabase.getInstance().getReference().child("ride_info").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                if (mCurrentRide != null) {
                    return;
                }


                RideModel mRide = new RideModel();
                mRide.setData(dataSnapshot);


                if(!mRide.getRequestService().equals(mDriver.getService())){

                    Log.e("a34", "onDataChange: "+"asdas" );
                    return;
                }else{
                    Log.e("a344", "onDataChange: "+"sdsdsds" );



                }


//                for (RideObject mRideIt : requestList) {
//                    Log.e("rrrr", "onDataChange: "+"1212122" );
//
//                    if (mRideIt.getId().equals(mRide.getId())) {
//
//                        Log.e("a344", "onDataChange: "+"xxxxxx" );
//
//                        if (mRide.getCancelled() || mRide.getEnded() || mRide.getDriver() != null) {
//                            requestList.remove(mRideIt);
//                            cardRequestAdapter.notifyDataSetChanged();
//                        }
//                        return;
//                    }
//                }

                if (!mRide.getCancelled() && !mRide.getEnded() && mRide.getDriver() == null && mRide.getState() == 0) {
                    requestList.add(mRide);
                    cardRequestAdapter.notifyDataSetChanged();
                    makeSound();

//                    HashMap<String, Object> map = new HashMap<String, Object>();
//                    map.put("timestamp_last_driver_read", ServerValue.TIMESTAMP);
//                    //FirebaseDatabase.getInstance().getReference().child("ride_info").child(key).updateChildren(map);
//                    FirebaseDatabase.getInstance().getReference().child("ride_info").child(key).setValue(map);

                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        });
    }


    private void makeSound() {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.driver_notification);
        mp.start();
    }



    private void requestListener() {
        if (mCurrentRide == null) {
            return;
        }

        driveHasEndedRefListener = mCurrentRide.getRideRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }
                mCurrentRide.setData(dataSnapshot);

                //if drive has ended or been cancelled then call endRide to retrieve all variables to their default state
                if (mCurrentRide.getCancelled() || mCurrentRide.getEnded()) {
                    endRide();
                    return;
                }

                checkRequestState();
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        });
    }


    private void getRouteToMarker(LatLng destination) {
        String serverKey = getResources().getString(R.string.google_maps_key);
        if (destination != null && mLastLocation != null) {
            GoogleDirection.withServerKey(serverKey)
                    .from(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .to(destination)
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);
        }
    }


    private void getAssignedCustomerInfo() {
        if (mCurrentRide.getCustomer().getId() == null) {
            return;
        }
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(mCurrentRide.getCustomer().getId());
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                if (mCurrentRide != null) {
                    mCurrentRide.getCustomer().setData(dataSnapshot);

                    mCustomerName.setText(mCurrentRide.getCustomer().getName());
                }

                mCustomerInfo.setVisibility(View.VISIBLE);
                mBottomSheetBehavior.setHideable(false);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        });
    }


    private void endRide() {
        if (mCurrentRide == null) {
            return;
        }

        if (driveHasEndedRefListener != null) {
            mCurrentRide.getRideRef().removeEventListener(driveHasEndedRefListener);
        }


        mRideStatus.setText(getString(R.string.picked_customer));
        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
        driverRef.removeValue();

        //Remove the request from the geofire child so that other drivers don't have to check this request in the future
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customer_requests");//fuck
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(mCurrentRide.getId(), (key, error) -> {
        });

        mCurrentRide = null;

        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mCustomerName.setText("");

        mMap.clear();
        getRequestsAround();

        //This will allow the map to re-zoom on the current location
        zoomUpdated = false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));

            if (!success) {
            }
        } catch (Resources.NotFoundException e) {
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);//interval with which the driver location will be updated
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                checkLocationPermission();
            }
        }
    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }

            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
                    GeoFire geoFireWorking = new GeoFire(refWorking);

                    if (!mWorkingSwitch.isChecked()) {
                        geoFireWorking.removeLocation(userId, (key, error) -> {
                        });
                        return;
                    }


                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> {
                    });

                    if (mCurrentRide != null && mLastLocation != null) {
                        mCurrentRide.setRideDistance(mCurrentRide.getRideDistance() + mLastLocation.distanceTo(location) / 1000);
                    }

                    mLastLocation = location;

                    if (!started) {
                        getRequestsAround();
                        started = true;
                    }

                    Map<String, Object> newUserMap = new HashMap<>();
                    newUserMap.put("last_updated", ServerValue.TIMESTAMP);
                    mUser.updateChildren(newUserMap);

                    if (!zoomUpdated) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                        zoomUpdated = true;
                    }
                }
            }
        }
    };


    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(MapDriverActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapDriverActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void logOut() {
        disconnectDriver();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MapDriverActivity.this, SplashActivity.class);
        startActivity(intent);
        finish();
    }


    private void connectDriver() {
        mWorkingSwitch.setChecked(true);
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void disconnectDriver() {
        mWorkingSwitch.setChecked(false);
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable").child(userId);
        ref.removeValue();
    }

    private List<Polyline> polylines;


    private void erasePolylines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }


    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }



    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            Route route = direction.getRouteList().get(0);

            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
            Polyline polyline = mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.BLACK));
            polylines.add(polyline);
            setCameraWithCoordinationBounds(route);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.history) {
            Intent intent = new Intent(MapDriverActivity.this, TripActivity.class);
            intent.putExtra("customerOrDriver", "Drivers");
            startActivity(intent);
        } else if (id == R.id.settings) {
            Intent intent = new Intent(MapDriverActivity.this, DriverSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.payout) {
            Intent intent = new Intent(MapDriverActivity.this, DriverPayoutActivity.class);
            startActivity(intent);
        } else if (id == R.id.logout) {
            logOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
