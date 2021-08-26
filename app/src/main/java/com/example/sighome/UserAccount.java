package com.example.sighome;

/*사용자 계정 정보 모델 클래스*/

public class UserAccount {

    private String userName;    // 계정 닉네임
    private String idToken;     // Firebase Uid (고유 토큰 정보)
    private String emailId;     // 이메일 아이디
    private String password;    // 비밀번호

    //클래스가 생성될 때 가장 먼저 호출하는 것
    public UserAccount() { }

    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getIdToken() { return idToken; }

    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getEmailId() { return emailId; }

    public void setEmailId(String emailId) { this.emailId = emailId; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

}
