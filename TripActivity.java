package com.ostaxi.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ostaxi.app.Adapters.TripAdapter;
import com.ostaxi.app.Model.RideModel;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class TripActivity extends AppCompatActivity {

    private RecyclerView.Adapter mHistoryAdapter;

    LinearLayout mEmpty;

    String idRef = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        RecyclerView mHistoryRecyclerView = findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mHistoryLayoutManager = new LinearLayoutManager(TripActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new TripAdapter(resultsHistory, TripActivity.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        mEmpty = findViewById(R.id.empty_layout);

        String customerOrDriver = getIntent().getExtras().getString("customerOrDriver");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(customerOrDriver.equals("Drivers")){
            idRef = "driverId";
        }else{
            idRef = "customerId";
        }


        getUserHistoryIds();
        setupToolbar();
    }


    private void setupToolbar() {
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.your_trips));
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(v -> finish());
    }



    private void getUserHistoryIds() {

        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = FirebaseDatabase.getInstance().getReference().child("ride_info").orderByChild(idRef).equalTo(driverId);

        query.addChildEventListener(new ChildEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(!dataSnapshot.exists()){return;}

                RideModel mRide = new RideModel();
                mRide.setData(dataSnapshot);

                if(mRide.getCancelled()){
                    return;
                }
                mEmpty.setVisibility(View.GONE);
                resultsHistory.add(0,  mRide);
                mHistoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        });


    }
    private ArrayList<RideModel> resultsHistory = new ArrayList<>();


}















