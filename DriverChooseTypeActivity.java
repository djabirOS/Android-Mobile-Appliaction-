package com.ostaxi.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ostaxi.app.Adapters.TypeAdapter;

import com.ostaxi.app.Model.TypeModel;

import java.util.ArrayList;


public class DriverChooseTypeActivity extends AppCompatActivity {

    private TypeAdapter mAdapter;

    ArrayList<TypeModel> typeArrayList = new ArrayList<>();

    ImageView mConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_choose_type);

        setupToolbar();

        mConfirm = findViewById(R.id.confirm);
        mConfirm.setOnClickListener(view -> confirmEntry());

        initRecyclerView();



        String service = getIntent().getStringExtra("service");
        for(TypeModel mType : typeArrayList){
            if(mType.getId().equals(service)){
                mAdapter.setSelectedItem(mType);
            }
        }

        mAdapter.notifyDataSetChanged();
    }


    private void setupToolbar() {
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getString(R.string.type));
        myToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void confirmEntry() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", mAdapter.getSelectedItem().getId());
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }



    private void initRecyclerView() {
        typeArrayList = Utils.getTypeList(DriverChooseTypeActivity.this);

        RecyclerView mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(DriverChooseTypeActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new TypeAdapter(typeArrayList, DriverChooseTypeActivity.this, null);
        mRecyclerView.setAdapter(mAdapter);
    }


}
