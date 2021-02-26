package com.example.wnmc_ble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    Global gv;
    private TextView devicename,time,temperature;
    private HashMap<BluetoothGatt, BluetoothGattCharacteristic> characteristicHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicename= findViewById(R.id.devicename);
        time= findViewById(R.id.time);
        temperature= findViewById(R.id.temperature);
        if (!getPackageManager().hasSystemFeature(getPackageManager().FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getBaseContext(), "No_sup_ble", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        gv = (Global) getApplicationContext();
        characteristicHashMap = new HashMap<>();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        startActivityForResult(new Intent(MainActivity.this, ScanActivity.class), 1);

        return true;


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1) {
            gv.choosedevice.connectGatt(getApplicationContext(), false, mbluetoothGattCallback);

        }


    }


    private BluetoothGattCallback mbluetoothGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {//连接状态改变


            Log.e(gatt.getDevice().getName(), status + "");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(gatt.getDevice().getName(), "連線成功");
                gatt.discoverServices();

            }
//            if(status==BluetoothGatt.)
//            if (status == BluetoothGatt.STATE_DISCONNECTED) {
//
//                Gattmap.remove(gatt);
//
//            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {//发现服务，在蓝牙连接的时候会调用


            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(gatt.getDevice().getName(), "服務發現成功");


                BluetoothGattCharacteristic chara = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));

                if (chara != null) {
                    gatt.setCharacteristicNotification(chara, true);

                    characteristicHashMap.put(gatt, chara);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
devicename.setText(gatt.getDevice().getName());
                        }
                    });


                }
            }


        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {//发送数据时调用

            if (status == BluetoothGatt.GATT_SUCCESS) {//写入成功


            }
            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.i("寫入", "不成功");
            }


        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {// Characteristic 改变，数据接收会调用
            byte[] rec = characteristic.getValue();


            String s = new String(rec).trim();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Date currentTime = Calendar.getInstance().getTime();
                    time.setText(currentTime+"");
                    temperature.setText(s);
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                }
            });


        }


    };



}
