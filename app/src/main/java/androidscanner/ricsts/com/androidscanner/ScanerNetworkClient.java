package androidscanner.ricsts.com.androidscanner;

import android.os.AsyncTask;
import android.speech.tts.Voice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

/**
 * Created by Radchuk on 10.02.2017.
 */

public class ScanerNetworkClient {

    public static boolean sendMessage(String message) throws ExecutionException, InterruptedException {
        SendTask sendTask = new SendTask();
        sendTask.execute(message);
        return  sendTask.get();
    }

    static class SendTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... strings) {
            String message = strings[0];
            boolean result = false;
           // String serverName = args[0];
            //int port = Integer.parseInt(args[1]);
            try {
                //System.out.println("Connecting to " + serverName + " on port " + port);
                Socket client = new Socket(ScanerNetworkSettings.getServerAddress(), ScanerNetworkSettings.getServerport());

                //System.out.println("Just connected to " + client.getRemoteSocketAddress());
                OutputStream outToServer = client.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);

                out.writeUTF(message + client.getLocalSocketAddress());
                //InputStream inFromServer = client.getInputStream();
                //DataInputStream in = new DataInputStream(inFromServer);

                //System.out.println("Server says " + in.readUTF());
                client.close();
                result = true;

            }catch(IOException e) {
                e.printStackTrace();

            }
            return result;
        }
    }
}
