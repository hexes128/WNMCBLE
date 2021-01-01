package com.example.wnmc_ble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    Global gv;

    private HashMap<BluetoothGatt, BluetoothGattCharacteristic> CharaMap;

    private HashMap<BluetoothGatt, Boolean> Gattmap;
    private HashMap<BluetoothGatt, Integer> GattTimeout;
    private HashMap<BluetoothGatt, Integer> GattDataFlag;
    private Handler handler = new Handler();

    private List<byte[]> byteData = new ArrayList<>();
    byte[] power = {0x00, 0x0A};
    byte[] time = {0x01, 0X0A};
    byte[] swstate = {0x08, 0X0A};
    byte[] password = {0x31, 0x32, 0x33, 0x69, 0x74, 0x61, 0x69, 0x77, 0x61, 0x6E, 0x0A};

    private Button sendpass;
    private Button startpolling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(getPackageManager().FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getBaseContext(), "No_sup_ble", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        sendpass = findViewById(R.id.button);
        startpolling = findViewById(R.id.button3);
        byteData.add(time);
        byteData.add(power);

        byteData.add(swstate);

        gv = (Global) getApplicationContext();
        Gattmap = new HashMap<>();
        CharaMap = new HashMap<>();

        GattTimeout = new HashMap<>();

        GattDataFlag = new HashMap<>();


        sendpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (BluetoothGatt gatt : Gattmap.keySet()) {


                    GattDataFlag.put(gatt, 123);
                    BluetoothGattCharacteristic characteristic = CharaMap.get(gatt);
                    characteristic.setValue(password);
                    gatt.writeCharacteristic(characteristic);


                }


            }
        });

    }


    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

            }
        }
    };

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
            Intent intent = new Intent(MainActivity.this, BluetoothLeService.class);

            startService(intent);

        }


    }


    Runnable polling = new Runnable() {
        @Override
        public void run() {


            for (BluetoothGatt gatt : Gattmap.keySet()) {

                if (Gattmap.get(gatt)) {
                    GattDataFlag.put(gatt, 0);
                    Gattmap.put(gatt, false);
                    BluetoothGattCharacteristic characteristic = CharaMap.get(gatt);
                    characteristic.setValue(power);
                    gatt.writeCharacteristic(characteristic);


                }

            }


//            handler.postDelayed(this, 1000);
        }
    };


//    private BluetoothGattCallback mbluetoothGattCallback = new BluetoothGattCallback() {
//
//
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {//连接状态改变
//
//
//            Log.e(gatt.getDevice().getName(),status+"");
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.e(gatt.getDevice().getName(), "連線成功");
//                gatt.discoverServices();
//
//            }
////            if(status==BluetoothGatt.)
////            if (status == BluetoothGatt.STATE_DISCONNECTED) {
////
////                Gattmap.remove(gatt);
////
////            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {//发现服务，在蓝牙连接的时候会调用
//
//
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.e(gatt.getDevice().getName(), "服務發現成功");
//
//                List<BluetoothGattService > serverList = gatt.getServices();
//                Log.e("服務數量",serverList.size()+"");
//                for(BluetoothGattService service : serverList){
//                   for(BluetoothGattCharacteristic characteristic :service.getCharacteristics()){
//
//
//                       Log.e(service.getUuid().toString(),characteristic.getUuid().toString());
//                   }
//
//                }
////                BluetoothGattCharacteristic chara = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
////
////                if (chara != null) {
////                    gatt.setCharacteristicNotification(chara, true);
////
////                    Gattmap.put(gatt, true);
////                    CharaMap.put(gatt, chara);
////
////
////                }
////                else {
////                    Log.e("取得","失敗");
////                }
//            }
//
//
//        }
//
//
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {//发送数据时调用
//
//            if (status == BluetoothGatt.GATT_SUCCESS) {//写入成功
//
//
//            }
//            if (status == BluetoothGatt.GATT_FAILURE) {
//                Log.i("寫入", "不成功");
//            }
//
//
//        }
//
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {// Characteristic 改变，数据接收会调用
//            byte[] rec = characteristic.getValue();
//
//
//            for(int i=0;i<rec.length;i++){
//                Log.e("資料",rec[i]+"");
//            }
////            switch (GattDataFlag.get(gatt)) {
////
////                case (123): {
////                    try {
////                        Log.e("密碼回復" + gatt.getDevice().getName(), new String(rec, "UTF-8"));
////                        Gattmap.put(gatt, true);
////                    } catch (UnsupportedEncodingException e) {
////                        e.printStackTrace();
////                    }
////
////
////                    break;
////                }
////
////                case (0): {
////                    try {
////                        Log.e("電力" + gatt.getDevice().getName(), new String(rec, "UTF-8"));
////                        GattDataFlag.put(gatt, 1);
////                        characteristic.setValue(time);
////                        gatt.writeCharacteristic(characteristic);
////
////                    } catch (UnsupportedEncodingException e) {
////                        e.printStackTrace();
////                    }
////
////
////                    break;
////                }
////                case (1): {
////                    try {
////                        Log.e("時間" + gatt.getDevice().getName(), new String(rec, "UTF-8"));
////                        GattDataFlag.put(gatt, 8);
////                        characteristic.setValue(swstate);
////                        gatt.writeCharacteristic(characteristic);
////                    } catch (UnsupportedEncodingException e) {
////                        e.printStackTrace();
////                    }
////
////
////                    break;
////                }
////                case (8): {
////                    try {
////                        Log.e("開關狀態" + gatt.getDevice().getName(), new String(rec, "UTF-8"));
////                        Gattmap.put(gatt, true);
////                    } catch (UnsupportedEncodingException e) {
////                        e.printStackTrace();
////                    }
////
////
////                    break;
////                }
////
////
////            }
//
//
//        }
//
//
//    };


}
