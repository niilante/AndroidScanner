package androidscanner.ricsts.com.androidscanner;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.camera2.params.StreamConfigurationMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Radchuk on 10.02.2017.
 */

public class ScanerNetworkSettings {
    private static final String SERVER_ADDRES_PARAMS_NAME = "SERVER_ADDRES_PARAMS_NAME";
    private static final String SERVER_PORT_PARAMS_NAME = "SERVER_PORT_PARAMS_NAME";
    private static final String IS_USE_SERVER = "IS_USE_SERVER";
    private static final String SCANER_ID_PARAMS_NAME = "SCANER_ID_PARAMS_NAME";

    private static String serverAddress = "";
    private static int serverport = -1;
    private static boolean useServer = false;
    private static String scanerId = "0000";



    private static Activity activity;

    public static String getScanerId() {
        return scanerId;
    }

    public static void setScanerId(String scanerId) {
        ScanerNetworkSettings.scanerId = scanerId;
    }

    public static boolean isUseServer() {
        return useServer;
    }

    public static void setUseServer(boolean useServer) {
        ScanerNetworkSettings.useServer = useServer;
    }

    public static int getServerport() {
        return serverport;
    }

    public static void setContext(Activity activity) {
        ScanerNetworkSettings.activity = activity;
    }

    public static void setServerport(int serverport) {
        ScanerNetworkSettings.serverport = serverport;
    }

    public static String getServerAddress() {

        return serverAddress;
    }

    public static void setServerAddress(String serverAddress) {
        ScanerNetworkSettings.serverAddress = serverAddress;
    }

    public static void saveSettings(){
        SharedPreferences sPref = activity.getPreferences(MODE_PRIVATE);
        sPref.edit().putString(SERVER_ADDRES_PARAMS_NAME,serverAddress);
        sPref.edit().putInt(SERVER_PORT_PARAMS_NAME,serverport);
        sPref.edit().putBoolean(IS_USE_SERVER, useServer);
        sPref.edit().putString(SCANER_ID_PARAMS_NAME, scanerId);
        sPref.edit().commit();

    }

    public static void loadSettings(){
        SharedPreferences sPref = activity.getPreferences(MODE_PRIVATE);
        serverAddress = sPref.getString(SERVER_ADDRES_PARAMS_NAME,"");
        serverport = sPref.getInt(SERVER_PORT_PARAMS_NAME,-1);
        useServer = sPref.getBoolean(IS_USE_SERVER, false);
        scanerId = sPref.getString(SCANER_ID_PARAMS_NAME,"0000");
    }
}
