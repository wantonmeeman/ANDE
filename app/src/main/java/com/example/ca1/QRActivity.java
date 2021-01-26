package com.example.ca1;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class QRActivity extends AppCompatActivity {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textView;
    View rightView;
    View leftView;
    BarcodeDetector barcodeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        String userid = "";
        SharedPreferences pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        GoogleSignInAccount gAcc = GoogleSignIn.getLastSignedInAccount(this);

        if(gAcc != null){
            userid = gAcc.getId();
        }else{
            userid = pref.getString("firebaseUserId","1");
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://schedulardb-default-rtdb.firebaseio.com");

        DatabaseReference myDbRef = database.getReference("usersInformation").child(userid).child("UserAlarms");

        setContentView(R.layout.qr_code);
        surfaceView = ((SurfaceView) findViewById(R.id.cameraPreview));
        surfaceView.setVisibility(View.INVISIBLE);
        textView = (TextView) findViewById(R.id.textView);

        barcodeDetector = new BarcodeDetector
                .Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        surfaceView.setVisibility(View.VISIBLE);
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(900, 480).setAutoFocusEnabled(true).build();


        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
                    return;
                } else {
                    try {
                        cameraSource.start(surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }


            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>(){

            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                Frame.Metadata meta = detections.getFrameMetadata();
                double nearestDistance = Double.MAX_VALUE;
                if(qrCodes.size()!= 0){
                    for (int i = 0; i < qrCodes.size(); ++i) {
                        int id = qrCodes.keyAt(i);
                        Barcode qrCode = qrCodes.get(id);
                        Log.e("Bounding Box",qrCode.getBoundingBox().toString());
                        Log.e("Center Y", String.valueOf(qrCode.getBoundingBox().centerY()));
                        Log.e("Center X", String.valueOf(qrCode.getBoundingBox().centerX()));

                        if(qrCode.getBoundingBox().centerY() > 325 && qrCode.getBoundingBox().centerY() < 575){
                            textView.post(new Runnable(){
                                public void run(){

                                    Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                    vibrator.vibrate(1000);
                                    String qrcode = qrCodes.valueAt(0).displayValue;
                                    JSONObject jObject = null;
                                    try{

                                        Alarm alarm = new Alarm();
                                        jObject = new JSONObject(qrcode);
                                        alarm.setTitle(jObject.getString("Title"));
                                        alarm.setDescription(jObject.getString("Description"));
                                        alarm.setLongitude(jObject.getDouble("Longitude"));
                                        alarm.setLatitude(jObject.getDouble("Latitude"));
                                        alarm.setUnixTime(jObject.getLong("UnixTime"));
                                        myDbRef.push().setValue(alarm);
                                        //Add some code to stop it to prevent spamming firebase
                                    }catch(JSONException e){
                                        Log.e("Error",e.toString());
                                    }

                                }
                            });
                        }

                    }

                }
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {//Handles the permission response from user
        Log.i("Work","Work");
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Accepted","Accepted");
                    try {
                        cameraSource.start(surfaceView.getHolder());//This error can be ignored
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i("Denied","Denied");
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }
}



