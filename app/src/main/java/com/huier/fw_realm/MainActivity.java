package com.huier.fw_realm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private Button btnDocument,btnIntroExample;
    /** 当前被点击的View **/
    private View mCurrrentClickView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
    }

    private void initView(){
        btnDocument = (Button)findViewById(R.id.btn_document);
        btnDocument.setOnClickListener(this);
        btnIntroExample = (Button)findViewById(R.id.btn_intro_example);
        btnIntroExample.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(requestPermission()){
            switch (view.getId()){
                case R.id.btn_document:
                    document();
                    break;
                case R.id.btn_intro_example:
                    introExample();
                    break;
            }
        }else{
            mCurrrentClickView = view;
        }
    }

    /** 官方文档中的实例汇总 **/
    private void document(){

    }

    private void introExample(){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case Constants.INTERNET_REQUEST_CODE:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    onClick(mCurrrentClickView);
                }
                break;
        }
    }

    private boolean requestPermission(){
        boolean hasPermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.INTERNET)== PackageManager.PERMISSION_GRANTED;
        if(!hasPermission){
            ActivityCompat.requestPermissions(scanForActivity(mContext),new String[]{Manifest.permission.INTERNET},
                    Constants.INTERNET_REQUEST_CODE);
            ActivityCompat.shouldShowRequestPermissionRationale(scanForActivity(mContext),Manifest.permission.INTERNET);
        }
        return hasPermission;
    }

    private Activity scanForActivity(Context context){
        if(context == null){
            return null;
        }else if(context instanceof Activity){
            return (Activity)context;
        }else if(context instanceof ContextWrapper){
            return scanForActivity(((ContextWrapper)context).getBaseContext());
        }
        return null;
    }
}
