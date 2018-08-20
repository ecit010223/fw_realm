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

import com.huier.fw_realm.activity.DemoActivity;
import com.huier.fw_realm.activity.DocumentActivity;
import com.huier.fw_realm.activity.GridViewActivity;
import com.huier.fw_realm.activity.Json2Activity;
import com.huier.fw_realm.activity.JsonActivity;
import com.huier.fw_realm.activity.ListViewActivity;
import com.huier.fw_realm.activity.MigrationActivity;
import com.huier.fw_realm.activity.NotificationActivity;
import com.huier.fw_realm.activity.QueryActivity;
import com.huier.fw_realm.activity.RealmActivity;
import com.huier.fw_realm.activity.RecyclerViewActivity;
import com.huier.fw_realm.activity.ThreadActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    /** 当前被点击的View **/
    private View mCurrentClickView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
    }

    private void initView(){
        ((Button)findViewById(R.id.btn_demo)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_document)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_json_2)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_query)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_realm)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_thread)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_json)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_notification)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_migration)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_listvew)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_recyclerview)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_gridview)).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(requestPermission()){
            switch (view.getId()){
                case R.id.btn_demo:
                    DemoActivity.entry(mContext);
                    break;
                case R.id.btn_document:
                    DocumentActivity.entry(mContext);
                    break;
                case R.id.btn_query:
                    QueryActivity.entry(mContext);
                    break;
                case R.id.btn_realm:
                    RealmActivity.entry(mContext);
                    break;
                case R.id.btn_thread:
                    ThreadActivity.entry(mContext);
                    break;
                case R.id.btn_json:
                    JsonActivity.entry(mContext);
                    break;
                case R.id.btn_json_2:
                    Json2Activity.entry(mContext);
                    break;
                case R.id.btn_notification:
                    NotificationActivity.entry(mContext);
                    break;
                case R.id.btn_migration:
                    MigrationActivity.entry(mContext);
                    break;
                case R.id.btn_listvew:
                    ListViewActivity.entry(mContext);
                    break;
                case R.id.btn_recyclerview:
                    RecyclerViewActivity.entry(mContext);
                    break;
                case R.id.btn_gridview:
                    GridViewActivity.entry(mContext);
                    break;
            }
        }else{
            mCurrentClickView = view;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case Constant.INTERNET_REQUEST_CODE:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    onClick(mCurrentClickView);
                }
                break;
        }
    }

    private boolean requestPermission(){
        boolean hasPermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.INTERNET)== PackageManager.PERMISSION_GRANTED;
        if(!hasPermission){
            ActivityCompat.requestPermissions(scanForActivity(mContext),new String[]{Manifest.permission.INTERNET},
                    Constant.INTERNET_REQUEST_CODE);
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
