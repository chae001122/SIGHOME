package com.example.sighome;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eclipse.paho.client.mqttv3.MqttClient;

//060700
public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private DatabaseReference mRef;
    private FirebaseAuth mFirebaseAuth; //파이어베이스 인증
    private FirebaseUser mUser;
    private TextView mTvUserName, turnOffBtn, emCallBtn;
    private ImageView modeIv;

    private TextView cctvBtn;

    private DrawerLayout mDrawerLayout;
    private Context context = this;
    private Activity activity = this;

    private MqttClient mqttClient;

    public static final String DEBUG_TAG = "MqttService"; // Debug TAG
    private static final String ACTION_START  = DEBUG_TAG + ".START"; // Action to start
    private static final String ACTION_STOP   = DEBUG_TAG + ".STOP"; // Action to stop
    private static final String ACTION_KEEPALIVE= DEBUG_TAG + ".KEEPALIVE";

    static final int SMS_SEND_PERMISSION=1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth=FirebaseAuth.getInstance();
        mUser=mFirebaseAuth.getCurrentUser();

        turnOffBtn=findViewById(R.id.turnOff_btn);
        modeIv=findViewById(R.id.mode_iv);

        cctvBtn = findViewById(R.id.watch_cctv_btn);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = new Intent(getApplicationContext(),AlarmService.class);
        //intent.setAction(ACTION_START);
       //startService(intent);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setTitle("스마트 홈 케어 시스템");

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.navi_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                if(id == R.id.inside_mode){//알람 모드
                    toolbar.setBackgroundColor(Color.parseColor("#BDD7EE"));
                    modeIv.setImageResource(R.drawable.alarm_on_mode);
                    Intent intent = new Intent(getApplicationContext(),AlarmService.class);
                    intent.setAction(ACTION_START);
                    startService(intent);
                }
                else if(id == R.id.outside_mode){//알람 해제 모드
                    toolbar.setBackgroundColor(Color.parseColor("#787878"));
                    modeIv.setImageResource(R.drawable.alarm_off_mode);
                    Intent intent = new Intent(getApplicationContext(),AlarmService.class);
                    intent.setAction(ACTION_STOP);
                    startService(intent);
                }
                else if(id == R.id.app_intro){//앱 소개
                    Intent intent = new Intent(MainActivity.this, com.example.sighome.ExplainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if(id == R.id.logout){//로그아웃
                    mFirebaseAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, com.example.sighome.LoginActivity.class);
                    startActivity(intent);
                    finish(); //현재 액티비티 파괴
                }

                return true;
            }
        });

        //SMS 권한 부여
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("알림");
                builder.setMessage("SMS 권한을 부여하지 않으면 이 앱이 제대로 작동하지 않습니다.");
                builder.setIcon(android.R.drawable.ic_dialog_info);

                builder.setNeutralButton("허가", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION);
            }
        }

        turnOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //전등 끄기 버튼 클릭 시
                Log.i(DEBUG_TAG,"알람 끄자");
                Intent intent = new Intent(getApplicationContext(),AlarmService.class);
                intent.setAction(ACTION_KEEPALIVE);
                startService(intent);
            }
        });


        cctvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG,"티비 보자");
                Intent intent = new Intent(getApplicationContext(), CctvPopUpActivity.class);
                startActivity(intent);
            }
        });

        String userId = mUser.getUid();
        mTvUserName=(TextView)findViewById(R.id.username_tv);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userName = snapshot.child("UserAccount").child(userId).child("userName").getValue().toString();
                    mTvUserName.setText(userName);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        emCallBtn = findViewById(R.id.em_call_btn);

        emCallBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), EmMessageSendActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case SMS_SEND_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:{//메뉴 버튼
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }
}