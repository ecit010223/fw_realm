package com.huier.fw_realm.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.huier.fw_realm.Constant;
import com.huier.fw_realm.R;
import com.huier.fw_realm.sqlite.FeedReaderContract;
import com.huier.fw_realm.sqlite.FeedReaderDbHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DemoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = DemoActivity.class.getSimpleName();
    private Context mContext;
    private FeedReaderDbHelper mDbHelper;

    public static void entry(Context from){
        Intent intent = new Intent(from,DemoActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mContext = this;
        initView();
        initData();
    }

    private void initView() {
        ((Button) findViewById(R.id.btn_net_query)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_share)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_sqlite)).setOnClickListener(this);
    }

    private void initData(){
        mDbHelper = new FeedReaderDbHelper(mContext);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_net_query:
                netQuery();
                break;
            case R.id.btn_share:
                share();
                break;
            case R.id.btn_sqlite:
                sqlite();
                break;
        }
    }

    /** SQLite的使用 **/
    private void sqlite(){
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, "sqlite title");
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE, "sqlite subtitle");

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
    }

    /** SharedPreferences的使用 **/
    private void share(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Constant.SHARE_ID,11);
        editor.putString(Constant.SHARE_VALUE,"welcome");
        editor.commit();
    }

    /**
     * 网络请求数据测试
     **/
    private void netQuery() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        //Enable Network Inspection
        okHttpClientBuilder.addNetworkInterceptor(new StethoInterceptor()).build();

        OkHttpClient okHttpClient = okHttpClientBuilder.build();
        Request request = new Request.Builder().url(Constant.URL_RXJAVA).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "网络请求失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, response.body().string());
            }
        });
    }
}
