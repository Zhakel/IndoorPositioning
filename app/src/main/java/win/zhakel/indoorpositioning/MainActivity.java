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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private static final String TAG = "mainTag";
    TextView tv_acc;
    TextView tv_gyo;
    TextView tv_mag;
    TextView tv_ori;
    TextView tv_wifi;
    TextView tv_myori;
    EditText et_ipid;

    WifiManager wifiManager;
    SensorManager sm;

    Handler wifi_handler;
    Handler oris_handler;
    Handler pdr_handler;

    private ArrayStore wifiStore;
    private ArrayStore pdrStore;

    private float[] rotations, accs, mags, oris, gyos, myoris;
    private float dt = 0.0001f;

    private int count = 0;

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
        et_ipid = findViewById(R.id.et_ipid);

        //获取wifimanager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        wifi_handler = new Handler();
        oris_handler = new Handler();
        pdr_handler = new Handler();

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
        Sensor ori = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        //注册传感器
        sm.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this, gyo, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this, mag, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(this, ori, SensorManager.SENSOR_DELAY_GAME);

        rotations = new float[9];
        accs = new float[3];
        mags = new float[3];
        oris = new float[3];
        gyos = new float[3];
        myoris = new float[3];

        wifiStore = new ArrayStore(100, "wifi");
        pdrStore = new ArrayStore(10000, "pdr");

        Button btn_show = findViewById(R.id.btn_show);
        Button btn_store = findViewById(R.id.btn_store);
        btn_show.setOnClickListener(this);
        btn_store.setOnClickListener(this);

        new Thread(wifiScanR).start();
        new Thread(getOrisRunnable).start();
        new Thread(pdrRunnable).start();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sm.unregisterListener(this);

        wifi_handler.removeCallbacks(wifiScanR);
        pdr_handler.removeCallbacks(pdrRunnable);
        oris_handler.removeCallbacks(getOrisRunnable);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_show:
                wifiStore.delFile();
                pdrStore.delFile();

                break;
            case R.id.btn_store:
                wifiStore.storeElements();
                pdrStore.storeElements();
                break;
        }
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
//        if (Math.abs(accs[0]) > dt && Math.abs(mags[0]) > dt) {
//            getOris();
//            tv_ori.setText(retSensorInfo(oris).toString());
//        }

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                tv_acc.setText(retSensorInfo(triple).toString());
                accs = triple;
                break;
            case Sensor.TYPE_GYROSCOPE:
                tv_gyo.setText(retSensorInfo(triple).toString());
                gyos = triple;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                tv_mag.setText(retSensorInfo(triple).toString());
                mags = triple;
                break;
            case Sensor.TYPE_ORIENTATION:
                tv_myori.setText(retSensorInfo(triple).toString());
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void tvset(final TextView tv, final StringBuilder sb) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(sb.toString());
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
            for (ScanResult sr : scanResults) {
                sb.append(sr.timestamp).append("\t");
                sb.append(sr.BSSID).append("\t");
                sb.append(sr.SSID).append("\t");
                sb.append(sr.level).append("\n");
                if (sr.SSID.equals(et_ipid.getText().toString().trim()))
                    wifiStore.addElements(sr.level);
            }

//            Log.e("test", sb.toString());
            tvset(tv_wifi, sb);

            wifi_handler.postDelayed(this, 1000);

        }
    };

    Runnable getOrisRunnable = new Runnable() {
        @Override
        public void run() {
            if (Math.abs(accs[0]) > dt && Math.abs(mags[0]) > dt) {
                getOris(accs, mags);
                tvset(tv_ori, retSensorInfo(oris));

//                tv_ori.setText(retSensorInfo(oris).toString());

            }
            oris_handler.postDelayed(this, 50);
            //测试频率
//            count++;
//            if(count==100){
//                Log.i(TAG, "count=100");
//                count=0;
//            }
        }
    };

    Runnable pdrRunnable = new Runnable() {
        @Override
        public void run() {
            count++;
            if (Math.abs(accs[0]) > dt && Math.abs(mags[0]) > dt && Math.abs(gyos[0]) > dt) {
                pdrStore.addElements(count);
                for (float fa : accs)
                    pdrStore.addElements(fa);

                for (float fm : mags)
                    pdrStore.addElements(fm);

                for (float fg : gyos)
                    pdrStore.addElements(fg);
//                pdrStore.addElements(0.00f);

                for (float fo : oris)
                    pdrStore.addElements(fo);

//                tv_ori.setText(retSensorInfo(oris).toString());

            }
            if (count % 40 == 0) {
//                pdrStore.printArray();
//                Log.i(TAG, "run: hello array size is " + pdrStore.getArraySize());
                Log.i(TAG, "run");
            }
            pdr_handler.postDelayed(this, 50);

        }
    };

    private void getOris(float[] a, float[] m) {
        SensorManager.getRotationMatrix(rotations, null, a, m);
        SensorManager.getOrientation(rotations, oris);
        for (int i = 0; i < 3; i++) {
            oris[i] = (float) (oris[i] * 180 / Math.PI);
            if(oris[i]<0)
                oris[i] += 360;
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

    //TODO: 方向角计算函数设计
    private float getStepOri(float[] g, float[] a, float[] m) {
        float orientation = 0.0f;

        return (float) (orientation * 180 / Math.PI);
    }

    //TODO: 步长计算函数
    private float getStepLength(float p) {
//        float stepLength = 0.0f;

        return 0;
    }

    //TODO: 步子检测函数
    private float getStepPeriod(float[] a) {
//        float period = 0.0f;

        return 0;
    }

    // TODO: 滤波器设计

    // 加速度数据的平方和的开根
    private float getATotal(float[] a) {
        return (float) Math.sqrt(Math.pow(a[0], 2) + Math.pow(a[1], 2) + Math.pow(a[2], 2));
    }

    // 重力加速度获取
    private float getGravity() throws InterruptedException {
        List<float[]> lists = new ArrayList<>();
        int i = 0;
        while (i++ < 10) {
            lists.add(accs);
            sleep(100);
        }
        List<Float> listsTotal = new ArrayList<>();
        while (!lists.isEmpty()) {
            listsTotal.add(getATotal(lists.remove(0)));
//            lists.remove(0);
        }

        float gravity = 0.0f;
        while (!listsTotal.isEmpty()) {
            gravity += listsTotal.remove(0);
        }

        return gravity / 10;
    }


}
