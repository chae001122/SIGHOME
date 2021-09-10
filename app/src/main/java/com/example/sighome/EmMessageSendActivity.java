package com.example.sighome;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class EmMessageSendActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_emmessagesend);

        TextView btn112 = findViewById(R.id.send_to_112_btn);
        TextView btn119 = findViewById(R.id.send_to_119_btn);

        btn112.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String emNum = "01047533589"; //일딴 윤세연 번호
                String emText = "<<긴급 신고>>\n위치:숭실대학교 정보과학관\n저는 청각 장애인이며,집에 사람이 침입했습니다.\nsend by SIGHOME";

                try{
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(emNum,null,emText,null,null);
                    Toast.makeText(getApplicationContext(), "긴급 문자 전송 완료!", Toast.LENGTH_LONG).show();
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "전송 오류!", Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();//오류 원인이 찍힌다.
                    e.printStackTrace();
                }
            }
        });

        btn119.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String emNum = "01056237516"; //일딴 박예진 번호
                String emText = "<<긴급 신고>>\n위치 : 숭실대학교 정보과학관\n저는 청각 장애인이며, 집에 화재가 발생했습니다.\nsend by SIGHOME";

                try{
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(emNum,null,emText,null,null);
                    Toast.makeText(getApplicationContext(), "긴급 문자 전송 완료!", Toast.LENGTH_LONG).show();
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "전송 오류!", Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();//오류 원인이 찍힌다.
                    e.printStackTrace();
                }
            }
        });

        Button cancelBtn = findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥 레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }
}