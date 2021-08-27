package com.example.sighome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ExplainActivity extends AppCompatActivity {

    private boolean fireBtnControl = true;
    private boolean bellBtnControl = true;
    private boolean enterBtnControl = true;
    private boolean windowBtnControl = true;
    private boolean garageBtnControl = true;

    private ImageView backIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explain);

        ImageButton fireBtn = findViewById(R.id.fire_demode);
        ImageButton doorBtn = findViewById(R.id.enter_demode);
        ImageButton bellBtn = findViewById(R.id.bell_demode);
        ImageButton windowBtn = findViewById(R.id.window_demode);
        ImageButton garageBtn = findViewById(R.id.garage_demode);

        backIv = findViewById(R.id.back_iv);

        doorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enterBtnControl) {
                    doorBtn.setBackgroundResource(R.drawable.enter_2);
                    enterBtnControl = false;
                } else {
                    doorBtn.setBackgroundResource(R.drawable.enter_1);
                    enterBtnControl = true;
                }
            }
        });

        bellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bellBtnControl) {
                    bellBtn.setBackgroundResource(R.drawable.bell_2);
                    bellBtnControl = false;
                } else {
                    bellBtn.setBackgroundResource(R.drawable.bell_1);
                    bellBtnControl = true;
                }
            }
        });

        windowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (windowBtnControl) {
                    windowBtn.setBackgroundResource(R.drawable.window_2);
                    windowBtnControl = false;
                } else {
                    windowBtn.setBackgroundResource(R.drawable.window_1);
                    windowBtnControl = true;
                }
            }
        });

        fireBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fireBtnControl) {
                    fireBtn.setBackgroundResource(R.drawable.fire_2);
                    fireBtnControl = false;
                } else {
                    fireBtn.setBackgroundResource(R.drawable.fire_1);
                    fireBtnControl = true;
                }
            }
        });

        garageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (garageBtnControl) {
                    garageBtn.setBackgroundResource(R.drawable.garage_2);
                    garageBtnControl = false;
                } else {
                    garageBtn.setBackgroundResource(R.drawable.garage_1);
                    garageBtnControl = true;
                }
            }
        });

        backIv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //회원가입 화면으로 이동
                Intent intent = new Intent(ExplainActivity.this, com.example.sighome.MainActivity.class);
                startActivity(intent);
            }
        });

    }



}