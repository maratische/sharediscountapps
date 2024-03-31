package maratische.android.sharediscountapps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

public class PhoneStateBroadcastReceiver extends BroadcastReceiver {

    int prev_state;
    String incoming_number;

    protected void checkNumber(String incomingNumber, Context context) {
        if (incomingNumber != null) {
            if (!incomingNumber.equals(incoming_number)) {
                incoming_number = incomingNumber;
                if ( incomingNumber.length() > 0) {
                    //send phoneNumber to telegram
                    context.startService(new Intent(context.getApplicationContext(), TelegramService.class).putExtra("phone", incomingNumber));
                }
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String phone = "";
        if (intent != null && intent.getExtras() != null) {
            phone = intent.getExtras().getString("incoming_number", "");
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(
                    context.getMainExecutor(),
                    new TelephonyCallback2(phone, context));
        } else {
            telephonyManager.listen(new CustomPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    class TelephonyCallback2 extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        String phone;
        Context context;
        TelephonyCallback2(String phone, Context context) {
            this.phone = phone;
            this.context = context;
        }
        @Override
        public void onCallStateChanged(int state) {
            System.out.println("state " + state);
            checkNumber(phone, context);
        }
    }

    class CustomPhoneStateListener extends PhoneStateListener {

        //private static final String TAG = "PhoneStateChanged";
        Context context; //Context to make Toast if required

        public CustomPhoneStateListener(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    //when Idle i.e no call
                    if ((prev_state == TelephonyManager.CALL_STATE_OFFHOOK)) {
                        prev_state = state;
                        //Answered Call which is ended
                        checkNumber("", context);
//					Toast.makeText(context, "Answered Call which is ended ", Toast.LENGTH_LONG).show();
                    }
                    if ((prev_state == TelephonyManager.CALL_STATE_RINGING)) {
                        prev_state = state;
                        //Rejected or Missed call
                        checkNumber("", context);
//					Toast.makeText(context, "Rejected or Missed call ", Toast.LENGTH_LONG).show();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    prev_state = state;
                    //when Off hook i.e in call
                    //Make intent and start your service here
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    prev_state = state;
                    checkNumber(PhoneUtils.validPhoneNumber(incomingNumber), context);
                    break;
                default:
                    break;
            }
        }

    }

}
