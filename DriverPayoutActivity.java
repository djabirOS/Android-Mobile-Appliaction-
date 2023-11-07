package com.ostaxi.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ostaxi.app.Adapters.PayoutAdapter;
import com.ostaxi.app.Model.DriverModel;
import com.ostaxi.app.Model.PayModel;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class DriverPayoutActivity extends AppCompatActivity {

    StripeInfo.ResponseCallback cb;

    private RecyclerView.Adapter mAdapter;

    ArrayList<PayModel> payoutArrayList = new ArrayList<>();

    Button mPayout;

    TextView mPayoutAmount;

    DriverModel mDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__driver_payout);

        setupToolbar();

        mPayout = findViewById(R.id.payout);
        mPayoutAmount = findViewById(R.id.payout_amount);

        mPayout.setOnClickListener(v -> {


            Toast.makeText(getApplicationContext(),"This is not real amount",Toast.LENGTH_SHORT).show();

        });




        initializeRecyclerView();
        loadData();
    }


    private void loadData() {
        FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mDriver = new DriverModel();
                    mDriver.setData(dataSnapshot);
                    String s = String.valueOf(new Utils().round(mDriver.getPayoutAmount(), 2));

                    mPayoutAmount.setText(s + getString(R.string.money_type));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }





    public void initializeRecyclerView(){
        RecyclerView mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PayoutAdapter(payoutArrayList, DriverPayoutActivity.this);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void setupToolbar() {
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.payout));
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
