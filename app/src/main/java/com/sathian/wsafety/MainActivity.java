package com.sathian.wsafety;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Map;


public class MainActivity extends AppCompatActivity {


    private MediaPlayer mediaPlayer;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;

    private boolean isButtonOn = false;

    private Handler handler = new Handler();

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String number1 = sharedPreferences.getString("ENUM1", "NONE");
        String number2 = sharedPreferences.getString("ENUM2", "NONE");
        String number3 = sharedPreferences.getString("ENUM3", "NONE");

        Log.d("MainActivity", "ENUM1: " + number1 + ", ENUM2: " + number2 + ", ENUM3: " + number3);

        if ("NONE".equals(number1) || "NONE".equals(number2) || "NONE".equals(number3)) {
            // If any number is missing, redirect to register activity
            startActivity(new Intent(this, RegisterNumberActivity.class));
            finish();
        } else {
            // If numbers are present, display them in the TextView
            TextView textView = findViewById(R.id.textNum);
            textView.setText("SOS Will Be Sent To\n" + number1 + "\n" + number2 + "\n" + number3);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = MediaPlayer.create(this, R.raw.ambul);


        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        ImageButton imageButton = findViewById(R.id.sosbtn);


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isButtonOn) {
                    // Change the image to the off state
                    if (mediaPlayer != null) {
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                    }
                    imageButton.setBackgroundResource(R.drawable.soson);

                    stopSound();
                    // Stop the siren sound
                    stopBlinking(); // Stop the flash blinking
                } else {
                    // Change the image to the on state
                    imageButton.setBackgroundResource(R.drawable.sosoff);
                    startSound();

                    // Start the siren sound
                    startBlinking(); // Start the flash blinking
                }
                // Toggle the state
                isButtonOn = !isButtonOn;
            }
        });







        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("MYID", "CHANNELFOREGROUND", NotificationManager.IMPORTANCE_DEFAULT);

                NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                m.createNotificationChannel(channel);
            }
        }

    }

    private void toggleFlash() {
        try {
            if (isFlashOn) {
                cameraManager.setTorchMode(cameraId, false);
            } else {
                cameraManager.setTorchMode(cameraId, true);
            }
            isFlashOn = !isFlashOn; // Toggle the flash state
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startSound() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
    }


    private void startBlinking() {
        handler.postDelayed(blinkRunnable, 500); // Start the blinking mechanism with a delay of 500ms
    }

    private void stopBlinking() {
        handler.removeCallbacks(blinkRunnable); // Stop the blinking mechanism
    }

    private final Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {
            toggleFlash(); // Toggle the flash state
            handler.postDelayed(this, 500); // Schedule the next toggle after 500ms
        }
    };




    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    private ActivityResultLauncher<String[]> multiplePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {

            for (Map.Entry<String,Boolean> entry : result.entrySet())
                if(!entry.getValue()){
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Needed Some Permission to Work Properly!", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Grant Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            multiplePermissions.launch(new String[]{entry.getKey()});
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }


        }

    });



    public void stopService(View view) {

        Intent notificationIntent = new Intent(this,ServiceMine.class);
        notificationIntent.setAction("stop");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(notificationIntent);
            Snackbar.make(findViewById(android.R.id.content),"Service Stopped!", Snackbar.LENGTH_LONG).show();
        }
    }

    public void startServiceV(View view) {





        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED  ) {
            Intent notificationIntent = new Intent(this,ServiceMine.class);
            notificationIntent.setAction("Start");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(notificationIntent);
                Snackbar.make(findViewById(android.R.id.content),"Started!", Snackbar.LENGTH_LONG).show();
            }
        }else{
            multiplePermissions.launch(new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION});
        }

    }

    public void PopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this,view);
        popupMenu.getMenuInflater().inflate(R.menu.popup,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.changeNum){
                    startActivity(new Intent(MainActivity.this,RegisterNumberActivity.class));
                }
                return true;
            }
        });
        popupMenu.show();
    }
}