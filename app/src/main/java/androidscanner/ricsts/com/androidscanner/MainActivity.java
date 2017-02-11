package androidscanner.ricsts.com.androidscanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import android.widget.TextView;
import android.widget.Toast;

import com.generalscan.NotifyStyle;
import com.generalscan.OnConnectedListener;
import com.generalscan.OnDisconnectListener;
import com.generalscan.SendConstant;
import com.generalscan.bluetooth.BluetoothConnect;
import com.generalscan.bluetooth.BluetoothSettings;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private TextView tvScanInfo;

    private Activity myActivity ;

    private ServerSettingsDialog serverSettingsDialog;


    private ReadBroadcast mReadBroadcast;

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
        initUI();
        setListeners();
        ScanerNetworkSettings.setContext(this);
        ScanerNetworkSettings.loadSettings();
        serverSettingsDialog = new ServerSettingsDialog();

    }

    private void initUI(){

        tvScanInfo = (TextView)findViewById(R.id.tvScanInfo);
    }


    private void setListeners(){

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
            fullScannedData = fullScannedData.replace(lastData + "\r\n","" );
            if(ScanerNetworkSettings.isUseServer()){
                try {
                    lastData = "#" + ScanerNetworkSettings.getScanerId() + " " + lastData + "\n";
                    ScanerNetworkClient.sendMessage(lastData);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

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
