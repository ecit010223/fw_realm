package com.huier.fw_realm.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.huier.fw_realm.R;

public class QueryActivity extends AppCompatActivity {
    private Context mContext;

    public static void entry(Context from){
        Intent intent = new Intent(from,QueryActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        mContext = this;
        initView();
    }

    private void initView(){

    }
}
