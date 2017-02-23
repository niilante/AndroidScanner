package androidscanner.ricsts.com.androidscanner;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.generalscan.NotifyStyle;
import com.generalscan.OnConnectedListener;
import com.generalscan.OnDisconnectListener;
import com.generalscan.SendConstant;
import com.generalscan.bluetooth.BluetoothConnect;
import com.generalscan.bluetooth.BluetoothSettings;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements SoundPool.OnLoadCompleteListener {

    private String tags[] = {"0291347033"};
    private List<String> tagsCollection = new ArrayList<>();

    private TextView tvScanInfo;
    private TextView textViewList;

    private Activity myActivity ;

    private ServerSettingsDialog serverSettingsDialog;


    private ReadBroadcast mReadBroadcast;

    private Button btnClear;
    private SoundPool soundPool;

    private int successSignalID;
    private int failureSignalId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myActivity = this;
        BluetoothConnect.CurrentNotifyStyle = NotifyStyle.NotificationStyle1;
        BluetoothConnect.BindService(myActivity);
        fillTagsCollection();
        initUI();
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
        setListeners();
        loadSounds();
        ScanerNetworkSettings.setContext(getApplicationContext() );
        ScanerNetworkSettings.loadSettings();
        serverSettingsDialog = new ServerSettingsDialog();
        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            }
        }
        else
        {
            Intent intent = new Intent(this, Service.class);
            startService(intent);
        }



    }

    private void playSuccessSound(){
        soundPool.play(successSignalID,1,1,1,0,1);
    }

    private void playFailureSound(){
        soundPool.play(failureSignalId,1,1,1,0,1);
    }

    private void loadSounds(){
        successSignalID = soundPool.load(this,R.raw.success,1);
        failureSignalId = soundPool.load(this,R.raw.failure,1);
    }


    private void fillTagsCollection(){
        for (int i = 0; i < tags.length ; i++) {
            tagsCollection.add(tags[i]);
        }
    }


    private void initUI(){

        tvScanInfo = (TextView)findViewById(R.id.tvScanInfo);
        tvScanInfo.setBackgroundResource(R.drawable.neutral_background);
        textViewList = (TextView)findViewById(R.id.textViewList);
        btnClear = (Button)findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvScanInfo.setText(R.string.waiting_data);
                tvScanInfo.setBackgroundResource(R.drawable.neutral_background);
                textViewList.setText("");
            }
        });
    }


    private void setListeners(){

        soundPool.setOnLoadCompleteListener(this);

        BluetoothConnect.SetOnConnectedListener(new OnConnectedListener() {

            @Override
            public void Connected() {
                Toast.makeText(myActivity, "Connected", Toast.LENGTH_SHORT).show();
            }

        });

        BluetoothConnect.SetOnDisconnectListener(new OnDisconnectListener() {

            @Override
            public void Disconnected() {
                Toast.makeText(myActivity, "Disconnected", Toast.LENGTH_SHORT).show();
            }

        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.chooseDevice: BluetoothSettings.SetScaner(myActivity);break;
            case R.id.connectDevice: BluetoothConnect.Connect(); break;
            case R.id.disconnectDevice: BluetoothConnect.Stop(myActivity); break;
            case R.id.serverSettings: serverSettingsDialog.show(getFragmentManager(),"serverSettingsDialog");break;
        }

        return super.onOptionsItemSelected(item);
    }


    private String fullScannedData = "";

    private void showScannedData(String data){
        fullScannedData = fullScannedData + data;
        if(fullScannedData.contains("\r\n")){
            String lastData;
            int position = fullScannedData.indexOf("\r"+"\n");
            lastData = fullScannedData.substring(0, position);
            tvScanInfo.setText(lastData);
            if(tagsCollection.contains(lastData)){
                playSuccessSound();
                tvScanInfo.setBackgroundResource(R.drawable.success_background);
            }
            else {
                playFailureSound();
                tvScanInfo.setBackgroundResource(R.drawable.failure_background);
            }
            textViewList.append(lastData + "\r\n");
            fullScannedData = fullScannedData.replace(lastData + "\r\n","" );
            if(ScanerNetworkSettings.isUseServer()){
                try {
                    lastData = "#" + ScanerNetworkSettings.getScanerId() + " " + lastData /*+ "\r\n"*/;
                    ScanerNetworkClient.sendMessage(lastData);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        Toast.makeText(this,"Resource id " + String.valueOf(i) + " loaded",Toast.LENGTH_SHORT).show();
    }

    public class ReadBroadcast extends BroadcastReceiver {

        public ReadBroadcast() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SendConstant.GetBatteryDataAction)) {

                String data = intent.getStringExtra(SendConstant.GetBatteryData);

                //((EditText) findViewById(R.id.editText1)).append(data);
                Toast.makeText(myActivity,data,Toast.LENGTH_SHORT).show();
            }

            if (intent.getAction().equals(SendConstant.GetDataAction)) {

                String data = intent.getStringExtra(SendConstant.GetData);
                showScannedData(data);
                //tvScanInfo.append(data);

            }

            if (intent.getAction().equals(SendConstant.GetReadDataAction)) {
                String name = intent.getStringExtra(SendConstant.GetReadName);
                String data = intent.getStringExtra(SendConstant.GetReadData);

                if (name.equals(myActivity.getString(R.string.gs_read_charge))) {
                    data = data.substring(7, 8);
                    if (data.equals("0")) {
                        data = myActivity
                                .getString(R.string.gs_usb_charge_fast);

                    } else {
                        data = myActivity
                                .getString(R.string.gs_usb_charge_normal);

                    }
                    Toast.makeText(myActivity,name + ":" + data, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(myActivity,name + ":" + data, Toast.LENGTH_SHORT).show();

                }
            }
        }

    }


    private void setBroadcast() {
        mReadBroadcast = new ReadBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SendConstant.GetDataAction);
        filter.addAction(SendConstant.GetReadDataAction);
        filter.addAction(SendConstant.GetBatteryDataAction);
        registerReceiver(mReadBroadcast, filter);

    }

    @Override
    protected void onStart() {

        setBroadcast();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mReadBroadcast != null) {

            this.unregisterReceiver(mReadBroadcast);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        BluetoothConnect.UnBindService(myActivity);
        super.onDestroy();
    }
}
