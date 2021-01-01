package com.example.wnmc_ble;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScanActivity extends AppCompatActivity {
    private SwipeRefreshLayout rescan;
    private BluetoothLeScanner BleScanner;
    private BluetoothAdapter BleAdapter;
    private HashMap<BluetoothDevice, Boolean> ScanedDevice;
    Global gv;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        gv = (Global) getApplicationContext();
        rescan = findViewById(R.id.refresh);
        ScanedDevice = new HashMap<>();
        BleScanner = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().getBluetoothLeScanner();
        BleAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = findViewById(R.id.recyclerView1);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(ScanedDevice);
        mRecyclerView.setAdapter(mAdapter);




        mAdapter.notifyDataSetChanged();
        rescan.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Scan();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

         gv.checkeddevice.addAll(ScanedDevice.keySet().stream().filter(x->ScanedDevice.get(x)).collect(Collectors.toList()));

            setResult(1);
        }
        return super.onKeyDown(keyCode, event);
    }

    public void Scan() {
//        BleScanner.startScan(scanCallback);
        BleAdapter.startLeScan(scanCallback);
        new Handler().postDelayed(stopscan, 5000);
        rescan.setRefreshing(true);

    }

    Runnable stopscan = new Runnable() {
        @Override
        public void run() {

            rescan.setRefreshing(false);

        }
    };

    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {


            if (!ScanedDevice.keySet().contains(bluetoothDevice) && bluetoothDevice.getName()!=null) {
                ScanedDevice.put(bluetoothDevice, false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }
        }
    };



    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private HashMap<BluetoothDevice, Boolean> mScandevice;


        public MyAdapter(HashMap<BluetoothDevice, Boolean> Scandevice) {
            mScandevice = Scandevice;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView device_name;
            public TextView device_address;
            public CheckBox checkBox;

            public ViewHolder(View v) {
                super(v);
                device_name = v.findViewById(R.id.device_name);
                device_address = v.findViewById(R.id.device_address);
                checkBox = v.findViewById(R.id.checkBox);
            }
        }


        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.scandevicecardview, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }


        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final List<BluetoothDevice> devicelist = new ArrayList<>(mScandevice.keySet());


            holder.checkBox.setClickable(false);
            holder.device_name.setText(devicelist.get(position).getName());
            holder.device_address.setText(devicelist.get(position).getAddress());


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mScandevice.put(devicelist.get(position), !holder.checkBox.isChecked());
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
                }
            });

        }

        @Override
        public int getItemCount() {
            return mScandevice.size();
        }
    }
}
