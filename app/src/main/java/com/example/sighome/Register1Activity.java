package com.example.sighome;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
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

import static android.content.ContentValues.TAG;

public class Register1Activity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;                            //파이어베이스 인증
    private FirebaseUser mUser;                                    //사용자 정보
    private DatabaseReference mDatabaseRef;                        //실시간 데이터베이스
    private EditText mEtUserName, mEtEmail, mEtPwd, mEtReRwd;      //회원가입 입력필드
    private TextView mBtnRegister;                                 //회원가입 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register1);

        mFirebaseAuth=FirebaseAuth.getInstance();
        mDatabaseRef= FirebaseDatabase.getInstance().getReference("SIGHOME");

        mEtUserName = findViewById(R.id.regist_username_et);
        mEtEmail = findViewById(R.id.regist_email_et);
        mEtPwd = findViewById(R.id.regist_password_et);
        mEtReRwd = findViewById(R.id.regist_repassword_et);

        mBtnRegister = findViewById(R.id.send_email_bt);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //회원가입 처리 시작
                String strUserName = mEtUserName.getText().toString();
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();
                String strReRwd = mEtReRwd.getText().toString();

                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

                if(strEmail==null||strEmail.isEmpty()||strPwd==null||strPwd.isEmpty()||strReRwd==null||strReRwd.isEmpty()){//공백란이 있음
                    Toast.makeText(Register1Activity.this, "로그인 이메일/비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                }else if(!strPwd.equals(strReRwd)){
                    Toast.makeText(Register1Activity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                }else if(status==NetworkStatus.TYPE_NOT_CONNECTED){
                    Toast.makeText(Register1Activity.this, "네트워크 상태를 확인해주세요", Toast.LENGTH_SHORT).show();
                }else{
                    //Firebase Auth 진행
                    mFirebaseAuth.createUserWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(Register1Activity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {//가입 성공시
                                mUser=mFirebaseAuth.getCurrentUser();
                                UserAccount account = new UserAccount();

                                account.setUserName(strUserName);
                                account.setIdToken(mUser.getUid());
                                account.setEmailId(mUser.getEmail());
                                account.setPassword(strPwd);

                                //setValue : database에 insert (삽입) 행위
                                mDatabaseRef.child("UserAccount").child(mUser.getUid()).setValue(account);

                                mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){//이메일 인증 성공 시
                                            Log.d(TAG, "Email sent");
                                            Toast.makeText(Register1Activity.this, "인증 이메일이 발송되었습니다.", Toast.LENGTH_SHORT).show();

                                            mFirebaseAuth.signOut(); //회원가입 완료시 로그아웃 후 로그인 화면으로 이동

                                            Intent intent = new Intent(Register1Activity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();

                                        }else{
                                            if(status==NetworkStatus.TYPE_NOT_CONNECTED){
                                                Toast.makeText(Register1Activity.this, "네트워크 상태를 확인해주세요", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(Register1Activity.this, "이메일 발송 실패", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
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