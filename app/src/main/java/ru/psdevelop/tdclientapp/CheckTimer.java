package ru.psdevelop.tdclientapp;

import android.os.Message;

/**
 * Created by ADMIN on 18.06.2015.
 */
public class CheckTimer extends Thread {
    private TDClientService ownerSrv;
    private long counter=0;

    public CheckTimer(TDClientService own)   {
        this.ownerSrv = own;
        counter=0;
        this.start();
    }

    public void checkStatus()   {
        Message msg = new Message();
        msg.obj = this.ownerSrv;
        msg.arg1 = ParamsAndConstants.CHECK_STATUSES;
        this.ownerSrv.handle.sendMessage(msg);
    }

    public void checkConnect()   {
        Message msg = new Message();
        msg.obj = this.ownerSrv;
        msg.arg1 = ParamsAndConstants.CHECK_CONNECT;
        this.ownerSrv.handle.sendMessage(msg);
    }

    public void disconnectSocketIO()   {
        Message msg = new Message();
        msg.obj = this.ownerSrv;
        msg.arg1 = ParamsAndConstants.DISCONNECT_SOCKIO;
        this.ownerSrv.handle.sendMessage(msg);
    }

    public void run() {
        while (true) {
            if(counter>2000000)
                counter=0;
            counter++;
            try {
                sleep(1000);
                if(counter%30==0)
                    checkStatus();
                if(counter%3==0)
                    checkConnect();
                if(this.ownerSrv.inactiveTimeout<350)
                    this.ownerSrv.inactiveTimeout++;
                else {
                    if(!this.ownerSrv.inactiveTimeoutBlock) {
                        this.ownerSrv.inactiveTimeoutBlock = true;
                        disconnectSocketIO();
                    }
                }
            } catch (Exception e) {
                //showMyMsg(
                //        "\nОшибка таймера!" + e.getMessage());
            }

        }

    }


}
