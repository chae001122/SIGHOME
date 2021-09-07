package com.example.sighome;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CctvPopUpActivity extends Activity {

    Button inBtn;
    Button outBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv_pop_up);

        inBtn = findViewById(R.id.insideCctv_btn);
        outBtn = findViewById(R.id.outsideCctv_btn);

        inBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Cctv.class);
                startActivity(intent);
            }
        });

        outBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Cctv_outdoorActivity.class);
                startActivity(intent);
            }
        });
    }
}