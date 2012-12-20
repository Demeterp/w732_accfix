/*
 * Copyright (c) 2006 jNetX.
 * http://www.jnetx.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * jNetX. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with jNetX.
 *
 * $Id: PhoneListenerService.java,v 1.1 20.12.12 11:54 dpetukhov Exp $
 */

package org.dap.accapp;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Service that listens for Phone State events
 */
public class PhoneListenerService extends IntentService {

    private NotificationManager mNM;

       // Unique Identification Number for the Notification.
       // We use it on Notification start, and to cancel it.
       private int NOTIFICATION = R.string.local_service_started;

    public PhoneListenerService() {
        super("accfix-phone-listener");
    }

    public PhoneListenerService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //showNotification();
    }

    /**
         * Show a notification while this service is running.
        private void showNotification() {
            // In this sample, we'll use the same text for the ticker and the expanded notification
            CharSequence text = getText(R.string.local_service_started);

            // Set the icon, scrolling text and timestamp
            Notification notification = new Notification(R.string.local_service_started, text,
                    System.currentTimeMillis());

            // Set the info for the views that show in the notification panel.
            notification.setLatestEventInfo(this, "service started",
                           text, null);

            // Send the notification.
            mNM.notify(NOTIFICATION, notification);
        }
     */

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //do nothing
    }

    private void writeState(String state){
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(MainActivity.DEV_ACCFIX));
            fw.write(state);
        } catch (IOException e) {
            //ignore
        } finally {
            if (fw!=null){
                try {
                    fw.close();
                } catch (IOException e) {
                }
            }
        }
    }

    final static int myID = 1234;

    @Override
    public int onStartCommand(Intent ignoreIntent, int flags, int startId) {
        Toast.makeText(this, "AccFixService Started", Toast.LENGTH_LONG).show();


        //The intent to launch when the user clicks the expanded notification
        Intent intent = new Intent(this, Service.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //This constructor is deprecated. Use Notification.Builder instead
        Notification notice = new Notification(android.R.drawable.sym_def_app_icon, "AccFix Service Started", System.currentTimeMillis());

        //This method is deprecated. Use Notification.Builder instead.
        notice.setLatestEventInfo(this, "I'm listening to phone calls", "", pendIntent);

        notice.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(myID, notice);

        // Create a new PhoneStateListener
        PhoneStateListener listener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        writeState("rrr"); //1 char = idle
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        writeState("rr"); //2 chars = ringing
                        break;
                }
            }
        };

        // Register the listener wit the telephony manager
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "PhoneListenerService Stopped", Toast.LENGTH_LONG).show();
        mNM.cancel(NOTIFICATION);
        super.onDestroy();
    }
}
