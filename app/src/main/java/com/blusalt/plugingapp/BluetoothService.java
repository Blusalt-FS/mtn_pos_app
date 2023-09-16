package com.blusalt.plugingapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class BluetoothService extends Service {
    public BluetoothService() {
    }

    BluetoothListener listener = new BluetoothListener() {
        @Override
        public void onStart() {
//            Toast.makeText(getApplicationContext(),"Waiting for connection device",
//                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStop() {

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        onTaskRemoved(intent);
        listener.onStart();
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }
}