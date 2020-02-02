package ru.psdevelop.tdclientapp;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.WindowManager;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import io.socket.client.IO;
import android.app.AlertDialog;
import android.widget.EditText;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

public class TDClientService extends Service implements LocationListener {

    private Socket mSocket;
    SocketIO socketIO;
    boolean auth=false;
    boolean gpsAviable=false;
    boolean connectAttempt=false;
    static boolean inactiveTimeoutBlock=false;
    static int inactiveTimeout=0;
    boolean singleGPSDetect=false;
    String tarifAndOptionsData = null;

    public static final String INFO_ACTION = "com.psdevelop.tdclientapp.INFO_ACTION";

    public static Handler handle;
    CheckTimer checkTimer=null;
    private LocationManager myManager;

    public static boolean ENABLE_SMS_NOTIFICATIONS=false;
    public static final int TDC_PERMISSIONS_REQUEST_READ_CONTACTS = 200;
    public static final int PERMISSION_APP_CODE = 201;

    static SharedPreferences prefs=null;
    //PowerManager.WakeLock wakeLock;
    int clientId=-1;
    String phone="";
    int satellites=0, satellitesInFix=0;

    public TDClientService() {
    }

    public void sendInfoBroadcast(int action_id, String message) {
        Intent intent = new Intent(INFO_ACTION);
        intent.putExtra(ParamsAndConstants.TYPE, action_id);
        intent.putExtra(ParamsAndConstants.MSG_TEXT, message);
        sendBroadcast(intent);
    }

    public static boolean checkString(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static int strToIntDef(String str_int, int def) {
        int res = def;

        if (checkString(str_int)) {
            res = Integer.parseInt(str_int);
        }

        return res;
    }

    public boolean checkStringDouble(String str) {
        try {
            Double.parseDouble(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public double strToDoubleDef(String str_double, double def) {
        double res = def;

        if (checkStringDouble(str_double)) {
            res = Double.parseDouble(str_double);
        }

        return res;
    }

    public void reloadPrefs()    {
        try {
        if(prefs!=null) {
            ENABLE_SMS_NOTIFICATIONS = prefs.getBoolean("ENABLE_SMS_NOTIFICATIONS", false);
        }
        } catch (Exception e) {
            //Toast.makeText(getBaseContext(),
            //        "Ошибка извлечения настроек! Текст сообщения: "
            //                +e.getMessage()+".", Toast.LENGTH_LONG).show();
        }
    }

    public void sendCCoords(String clat, String clon)   {
        try {
            if (auth) {
                //showToast("...");

                //clId = clientId;//strToIntDef(prefs.getString("example_list", "-1"), -1);
                phone = prefs.getString("example_text", "-1");
                JSONObject resultJson = new JSONObject();
                resultJson.put("id", clientId);
                resultJson.put("phone", phone);
                resultJson.put("clat", clat);
                resultJson.put("clon", clon);
                mSocket.emit("ccoords", resultJson.toString());
            }
        } catch (Exception ex) {
            showToast("Ошибка отправки запроса: " + ex.getMessage());
        }
    }

    @Override
    public void onCreate() {

        prefs = PreferenceManager.
                getDefaultSharedPreferences(this);
        reloadPrefs();

        try	{
            myManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
            /*GpsStatus.Listener lGPS = new GpsStatus.Listener() {
                public void onGpsStatusChanged(int event) {
                    try	{
                        if( event == GpsStatus.GPS_EVENT_SATELLITE_STATUS){
                            //GpsStatus status = lm.getGpsStatus(null);
                            //Iterable<GpsSatellite> sats = status.getSatellites();
                            //doSomething();
                            satellites = 0;
                            satellitesInFix = 0;
                            //int timetofix = locationManager.getGpsStatus(null).getTimeToFirstFix();
                            //Log.i(TAG, "Time to first fix = " + timetofix);
                            for (GpsSatellite sat : myManager.getGpsStatus(null).getSatellites()) {
                                if(sat.usedInFix()) {
                                    satellitesInFix++;
                                }
                                satellites++;
                            }
                            //Log.i(TAG, satellites + " Used In Last Fix ("+satellitesInFix+")");
                        }
                    } catch(Exception gpse)	{
                        showMyMsg("Ошибка определения статуса GPS! "+gpse.getMessage());
                    }
                }
            };
            myManager.addGpsStatusListener(lGPS);*/
            //showMyMsg("Инициализирован объект T!");
        } catch(Exception le)	{
            showToast("Ошибка инициализации GPS!");
        }

        handle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == ParamsAndConstants.SHOW_MESSAGE_TOAST) {
                    showToast(msg.getData().
                            getString("msg_text"));
                }   else if (msg.arg1 == ParamsAndConstants.DISCONNECT_SOCKIO) {
                    try {
                        auth=false;
                        inactiveTimeoutBlock=true;
                        mSocket.disconnect();
                        mSocket = null;
                        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_SET_STATUS_TEXTVIEW,
                                "Отключен от сервера, нажмите на любую кнопку для поключения!");
                        //showToast("Отключаюсь от сервера!");
                    } catch(Exception e)    {
                        showToast("Ошибка отключения из-за бездействия! "+e.getMessage());
                    }
                }
                else if (msg.arg1 == ParamsAndConstants.RECEIVE_AUTH) {
                    if(msg.getData().getInt("client_id")>0) {
                        auth=true;
                        clientId=msg.getData().getInt("client_id");
                        //showToast("Подключен");
                        JSONObject resultJson = new JSONObject();
                        try {
                            resultJson.put("cid",clientId);
                            resultJson.put("clphone",phone);
                            mSocket.emit("status", resultJson.toString());
                        } catch (Exception e) { //
                            //throw new RuntimeException(e);
                            //mSocket = null;
                            //showToast("Соединение неудачно! "+e.getMessage());
                        }
                    }
                    else
                        showToast("Соединение отклонено!");
                }
                else if (msg.arg1 == ParamsAndConstants.CHECK_STATUSES) {
                    if(auth)    {
                        JSONObject resultJson = new JSONObject();
                        try {
                            resultJson.put("cid",clientId);
                            resultJson.put("clphone",phone);
                            mSocket.emit("status", resultJson.toString());
                        } catch (Exception e) { //
                            //throw new RuntimeException(e);
                            //mSocket = null;
                            //showToast("Соединение неудачно! "+e.getMessage());
                        }
                    }
                } else if (msg.arg1 == ParamsAndConstants.RECEIVER_CLSTAT) {
                    //showToast(msg.getData().
                    //        getString("data"));
                    parseStatus(msg.getData().
                            getString("data"));
                } else if (msg.arg1 == ParamsAndConstants.RECEIVE_SECTOR_DETECT) {
                    //showToast(msg.getData().
                    //        getString("data"));
                    sendInfoBroadcast( ParamsAndConstants.ID_ACTION_SHOW_SECTOR_DETECT_INFO, msg.getData().
                            getString("data"));
                } else if (msg.arg1 == ParamsAndConstants.RECEIVE_TARIFS_OPTIONS) {
                    //showToast(msg.getData().
                    //        getString("data"));
                    tarifAndOptionsData = msg.getData().
                            getString("data");
                    sendInfoBroadcast( ParamsAndConstants.ID_ACTION_SHOW_TARIF_OPTIONS, msg.getData().
                            getString("data"));
                } else if (msg.arg1 == ParamsAndConstants.REQUEST_CLSTAT) {

                }
                else if (msg.arg1 == ParamsAndConstants.CHECK_CONNECT) {
                    connectCheck();
                } else if (msg.arg1 == ParamsAndConstants.REQ_DECLINE) {
                    showToast("Запрос не чаще раза в минуту!");
                } else if (msg.arg1 == ParamsAndConstants.SERVER_OVERLOAD) {
                    showToast("СЕРВИС ПЕРЕГРУЖЕН, ПОДОЖДИТЕ И НАЖМИТЕ НА ЛЮБУЮ КНОПКУ ДЛЯ ПОДКЛЮЧЕНИЯ!");
                }
            }
        };

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra(ParamsAndConstants.TYPE, -1);
                switch (type) {
                    case ParamsAndConstants.ID_ACTION_WAKE_UP_NEO: //))))
                        //if(inactiveTimeoutBlock)
                        //    showToast("Восстановление соединения!");
                        inactiveTimeoutBlock=false;
                        inactiveTimeout=0;
                        break;
                    case ParamsAndConstants.ID_ACTION_SEND_CCOORDS:
                        DecimalFormat df = new DecimalFormat("#.######");
                        sendCCoords(df.format(intent.getDoubleExtra("clat", 0)).replace(",", "."),
                                df.format(intent.getDoubleExtra("clon", 0)).replace(",", "."));
                        break;
                    case ParamsAndConstants.ID_ACTION_GO_ORDERING:
                        try {
                            if (auth) {
                                showToast("Отсылаю заказ...");
                                //clId = clientId;//strToIntDef(prefs.getString("example_list", "-1"), -1);
                                phone = prefs.getString("example_text", "-1");
                                JSONObject resultJson = new JSONObject();
                                resultJson.put("id", clientId);
                                resultJson.put("phone", phone);
                                resultJson.put("stadr", intent.getStringExtra(ParamsAndConstants.MSG_TEXT));
                                resultJson.put("enadr", intent.getStringExtra("end_adr"));
                                resultJson.put("comment", intent.getStringExtra("comment"));
                                resultJson.put("shedule_date", intent.getStringExtra("shedule_date"));
                                resultJson.put("tariffPlanId", intent.getIntExtra("tariff_plan_id", 0));
                                mSocket.emit("new order", resultJson.toString());
                            }
                        } catch (Exception ex) {
                            showToast("Ошибка отправки запроса: " + ex.getMessage());
                        }
                        //showToast(intent.getStringExtra(RouteService.MSG_TEXT));
                        break;
                    case ParamsAndConstants.ID_ACTION_GO_ORDER_CANCELING:
                        try {
                            if (auth) {
                                showToast("Отсылаю запрос отмены...");
                                //clId = clientId;//strToIntDef(prefs.getString("example_list", "-1"), -1);
                                phone = prefs.getString("example_text", "-1");
                                JSONObject resultJson = new JSONObject();
                                resultJson.put("id", clientId);
                                resultJson.put("phone", phone);
                                mSocket.emit("cancel order", resultJson.toString());
                            }
                        } catch (Exception ex) {
                            showToast("Ошибка отправки запроса: " + ex.getMessage());
                        }
                        //showToast(intent.getStringExtra(RouteService.MSG_TEXT));
                        break;
                    case ParamsAndConstants.ID_ACTION_GET_TARIF_AND_OPTIONS:
                        try {
                            if (auth) {
                                //showToast("Отсылаю запрос отмены...");
                                //clId = clientId;//strToIntDef(prefs.getString("example_list", "-1"), -1);
                                phone = prefs.getString("example_text", "-1");
                                JSONObject resultJson = new JSONObject();
                                resultJson.put("id", clientId);
                                resultJson.put("phone", phone);
                                mSocket.emit("tarifs_and_options", resultJson.toString());
                            }
                        } catch (Exception ex) {
                            showToast("Ошибка отправки запроса тарифов и опций: " + ex.getMessage());
                        }
                        //showToast(intent.getStringExtra(RouteService.MSG_TEXT));
                        break;
                    case ParamsAndConstants.ID_ACTION_START_GPS_DETECTING:
                        singleGPSDetect=true;
                        requestLUpd(true);
                        break;
                }
            }
        }, new IntentFilter(MainActivity.INFO_ACTION));

        PowerManager powerManager = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        //wakeLock = powerManager.newWakeLock
        //        (PowerManager.FULL_WAKE_LOCK//PARTIAL_WAKE_LOCK
        //                , "No sleep");
        //wakeLock.acquire();

        checkTimer = new CheckTimer(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //showToast("onLocationChanged");
        Intent intent = new Intent(INFO_ACTION);
        intent.putExtra(ParamsAndConstants.TYPE, ParamsAndConstants.ID_ACTION_SHOW_COORD_INFO);
        intent.putExtra(ParamsAndConstants.MSG_TEXT, "---");
        intent.putExtra("lastLat", location.getLatitude());
        intent.putExtra("lastLon", location.getLongitude());
        sendBroadcast(intent);
        DecimalFormat df = new DecimalFormat("#.######");
        sendCCoords(df.format(location.getLatitude()).replace(",", "."),
                df.format(location.getLongitude()).replace(",", "."));
        if(singleGPSDetect) {
            removeLUpd(true);
            singleGPSDetect=false;
        }
    }

    public void requestLUpd(boolean singleReq)	{
	if (MainActivity.getUseFineLocation()) {
            try {
                myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
            } catch (Exception le) {
                //showToast("Ошибка запуска слушателя TLM GPS_PROVIDER!" + le.getMessage());
            }
        }

        try	{
            /*if (!mainActiv.USE_NETWORK_LOCATION||mainActiv.USE_BOTH_LOCATIONS)	{*/
            myManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
        } catch(Exception le)	{
            showToast("Ошибка запуска слушателя TLM NETWORK_PROVIDER!"+le.getMessage());
        }
    }

    public void removeLUpd(boolean anyWay)	{
        try	{
            //if(!singleGPSActivating||anyWay)	{
            myManager.removeUpdates(this);
                //myManager.re
            /*    if(anyWay) singleGPSActivating=false;*/
        } catch(Exception le)	{
            showToast("Ошибка остановки слушателя TLM!");
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Если пользователь запретил локацию или с его согласия система отключила
        if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER))	{

        }
        if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER))	{

        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Если пользователь разрешил локацию или с его согласия система включила
        if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER))	{

        }
        if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER))	{

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
        if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER))	{
            switch(status){
                case LocationProvider.AVAILABLE:
                    //выводим уведомления ит.д.
                    gpsAviable=true;
                    //showMyMsg("Спутники доступны!");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    //выводим уведомления ит.д.
                    gpsAviable=false;
                    //lastGPSLocation=null;
                    //showMyMsg("Спутники недоступны!");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    //выводим уведомления ит.д.
                    gpsAviable=false;
                    //lastGPSLocation=null;
                    //showMyMsg("Спутники временно недоступны!");
                    break;
                default:
                    //действия
            }
        }
        if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER))	{
            switch(status){
                case LocationProvider.AVAILABLE:
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    break;
                default:
            }
        }
    }

    public String lastStatusData = "";
    public String prevLastStatusData = "";

    public void parseStatus(String data)    {
        lastStatusData = data;
        sendInfoBroadcast( ParamsAndConstants.ID_ACTION_SHOW_STATUS_INFO, data);
        prevLastStatusData = lastStatusData;
    }

    public void setStatusString(String status)  {
        sendInfoBroadcast( ParamsAndConstants.ID_ACTION_SHOW_STATUS_STRING, status);
    }

    public void connectCheck()   {
        if(!connectAttempt&&!inactiveTimeoutBlock) {
            try {
                connectAttempt=true;
                try {
                    if (mSocket == null) {// ? true : !mSocket.connected()) {
                        setStatusString("Подключение...");
                        lastStatusData="";
                        auth = false;
                        mSocket = IO.socket(ParamsAndConstants.srvHost);
                        /*mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                showToast("connected");
                            }
                        });
                        mSocket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                Object arg = args[0];
                                showToast("error" + (arg != null ? arg.toString() : "none"));
                            }
                        });*/
                        if (mSocket != null) {
                            inactiveTimeoutBlock=false;
                            mSocket.connect();
                            mSocket.on("auth", onAuth);
                            mSocket.on("clstat", onClStat);
                            mSocket.on("req_decline", onReqDecline);
                            mSocket.on("sector_detecting", onSectorDetect);
                            mSocket.on("tarifs_and_options", onTarifOptionsGet);
                        }
                    }
                    if (mSocket != null) {// ? mSocket.connected() : false) {
                        if (!auth) {
                            lastStatusData="";
                            setStatusString("Найдено соедениние... идентификация...");
                            clientId = strToIntDef(prefs.getString("example_list", "-1"), -1);
                            phone = prefs.getString("example_text", "-1");
                            JSONObject resultJson = new JSONObject();
                            resultJson.put("id", clientId);
                            resultJson.put("phone", phone);
                            mSocket.emit("ident", resultJson.toString());
                        }
                        else {
                            //if (lastStatusData.length() > 0 &&
                            //        !prevLastStatusData.equals(lastStatusData))
                            //    parseStatus(lastStatusData);
                        }
                    } else {
                        lastStatusData="";
                        if (mSocket != null) {
                            try {
                                mSocket.disconnect();
                                mSocket = null;
                                showToast("TDC dsconn");
                            } catch (Exception exx) {

                            }
                        }
                        mSocket = null;
                        setStatusString("Ожидание соединения...");
                    }
                    //mSocket.emit("ident", "me");

                } catch (Exception e) {
                    auth = false;
                    if (mSocket != null) {
                        try {
                            mSocket.disconnect();
                            mSocket = null;
                            //showToast("TDC dsconn");
                        } catch (Exception ex) {

                        }
                    }
                    mSocket = null;
                    setStatusString("Соединение неудачно! Пробуем снова..." + e.getMessage());
                }
            } finally {
                connectAttempt = false;
            }
        }   else    {
            if(inactiveTimeoutBlock)
                setStatusString("Отключен от сервера, нажмите на любую кнопку для поключения!");
        }
    }

    private Emitter.Listener onAuth = new Emitter.Listener() {
        public void handleAuth(int client_id, int req_trust, int isagainr, int acc_status) {
            Message msg = new Message();
            //msg.obj = this.mainActiv;
            msg.arg1 = ParamsAndConstants.RECEIVE_AUTH;
            Bundle bnd = new Bundle();
            bnd.putInt("client_id", client_id);
            bnd.putInt("req_trust", req_trust);
            bnd.putInt("isagainr", isagainr);
            bnd.putInt("acc_status", acc_status);
            msg.setData(bnd);
            handle.sendMessage(msg);
        }

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int client_id=-1, req_trust=0, isagainr=0, acc_status=0;
            try {
                client_id = data.getInt("client_id");
                req_trust = data.getInt("req_trust");
                isagainr = data.getInt("isagainr");
                acc_status = data.getInt("acc_status");
                //showToast("cid="+cid);
            } catch (Exception e) {
                //showToast(e.getMessage());
            }

            handleAuth(client_id, req_trust, isagainr, acc_status);
        }
    };

    private Emitter.Listener onClStat = new Emitter.Listener() {
        public void handleJSONStr(String data) {
            //this.showMyMsg("sock show timer");
            Message msg = new Message();
            //msg.obj = this.mainActiv;
            msg.arg1 = ParamsAndConstants.RECEIVER_CLSTAT;
            Bundle bnd = new Bundle();
            bnd.putString("data", data);
            msg.setData(bnd);
            handle.sendMessage(msg);
        }

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            handleJSONStr(data.toString());
        }
    };

    private Emitter.Listener onSectorDetect = new Emitter.Listener() {
        public void handleJSONStr(String data) {
            //this.showMyMsg("sock show timer");
            Message msg = new Message();
            //msg.obj = this.mainActiv;
            msg.arg1 = ParamsAndConstants.RECEIVE_SECTOR_DETECT;
            Bundle bnd = new Bundle();
            bnd.putString("data", data);
            msg.setData(bnd);
            handle.sendMessage(msg);
        }

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            handleJSONStr(data.toString());
        }
    };

    private Emitter.Listener onTarifOptionsGet = new Emitter.Listener() {
        public void handleJSONStr(String data) {
            //this.showMyMsg("sock show timer");
            Message msg = new Message();
            //msg.obj = this.mainActiv;
            msg.arg1 = ParamsAndConstants.RECEIVE_TARIFS_OPTIONS;
            Bundle bnd = new Bundle();
            bnd.putString("data", data);
            msg.setData(bnd);
            handle.sendMessage(msg);
        }

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            handleJSONStr(data.toString());
        }
    };

    private Emitter.Listener onReqDecline = new Emitter.Listener() {
        public void handleJSONStr(String data) {
            //this.showMyMsg("sock show timer");
            Message msg = new Message();
            //msg.obj = this.mainActiv;
            msg.arg1 = ParamsAndConstants.REQ_DECLINE;
            Bundle bnd = new Bundle();
            bnd.putString("data", data);
            msg.setData(bnd);
            handle.sendMessage(msg);
        }

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                if (data.getString("status").equalsIgnoreCase("many_new_order_req"))
                    handleJSONStr(data.toString());
            }   catch(Exception e)  {

            }
        }
    };

    private Emitter.Listener onSrvOverload = new Emitter.Listener() {
        public void handleJSONStr(String data) {
            //this.showMyMsg("sock show timer");
            Message msg = new Message();
            //msg.obj = this.mainActiv;
            msg.arg1 = ParamsAndConstants.SERVER_OVERLOAD;
            Bundle bnd = new Bundle();
            bnd.putString("data", data);
            msg.setData(bnd);
            handle.sendMessage(msg);
        }

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                if (data.getString("status").equalsIgnoreCase("many_new_order_req"))
                    handleJSONStr(data.toString());
            }   catch(Exception e)  {

            }
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        //wakeLock.release();
        /*if(mSocket!=null)   {
            try {

                mSocket.disconnect();
                mSocket = null;
                showToast("TDC Destroy");
            } catch(Exception e)    {

            }
        }*/
        return super.onUnbind(intent);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        reloadPrefs();
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            singleGPSDetect = true;
            requestLUpd(true);
        }
        //showNotification("Упр. шлюз", "Запущена основная служба шлюза!");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //unregisterReceiver(messageSent);
        super.onDestroy();
        if(mSocket!=null)   {
            try {
                mSocket.disconnect();
                mSocket = null;
                showToast("TDC Destroy");
            } catch(Exception e)    {

            }
        }

    }

    public void showToast(String message)   {
        Toast.makeText(getBaseContext(),
                message, Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void performDial(String phone){
        if(phone!=null){
            try {
                //dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
            } catch (Exception e) {
                //e.printStackTrace();
                Toast toastErrorStartCallActivity = Toast.makeText(this,
                        "Ошибка осуществления голосового вызова! Текст сообщения: "
                                +e.getMessage()+".", Toast.LENGTH_LONG);
                toastErrorStartCallActivity.show();
            }
        }
    }

    public void showNotify(String title, String msg_txt)    {
        // prepare intent which is triggered if the
        // notification is selected
        /*NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(msg_txt);
        int NOTIFICATION_ID = 12345;

        Intent targetIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());*/
        /*Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification n  = new Notification.Builder(this)
                .setContentTitle("New mail from " + "test@gmail.com")
                .setContentText("Subject")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_launcher, "Call", pIntent)
                .addAction(R.drawable.ic_launcher, "More", pIntent)
                .addAction(R.drawable.ic_launcher, "And more", pIntent).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);*/
    }

}
