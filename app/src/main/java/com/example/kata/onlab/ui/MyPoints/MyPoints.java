package com.example.kata.onlab.ui.MyPoints;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.kata.onlab.R;
import com.example.kata.onlab.network.Data;
import com.example.kata.onlab.ui.AddPlaceFragment;
import com.example.kata.onlab.ui.list.ItemAdapter;

import java.util.List;

public class MyPoints extends AppCompatActivity implements MyPointsScreen {
    private RecyclerView recyclerView;
    ItemAdapter adapter;
    SwipeRefreshLayout swipeRefresh;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_points);
        initRecycleView();
        context=this;
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyPointsPresenter.getInstance().newItemView();

            }
        });
    }

    public void newItemView(){
        new AddPlaceFragment().show(getSupportFragmentManager(), AddPlaceFragment.TAG);
    }


    public void updateData() {
        swipeRefresh.setRefreshing(true);
        MyPointsPresenter.getInstance().updateNetworkData();
    }
    @Override
    public void postDataCallback(Data item){
        adapter.addItem(item);
    }

    @Override
    public void updateDataCallback(List<Data> list) {
        adapter.update(list);
        swipeRefresh.setRefreshing(false);
    }

    private void initRecycleView() {
        recyclerView = (RecyclerView) findViewById(R.id.MainRecyclerView);
        adapter = new ItemAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }



    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onStart() {
        super.onStart();
        MyPointsPresenter.getInstance().attachScreen(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        MyPointsPresenter.getInstance().detachScreen();
    }

}