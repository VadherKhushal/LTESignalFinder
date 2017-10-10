package com.signalfinder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class MainActivity extends AppCompatActivity implements Observer{
    public static final int UPLOAD = 1;
    public static final int DOWNLOAD = 2;
    private static final String TAG = "Main Activity";
    TelephonyManager tManager;
    ProgressBar progressBar, progressBar2;
    PhoneState phoneState;
    TextView tv, tv2, tv3, tvDownload, tvUpload, tvProg;
    CustomPhoneStateListener phoneStateListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneState = PhoneState.getObserver();
        phoneState.addObserver(this);

        tManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        tv = (TextView)findViewById(R.id.textView);
        tv2 = (TextView)findViewById(R.id.textView2);
        tv3 = (TextView)findViewById(R.id.textView3);
        tvDownload = (TextView)findViewById(R.id.textDownload);
        tvUpload = (TextView)findViewById(R.id.textUpload);
        tvProg = (TextView)findViewById(R.id.textProg);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar2 = (ProgressBar)findViewById(R.id.progressBar2);
        progressBar.setProgress(0);

        phoneStateListener = new CustomPhoneStateListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tv.setText("");
        tv2.setText("");
        tv3.setText("");
        if(phoneStateListener==null) {
            phoneStateListener = new CustomPhoneStateListener(this);
        }
        tManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                PhoneStateListener.LISTEN_DATA_ACTIVITY |
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE );

        Log.d(TAG, "onResume: Registered PhoneStateListener");
    }

    @Override
    protected void onPause() {
        super.onPause();
        tManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        Log.d(TAG, "onPause: UnRegistered PhoneStateListener");
    }

    @Override
    public void update(Observable observable, Object o) {
        tv.setText(phoneState.state);
        tv2.setText(phoneState.strength);
        tv3.setText("N/W Type: "+ phoneState.networkType+
                "\nN/W State: " + phoneState.connectionState+
                "\nN/W DataFlow: "+phoneState.getDataDirection());
        if(phoneState.signalDbm==-1) {
            progressBar.setProgress(0);
        }else{
            int prog = (((phoneState.signalDbm*-1)-60)*100)/(120-60);
            progressBar.setProgress(prog);
        }
    }

    public void startUpload(int mode) {
        new SpeedTestTask().execute(UPLOAD, mode);
    }

    public void startDownload(View view) {
        new SpeedTestTask().execute(DOWNLOAD, 1);
    }

    public void start10sDownload(View view) {
        new SpeedTestTask().execute(DOWNLOAD, 2);
    }

    String downResult = "", uploadResult = "";

    public class SpeedTestTask extends AsyncTask<Integer, String, String> {

        int testMode = DOWNLOAD, mode = 1;
        String result = "";
        SpeedTestSocket speedTestSocket = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvDownload.setText("");
            tvUpload.setText("");
            tvProg.setText("");
            progressBar2.setVisibility(View.VISIBLE);
            tvProg.setVisibility(View.VISIBLE);
            findViewById(R.id.buttonDown).setEnabled(false);
        }

        private void resetSTT(){
            speedTestSocket.closeSocket();
            speedTestSocket.forceStopTask();
        }

        @Override
        protected String doInBackground(Integer... params) {
            Log.d(TAG, "doInBackground: Creating Socket");
            speedTestSocket = new SpeedTestSocket();
            Log.d(TAG, "doInBackground: Socket Created");

            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onCompletion(SpeedTestReport report) {
                    // called when download/upload is finished
                    Log.v("speedtest", "[COMPLETED] rate in octet/s : " + report.getTransferRateOctet());
                    Log.v("speedtest", "[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
                    BigDecimal divisor = new BigDecimal(1024);
                    BigDecimal dec = report.getTransferRateBit().divide(divisor,2,BigDecimal.ROUND_HALF_UP);
                    dec = dec.divide(divisor,2,BigDecimal.ROUND_HALF_UP);
                    if(testMode==DOWNLOAD) {
                        downResult = String.valueOf(dec);
                        Log.v("speedtest", "[COMPLETED] download rate in Mbps : " + downResult );
                    }else if(testMode==UPLOAD){
                        uploadResult = String.valueOf(dec);
                        Log.v("speedtest", "[COMPLETED] upload rate in Mbps : " + uploadResult);
                    }
                    publishProgress(String.valueOf(testMode));
                    resetSTT();
                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {
                    // called when a download/upload error occur
                    Log.d("speedtest", "onError: "+errorMessage);
                    String[] str = new String[]{"ERROR", errorMessage};
                    publishProgress(str);
                    resetSTT();
                }

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                    // called to notify download/upload progress
                    Log.v("speedtest", "[PROGRESS] progress : " + percent + "%");
                    Log.v("speedtest", "[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                    Log.v("speedtest", "[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
                    String[] str = new String[]{"PROG", String.valueOf(percent)};
                    publishProgress(str);
                }
            });

            testMode = params[0];
            mode = params[1];

            if(params[0]==DOWNLOAD) {
                Log.d(TAG, "doInBackground: Starting Download");
                if(mode == 1) {
                    speedTestSocket.startDownload("http://2.testdebit.info/fichiers/1Mo.dat");
                }else{
                    speedTestSocket.startFixedDownload("http://2.testdebit.info/fichiers/100Mo.dat", 15000);
                }
            }else if(params[0]==UPLOAD){
                Log.d(TAG, "doInBackground: Starting Upload");
                if(mode == 1) {
                    speedTestSocket.startUpload("http://2.testdebit.info/", 1000000);
                }else{
                    speedTestSocket.startFixedUpload("http://2.testdebit.info/", 10000000, 15000);
                }
            }

            return null;
        }

        private void showResult(int teste, int mode) {
            if(teste==DOWNLOAD){
                testMode = 0;
                tvDownload.setText("Download:"+downResult+"Mbps");
                startUpload(mode);
            }else if(teste==UPLOAD){
                progressBar2.setVisibility(View.GONE);
                tvProg.setVisibility(View.GONE);
                tvDownload.setText("Download:"+downResult+"Mbps");
                tvUpload.setText("Upload:"+uploadResult+"Mbps");
                findViewById(R.id.buttonDown).setEnabled(true);
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(values.length>1 && values[0].equals("PROG")) {
                if (testMode == DOWNLOAD) {
                    tvProg.setText("Downloading:" + values[1] + "%");
                } else if (testMode == UPLOAD) {
                    tvProg.setText("Uploading:" + values[1] + "%");
                }
            }else if(values.length>1 && values[0].equals("ERROR")) {
                progressBar2.setVisibility(View.GONE);
                tvProg.setVisibility(View.GONE);
                if(testMode==UPLOAD){
                    tvDownload.setText("Download:"+downResult+"Mbps");
                }
                findViewById(R.id.buttonDown).setEnabled(true);
                Toast.makeText(MainActivity.this, "Error performing test.\n"+values[1], Toast.LENGTH_SHORT).show();
            }else{
                showResult(testMode, mode);
            }
        }
    }
}
