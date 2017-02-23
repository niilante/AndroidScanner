package androidscanner.ricsts.com.androidscanner;

import android.os.AsyncTask;
import android.speech.tts.Voice;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

/**
 * Created by Radchuk on 10.02.2017.
 */

public class ScanerNetworkClient {

    public static void sendMessage(String message) throws ExecutionException, InterruptedException {
        SendTask sendTask = new SendTask();
        sendTask.execute(message);
        //return  sendTask.get();
    }

    static class SendTask extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            String message = strings[0];
            try {

                Socket client = new Socket(ScanerNetworkSettings.getServerAddress(), ScanerNetworkSettings.getServerport());
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                out.println(message);
                client.close();
                out.close();


            }catch(IOException e) {
                e.printStackTrace();

            }

            return null;
        }
    }
}
