package com.signalfinder;

import android.telephony.TelephonyManager;

import java.util.Observable;

/**
 * Created by khushal.v on 20-09-2017.
 */

public class PhoneState extends Observable {
    public String state;
    public String strength;
    public int signalDbm;
    public int dataDirection;
    public String connectionState;
    public String networkType;

    private static PhoneState phoneState = null;
    private PhoneState(){}
    private void updateObserver(){
        setChanged();
        notifyObservers();
    }

    public static PhoneState getObserver(){
        if(phoneState == null){
            phoneState = new PhoneState();
        }
        return phoneState;
    }

    public void setState(String state){
        this.state = state;
        updateObserver();
    }
    public void setStrength(String state){
        this.strength = state;
        updateObserver();
    }

    public void setSignalDbm(int signalDbm){
        this.signalDbm = signalDbm;
    }

    public void setDataDirection(int dir){
        this.dataDirection = dir;
        updateObserver();
    }

    public String getDataDirection(){
        switch (dataDirection){
            case TelephonyManager.DATA_ACTIVITY_IN:
                return "IN";
            case TelephonyManager.DATA_ACTIVITY_OUT:
                return "OUT";
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                return "IN + OUT";
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                return "--";
            case TelephonyManager.DATA_ACTIVITY_NONE:
                return "NONE";
            default:
                return "--";
        }
    }

    public void setDataConnectionState(int state, int networkType) {
        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                this.connectionState = "Disconnected";
                break;
            case TelephonyManager.DATA_CONNECTING:
                this.connectionState = "Connecting";
                break;
            case TelephonyManager.DATA_CONNECTED:
                this.connectionState = "Connected";
                break;
            case TelephonyManager.DATA_SUSPENDED:
                this.connectionState = "Suspended";
                break;
            default:
                this.connectionState = "--";
                break;
        }

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_CDMA:
                this.networkType = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                this.networkType = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                this.networkType = "EVDO";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                this.networkType = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                this.networkType = "HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                this.networkType ="HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                this.networkType = "IDEN";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                this.networkType = "LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                this.networkType = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                this.networkType = "UNKNOWN";
                break;
            default:
                this.networkType = String.valueOf(networkType);
                break;
        }
        updateObserver();
    }
}
