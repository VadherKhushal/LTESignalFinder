package com.signalfinder;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by khushal.v on 20-09-2017.
 */

public class CustomPhoneStateListener extends PhoneStateListener {
    Context mContext;
    public static String LOG_TAG = "CustomPhoneStateListener";

    public CustomPhoneStateListener(Context context) {
        mContext = context;
    }

    /**
     * In this method Java Reflection API is being used please see link before
     * using.
     *
     * @see <a
     *      href="http://docs.oracle.com/javase/tutorial/reflect/">http://docs.oracle.com/javase/tutorial/reflect/</a>
     *
     */
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        Log.i(LOG_TAG, "onSignalStrengthsChanged: " + signalStrength);
        String str = "onSignalStrengthsChanged: " + signalStrength +"\n\n";
        if (signalStrength.isGsm()) {
            Log.i(LOG_TAG, "onSignalStrengthsChanged: getGsmBitErrorRate "
                    + signalStrength.getGsmBitErrorRate());
            str += "onSignalStrengthsChanged: getGsmBitErrorRate "
                    + signalStrength.getGsmBitErrorRate()+"\n";
            Log.i(LOG_TAG, "onSignalStrengthsChanged: getGsmSignalStrength "
                    + signalStrength.getGsmSignalStrength());
            str += "onSignalStrengthsChanged: getGsmSignalStrength "
                    + signalStrength.getGsmSignalStrength()+"\n";

        } else if (signalStrength.getCdmaDbm() > 0) {
            Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaDbm "
                    + signalStrength.getCdmaDbm());
            str += "onSignalStrengthsChanged: getCdmaDbm "
                    + signalStrength.getCdmaDbm()+"\n";
            Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaEcio "
                    + signalStrength.getCdmaEcio());
            str += "onSignalStrengthsChanged: getCdmaEcio "
                    + signalStrength.getCdmaEcio()+"\n";
        } else {
            Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoDbm "
                    + signalStrength.getEvdoDbm());
            str += "onSignalStrengthsChanged: getEvdoDbm "
                    + signalStrength.getEvdoDbm()+"\n";
            Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoEcio "
                    + signalStrength.getEvdoEcio());
            str += "onSignalStrengthsChanged: getEvdoEcio "
                    + signalStrength.getEvdoEcio()+"\n";
            Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoSnr "
                    + signalStrength.getEvdoSnr());
            str += "onSignalStrengthsChanged: getEvdoSnr "
                    + signalStrength.getEvdoSnr()+"\n";
        }

        PhoneState.getObserver().setState(str);

        // Reflection code starts from here
        try {
            Method[] methods = SignalStrength.class
                    .getMethods();
            String strength = "";
            int signalDbm = -1;
            for (Method mthd : methods) {
                Log.d(LOG_TAG, "onSignalStrengths: "+ mthd.getName());
            }
            for (Method mthd : methods) {
                if (mthd.getName().equals("getLteSignalStrength")
                        || mthd.getName().equals("getLteRsrp")
                        || mthd.getName().equals("getLteAsuLevel")
                        || mthd.getName().equals("getLteRsrq")
                        || mthd.getName().equals("getLteRssnr")
                        || mthd.getName().equals("getLteDbm")
                        || mthd.getName().contains("Rssi")
                        || mthd.getName().equals("getLteLevel")
                        || mthd.getName().equals("getLteCqi")) {
                    Log.i(LOG_TAG,
                            "onSignalStrengthsChanged: " + mthd.getName() + " " + mthd.invoke(signalStrength));
                    strength += mthd.getName() + " "+ mthd.invoke(signalStrength)+"\n";
                    if(mthd.getName().equals("getLteRsrp")){
                        signalDbm = Integer.parseInt(mthd.invoke(signalStrength).toString());
                    }
                }
            }
            PhoneState.getObserver().setSignalDbm(signalDbm);
            PhoneState.getObserver().setStrength(strength);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        // Reflection code ends here
    }

    @Override
    public void onDataActivity(int direction) {
        super.onDataActivity(direction);
        PhoneState.getObserver().setDataDirection(direction);
    }

    @Override
    public void onDataConnectionStateChanged(int state, int networkType) {
        super.onDataConnectionStateChanged(state, networkType);
        PhoneState.getObserver().setDataConnectionState(state, networkType);
    }
}
