package com.example.sighome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class PopUpActivity extends Activity {
    private TextView notice;
    private TextView bar;
    private Button checkBtn;
    private Button closeBtn;


    private static final String ACTION_WINDOW = "window";
    private static final String ACTION_ENTER = "enter";
    private static final String ACTION_BELL = "bell";
    private static final String ACTION_FIRE = "fire";
    private static final String ACTION_GARAGE = "garage";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup);
        //no title bar


        notice = findViewById(R.id.noticeText);
        checkBtn = findViewById(R.id.checkButton);
        closeBtn = findViewById(R.id.closeButton);
        bar = findViewById(R.id.colorBar);


        Intent intent = getIntent();
        String notice_data = intent.getStringExtra("data");
        String action = intent.getAction();

        if(action.equals(ACTION_WINDOW))
        {
            bar.setBackgroundResource(R.color.swaying_window);
            Log.i("window","swaying window");
        }else if(action.equals(ACTION_ENTER))
        {
            bar.setBackgroundResource(R.color.explain);
            Log.i("emergency","entered someone");
        }else if(action.equals(ACTION_BELL))
        {
            bar.setBackgroundResource(R.color.ring_bell);
            Log.i("bell","ringing bell");
        }
        else if(action.equals(ACTION_FIRE))
        {
            bar.setBackgroundResource(R.color.emergency);
            Log.i("bell","ringing bell");
        }
        else if(action.equals(ACTION_GARAGE))
        {
            bar.setBackgroundResource(R.color.white);
            Log.i("bell","ringing bell");
        }
        notice.setText(notice_data);
        Log.i("notice",notice_data);
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
}