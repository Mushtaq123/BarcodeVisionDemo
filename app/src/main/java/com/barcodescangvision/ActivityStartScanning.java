package com.barcodescangvision;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ActivityStartScanning extends AppCompatActivity {

    private static final String TAG = ActivityStartScanning.class.getSimpleName();
    private Context context;
    private Handler handler = new Handler();
    private RelativeLayout parent;
    private RelativeLayout parent_surface;
    private SurfaceView preview;
    private CameraSource mCameraSource;
    private CustomRequestPreviewT requestPreview;
    private BarcodeDetector barcodeDetector;

    private long nextDelayTime = 5 * 1000;
    private boolean isAcceptValue = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testcustomisation);

        context = ActivityStartScanning.this;
        initializeViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(task);
    }


    private void initializeViews() {
        parent = (RelativeLayout) findViewById(R.id.parent);
        parent_surface = (RelativeLayout) findViewById(R.id.parent_surface);
        //Rect Preview
        requestPreview = new CustomRequestPreviewT(context);
        RelativeLayout.LayoutParams pram1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        pram1.addRule(RelativeLayout.CENTER_IN_PARENT);
        parent.addView(requestPreview);

        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    //SurfaceView
                    preview = new SurfaceView(context);
                    RelativeLayout.LayoutParams pram2 = new RelativeLayout.LayoutParams(requestPreview.getWD() - (requestPreview.paddingLR * 2),
                            requestPreview.getHT() - (requestPreview.paddingTB * 2));
                    pram2.addRule(RelativeLayout.CENTER_IN_PARENT);
                    preview.setLayoutParams(pram2);
                    parent_surface.addView(preview);

                    startScanning();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 200);

    }

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            isAcceptValue = true;
            requestPreview.setStartScanning(true);
        }
    };

    private void startScanning() {
        barcodeDetector = new BarcodeDetector.Builder(context).build();
        mCameraSource = new CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(1)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(requestPreview.getWD() - (requestPreview.paddingLR * 2),
                        requestPreview.getHT() - (requestPreview.paddingTB * 2))
                .build();

        preview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ActivityStartScanning.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mCameraSource.start(preview.getHolder());
                    requestPreview.setStartScanning(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> items = detections.getDetectedItems();
                if (items != null && items.size() >= 1) {
                    final String code = items.valueAt(0).displayValue;
                    if (!isAcceptValue)
                        return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ActivityStartScanning.this, code, Toast.LENGTH_SHORT).show();
                            isAcceptValue = false;
                            handler.postDelayed(task, nextDelayTime);
                            requestPreview.setStartScanning(false);
                        }
                    });
                }

//                final SparseArray<Barcode> items = detections.getDetectedItems();
//
//                if (!isAcceptValue)
//                    return;
//
//                if (items != null && items.size() >= 1) {
//
//                    for (int i = 0; i < items.size(); i++) {
//                        Barcode barcode = items.valueAt(1);
//                        final String code = barcode.rawValue;
//                        RectF rectF = new RectF(barcode.getBoundingBox());
//
//                        Log.i(TAG, "L: " + rectF.left + "R: " + rectF.right + "T: " + rectF.top + "B: " + rectF.bottom);
//                        Log.i(TAG, "Detected barcode: " + code);
//
//                        int rectL = requestPreview.paddingLR;
//                        int rectR = (requestPreview.getWD() - requestPreview.paddingLR);
//                        int rectT = requestPreview.paddingTB;
//                        int rectB = (requestPreview.getHT() - requestPreview.paddingTB);
//
//                        Log.i(TAG, "RectL: " + rectL + " RectR: " + rectR + " RectT: " + rectT + " RectB: " + rectB);
//
//                        if (rectF.left > rectL
//                                && rectF.right < rectR
//                                && rectF.top > rectT
//                                && rectF.bottom < rectB) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(ActivityStartScanning.this, code, Toast.LENGTH_SHORT).show();
//                                    isAcceptValue = false;
//                                    handler.postDelayed(task, nextDelayTime);
//                                }
//                            });
//                        }
//                    }
//                }
            }
        });
    }
}



//         720*1280
//
//           179/206
//
// 201/100               995/668
//
//           491/248
//
//I/ActivityStartScanning: L: 100.0 R: 668.0 T: 206.0 B: 248.0
//I/ActivityStartScanning: Detected barcode: 00001234560000000018
//I/ActivityStartScanning: RectL: 201 RectR: 995 RectT: 670 RectB: 491
//
//I/ActivityStartScanning: L: 100.0 R: 668.0 T: 206.0 B: 248.0
//I/ActivityStartScanning: Detected barcode: 00001234560000000018
//I/ActivityStartScanning: RectL: 201 RectR: 995 RectT: 670 RectB: 491
//
//I/ActivityStartScanning: L: 81.0 R: 705.0 T: 198.0 B: 248.0
//I/ActivityStartScanning: Detected barcode: 00001234560000000018
//I/ActivityStartScanning: RectL: 201 RectR: 995 RectT: 179 RectB: 491
//I/ActivityStartScanning: L: 81.0 R: 705.0 T: 198.0 B: 248.0
//I/ActivityStartScanning: Detected barcode: 00001234560000000018
//I/ActivityStartScanning: RectL: 201 RectR: 995 RectT: 179 RectB: 491
