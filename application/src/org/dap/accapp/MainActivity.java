package org.dap.accapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import org.dap.accapp.R;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    public static final String SDCARD_ACCFIX_KO = "/sdcard/accfix.ko";
    public static final String DEV_ACCFIX = "/dev/accfix";
    private static final int IDM_EXIT = 101;
    TextView textKey;
    TextView textLog;


//    class IncomingHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MessengerService.MSG_SET_VALUE:
//                    mCallbackText.setText("Received from service: " + msg.arg1);
//                    break;
//                default:
//                    super.handleMessage(msg);
//            }
//        }
//    }
//    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textKey = (TextView) findViewById(R.id.textKey);
        textKey.setText("Press any key to see its keyCode and scanCode\n\n");

        textLog = (TextView) findViewById(R.id.textLog);
        textLog.setText("\n");
        textLog.setMovementMethod(new ScrollingMovementMethod());


        // Create a new PhoneStateListener
        PhoneStateListener listener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                String stateString = "N/A";
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        stateString = "Idle";
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        stateString = "Off Hook";
                        //do not notify
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        stateString = "Ringing";
                        break;
                }
                textLog.append(String.format("\nonCallStateChanged: %s", stateString));
            }
        };

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        Intent svc = new Intent(PhoneListenerService.class.getName());
        startService(svc);
        //bindService(svc, null, Context.BIND_AUTO_CREATE);
    }


    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        textKey.setText("keyCode: " + keyCode + "\n" + event.toString());
        return super.onKeyDown(keyCode, event);
    }

    public void btCheck_Click(View v) {
        textLog.setText("\n");
        textLog.scrollTo(0,0);
        File f = new File("/dev/accfix");
        if (f.exists()) {
            textLog.append("/dev/accfix driver is present\n");
            if (f.canWrite()) {
                textLog.append("Permissions are correct\n");
            } else {
                textLog.append("666 permissions are not set!\n");
            }
        } else {
            textLog.append("Can't install, no /dev/accfix driver found!\n");
            f = new File(SDCARD_ACCFIX_KO);
            if (f.exists()) {
                textLog.append(SDCARD_ACCFIX_KO + " can be installed\n");
            } else {
                textLog.append("No " + SDCARD_ACCFIX_KO + " found!\n");
            }
        }
    }

    public void btRoot_Click(View v) {
        try {
            ProcessBuilder pb = new ProcessBuilder("su");
            pb.directory(new File("/sdcard"));
            textKey.setText("\n");
            textLog.setText("");
            textLog.scrollTo(0,0);
            textLog.append("dmesg | grep accfix\n");
            Process p = pb.start();
            DataOutputStream pOut = new DataOutputStream(p.getOutputStream());

            pOut.writeBytes("dmesg | grep accfix\n");
            pOut.writeBytes("exit\n");
            pOut.flush();

            p.waitFor();
            printStream(p.getErrorStream());
            printStream(p.getInputStream());
        } catch (IOException e) {
            textLog.append("Fail: " + e.toString() + "\n");
        } catch (InterruptedException e) {
            textLog.append("Fail: " + e.toString() + "\n");
        }
    }

    private void printStream(InputStream is) throws IOException {
        BufferedReader rr = new BufferedReader(new InputStreamReader(is));
        String s = null;
        while ((s = rr.readLine()) != null) {
            textLog.append(s + "\n");
        }
        rr.close();
    }

    private void printResult(Process p) throws IOException, InterruptedException {
        printStream(p.getInputStream());
        printStream(p.getErrorStream());
        p.waitFor();
        textLog.append("ExitCode: " + p.exitValue() + "\n");
    }

    public void btInstall_Click(View v) {
        textLog.setText("");
        textLog.scrollTo(0,0);

        if (!new File(SDCARD_ACCFIX_KO).exists()) {
            textLog.append("No " + SDCARD_ACCFIX_KO + " found!\n");
        } else {
            try {
                ProcessBuilder pb = new ProcessBuilder("su");
                pb.directory(new File("/sdcard"));
                textLog.setText("\n");
                textLog.append("insmod " + SDCARD_ACCFIX_KO + " ...\n");
                Process p = pb.start();

                DataOutputStream pOut = new DataOutputStream(p.getOutputStream());

                //String rv = "";
                // su must exit before its output can be read
                pOut.writeBytes("insmod /sdcard/accfix.ko" + "\n");
                pOut.writeBytes("sleep 1\n");
                pOut.writeBytes("echo \"chmod 666 /dev/accfix\"\n");
                pOut.writeBytes("chmod 666 /dev/accfix\n");
                pOut.writeBytes("echo \"Success, exiting...\"\n");
                pOut.writeBytes("exit\n");
                pOut.flush();

                p.waitFor();

                printStream(p.getErrorStream());
                printStream(p.getInputStream());

            } catch (IOException e) {
                textLog.append("Fail: " + e.toString() + "\n");
            } catch (InterruptedException e) {
                textLog.append("Fail: " + e.toString() + "\n");
            }
        }

    }

    public boolean onCreateOptionsMenu(Menu menu){
    	menu.add(Menu.NONE, IDM_EXIT, Menu.NONE, "Exit").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)  {
        if (item.getItemId() == IDM_EXIT){
            this.finish();
        }
        return true;
    }
}
