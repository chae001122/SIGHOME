package com.example.sighome;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class Cctv_outdoorActivity extends AppCompatActivity {

    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; //웹뷰세팅

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cctv_outdoor);

        mWebView = (WebView) findViewById(R.id.watch_cctv_out);


        mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
       // mWebSettings = mWebView.getSettings(); //세부 세팅 등록
        //mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
        //mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        //mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        //mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        //mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        //mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        //mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
       // mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
       // mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        //mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부


        setDesktopMode(mWebView,true);
        mWebView.loadUrl("http://111.118.51.164:81/stream"); // 웹뷰에 표시할 라즈베리파이 주소, 웹뷰 시작
        //mWebView.setWebChromeClient(new WebChromeClient());
        //mWebView.setWebViewClient(new WebViewClientClass());

        Button cancelBtn = findViewById(R.id.cancel_btn_out);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
            }
        });


    }
    public void setDesktopMode(WebView webView,boolean enabled) {
        String newUserAgent = webView.getSettings().getUserAgentString();
        if (enabled) {
            try {
                String ua = webView.getSettings().getUserAgentString();
                String androidOSString = webView.getSettings().getUserAgentString().substring(ua.indexOf("("), ua.indexOf(")") + 1);
                newUserAgent = webView.getSettings().getUserAgentString().replace(androidOSString, "(X11; Linux x86_64)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            newUserAgent = null;
        }

        webView.getSettings().setUserAgentString(newUserAgent);
        webView.getSettings().setUseWideViewPort(enabled);
        webView.getSettings().setLoadWithOverviewMode(enabled);
        webView.reload();
    }

    private class WebViewClientClass extends WebViewClient {//페이지 이동
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("check URL",url);
            view.loadUrl(url);
            return true;
        }
    }
}