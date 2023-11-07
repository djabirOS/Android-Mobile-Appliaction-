package com.ostaxi.app.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.logicbeanzs.uberpolylineanimation.MapAnimator;
import com.ncorti.slidetoact.SlideToActView;
import com.ostaxi.app.Adapters.TypeAdapter;
import com.ostaxi.app.CusSettingsActivity;
import com.ostaxi.app.TripActivity;
import com.ostaxi.app.SplashActivity;
import com.ostaxi.app.Model.CusModel;
import com.ostaxi.app.Model.LocationModel;
import com.ostaxi.app.Model.RideModel;
import com.ostaxi.app.Model.TypeModel;
import com.ostaxi.app.PaymentActivity;
import com.ostaxi.app.R;
import com.ostaxi.app.SendNotification;
import com.ostaxi.app.Utils;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MapCusActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, DirectionCallback {



    private ImageView mDriverProfileImage;
    private TextView mDriverName;
    private TextView mDriverCar;
    private TextView mDriverLicense;
    private TextView mRatingText;
    private TextView autocompleteFragmentTo;
    private TextView autocompleteFragmentFrom;
    CardView autocompleteFragmentFromContainer, mContainer;
    FloatingActionButton mCallDriver;
    FloatingActionButton mCancel;
    FloatingActionButton mCancelTimeout;
    FloatingActionButton mCurrentLocation;
    DrawerLayout drawer;
    private GoogleMap mMap;
    RideModel mCurrentRide;
    private TypeAdapter mAdapter;
    ArrayList<TypeModel> typeArrayList = new ArrayList<>();
    private Boolean driverFound = false;
    private ValueEventListener driveHasEndedRefListener;
    Handler cancelHandler, timeoutHandler;
    int TIMEOUT_MILLISECONDS = 20000, CANCEL_OPTION_MILLISECONDS = 10000;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private SlideToActView mRequest;
    private LocationModel pickupLocation, currentLocation, destinationLocation;
    private Boolean requestBol = false;
    int bottomSheetStatus = 1;
    private Marker destinationMarker, pickupMarker;
    private LinearLayout mDriverInfo, mRadioLayout, mLocation, mLooking, mTimeout;

    @SuppressLint("RtlHardcoded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cus_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCurrentRide = new RideModel(MapCusActivity.this, null);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserData();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        mDriverInfo = findViewById(R.id.driverInfo);
        mRadioLayout = findViewById(R.id.radioLayout);

        mDriverProfileImage = findViewById(R.id.driverProfileImage);

        mDriverName = findViewById(R.id.driverName);
        mDriverCar = findViewById(R.id.driverCar);
        mDriverLicense = findViewById(R.id.driverPlate);

        mCallDriver = findViewById(R.id.phone);

        mRatingText = findViewById(R.id.ratingText);

        mContainer = findViewById(R.id.container_card);

        autocompleteFragmentTo = findViewById(R.id.place_to);
        autocompleteFragmentFrom = findViewById(R.id.place_from);
        autocompleteFragmentFromContainer = findViewById(R.id.place_from_container);
        mCurrentLocation = findViewById(R.id.current_location);
        mLocation = findViewById(R.id.location_layout);
        mLooking = findViewById(R.id.looking_layout);
        mTimeout = findViewById(R.id.timeout_layout);
        TextView mLogout = findViewById(R.id.logout);

        mRequest = findViewById(R.id.request);
        mCancel = findViewById(R.id.cancel);
        mCancelTimeout = findViewById(R.id.cancel_looking);

        mLogout.setOnClickListener(v -> logOut());

        mCancelTimeout.setOnClickListener(v -> {
            bottomSheetStatus = 0;
            mCurrentRide.cancelRide();
            endRide();
        });
        mRequest.setOnSlideCompleteListener(v ->

                startRideRequest());
        mCancel.setOnClickListener(v -> {
            bottomSheetStatus = 0;
            mCurrentRide.cancelRide();
            endRide();
        });
        mCallDriver.setOnClickListener(view -> {
            if (mCurrentRide == null) {
                Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.driver_no_phone), Snackbar.LENGTH_LONG).show();
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mCurrentRide.getDriver().getPhone()));
                startActivity(intent);
            } else {
                Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.no_phone_call_permissions), Snackbar.LENGTH_LONG).show();
            }
        });

        ImageView mDrawerButton = findViewById(R.id.drawerButton);
        mDrawerButton.setOnClickListener(v -> drawer.openDrawer(Gravity.LEFT));

        mCurrentLocation.setOnClickListener(view -> {

            autocompleteFragmentFrom.setText(getString(R.string.current_location));
            mCurrentLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_on_primary_24dp));
            pickupLocation = currentLocation;
            if (pickupLocation == null) {
                return;
            }
            fetchLocationName();


            mMap.clear();
            pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation.getCoordinates()).title("Pickup").icon(BitmapDescriptorFactory.fromBitmap(generateBitmap(MapCusActivity.this, pickupLocation.getName(), null))));
            mCurrentRide.setPickup(pickupLocation);
            autocompleteFragmentFrom.setText(pickupLocation.getName());
            if (destinationLocation != null) {
                destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLocation.getCoordinates()).title("Destination").icon(BitmapDescriptorFactory.fromBitmap(generateBitmap(MapCusActivity.this, destinationLocation.getName(), null))));
                bringBottomSheetDown();
            }

            MapAnimator();
            getRouteToMarker();

            mRequest.setText(getString(R.string.call_uber));
        });


        bringBottomSheetUp();
        initPlacesAutocomplete();
        initRecyclerView();
        isRequestInProgress();

    }



    private void startRideRequest() {
        cancelHandler = new Handler();
        cancelHandler.postDelayed(() -> {
            if (mCurrentRide == null) {

                Log.e("a1", "startRideRequest: "+"mCurrentRide");
                return;
            }
            if (mCurrentRide.getDriver() == null) {
                runOnUiThread(() -> {

                    Log.e("a1", "startRideRequest: "+"mCurrentRideDriver");

                    mTimeout.setVisibility(View.VISIBLE);
                });
            }
        }, CANCEL_OPTION_MILLISECONDS);

        timeoutHandler = new Handler();
        cancelHandler.postDelayed(() -> {
            if (mCurrentRide == null) {
                Log.e("a1", "startRideRequest: "+"mCurrentRide22");

                return;
            }
            if (mCurrentRide.getDriver() == null) {
                runOnUiThread(() -> {
                    Log.e("a1", "startRideRequest: "+"mCurrentRide555");

                    bottomSheetStatus = 0;
                    mCurrentRide.cancelRide();
                    endRide();
                    new AlertDialog.Builder(MapCusActivity.this)
                            .setTitle(getResources().getString(R.string.no_drivers_around))
                            .setMessage(getResources().getString(R.string.no_driver_found))
                            .setPositiveButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                            .setIcon(R.drawable.ic_cancel_black_24dp)
                            .show();

                });
            }
        }, TIMEOUT_MILLISECONDS);

        bringBottomSheetDown();

        if (!requestBol) {
            mCurrentRide.setDestination(destinationLocation);
            mCurrentRide.setPickup(pickupLocation);
            mCurrentRide.setRequestService(mAdapter.getSelectedItem().getId());
            mCurrentRide.setDistance(routeData.get(0));
            mCurrentRide.setDuration(routeData.get(1));

            if (mCurrentRide.checkRide() == -1) {
                return;
            }

            requestBol = true;

            mRequest.setText(getResources().getString(R.string.getting_driver));

            mCurrentRide.postRideInfo();

            requestListener();
        }
    }

    private void initRecyclerView() {
        typeArrayList = Utils.getTypeList(MapCusActivity.this);

        RecyclerView mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MapCusActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new TypeAdapter(typeArrayList, MapCusActivity.this, routeData);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void bringBottomSheetUp() {
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);

        mContainer.startAnimation(slideUp);
        mContainer.setVisibility(View.VISIBLE);

    }


    private void bringBottomSheetDown() {
        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);

        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                switch (bottomSheetStatus) {
                    case 0:
                        bottomSheetStatus = 1;
                        destinationLocation = null;
                        pickupLocation = null;
                        mCurrentRide.setCurrent(null);
                        mCurrentRide.setDestination(null);
                        autocompleteFragmentFrom.setText(getString(R.string.from));
                        autocompleteFragmentTo.setText(getString(R.string.to));
                        mCurrentLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_on_grey_24dp));
                        mMap.clear();
                        MapAnimator();
                        erasePolylines();
                        mRadioLayout.setVisibility(View.GONE);
                        mLocation.setVisibility(View.VISIBLE);
                        mLooking.setVisibility(View.GONE);
                        mDriverInfo.setVisibility(View.GONE);
                        break;
                    case 1:
                        bottomSheetStatus = 2;
                        mRequest.resetSlider();
                        mRadioLayout.setVisibility(View.VISIBLE);
                        mLocation.setVisibility(View.GONE);
                        mLooking.setVisibility(View.GONE);
                        mDriverInfo.setVisibility(View.GONE);
                        mTimeout.setVisibility(View.GONE);
                        break;
                    case 2:
                        bottomSheetStatus = 3;
                        mLocation.setVisibility(View.GONE);
                        mRadioLayout.setVisibility(View.GONE);
                        mLooking.setVisibility(View.VISIBLE);
                        mDriverInfo.setVisibility(View.GONE);
                        break;
                    case 3:
                        bottomSheetStatus = 0;
                        mLocation.setVisibility(View.GONE);
                        mRadioLayout.setVisibility(View.GONE);
                        mLooking.setVisibility(View.GONE);
                        mDriverInfo.setVisibility(View.VISIBLE);
                }
                bringBottomSheetUp();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mContainer.startAnimation(slideDown);
    }

    void initPlacesAutocomplete() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyANvvaIvNs8EUeuh--wfo7nrgd9-TOUSwc");
        }

        autocompleteFragmentTo.setOnClickListener(v -> {
            if (requestBol) {
                return;
            }
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                    .build(getApplicationContext());
            startActivityForResult(intent, 1);
        });

        autocompleteFragmentFrom.setOnClickListener(v -> {
            if (requestBol) {
                return;
            }
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                    .build(getApplicationContext());
            startActivityForResult(intent, 2);
        });
    }



    private void getUserData() {
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View header = navigationView.getHeaderView(0);

                    CusModel mCustomer = new CusModel();
                    mCustomer.setData(dataSnapshot);

                    TextView mUsername = header.findViewById(R.id.usernameDrawer);
                    ImageView mProfileImage = header.findViewById(R.id.imageViewDrawer);

                    mUsername.setText(mCustomer.getName());

                    if (!mCustomer.getProfileImage().equals("default"))
                        Glide.with(getApplication()).load(mCustomer.getProfileImage()).apply(RequestOptions.circleCropTransform()).into(mProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void isRequestInProgress() {
        FirebaseDatabase.getInstance().getReference().child("ride_info").orderByChild("customerId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }

                for (DataSnapshot mData : dataSnapshot.getChildren()) {
                    mCurrentRide = new RideModel();
                    mCurrentRide.setData(mData);

                    if (mCurrentRide.getCancelled() || mCurrentRide.getEnded()) {
                        mCurrentRide = new RideModel();
                        return;
                    }

                    if (mCurrentRide.getDriver() == null) {
                        mTimeout.setVisibility(View.VISIBLE);
                        bottomSheetStatus = 2;
                    } else {
                        bottomSheetStatus = 3;
                    }
                    bringBottomSheetDown();
                    requestListener();
                }

            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        });
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
                RideModel mRide = new RideModel();
                mRide.setData(dataSnapshot);

                if (mRide.getCancelled() || mRide.getEnded()) {
                    if (!mCurrentRide.getEnded() && mRide.getEnded()) {
                        mCurrentRide.showDialog(MapCusActivity.this);
                    }
                    cancelHandler.removeCallbacksAndMessages(null);
                    timeoutHandler.removeCallbacksAndMessages(null);
                    bottomSheetStatus = 0;
                    endRide();

                    if (mRide.getCancelledType() == 11) {
                        new AlertDialog.Builder(MapCusActivity.this)
                                .setTitle(getResources().getString(R.string.no_default_payment))
                                .setMessage(getResources().getString(R.string.no_payment_available_message))
                                .setPositiveButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                                .setIcon(R.drawable.ic_cancel_black_24dp)
                                .show();
                    }
                    return;
                }

                if (mCurrentRide.getDriver() == null && mRide.getDriver() != null) {
                    try {
                        mCurrentRide = (RideModel) mRide.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    cancelHandler.removeCallbacksAndMessages(null);
                    timeoutHandler.removeCallbacksAndMessages(null);

                    getDriverInfo();
                    getDriverLocation();
                }

            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        });
    }

    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;

    private void getDriverLocation() {
        if (mCurrentRide.getDriver().getId() == null) {
            return;
        }
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(mCurrentRide.getDriver().getId()).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBol) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();

                    if(map == null){
                        return;
                    }
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LocationModel mDriverLocation = new LocationModel(new LatLng(locationLat, locationLng), "");
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.getCoordinates().latitude);
                    loc1.setLongitude(pickupLocation.getCoordinates().longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(mDriverLocation.getCoordinates().latitude);
                    loc2.setLongitude(mDriverLocation.getCoordinates().longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance < 100) {
                        mRequest.setText(getResources().getString(R.string.driver_here));
                    } else {
                        mRequest.setText(getResources().getString(R.string.driver_found));
                    }

                    mCurrentRide.getDriver().setLocation(mDriverLocation);


                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(mCurrentRide.getDriver().getLocation().getCoordinates()).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                }

            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        });

    }


    private void getDriverInfo() {
        if (mCurrentRide == null) {
            return;
        }

        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(mCurrentRide.getDriver().getId());
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    mCurrentRide.getDriver().setData(dataSnapshot);

                    mDriverName.setText(mCurrentRide.getDriver().getNameDash());
                    mDriverCar.setText(mCurrentRide.getDriver().getCarDash());
                    mDriverLicense.setText(mCurrentRide.getDriver().getLicenseDash());
                    if (mCurrentRide.getDriver().getProfileImage().equals("default")) {
                        mDriverProfileImage.setImageResource(R.mipmap.ic_default_user);
                    } else {
                        Glide.with(getApplication())
                                .load(mCurrentRide.getDriver().getProfileImage())
                                .apply(RequestOptions.circleCropTransform())
                                .into(mDriverProfileImage);
                    }


                    mRatingText.setText(String.valueOf(mCurrentRide.getDriver().getDriverRatingString()));

                    bringBottomSheetDown();

                    new SendNotification("You have a customer waiting", "New Ride", mCurrentRide.getDriver().getNotificationKey());
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        });
    }



    private void endRide() {


        if (cancelHandler != null) {
            cancelHandler.removeCallbacksAndMessages(null);
        }

        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
        }

        requestBol = false;
        if (driverLocationRefListener != null)
            driverLocationRef.removeEventListener(driverLocationRefListener);

        if (driveHasEndedRefListener != null && mCurrentRide.getRideRef() != null)
            mCurrentRide.getRideRef().removeEventListener(driveHasEndedRefListener);

        if (mCurrentRide != null && driverFound) {
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(mCurrentRide.getDriver().getId()).child("customerRequest");
            driverRef.removeValue();
        }

        pickupLocation = null;
        destinationLocation = null;

        driverFound = false;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, (key, error) -> {
        });

        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (mDriverMarker != null) {
            mDriverMarker.remove();
        }
        mMap.clear();
        mRequest.setText(getString(R.string.call_uber));

        mDriverName.setText("");
        mDriverCar.setText(getString(R.string.destination));
        mDriverProfileImage.setImageResource(R.mipmap.ic_default_user);

        autocompleteFragmentTo.setText(getString(R.string.to));
        autocompleteFragmentFrom.setText(getString(R.string.from));
        mCurrentLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_on_grey_24dp));

        mCurrentRide = new RideModel(MapCusActivity.this, null);
        getDriversAround();
        bringBottomSheetDown();
        zoomUpdated = false;

        mAdapter.setSelectedItem(typeArrayList.get(0));
        mAdapter.notifyDataSetChanged();

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
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            } else {
                checkLocationPermission();
            }
        }

    }

    boolean zoomUpdated = false;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplication() != null) {
                    currentLocation = new LocationModel(new LatLng(location.getLatitude(), location.getLongitude()), "");
                    mCurrentRide.setCurrent(currentLocation);

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (!zoomUpdated) {
                        float zoomLevel = 17.0f; //This goes up to 21
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
                        zoomUpdated = true;
                    }

                    if (!getDriversAroundStarted)
                        getDriversAround();

                }
            }
        }
    };


    private void fetchLocationName() {
        if (pickupLocation == null) {
            return;
        }
        try {

            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(currentLocation.getCoordinates().latitude, currentLocation.getCoordinates().longitude, 1);
            if (addresses.isEmpty()) {
                autocompleteFragmentFrom.setText(R.string.waiting_for_location);
            } else {
                addresses.size();
                if (addresses.get(0).getThoroughfare() == null) {
                    pickupLocation.setName(addresses.get(0).getLocality());
                } else if (addresses.get(0).getLocality() == null) {
                    pickupLocation.setName("Unknown Location");
                } else {
                    pickupLocation.setName(addresses.get(0).getLocality() + ", " + addresses.get(0).getThoroughfare());
                }
                autocompleteFragmentFrom.setText(pickupLocation.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(MapCusActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE}, 1))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapCusActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE}, 1);
            }
        }
    }


    public static Bitmap generateBitmap(Context context, String location, String duration) {
        Bitmap bitmap = null;
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RelativeLayout view = new RelativeLayout(context);
        try {
            mInflater.inflate(R.layout.item_marker, view, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView locationTextView = (TextView) view.findViewById(R.id.location);
        TextView durationTextView = (TextView) view.findViewById(R.id.duration);
        locationTextView.setText(location);

        if(duration != null){
            durationTextView.setText(duration);
        }else{
            durationTextView.setVisibility(View.GONE);
        }

        view.setLayoutParams(new ViewGroup.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);

        view.draw(c);

        return bitmap;
    }

    public ArrayList<Double> parseJson(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        JSONObject jDistance = null;
        JSONObject jDuration = null;
        long totalDistance = 0;
        int totalSeconds = 0;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /* Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");

                /* Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {

                    jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");

                    totalDistance = totalDistance + Long.parseLong(jDistance.getString("value"));

                    jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
                    totalSeconds = totalSeconds + Integer.parseInt(jDuration.getString("value"));

                }
            }

            double dist = totalDistance / 1000.0;
            Log.d("distance", "Calculated distance:" + dist);

            int days = totalSeconds / 86400;
            int hours = (totalSeconds - days * 86400) / 3600;
            int minutes = (totalSeconds - days * 86400 - hours * 3600) / 60;
            int seconds = totalSeconds - days * 86400 - hours * 3600 - minutes * 60;
            Log.d("duration", days + " days " + hours + " hours " + minutes + " mins" + seconds + " seconds");

            ArrayList<Double> list = new ArrayList<Double>();
            list.add(dist);
            list.add((double) totalSeconds);

            return list;



        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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
                Toast.makeText(getApplication(), "Please provide the permission", Toast.LENGTH_LONG).show();
            }
        }
    }


    boolean getDriversAroundStarted = false;
    List<Marker> markerList = new ArrayList<Marker>();


    private void getDriversAround() {
        if (currentLocation == null) {
            return;
        }
        getDriversAroundStarted = true;
        DatabaseReference driversLocation = FirebaseDatabase.getInstance().getReference().child(("driversWorking"));


        GeoFire geoFire = new GeoFire(driversLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getCoordinates().latitude, currentLocation.getCoordinates().longitude), 10000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (mCurrentRide != null) {
                    if (mCurrentRide.getDriver() != null) {
                        return;
                    }
                }
                for (Marker markerIt : markerList) {
                    if (markerIt.getTag() == null || key == null) {
                        continue;
                    }
                    if (markerIt.getTag().equals(key))
                        return;
                }


                checkDriverLastUpdated(key);
                LatLng driverLocation = new LatLng(location.latitude, location.longitude);


                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)).position(driverLocation).title(key));
                mDriverMarker.setTag(key);

                markerList.add(mDriverMarker);

            }

            @Override
            public void onKeyExited(String key) {
                for (Marker markerIt : markerList) {
                    if (markerIt.getTag() == null || key == null) {
                        continue;
                    }
                    if (markerIt.getTag().equals(key)) {
                        markerIt.remove();
                        markerList.remove(markerIt);
                        return;
                    }

                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker markerIt : markerList) {
                    if (markerIt.getTag() == null || key == null) {
                        continue;
                    }
                    if (markerIt.getTag().equals(key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                        return;
                    }
                }

                checkDriverLastUpdated(key);
                LatLng driverLocation = new LatLng(location.latitude, location.longitude);

                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)).position(driverLocation).title(key));
                mDriverMarker.setTag(key);

                markerList.add(mDriverMarker);
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void checkDriverLastUpdated(String key) {
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child("Drivers")
                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return;
                        }

                        if (dataSnapshot.child("last_updated").getValue() != null) {
                            long lastUpdated = Long.parseLong(dataSnapshot.child("last_updated").getValue().toString());
                            long currentTimestamp = System.currentTimeMillis();

                            if (currentTimestamp - lastUpdated > 10000) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversWorking");
                                GeoFire geoFire = new GeoFire(ref);
                                geoFire.removeLocation(dataSnapshot.getKey(), (key1, error) -> {
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                    }
                });
    }


    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MapCusActivity.this, SplashActivity.class);
        startActivity(intent);
        finish();
    }


    private void getRouteToMarker() {

        String serverKey = getResources().getString(R.string.google_maps_key);
        if (mCurrentRide.getDestination() != null && mCurrentRide.getPickup() != null) {
            GoogleDirection.withServerKey(serverKey)
                    .from(mCurrentRide.getDestination().getCoordinates())
                    .to(mCurrentRide.getPickup().getCoordinates())
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);
        }
    }

    private List<Polyline> polylines = new ArrayList<>();


    private void erasePolylines() {
        if (polylines == null) {
            return;
        }
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




    ArrayList<Double> routeData;

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        if (direction.isOK()) {
            Route route = direction.getRouteList().get(0);


            try {
                JSONObject obj = new JSONObject(rawBody);

                routeData = parseJson(obj);

                mAdapter.setData(routeData);
                mAdapter.notifyDataSetChanged();
                Log.e("My App", obj.toString());

            } catch (Throwable ignored) {
            }

            destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLocation.getCoordinates()).icon(BitmapDescriptorFactory.fromBitmap(generateBitmap(MapCusActivity.this, destinationLocation.getName(), route.getLegList().get(0).getDuration().getText()))));

            List<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();

            MapAnimator.getInstance().animateRoute(mMap, directionPositionList);

            setCameraWithCoordinationBounds(route);
        }else{

            Log.e("My App", direction.getErrorMessage());

        }
    }


    private void MapAnimator() {
        if (polylines == null) {
            return;
        }
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            LocationModel mLocation;

            if (currentLocation == null) {
                Snackbar.make(findViewById(R.id.drawer_layout), "First Activate GPS", Snackbar.LENGTH_LONG).show();
                return;
            }
            Place place = Autocomplete.getPlaceFromIntent(data);

            mLocation = new LocationModel(place.getLatLng(), place.getName());


            currentLocation = new LocationModel(new LatLng(currentLocation.getCoordinates().latitude, currentLocation.getCoordinates().longitude), "");


            if (requestCode == 1) {
                mMap.clear();
                destinationLocation = mLocation;
                destinationMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(generateBitmap(MapCusActivity.this, destinationLocation.getName(), null))).position(destinationLocation.getCoordinates()));
                mCurrentRide.setDestination(destinationLocation);
                autocompleteFragmentTo.setText(destinationLocation.getName());
                if (pickupLocation != null) {
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation.getCoordinates()).icon(BitmapDescriptorFactory.fromBitmap(generateBitmap(MapCusActivity.this, pickupLocation.getName(), null))));
                    bringBottomSheetDown();
                }
            } else if (requestCode == 2) {
                mMap.clear();
                pickupLocation = mLocation;
                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation.getCoordinates()).icon(BitmapDescriptorFactory.fromBitmap(generateBitmap(MapCusActivity.this, pickupLocation.getName(), null))));
                mCurrentRide.setPickup(pickupLocation);
                autocompleteFragmentFrom.setText(pickupLocation.getName());
                if (destinationLocation != null) {
                    destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLocation.getCoordinates()).icon(BitmapDescriptorFactory.fromBitmap(generateBitmap(MapCusActivity.this, destinationLocation.getName(), null))));
                    bringBottomSheetDown();
                }
            }

            MapAnimator();
            getRouteToMarker();
            getDriversAround();

            mRequest.setText(getString(R.string.call_uber));


        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // TODO: Handle the error.
            Status status = Autocomplete.getStatusFromIntent(data);
            assert status.getStatusMessage() != null;
            Log.i("PLACE_AUTOCOMPLETE", status.getStatusMessage());
        } else if (resultCode == RESULT_CANCELED) {
            initPlacesAutocomplete();
        }
        initPlacesAutocomplete();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (bottomSheetStatus == 2) {
                bottomSheetStatus = 0;
                bringBottomSheetDown();
            } else {
                super.onBackPressed();
            }
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
            Intent intent = new Intent(MapCusActivity.this, TripActivity.class);
            intent.putExtra("customerOrDriver", "Customers");
            startActivity(intent);
        } else if (id == R.id.settings) {
            Intent intent = new Intent(MapCusActivity.this, CusSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.payment) {
            Intent intent = new Intent(MapCusActivity.this, PaymentActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
