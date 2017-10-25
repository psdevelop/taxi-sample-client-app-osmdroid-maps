package ru.psdevelop.tdclientapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity {

    public static final String INFO_ACTION = "com.psdevelop.tdclientapp.MA_INFO_ACTION"; //recompile
    public static Handler handle;
    MACheckTimer maCheckTimer=null;
    static SharedPreferences prefs=null;
    boolean phoneDlgIsOpened=false;
    static boolean activeCoordSearch=false;
    boolean coordSerchIsComplete=false;
    static double lastLat=ParamsAndConstants.defLat, lastLon=ParamsAndConstants.defLon, drLat=0.0, drLon=0.0,
        lastRevLat=-1, lastRevLon=-1;
    static boolean hasMeGPSDetecting=false;
    static boolean hasMeGAdrDetecting=false;
    ProgressDialog Indicator;
    int totalProgressTime=200;
    static int lastOrdersCount=0;
    public static String drivers_markers="";
    EmployeeDAO empldao=null;
    static boolean hasMAOrdering=false;
    static String hasMAOrderAdr="";
    static boolean coordSearchDetectFromAdr=false;
    static boolean maoRequest=false;
    static String maoSadr="", maoEadr="";
    static boolean hasOrderRequest = false;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    static WebView wv;
    static MapView map;
    static TextView textViewStatus=null;
    static String ordersInfo="";

    public void checkGPSPermission()    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        TDClientService.TDC_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        TDClientService.TDC_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
                        .setMessage("При смене разрешения необходимо заново войти в приложение?")
                        // кнопка "Yes", при нажатии на которую приложение закроется
                        .setPositiveButton("Ок",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int whichButton)
                                    {
                                        //if(!SOCKET_IN_SERVICE)	{
                                        //    sendInfoBroadcast(TSI_STOP_NSOCK_SERVICE, "---");
                                        //}
                                        //userInterrupt = true;
                                        finish();
                                    }
                                })
                        /*.setNegativeButton("Отмена",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int whichButton)
                                    {

                                    }
                                })*/
                        .show();
            }
        }
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

    public static boolean checkStringDouble(String str) {
        try {
            Double.parseDouble(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static double strToDoubleDef(String str_double, double def) {
        double res = def;

        if (checkStringDouble(str_double)) {
            res = Double.parseDouble(str_double);
        }

        return res;
    }

    public void insertRecCurDt(String adr, double sal)	{
        //if(emplf==null) {
        //    empl = new EmpListFragment();
        //}
        if(empldao!=null) {
            try {
                empldao.insertRecCurDt(adr, sal, null);
                //showMyMsg("Good insertRecCurDt!");
            }   catch(Exception e)  {
                showMyMsg("Bad insertRecCurDt!");
            }
        }
    }

    public void playMP3(int res_id)	{
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(this, res_id);
        mediaPlayer.setVolume(1, 1);
        mediaPlayer.start();
    }

    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        empldao = new EmployeeDAO(this);

        prefs = PreferenceManager.
                getDefaultSharedPreferences(this);
        phoneDlgIsOpened=false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        //mSectionsPagerAdapter.

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        checkGPSPermission();
        //mSectionsPagerAdapter.

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                //tab.getIcon().setAlpha(255);
                //showMyMsg(tab.getPosition()+"");
                sendInfoBroadcast(ParamsAndConstants.ID_ACTION_WAKE_UP_NEO, "---");
                if (tab.getPosition() == 1)
                    if (hasMeGPSDetecting||hasMeGAdrDetecting)
                        showMeOnMap();
                    else
                        startGPSCoordsProcessing(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);
                //tab.getIcon().setAlpha(127);
            }
        });

        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_WAKE_UP_NEO, "---");

        handle = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == ParamsAndConstants.SHOW_MESSAGE_TOAST) {
                    showMyMsg(msg.getData().
                            getString("msg_text"));
                } else if (msg.arg1 == ParamsAndConstants.MA_SET_STAT_TEXTVIEW) {
                    setTextViewStatus(msg.getData().
                            getString(ParamsAndConstants.MSG_TEXT));
                }
                else if (msg.arg1 == ParamsAndConstants.SHOW_STATUS_STRING) {
                    try {
                    setTextViewStatus(msg.getData().
                            getString("msg_text"));
                    }   catch(Exception e)  {
                        showMyMsg("SHOW_STATUS_STRING!"+e.getMessage());
                    }
                }
                else if (msg.arg1 == ParamsAndConstants.SHOW_STATUS_INFO) {
                    //showMyMsg(msg.getData().
                    //        getString("msg_text"));
                    try {
                        JSONObject resultJson = new JSONObject((new JSONObject(msg.getData().
                                getString("msg_text"))).getString("cl_status"));

                        //showMyMsg((new JSONObject(msg.getData().
                        //        getString("msg_text"))).getString("cl_status"));
                        boolean hasOrders=false;
                        if(resultJson.has("ocn")) {
                            drivers_markers="";
                            //showMyMsg("Заказов " + resultJson.getInt("ocn"));
                            String ords_dt = "";
                            String prevOrdersInfo = ordersInfo;
                            ordersInfo = "";

                            lastOrdersCount = resultJson.getInt("ocn");
                            if(lastOrdersCount<=0) {
                                drLat=0;
                                drLon=0;
                            }

                            if(hasOrderRequest&&lastOrdersCount>0)  {
                                try {
                                    hasOrderRequest = false;
                                    Intent bintent = new Intent(INFO_ACTION);
                                    bintent.putExtra(ParamsAndConstants.TYPE, ParamsAndConstants.ID_ACTION_SEND_CCOORDS);
                                    bintent.putExtra("clat", lastLat);
                                    bintent.putExtra("clon", lastLon);
                                    sendBroadcast(bintent);
                                } catch(Exception e)    {

                                }
                            }
                            for (int i = 0; i < resultJson.getInt("ocn"); i++) {
                                hasOrders = true;

                                if(i==0) {
                                    try {
                                        //showMyMsg(resultJson.getString("odt"+i));
                                        showGMAddressIfEmpty(resultJson.getString("odt"+i).replace("(ONLINE)",""));
                                    } catch (Exception e) { }
                                }

                                ords_dt = ords_dt+" "+(i+1)+". "+(resultJson.has("osdt"+i)?resultJson.getString("osdt"+i)+" ":"")+
                                        resultJson.getString("odt"+i).replace("(ONLINE)","");
                                if(hasMAOrdering&&hasMAOrderAdr.length()>0) {
                                    if(hasMAOrderAdr.equalsIgnoreCase(resultJson.getString("odt"+i).replace("(ONLINE)","")))    {
                                        //showMyMsg("hasMAOrdering");
                                        insertRecCurDt(hasMAOrderAdr,0);
                                        hasMAOrdering=false;
                                    }
                                }
                                else    {
                                    //showMyMsg("No hasMAOrdering");
                                }
                                int rcst=0;
                                if(resultJson.has("rcst"+i))	{
                                    ///strToIntDef()
                                    rcst=strToIntDef(resultJson.getString("rcst"+i),0);
                                }
                                //if(!rcst) rcst=0;
                                ords_dt = ords_dt +" ";
                                if(rcst==-2)	{
                                    //if(rcst==-2)
                                    ords_dt = ords_dt + "извините, нет свободных машин";
                                }
                                else {
                                    if (rcst == 2)
                                        ords_dt = ords_dt + " в обработке";
                                    if (resultJson.has("ors" + i)) {
                                        ordersInfo = ordersInfo + "status" + resultJson.getInt("ors" + i);
                                        if (resultJson.getInt("ors" + i) == 0)
                                            ords_dt = ords_dt + "ищем машину";
                                        if (resultJson.getInt("ors" + i) == 8)
                                            ords_dt = ords_dt + "за Вами отправлена машина";
                                        if (resultJson.has("opl" + i))
                                        if (resultJson.getInt("opl" + i) == 1 && resultJson.getInt("ors" + i) == 8) {
                                            ords_dt = ords_dt + "ожидает выходите";
                                            ordersInfo = ordersInfo + "opl";
                                        }
                                        if (resultJson.getInt("ors" + i) == 26)
                                            ords_dt = ords_dt + "дан отчет " + resultJson.getString("osumm" + i);
                                        if (resultJson.has("tmh" + i))
                                        if (resultJson.getString("tmh" + i).length() > 0 && resultJson.getInt("ors" + i) == 8)
                                            ords_dt = ords_dt + ">на выполнении (таксометр активен)";

                                        if (resultJson.has("dgn" + i)) {
                                            if (resultJson.getString("dgn" + i).length() > 0) {
                                                ords_dt = ords_dt + " Гос. номер: " + resultJson.getString("dgn" + i);
                                                ordersInfo = ordersInfo + resultJson.getString("dgn" + i);
                                            }
                                        }

                                        if (resultJson.has("dmrk" + i)) {
                                            if (resultJson.getString("dmrk" + i).length() > 0) {
                                                ords_dt = ords_dt + " Марка: " + resultJson.getString("dmrk" + i);
                                                ordersInfo = ordersInfo + resultJson.getString("dmrk" + i);
                                            }
                                        }

                                        if (resultJson.has("dphn" + i)) {
                                            if (resultJson.getString("dphn" + i).length() > 0) {
                                                ords_dt = ords_dt + " Телефон водителя: " + resultJson.getString("dphn" + i);
                                                ordersInfo = ordersInfo + resultJson.getString("dphn" + i);
                                            }
                                        }

                                    }


                                }

                                boolean hasLat=false;
                                boolean hasLon=false;
                                if(resultJson.has("dlat"+i))	{
                                    drLat=strToDoubleDef(resultJson.getString("dlat"+i),0);
                                    if(drLat>0)
                                        hasLat=true;
                                }
                                if(resultJson.has("dlon"+i))	{
                                    drLon=strToDoubleDef(resultJson.getString("dlon"+i),0);
                                    if(drLon>0)
                                        hasLon=true;
                                }

                                if(hasLat&&hasLon)	{
                                    drivers_markers=" var dmarker"+i+" = new google.maps.Marker({ " +
                                            "position: new google.maps.LatLng("+drLat+", "+drLon+"), " +
                                            "map: map, " +
                                            "title: \"Водитель находится здесь!\" " +
                                            "}); " +
                                            "var dinfowindow"+i+" = new google.maps.InfoWindow({ " +
                                            "content: '<div id=\"content\" style=\"min-height:50px;\"><center>Такси</center></div>' " +
                                            " }); " +
                                            "dinfowindow"+i+".open( map, dmarker"+i+"); " +
                                            "bounds.extend(dmarker"+i+".position); ";
                                    //coords[i] = { drLat:drLat, drLon: drLon };
                                    //if(rcst!=-2&&rcst!==2)
                                    //    ords_dt = ords_dt + '<a id="dr_on_map'+i+'" class="ui-shadow ui-btn ui-corner-all ui-btn-inline ui-btn-icon-left ui-icon-star" href="javascript:void(0)" onclick="showMeOnMap('+i+');">На карте</a>';
                                }
                            }
                            try {
                                //showMyMsg("У вас Заказов всего " + resultJson.getString("ocn") + ords_dt);
                                setTextViewStatus("У вас Заказов всего " + resultJson.getString("ocn")+": " + ords_dt);
                            }   catch(Exception e)  {

                            }

                            if(ordersInfo.indexOf("status8")>=0 && !prevOrdersInfo.equals(ordersInfo)) {
                                playMP3(R.raw.truck);
                                showMyMsg("Изменился статус заказов (назначена, прибыла машина и т.д.)!");
                            }

                            if(tabLayout.getSelectedTabPosition() == 1) {
                                //showMyMsg("Обновление карты..."+drLat+"==="+drLon);
                                showMeOnMap();
                            }
                            //Конец анализа данных о заказах
                        }
                        else
                            showMyMsg("Нет информации по количеству заказов в ответе сервера!");

                        //showMyMsg("Обновление..."+tabLayout.getSelectedTabPosition());


                    }   catch(Exception e)  {
                        showMyMsg("Неудачное чтение статуса!"+e.getMessage());
                    }
                    hasMAOrdering=false;
                    hasMAOrderAdr="";
                    //tvSMSSendInfo.setText(systemTimeStamp()+": "+msg.getData().
                    //        getString("msg_text"));
                }
                else if (msg.arg1 == ParamsAndConstants.SHOW_DECLINE_INFO) {
                    //tvInCallInfo.setText(systemTimeStamp()+": "+msg.getData().
                    //        getString("msg_text"));
                }
                else if (msg.arg1 == ParamsAndConstants.SHOW_COORDS_INFO) {
                    //tvInitCallInfo.setText(systemTimeStamp()+": "+msg.getData().
                    //        getString("msg_text"));
                    //showMyMsg("Определены"+lastLat+"-"+lastLon);
                    mViewPager.setCurrentItem(1);
                    showMeOnMap();
                } else if (msg.arg1 == ParamsAndConstants.MA_CHECK_STATUSES) {
                    //showToast("--==");
                    checkStatus();
                }   else if(msg.arg1 == ParamsAndConstants.MA_GPS_DETECTING)   {
                    hasMeGPSDetecting=false;
                    hasMeGAdrDetecting=false;
                    startGPSCoordsProcessing(true);
                }   else if(msg.arg1 == ParamsAndConstants.MA_ORDERING)   {
                    hasMAOrdering=true;
                    sendOrderRequest(msg.getData().getString("msg_text"), msg.getData().getString("end_adr"));
                    hasMAOrdering=true;
                    hasOrderRequest=true;
                    hasMAOrderAdr=msg.getData().getString("msg_text");
                }   else if(msg.arg1 == ParamsAndConstants.MA_CANCELING)   {
                    sendOrderCancelRequest();
                }   else if(msg.arg1 == ParamsAndConstants.SHOW_GM_ADDRESS)   {
                    showGMAddress(msg.getData().getString("msg_text"));
                }
                else if(msg.arg1 == ParamsAndConstants.ID_ACTION_SET_HISTORY_ADR)   {
                    try {
                        mViewPager.setCurrentItem(0);
                        showGMAddress(msg.getData().getString("msg_text"));
                    } catch(Exception hex)  {
                        showMyMsg("ID_ACTION_SET_HISTORY_ADR "+hex);
                    }
                }   else if(msg.arg1 == ParamsAndConstants.MA_SEND_INFO_BCAST)   {
                    sendInfoBroadcast(msg.getData().getInt(ParamsAndConstants.TYPE),
                            msg.getData().getString(ParamsAndConstants.MSG_TEXT));
                }
            }
        };

        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra(ParamsAndConstants.TYPE, -1);
                switch (type) {
                    case ParamsAndConstants.ID_ACTION_SHOW_SERVICE_INFO:
                        try {
                            Message msg = new Message();
                            msg.arg1 = ParamsAndConstants.SHOW_MESSAGE_TOAST;
                            Bundle bnd = new Bundle();
                            bnd.putString("msg_text", intent.getStringExtra(ParamsAndConstants.MSG_TEXT));
                            msg.setData(bnd);
                            handle.sendMessage(msg);

                        } catch (Exception ex) {
                            showMyMsg("Ошибка ID_ACTION_SHOW_SERVICE_INFO: " + ex);
                        }
                        //showToast(intent.getStringExtra(RouteService.MSG_TEXT));
                        break;
                    case ParamsAndConstants.ID_ACTION_SET_STATUS_TEXTVIEW:
                        try {
                            Message msg = new Message();
                            msg.arg1 = ParamsAndConstants.MA_SET_STAT_TEXTVIEW;
                            Bundle bnd = new Bundle();
                            bnd.putString("msg_text", intent.getStringExtra(ParamsAndConstants.MSG_TEXT));
                            msg.setData(bnd);
                            handle.sendMessage(msg);

                        } catch (Exception ex) {
                            showMyMsg("Ошибка ID_ACTION_SET_STATUS_TEXTVIEW: " + ex);
                        }
                        //showToast(intent.getStringExtra(RouteService.MSG_TEXT));
                        break;
                    case ParamsAndConstants.ID_ACTION_SHOW_STATUS_INFO:
                        try {
                            Message msg = new Message();
                            msg.arg1 = ParamsAndConstants.SHOW_STATUS_INFO;
                            Bundle bnd = new Bundle();
                            bnd.putString("msg_text", intent.getStringExtra(ParamsAndConstants.MSG_TEXT));
                            msg.setData(bnd);
                            //showMyMsg("SHOW_STATUS_INFO"+intent.getStringExtra(ParamsAndConstants.MSG_TEXT));
                            handle.sendMessage(msg);

                        } catch (Exception ex) {
                            showMyMsg("Ошибка ID_ACTION_SHOW_STATUS_INFO: " + ex);
                        }
                        break;
                    case ParamsAndConstants.ID_ACTION_SHOW_STATUS_STRING:
                        try {
                            Message msg = new Message();
                            msg.arg1 = ParamsAndConstants.SHOW_STATUS_STRING;
                            Bundle bnd = new Bundle();
                            bnd.putString("msg_text", intent.getStringExtra(ParamsAndConstants.MSG_TEXT));
                            msg.setData(bnd);
                            handle.sendMessage(msg);

                        } catch (Exception ex) {
                            showMyMsg("Ошибка ID_ACTION_SHOW_STATUS_STRING: " + ex);
                        }
                        break;
                    case ParamsAndConstants.ID_ACTION_SHOW_COORD_INFO:
                        try {
                            if (activeCoordSearch) {
                                Message msg = new Message();
                                msg.arg1 = ParamsAndConstants.SHOW_COORDS_INFO;
                                Bundle bnd = new Bundle();
                                lastLat = intent.getDoubleExtra("lastLat", 0);
                                lastLon = intent.getDoubleExtra("lastLon", 0);
                                hasMeGPSDetecting = true;
                                coordSearchDetectFromAdr = false;
                                bnd.putString("msg_text", intent.getStringExtra(ParamsAndConstants.MSG_TEXT));
                                msg.setData(bnd);
                                handle.sendMessage(msg);
                            }
                            activeCoordSearch = false;
                        } catch (Exception ex) {
                            showMyMsg("Ошибка ID_ACTION_SHOW_COORD_INFO: " + ex);
                        }

                        break;
                }
            }
        }, new IntentFilter(TDClientService.INFO_ACTION));
        maCheckTimer = new MACheckTimer(this);
    }

    @Override
    public void onBackPressed()	{
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
                .setMessage("Закрыть приложение?")
                        // кнопка "Yes", при нажатии на которую приложение закроется
                .setPositiveButton("Ок",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                //if(!SOCKET_IN_SERVICE)	{
                                //    sendInfoBroadcast(TSI_STOP_NSOCK_SERVICE, "---");
                                //}
                                //userInterrupt = true;
                                finish();
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {

                            }
                        })
                .show();
    }

    public void showGMAddress(String txt)   {
        try {
            mSectionsPagerAdapter.firstTab.editTextFromAdres.setText(txt);
        }   catch(Exception e)  {
            showMyMsg("showGMAddress: "+e.getMessage());
        }
    }

    public void showGMAddressIfEmpty(String txt)   {
        try {
            if(mSectionsPagerAdapter.firstTab.editTextFromAdres.getText().length()<=0 ||
                    !mSectionsPagerAdapter.firstTab.editTextFromAdres.getText().equals(txt))
                mSectionsPagerAdapter.firstTab.editTextFromAdres.setText(txt);
        }   catch(Exception e)  {
            showMyMsg("showGMAddress: "+e.getMessage());
        }
    }

    public void setTextViewStatus(String txt)   {
        try {
            textViewStatus.setText(txt);
        }   catch(Exception e)  {
            showMyMsg("setTextViewStatus: "+e.getMessage());
        }
    }

    public void sendOrderRequest(String start_adr, String end_adr)  {
        Intent intent = new Intent(INFO_ACTION);
        intent.putExtra(ParamsAndConstants.TYPE, ParamsAndConstants.ID_ACTION_GO_ORDERING);
        intent.putExtra(ParamsAndConstants.MSG_TEXT, start_adr);
        intent.putExtra("end_adr", end_adr);
        sendBroadcast(intent);
    }

    public void sendOrderCancelRequest()  {
        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_GO_ORDER_CANCELING, "---");
    }

    public static String lastAdr="";

    public void getCoordsByAdr(String gadr)    {
        if(gadr.length()>2)
        try {
            final String tgadr = gadr;
            sendReverseGeocodeHTTPRequest(tgadr);
        }   catch(Exception e)  {
            showMyMsg("getCoordsByAdr: "+e.getMessage());
        }
    }

    Marker startMarker;
    Marker driverMarker;

    public void showMeOnMap()   {
        try {

            if(!hasMeGAdrDetecting && lastLat>0 && lastLon>0)
                sendGeocodeHTTPRequest( lastLat, lastLon);

            if(startMarker!=null)
                try {
                    map.getOverlays().remove(startMarker);
                } catch(Exception e) {

                }
            startMarker = null;

            if(driverMarker!=null)
                try {
                    map.getOverlays().remove(driverMarker);
                } catch(Exception e) {

                }
            driverMarker = null;
            map.getOverlays().clear();

            IMapController mapController = map.getController();
            mapController.setZoom(15);

            GeoPoint startPoint=null;
            if(lastLat>0 && lastLon>0) {
                startPoint = new GeoPoint(lastLat, lastLon);

                //0. Using the Marker overlay
                startMarker = new Marker(map);
                startMarker.setPosition(startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setTitle((hasMeGAdrDetecting ? lastAdr : "Вы здесь!"));
                //startMarker.setIcon(getResources().getDrawable(R.drawable.marker_kml_point).mutate());
                //startMarker.setImage(getResources().getDrawable(R.drawable.ic_launcher));
                startMarker.setInfoWindow(new MarkerInfoWindow(R.layout.bonuspack_bubble_black, map));
                startMarker.setDraggable(true);
                //startMarker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer());
                map.getOverlays().add(startMarker);
                startMarker.showInfoWindow();
            }

            if(drLat>0 && drLon>0)  {
                GeoPoint driverPoint = new GeoPoint(drLat, drLon);
                driverMarker = new Marker(map);
                driverMarker.setPosition(driverPoint);
                driverMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                driverMarker.setTitle("Ваше такси");
                //startMarker.setInfoWindow(new MarkerInfoWindow(R.layout.bonuspack_bubble_black, map));
                driverMarker.setDraggable(true);
                //startMarker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer());
                map.getOverlays().add(driverMarker);
                driverMarker.showInfoWindow();

                if(startPoint!=null) {
                    double latMax = startPoint.getLatitude()>driverPoint.getLatitude() ?
                            startPoint.getLatitude() : driverPoint.getLatitude();
                    double latMin = startPoint.getLatitude()<driverPoint.getLatitude() ?
                            startPoint.getLatitude() : driverPoint.getLatitude();
                    double lonMax = startPoint.getLongitude()>driverPoint.getLongitude() ?
                            startPoint.getLongitude() : driverPoint.getLongitude();
                    double lonMin = startPoint.getLongitude()<driverPoint.getLongitude() ?
                            startPoint.getLongitude() : driverPoint.getLongitude();
                    BoundingBox oBB = new BoundingBox(latMax+0.1, lonMax+0.1, latMin-0.1, lonMin-0.1);
                    map.zoomToBoundingBox(oBB, false);
                }

            } else {
                if(startPoint!=null)
                    mapController.setCenter(startPoint);
            }

            //showMyMsg("OSM MAP SUCC!");
        }   catch(Exception e)  {
            showMyMsg("OSM MAP REPAINT ERROR!"+e.getMessage());
        }

    }

    public void startGPSCoordsProcessing(boolean gadr_alternative) {
        if(lastAdr.length()>2) {
            final boolean gadraa = gadr_alternative;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Поиск по адресу").setMessage("Искать в регионе '" + ParamsAndConstants.REGION_DEFAULT + "'")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                            //sendReverseGeocodeHTTPRequest(ParamsAndConstants.REGION_DEFAULT+" "+tgadr);
                            lastAdr = ParamsAndConstants.REGION_DEFAULT + lastAdr;
                            startGPSCoordsProcessingInc(gadraa);
                        }
                    })
                    .setNegativeButton("Искать везде", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            //sendReverseGeocodeHTTPRequest(tgadr);
                            startGPSCoordsProcessingInc(gadraa);
                        }
                    }).show();
        }   else
            startGPSCoordsProcessingInc(gadr_alternative);
    }
    public void startGPSCoordsProcessingInc(boolean gadr_alternative) {
        //Создаем ProgressDialog
        Indicator = new ProgressDialog(this);
        //Настраиваем для ProgressDialog название его окна:
        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_START_GPS_DETECTING, "---");
        if(gadr_alternative)
            getCoordsByAdr(lastAdr);
        activeCoordSearch = true;
        Indicator.setMessage("Определение местоположения...");
        //Настраиваем стиль отображаемого окна:
        Indicator.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //Выставляем прогресс задачи на 0 позицию:
        Indicator.setProgress(0);
        //Устанавливаем максимально возможное значение в работе цикла:
        Indicator.setMax(totalProgressTime);
        //Отображаем ProgressDialog:
        Indicator.show();

        //Создаем параллельный поток с выполнением цикла, который будет
        //работать, пока не достигнет значения в 20 (totalProgressTime):
        new Thread(new Runnable() {
            @Override
            public void run(){
                int counter = 0;
                while(activeCoordSearch)    {//counter < totalProgressTime ){
                    try {
                        //Устанавливаем время задержки между итерациями
                        //цикла (между действиями цикла):
                        Thread.sleep(300);
                        //counter ++;
                        //Обновляем индикатор прогресса до значения counter:
                        //Indicator.setProgress(counter);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                //При завершении работы цикла закрываем наш ProgressDialog:
                Indicator.dismiss();
            }
        }).start();
    }

    public void sendInfoBroadcast(int action_id, String message) {
        Intent intent = new Intent(INFO_ACTION);
        intent.putExtra(ParamsAndConstants.TYPE, action_id);
        intent.putExtra(ParamsAndConstants.MSG_TEXT, message);
        sendBroadcast(intent);
    }

    public void checkStatus()   {
        if (!(prefs.getString("example_text", "").length() == 10)&&!phoneDlgIsOpened) {
            phoneDlgIsOpened=true;
            //sendInfoBroadcast(ID_ACTION_ASK_FOR_PHONE, "Запущена основная служба шлюза!");
            this.showInputDialogElement("phone_num_input", "Введите номер телефона (10 цифр без восьмерки)");
        }
    }

    public void showInputDialogElement(String dlg_type, String msg) {
        try	{
            AlertDialog.Builder inp_builder = new AlertDialog.Builder(this);

            final EditText input_text = new EditText(this);
            input_text.setInputType(InputType.TYPE_CLASS_NUMBER);

            inp_builder.setView(input_text);
            inp_builder.setTitle("ВВОД ДАННЫХ")
                    .setMessage(msg)
                            // кнопка "Yes", при нажатии на которую приложение закроется
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        SharedPreferences.Editor edt = prefs.edit();
                                        edt.putString("example_text",input_text.getText().toString());
                                        edt.commit();
                                    } catch (Exception pex) {
                                        showMyMsg("Неудачное присваивание настроек PHONE_NUM от клиента! " +
                                                pex.getMessage());
                                    }
                                    phoneDlgIsOpened=false;

                                }
                            }).show();

        }	catch(Exception e)	{
            showMyMsg("Ошибка вывода диалога: " + e.getMessage());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.checkGPSPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Intent i = new Intent(getBaseContext(), TDClientService.class);
            startService(i);
            this.checkGPSPermission();
            //this.showMyMsg("Запуск основной службы!");
        } catch(Exception ex)	{
            this.showMyMsg("Ошибка запуска сервиса!");
        }
        //sendInfoBroadcast(ParamsAndConstants.ID_ACTION_WAKE_UP_NEO, "---");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //sendInfoBroadcast(ParamsAndConstants.ID_ACTION_WAKE_UP_NEO, "---");
    }

    public void showMyMsg(String message)   {
        try {
            Toast alertMessage = Toast.makeText(getApplicationContext(),
                    "СООБЩЕНИЕ: "
                            +message, Toast.LENGTH_LONG);
            alertMessage.show();
        } catch(Exception ex)   {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            try	{
                Intent settingsActivity = new Intent(getBaseContext(),
                        SettingsActivity.class);
                startActivity(settingsActivity);
            } catch (Exception e) {
                Toast toastErrorStartActivitySMS = Toast.
                        makeText(getApplicationContext(),
                                "Ошибка вывода настроек! Текст сообщения: "
                                        +e.getMessage()+".", Toast.LENGTH_LONG);
                toastErrorStartActivitySMS.show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendReverseGeocodeHTTPRequest(String adress)	{
        final String addres = "https://maps.googleapis.com/maps/api/geocode/xml?key="+ParamsAndConstants.gm_key+
                "&address="+URLEncoder.encode(adress)+"&sensor=false&language=ru";
        if(adress.length()>4)
        new Thread(new Runnable() {

            public void parseAnswer(String xmlData) {
                //setContentView(R.layout.main);
                String tmp = "";
                boolean hasGAdrLat=false, hasGAdrLon=false;

                try {
                    XmlPullParserFactory factory = XmlPullParserFactory
                            .newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();

                    xpp.setInput(new StringReader(xmlData));
                    boolean isLocation = false;
                    boolean isResult = false;
                    boolean isGeometry = false;
                    boolean isLng = false, isLat = false;
                    boolean isStatus = false;
                    boolean statusIsOK = false;



                    while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                        switch (xpp.getEventType()) {
                            // начало документа
                            case XmlPullParser.START_DOCUMENT:
                                //Log.d(LOG_TAG, "START_DOCUMENT");
                                break;
                            // начало тэга
                            case XmlPullParser.START_TAG:
                                if(xpp.getName().equalsIgnoreCase("status"))  {
                                    isStatus=true;
                                }
                                if(xpp.getName().equalsIgnoreCase("result"))  {
                                    isResult=true;
                                }
                                if(xpp.getName().equalsIgnoreCase("geometry"))  {
                                    isGeometry=true;
                                }
                                if(xpp.getName().equalsIgnoreCase("location"))  {
                                    isLocation=true;
                                }
                                if(xpp.getName().equalsIgnoreCase("lng"))  {
                                    isLng=true;
                                }
                                if(xpp.getName().equalsIgnoreCase("lat"))  {
                                    isLat=true;
                                }
                                //Log.d(LOG_TAG, "START_TAG: name = " + xpp.getName()
                                //        + ", depth = " + xpp.getDepth() + ", attrCount = "
                                //        + xpp.getAttributeCount());
                                tmp = "";
                                for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                    tmp = tmp + xpp.getAttributeName(i) + " = "
                                            + xpp.getAttributeValue(i) + ", ";
                                }
                                //if (!TextUtils.isEmpty(tmp))
                                    //Log.d(LOG_TAG, "Attributes: " + tmp);
                                break;
                            // конец тэга
                            case XmlPullParser.END_TAG:
                                //Log.d(LOG_TAG, "END_TAG: name = " + xpp.getName());
                                if(xpp.getName().equalsIgnoreCase("status"))  {
                                    isStatus=false;
                                }
                                if(xpp.getName().equalsIgnoreCase("result"))  {
                                    isResult=false;
                                }
                                if(xpp.getName().equalsIgnoreCase("geometry"))  {
                                    isGeometry=false;
                                }
                                if(xpp.getName().equalsIgnoreCase("location"))  {
                                    isLocation=false;
                                }
                                if(xpp.getName().equalsIgnoreCase("lng"))  {
                                    isLng=false;
                                }
                                if(xpp.getName().equalsIgnoreCase("lat"))  {
                                    isLat=false;
                                }
                                break;
                            // содержимое тэга
                            case XmlPullParser.TEXT:
                                //Log.d(LOG_TAG, "text = " + xpp.getText());
                                //if(isLocation)  {
                                //    isLocation=false;
                                //    showMsg("isLocation: "+xpp.getText());
                                //}
                                if(isStatus)    {
                                    if(xpp.getText().equalsIgnoreCase("OK"))    {
                                        statusIsOK=true;
                                        lastRevLat=-1;
                                        lastRevLon=-1;
                                    }
                                }
                                if(isLng&&isGeometry&&isLocation&&isResult&&statusIsOK)  {
                                    isLng=false;
                                    lastRevLon = strToDoubleDef(xpp.getText(), -1);
                                    hasGAdrLon=true;
                                    //showMsg("isLng: "+lastRevLon);
                                }
                                if(isLat&&isGeometry&&isLocation&&isResult&&statusIsOK)  {
                                    isLat=false;
                                    lastRevLat = strToDoubleDef(xpp.getText(), -1);
                                    hasGAdrLat=true;
                                    //showMsg("isLat: "+lastRevLat);
                                }
                                break;

                            default:
                                break;
                        }
                        // следующий элемент
                        xpp.next();
                    }
                    //Log.d(LOG_TAG, "END_DOCUMENT");

                } catch (XmlPullParserException e) {
                    showMsg("XmlPullParserException "+e.getMessage());
                    //e.printStackTrace();
                } catch (IOException e) {
                    showMsg("IOException "+e.getMessage());
                    //e.printStackTrace();
                }

                if(hasGAdrLat&&hasGAdrLon)
                if(activeCoordSearch)    {
                    lastLon=lastRevLon;
                    lastLat=lastRevLat;
                    Intent bintent = new Intent(INFO_ACTION);
                    bintent.putExtra(ParamsAndConstants.TYPE, ParamsAndConstants.ID_ACTION_SEND_CCOORDS);
                    bintent.putExtra("clat", lastLat);
                    bintent.putExtra("clon", lastLon);
                    sendBroadcast(bintent);
                    hasMeGAdrDetecting=true;
                    activeCoordSearch=false;
                    coordSearchDetectFromAdr=true;
                    Message msg = new Message();
                    msg.arg1 = ParamsAndConstants.SHOW_COORDS_INFO;
                    Bundle bnd = new Bundle();
                    bnd.putString("msg_text", "===");
                    msg.setData(bnd);
                    handle.sendMessage(msg);
                }
            }

            public void showMsg(String msgtext)    {
                Message msg = new Message();
                msg.arg1 = ParamsAndConstants.SHOW_MESSAGE_TOAST;
                Bundle bnd = new Bundle();
                bnd.putString("msg_text", msgtext);
                msg.setData(bnd);
                handle.sendMessage(msg);
            }

            public void showGMAdress(String msgtext)    {
                Message msg = new Message();
                msg.arg1 = ParamsAndConstants.SHOW_GM_ADDRESS;
                Bundle bnd = new Bundle();
                bnd.putString("msg_text", msgtext);
                msg.setData(bnd);
                handle.sendMessage(msg);
            }

            @Override
            public void run(){
                try {
                    String url = addres;

                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    // optional default is GET
                    con.setRequestMethod("GET");

                    //add request header
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");

                    int responseCode = con.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + url);
                    System.out.println("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //showMsg(response.toString());
                    try {

                        //showMsg("Коорд по адресу "+response.toString());
                        parseAnswer(response.toString());
                        //JSONObject resultJson = new JSONObject((new JSONObject
                        //        (response.toString())).getString("result"));
                                //(new JSONArray((new JSONObject
                        //        (response.toString())).getString("results"))).getJSONObject(0);
                        //{'status':'OK', 'result':{'type':'geo'}} <status>OK</status><result><type>geo</type></result>
                        //showGMAdress(resultJson.getString("formatted_address").replace("Россия",""));
                        //showMsg("Коорд по адресу "+resultJson.toString());
                    } catch (Exception ex) {
                        showMsg("Парсинг ответа обратного запроса Geocoder"+ex.getMessage());
                    }
                }
                catch (Exception e) {
                    showMsg("Парсинг обратного запроса геокодера Google Maps! "+e.getMessage());
                }
            }
        }).start();
    }

    public void sendGeocodeHTTPRequest(double rlat, double rlon)	{
        final String addres = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+rlat+","+rlon+"&sensor=false&language=ru";
        new Thread(new Runnable() {

            public void showMsg(String msgtext)    {
                Message msg = new Message();
                msg.arg1 = ParamsAndConstants.SHOW_MESSAGE_TOAST;
                Bundle bnd = new Bundle();
                bnd.putString("msg_text", msgtext);
                msg.setData(bnd);
                handle.sendMessage(msg);
            }

            public void showGMAdress(String msgtext)    {
                Message msg = new Message();
                msg.arg1 = ParamsAndConstants.SHOW_GM_ADDRESS;
                Bundle bnd = new Bundle();
                bnd.putString("msg_text", msgtext);
                msg.setData(bnd);
                handle.sendMessage(msg);
            }

            @Override
            public void run(){
                try {
                    String url = addres;

                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    // optional default is GET
                    con.setRequestMethod("GET");

                    //add request header
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");

                    int responseCode = con.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + url);
                    System.out.println("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //showMsg(response.toString());
                    try {

                        JSONObject resultJson = (new JSONArray((new JSONObject
                                (response.toString())).getString("results"))).getJSONObject(0);
                        showGMAdress(resultJson.getString("formatted_address").replace("Россия",""));
                    } catch (Exception e) {
                            showMsg("Парсинг ответа Geocoder"+e.getMessage());
                    }
                }
                catch (Exception e) {
                    showMsg("Запроса геокодера Google Maps! "+e.getMessage());
                }
            }
        }).start();
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        public View fragmentViev;
        Button orderButton, cancelButton, gpsDetectButton, clearBtn;
        AutoCompleteTextView editTextFromAdres;
        EditText editTextToAdres;

        public static final String ARG_ITEM_ID = "employee_list";

        Activity activity;
        ListView employeeListView;
        ArrayList<Employee> employees;
        PlacesTask placesTask;
        GetEmpTask eTask;

        //EmpListAdapter employeeListAdapter;
        EmployeeDAO employeeDAO;

        //private GetEmpTask task;
        //TextView

        public PlaceholderFragment() {
            activity = getActivity();
            employeeDAO = new EmployeeDAO(activity);
        }

        public void sendInfoBroadcast(int action_id, String message) {
            Message msg = new Message();
            msg.arg1 = ParamsAndConstants.MA_SEND_INFO_BCAST;
            Bundle bnd = new Bundle();
            bnd.putString(ParamsAndConstants.MSG_TEXT, message);
            bnd.putInt(ParamsAndConstants.TYPE, action_id);
            msg.setData(bnd);
            handle.sendMessage(msg);
        }

        /*class JIFace {
            @JavascriptInterface
            public void print(String data) {
                data =""+data+"";
                System.out.println(data);
                //DO the stuff
                Toast toastErrorStartActivitySMS2 = Toast.
                        makeText(getActivity(),
                                data, Toast.LENGTH_LONG);
                toastErrorStartActivitySMS2.show();
            }
        }*/


        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView=null;
            orderButton=null;


            if(getArguments().getInt(ARG_SECTION_NUMBER)==1)    {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                textViewStatus = (TextView) rootView.findViewById(R.id.textViewStatus);
                orderButton = (Button)rootView.findViewById(R.id.orderButton);
                gpsDetectButton = (Button)rootView.findViewById(R.id.gpsDetectButton);
                cancelButton = (Button)rootView.findViewById(R.id.cancelButton);
                editTextFromAdres = (AutoCompleteTextView)rootView.findViewById(R.id.editTextFromAdr);
                editTextToAdres = (EditText)rootView.findViewById(R.id.editTextToAdr);
                clearBtn = (Button)rootView.findViewById(R.id.btn_clear);
                editTextFromAdres.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //here is your code
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                        // TODO Auto-generated method stub
                        if(s.toString().length()>2&&s.toString().length()<15) {
                            //placesTask = new PlacesTask();
                            //placesTask.execute(ParamsAndConstants.PLACES_DEFAULT + s.toString());
                            eTask = new GetEmpTask(getActivity());
                            eTask.execute(s.toString());
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // TODO Auto-generated method stub
                        lastAdr = editTextFromAdres.getText().toString();
                        hasMeGAdrDetecting=false;
                    }
                });
                clearBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_WAKE_UP_NEO,"---");
                        editTextFromAdres.setText("");
                    }
                });

                orderButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendInfoBroadcast(ParamsAndConstants.ID_ACTION_WAKE_UP_NEO,"---");
                            if(lastOrdersCount>0) {
                                Toast toastErrorStartActivitySMS2 = Toast.
                                        makeText(getActivity(),
                                                "Вами уже создан заказ!", Toast.LENGTH_LONG);
                                toastErrorStartActivitySMS2.show();
                            }
                            else {
                                if (editTextFromAdres.getText().toString().length() >= 3) {
                                    Message msg = new Message();
                                    msg.arg1 = ParamsAndConstants.MA_ORDERING;
                                    Bundle bnd = new Bundle();
                                    bnd.putString("msg_text", editTextFromAdres.getText().toString());
                                    bnd.putString("end_adr", editTextToAdres.getText().toString());
                                    msg.setData(bnd);
                                    handle.sendMessage(msg);
                                } else {
                                    Toast toastErrorStartActivitySMS2 = Toast.
                                            makeText(getActivity(),
                                                    "Длина адреса меньше 3 символов!", Toast.LENGTH_LONG);
                                    toastErrorStartActivitySMS2.show();
                                }
                            }
                        }
                    });
                gpsDetectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lastAdr = editTextFromAdres.getText().toString();
                        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_WAKE_UP_NEO,"---");
                        Message msg = new Message();
                        msg.arg1 = ParamsAndConstants.MA_GPS_DETECTING;
                        Bundle bnd = new Bundle();
                        bnd.putString("msg_text", "ddddd");
                        msg.setData(bnd);
                        handle.sendMessage(msg);
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_WAKE_UP_NEO,"---");
                        if(lastOrdersCount>0) {
                            Message msg = new Message();
                            msg.arg1 = ParamsAndConstants.MA_CANCELING;
                            Bundle bnd = new Bundle();
                            bnd.putString("msg_text", "ddddd");
                            msg.setData(bnd);
                            handle.sendMessage(msg);
                        }   else    {
                            Toast toastErrorStartActivitySMS2 = Toast.
                                    makeText(getActivity(),
                                            "Нет активных заказов!", Toast.LENGTH_LONG);
                            toastErrorStartActivitySMS2.show();
                        }
                    }
                });

            }
            else if (getArguments().getInt(ARG_SECTION_NUMBER)==2) {
                rootView = inflater.inflate(R.layout.map_layout, container, false);

                try {
                    map = (MapView) rootView.findViewById(R.id.map);
                    //map.setTileSource(TileSourceFactory.MAPNIK);
                    //map.setBuiltInZoomControls(true);
                    //map.setMultiTouchControls(true);
                    //Toast toastErrorStartActivitySMS2 = Toast.
                    //        makeText(getActivity(),
                    //                "OSM MAP SUCC!", Toast.LENGTH_LONG);
                    //toastErrorStartActivitySMS2.show();
                }   catch(Exception e)  {
                    Toast toastErrorStartActivitySMS2 = Toast.
                            makeText(getActivity(),
                                    "OSM MAP ERROR!"+e.getMessage(), Toast.LENGTH_LONG);
                    toastErrorStartActivitySMS2.show();
                }

            } else if(getArguments().getInt(ARG_SECTION_NUMBER)==3) {
                rootView = inflater.inflate(R.layout.hist_layout, container, false);
            }
            fragmentViev = rootView;
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (getArguments().getInt(ARG_SECTION_NUMBER)==2) {
                try {
                    map.setTileSource(TileSourceFactory.MAPNIK);
                    //map.setBuiltInZoomControls(true);
                    map.setMultiTouchControls(true);
                    map.getController().setZoom(15);
                    map.getController().setCenter(new GeoPoint(ParamsAndConstants.defLat ,
                            ParamsAndConstants.defLon));
                    //Toast toastErrorStartActivitySMS2 = Toast.
                    //        makeText(getActivity(),
                    //                "OSM MAP SUCC CREATE!", Toast.LENGTH_LONG);
                    //toastErrorStartActivitySMS2.show();
                }   catch(Exception e)  {
                    Toast toastErrorStartActivitySMS2 = Toast.
                            makeText(getActivity(),
                                    "OSM MAP ERROR!"+e.getMessage(), Toast.LENGTH_LONG);
                    toastErrorStartActivitySMS2.show();
                }
            }
        }

        /** A method to download json data from url */
        private String downloadUrl(String strUrl) throws IOException{
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try{
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuilder sb = new StringBuilder();

                String line = "";
                while( ( line = br.readLine()) != null){
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            }catch(Exception e){
                Log.d("Exception while downloading url", e.toString());
                //Toast toastErr = Toast.makeText(getActivity(),
                //        "Exception while downloading url "+e.toString(), Toast.LENGTH_LONG);
                //toastErr.show();
            }finally{
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }

        public class GetEmpTask extends AsyncTask<String, Void, ArrayList<Employee>> {

            private final WeakReference<Activity> activityWeakRef;
            private String fplace;

            public GetEmpTask(Activity context) {
                this.activityWeakRef = new WeakReference<Activity>(context);
            }

            @Override
            protected ArrayList<Employee> doInBackground(String... place) {
                fplace=place[0];
                ArrayList<Employee> employeeList = employeeDAO.getEmployees();
                //Log.d("employees", employeeList.toString());
                return employeeList;
            }

            @Override
            protected void onPostExecute(ArrayList<Employee> empList) {
                Log.d("employees", empList.toString());
                if (activityWeakRef.get() != null
                        && !activityWeakRef.get().isFinishing()) {
                    Log.d("employees", empList.toString());
                    employees = empList;
                    if (empList != null) {
                        if (empList.size() != 0) {
                            //employeeListAdapter = new EmpListAdapter(activity,
                            //         empList);
                            //employeeListView.setAdapter(employeeListAdapter);
                            List<HashMap<String, String>> result = new ArrayList();

                            String[] from =  { "description" };//new String[]
                            int[] to = new int[] { android.R.id.text1 };
                            for(int i=0;i<empList.size();i++)   {
                                if(empList.get(i).getName().indexOf(fplace,0)!=-1)
                                {
                                    boolean hasInRes=false;
                                    for(int k=0;k<result.size();k++) {
                                        if(result.get(k).get("description").equals(empList.get(i).getName()))
                                        {
                                            hasInRes=true;
                                            break;
                                        }
                                    }
                                    if(!hasInRes) {
                                        HashMap<String, String> hm = new HashMap<String, String>();
                                        hm.put("description", empList.get(i).getName());
                                        result.add(hm);
                                    }
                                }
                            }
                                //Log.d("result ", empList.get(i).getName());
                            // Creating a SimpleAdapter for the AutoCompleteTextView
                            if(result.size()>0) {
                                SimpleAdapter adapter = new SimpleAdapter(getActivity(), result, android.R.layout.simple_list_item_1, from, to);

                                // Setting the adapter
                                editTextFromAdres.setAdapter(adapter);
                                editTextFromAdres.showDropDown();
                            }
                        } else {
                            //Toast.makeText(activity, "No Employee Records",
                            //		Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }
        }

        // Fetches all places from GooglePlaces AutoComplete Web Service
        private class PlacesTask extends AsyncTask<String, Void, String>{
            ParserTask parserTask;

            @Override
            protected String doInBackground(String... place) {
                // For storing data from web service
                String data = "";

                // Obtain browser key from https://code.google.com/apis/console
                String key = "key="+ParamsAndConstants.gm_key;

                String input="";

                try {
                    input = "input=" + URLEncoder.encode(place[0], "utf-8");
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }

                // place type to be searched
                String types = "types=geocode";

                // Sensor enabled
                String sensor = "sensor=false";

                // Building the parameters to the web service
                String parameters = input+"&"+types+"&"+sensor+"&"+key;

                // Output format
                String output = "json";

                // Building the url to the web service
                String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

                try{
                    // Fetching the data from we service
                    data = downloadUrl(url);
                    //Toast toastErr = Toast.makeText(getActivity(),
                    //        "Background Task PlacesTask complete  "+data, Toast.LENGTH_LONG);
                    //toastErr.show();
                    //Log.d("================",data);
                    //System.out.print("===================");
                }catch(Exception e){
                    Log.d("Background Task",e.toString());
                    //Toast toastErr = Toast.makeText(getActivity(),
                    //        "Background Task PlacesTask "+e.toString(), Toast.LENGTH_LONG);
                    //toastErr.show();
                }
                return data;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                // Creating ParserTask
                parserTask = new ParserTask();

                // Starting Parsing the JSON string returned by Web Service
                parserTask.execute(result);
            }
        }
        /** A class to parse the Google Places in JSON format */
        private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>> {

            JSONObject jObject;

            @Override
            protected List<HashMap<String, String>> doInBackground(String... jsonData) {

                List<HashMap<String, String>> places = null;

                PlaceJSONParser placeJsonParser = new PlaceJSONParser();

                try{
                    jObject = new JSONObject(jsonData[0]);

                    // Getting the parsed data as a List construct
                    places = placeJsonParser.parse(jObject);

                }catch(Exception e){
                    Log.d("Exception", e.toString());
                    //Toast toastErr = Toast.makeText(getActivity(),
                    //        "Background Task ParserTask "+e.toString(), Toast.LENGTH_LONG);
                    //toastErr.show();
                }
                return places;
            }

            @Override
            protected void onPostExecute(List<HashMap<String, String>> result) {

                String[] from =  { "description" };//new String[]
                int[] to = new int[] { android.R.id.text1 };
                for(int i=0;i<result.size();i++)
                    Log.d("result ", result.get(i).get("description").toString());
                // Creating a SimpleAdapter for the AutoCompleteTextView
                SimpleAdapter adapter = new SimpleAdapter(getActivity(), result, android.R.layout.simple_list_item_1, from, to);

                // Setting the adapter
                editTextFromAdres.setAdapter(adapter);
                editTextFromAdres.showDropDown();
            }
        }
    }

    /*private class AutoCompleteAdapter extends SimpleAdapter {

        public AutoCompleteAdapter(Context context, List<Map<String, Object>> layout, int c, String[] from, int[] to) {
            super(context, layout, c, from, to);


            //this.
            setToStringConverter(new CursorToStringConverter() {
                @Override
                public CharSequence convertToString(Cursor item) {
                    return item.getString(item.getColumnIndex(DESIRED_COLUMN_NAME));
                }
            });
        }
    }*/

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        PlaceholderFragment firstTab=null;

        @Override
        public Fragment getItem(int position) {
            Fragment fr=null;
            if(position==2) {
                try {
                    fr = new EmpListFragment();
                    //emplf = (EmpListFragment)fr;
                } catch (Exception e)   {
                    fr = PlaceholderFragment.newInstance(position + 1);
                    Toast toastErrorStartActivitySMS2 = Toast.
                            makeText(getBaseContext(),
                                    "EmpListFragment "+e.getMessage(), Toast.LENGTH_LONG);
                    toastErrorStartActivitySMS2.show();
                }
            } else {
                fr = PlaceholderFragment.newInstance(position + 1);
            }
            if(position==0)
                firstTab=(PlaceholderFragment)fr;
            return fr;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            /*Message msg = new Message();
            msg.arg1 = ParamsAndConstants.SHOW_MESSAGE_TOAST;
            Bundle bnd = new Bundle();
            bnd.putString("msg_text", "instantiateItem"+position+"==="+this.getItemPosition());
            msg.setData(bnd);
            handle.sendMessage(msg);*/
            return super.instantiateItem(container, position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ЗАКАЗ";
                case 1:
                    return "КАРТА";
                case 2:
                    return "ИСТОРИЯ";
            }
            return null;
        }
    }

}
