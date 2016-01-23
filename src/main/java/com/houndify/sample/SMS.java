package com.houndify.sample;
import android.telephony.SmsManager;

/**
 * Created by PhucNgo on 1/22/16.
 */
public class SMS {
    public SMS() {

    }

    public void SendSMS(String phoneNo) {
        try {
            String sms = "Ayyy";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, sms, null, null);
//            Toast.makeText(getApplicationContext(), "SMS Sent!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

}
