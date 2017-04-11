package com.example.henning.matrixmultiplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static com.example.henning.matrixmultiplication.R.id.listPairedDevices;

/**
 * Created by Henning on 10/04/2017.
 */

public class bluetoothMenu extends Activity{

    private final static int REQUEST_ENABLE_BT = 1;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private ListView mPairedDevices;
    private ListView mNewDevices;

    private ArrayList<BluetoothDevice> newDevices;
    private ArrayList<String> listNewDevices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetoothmenu);

        mPairedDevices = (ListView) findViewById(listPairedDevices);
        mNewDevices = (ListView) findViewById(R.id.listNewDevices);

        //Is bluetooth supported?
        if (mBluetoothAdapter == null) {
            //device does not support bluetooth
        }
        //is bluetooth enabled?
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(bluetoothMenu.this, "Bluetooth enabled", Toast.LENGTH_LONG).show();
        }

        ArrayList<String> listPairedDevices = new ArrayList<>();



        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //any devices already connected with the phone?
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceMACAddress = device.getAddress();

                listPairedDevices.add(deviceName + "\n" + deviceMACAddress);

                mPairedDevices.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listPairedDevices));
            }
        }



        Button b = (Button)findViewById(R.id.btnSearchNewDevices);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listNewDevices = new ArrayList<>();
                newDevices = new ArrayList<>();
                mBluetoothAdapter.startDiscovery();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter);
            }
        });

        mNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice selectedDevice = newDevices.get(position);

                //attempt to pair to new device
                selectedDevice.createBond();
            }
        });

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                newDevices.add(device);
                String deviceName = device.getName();
                String deviceMACAddress = device.getAddress();

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    listNewDevices.add(deviceName + "\n" + deviceMACAddress);

                    mNewDevices.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, listNewDevices));
                }
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //pair from device: dev.getName()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    dev.setPairingConfirmation(true);
                    Toast.makeText(bluetoothMenu.this, "Paired with " + dev.getName(), Toast.LENGTH_LONG).show();
                    //successfull pairing
                } else {
                    Toast.makeText(bluetoothMenu.this, "Not paired!", Toast.LENGTH_LONG).show();
                    //impossible to automatically perform pairing,
                    //your Android version is below KITKAT
                }
            }
        }
    };
}
