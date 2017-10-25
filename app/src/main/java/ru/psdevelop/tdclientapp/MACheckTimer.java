package ru.psdevelop.tdclientapp;

import android.os.Bundle;
import android.os.Message;

/**
 * Created by ADMIN on 05.01.2015.
 */
public class MACheckTimer extends Thread {
    private MainActivity ownerSrv;
    private long counter=0;

    public MACheckTimer(MainActivity own)   {
        this.ownerSrv = own;
        counter=0;
        this.start();
    }

    public void checkWaitingSMS()   {
        Message msg = new Message();
        msg.obj = this.ownerSrv;
        msg.arg1 = ParamsAndConstants.MA_CHECK_STATUSES;
        this.ownerSrv.handle.sendMessage(msg);
    }

    public void run() {
        while (true) {
            if(counter>2000000)
                counter=0;
            counter++;
            try {
                sleep(1000);
                checkWaitingSMS();
            } catch (Exception e) {
                //showMyMsg(
                //        "\nОшибка таймера!" + e.getMessage());
            }

        }

    }

}
