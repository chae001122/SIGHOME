package com.example.sighome;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;     //파이어베이스 인증
    private FirebaseUser mUser;             //사용자 정보
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스
    private EditText mEtEmail, mEtPwd;      //로그인 입력필드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth=FirebaseAuth.getInstance();
        mDatabaseRef= FirebaseDatabase.getInstance().getReference("SIGHOME");

        mEtEmail = findViewById(R.id.email_et);
        mEtPwd = findViewById(R.id.password_et);

        TextView login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 요청
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();

                mFirebaseAuth.signInWithEmailAndPassword(strEmail,strPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            mUser=mFirebaseAuth.getCurrentUser();
                            if(mUser.isEmailVerified()) {//사용자가 이메일 인증을 완료했다면
                                //로그인 성공
                                Toast.makeText(LoginActivity.this, "환영합니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, com.example.sighome.MainActivity.class);
                                startActivity(intent);
                                finish(); //현재 액티비티 파괴
                            }else{
                                Toast.makeText(LoginActivity.this, "이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(LoginActivity.this, "잘못된 이메일/비밀번호 입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        TextView gotoRegister = (TextView) findViewById(R.id.goto_register1_btn);
        gotoRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //회원가입 화면으로 이동
                Intent intent = new Intent(LoginActivity.this, com.example.sighome.Register1Activity.class);
                startActivity(intent);
            }
        });

        //이메일, 비밀번호 입력시 로그인 버튼 색상 변경
        mEtPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!TextUtils.isEmpty(mEtEmail.getText())&&!TextUtils.isEmpty(mEtPwd.getText())){
                    login_btn.setBackgroundColor(Color.parseColor("#0070C0"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    //빈 화면 클릭시 키보드 내리는 함수
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}