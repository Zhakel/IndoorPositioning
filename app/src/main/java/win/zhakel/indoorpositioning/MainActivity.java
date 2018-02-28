package win.zhakel.indoorpositioning;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView tv_acc;
    TextView tv_gyo;
    TextView tv_mag;
    TextView tv_ori;
    TextView tv_wifi;
    TextView tv_myori;

    WifiManager wifiManager;
    SensorManager sm;

    Handler handler;

    private float[] rotations, accs, mags, oris, gyos, myoris;
    private float dt = 0.01f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_ori = findViewById(R.id.tv_ori);
        tv_acc = findViewById(R.id.tv_acc);
        tv_gyo = findViewById(R.id.tv_gyo);
        tv_mag = findViewById(R.id.tv_mag);
        tv_wifi = findViewById(R.id.tv_wifi);
        tv_myori = findViewById(R.id.tv_myori);

        //获取wifimanager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        handler = new Handler();

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sm != null;

//        //得到传感器列表
//        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
//
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < sensors.size(); i++) {
//            sb.append(sensors.get(i).getName());
//            sb.append("\n");
//        }
//
//        tv.setText(sb.toString());

        //获取传感器
        Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyo = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //注册传感器
        sm.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this, gyo, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this, mag, SensorManager.SENSOR_DELAY_GAME);

        rotations = new float[9];
        accs = new float[3];
        mags = new float[3];
        oris = new float[3];
        gyos = new float[3];
        myoris = new float[3];

        //进行wifi扫描结果显示
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                List<ScanResult> scanResults;
//
//                scanResults = wifiManager.getScanResults();
//                final StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < scanResults.size(); i++) {
//                    sb.append(scanResults.get(i).timestamp);
//                    sb.append("\t");
//                    sb.append(scanResults.get(i).BSSID);
//                    sb.append("\t");
//                    sb.append(scanResults.get(i).SSID);
//                    sb.append("\t");
//                    sb.append(scanResults.get(i).level);
//                    sb.append("\n");
//                }
//
//                tvset(sb);
//
//                handler.postDelayed(this,500);
//            }
//        };
        new Thread(wifiScanR).start();
        float mytest = 0.01f;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sm.unregisterListener(this);

        handler.removeCallbacks(wifiScanR);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float triple[] = sensorEvent.values;
//        StringBuilder stringBuffer = new StringBuilder();
//
//        for (float aTriple : triple) {
//            stringBuffer.append(aTriple);
//            stringBuffer.append("\n");
//        }
        if (Math.abs(accs[0]) > dt && Math.abs(mags[0]) > dt) {
            getOris();
            tv_ori.setText(retSensorInfo(oris).toString());
        }

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                tv_acc.setText(retSensorInfo(triple).toString());
                accs = triple;
                break;
            case Sensor.TYPE_GYROSCOPE:
                tv_gyo.setText(retSensorInfo(triple).toString());
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                tv_mag.setText(retSensorInfo(triple).toString());
                mags = triple;
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void tvset(final StringBuilder sb) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_wifi.setText(sb.toString());
            }
        });
    }

    Runnable wifiScanR = new Runnable() {
        @Override
        public void run() {
            //进行wifi扫描线程
            wifiManager.startScan();
            //收集扫描结果
            List<ScanResult> scanResults;
            scanResults = wifiManager.getScanResults();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < scanResults.size(); i++) {
                sb.append(scanResults.get(i).timestamp);
                sb.append("\t");
                sb.append(scanResults.get(i).BSSID);
                sb.append("\t");
                sb.append(scanResults.get(i).SSID);
                sb.append("\t");
                sb.append(scanResults.get(i).level);
                sb.append("\n");
            }

//            Log.e("test", sb.toString());
            tvset(sb);
            handler.postDelayed(this, 1000);
        }
    };

    private void getOris() {
        SensorManager.getRotationMatrix(rotations, null, accs, mags);
        SensorManager.getOrientation(rotations, oris);
        for (int i = 0; i < 3; i++) {
            oris[i] = (float) (oris[i]/(Math.PI)*180);
        }
    }

    private StringBuilder retSensorInfo(float[] triple) {
        StringBuilder stringBuilder = new StringBuilder();

        for (float aTriple : triple) {
            stringBuilder.append(aTriple);
            stringBuilder.append("\n");
        }

        return stringBuilder;
    }

}
