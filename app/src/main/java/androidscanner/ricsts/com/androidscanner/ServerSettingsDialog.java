package androidscanner.ricsts.com.androidscanner;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Radchuk on 10.02.2017.
 */

public class ServerSettingsDialog extends DialogFragment implements View.OnClickListener {
    private Button btnDssApply;
    private Button btmDssCancel;
    private EditText edtDssIP;
    private EditText edtDssPort;
    private EditText edtDssScanerId;
    private CheckBox cbDssUseServer;
    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.dialog_server_settings, null);
        getDialog().setTitle("ServerSettings");
        btmDssCancel = (Button)mView.findViewById(R.id.btmDssCancel);
        btnDssApply = (Button)mView.findViewById(R.id.btnDssApply);
        edtDssIP = (EditText)mView.findViewById(R.id.edtDssIP);
        edtDssIP.setText(ScanerNetworkSettings.getServerAddress());
        edtDssPort = (EditText)mView.findViewById(R.id.edtDssPort);
        edtDssPort.setText(String.valueOf(ScanerNetworkSettings.getServerport()));
        edtDssScanerId = (EditText)mView.findViewById(R.id.edtDssScanerId);
        edtDssScanerId.setText(ScanerNetworkSettings.getScanerId());
        cbDssUseServer = (CheckBox)mView.findViewById(R.id.cbDssUseServer);
        cbDssUseServer.setChecked(ScanerNetworkSettings.isUseServer());
        btnDssApply.setOnClickListener(this);
        btmDssCancel.setOnClickListener(this);
        return mView;
    }

    private void saveServerSettings(){
        String serverIp = "";
        int port = -1;
        String scanerID = "0000";
        Pattern IP_ADDRESS = Pattern.compile(
                "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                        + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                        + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                        + "|[1-9][0-9]|[0-9]))");
        Matcher matcher = IP_ADDRESS.matcher(edtDssIP.getText());
        if (matcher.matches()) {
            serverIp = edtDssIP.getText().toString();
            try {
                port = Integer.valueOf(edtDssPort.getText().toString());
                if((port < 1) || (port > 65535 )){
                    edtDssPort.setText("");
                    edtDssIP.setHint("Port  must be in [1,65535] range");
                    return;
                }
                try {
                    int iID = Integer.valueOf(edtDssScanerId.getText().toString());
                    if(edtDssScanerId.getText().toString().length() > 4){
                        edtDssScanerId.setText("");
                        edtDssScanerId.setHint("Id must be in [0-9999] range");
                        return;
                    }
                    //scanerID = edtDssScanerId.getText().toString();
                    scanerID = String.format("%04d", iID );
                    //ScanerNetworkSettings.set
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    edtDssScanerId.setText("");
                    edtDssScanerId.setHint("Id must be in [0-9999] range");
                }

                ScanerNetworkSettings.setServerAddress(serverIp);
                ScanerNetworkSettings.setServerport(port);
                ScanerNetworkSettings.setUseServer(cbDssUseServer.isChecked());
                ScanerNetworkSettings.setScanerId(scanerID);
                ScanerNetworkSettings.saveSettings();
                dismiss();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                edtDssPort.setText("");
                edtDssIP.setHint("Port number must contain Integer value");
            }
        }
        else{
            edtDssIP.setText("");
            edtDssIP.setHint("enter valid IP address value ");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnDssApply: saveServerSettings();break;
            case R.id.btmDssCancel : dismiss(); break;

        }
    }
}
