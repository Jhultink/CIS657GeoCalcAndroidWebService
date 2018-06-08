package com.example.hultinj.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class HistoryActivity extends AppCompatActivity
        implements HistoryFragment.OnListFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onListFragmentInteraction(LocationLookup item) {
        System.out.println("Interact!");
        Intent intent = new Intent();
        String[] vals = {String.valueOf(item.getOrigLat()), String.valueOf(item.getOrigLng()), String.valueOf(item.getEndLat()), String.valueOf(item.getEndLng()) };
        intent.putExtra("item", vals);
        setResult(MainActivity.HISTORY_RESULT,intent);
        finish();
    }

}
