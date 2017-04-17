package com.example.navi_blind.bluetooth;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navi_blind.R;

/**
 * Created by 杨利娟 on 2017/3/28.
 */
public class BluetoothActivity extends Activity implements View.OnClickListener{
    private Button findBlindStickBtn;
    private Button startUpAvoidBtn;
    private TextView titleTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_layout);
        titleTv=(TextView)findViewById(R.id.text_title);
        titleTv.setText("蓝牙");
        findBlindStickBtn=(Button)findViewById(R.id.find_blindStick_btn);
        findBlindStickBtn.setOnClickListener(this);
        startUpAvoidBtn =(Button)findViewById(R.id.startup_avoidance_btn);
        startUpAvoidBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.find_blindStick_btn:
                Toast.makeText(BluetoothActivity.this,"寻找盲杖",Toast.LENGTH_SHORT).show();
                break;
            case R.id.startup_avoidance_btn:
                Toast.makeText(BluetoothActivity.this,"开启避障功能",Toast.LENGTH_SHORT).show();
        }
    }
}
