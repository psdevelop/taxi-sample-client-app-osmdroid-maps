package ru.psdevelop.tdclientapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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
import android.text.format.DateFormat;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import javax.net.ssl.HttpsURLConnection;
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
import java.util.Calendar;
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
    static boolean backgroundCoordSearchCompleted = false;
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
    static boolean useSMSInRegistration = false;
    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 25000;
    public static final int CONNECTION_TIMEOUT = 25000;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    static WebView wv;
    static MapView map;
    static TextView textViewStatus=null;
    static TextView mapStatusView = null, driverInfoMapView = null;
    static TextView sectDetectInfo = null;
    static String ordersInfo="";
    static RadioGroup tarifPlanChoice = null;
    static Context firstFragmentContext = null;
    static String tariffPlanName = "";
    static int tariffPlanId = 0;
    static boolean isPermissionAllowed = false;
    static String districtGeo = "";
    static boolean useFineLocation = true;
    static boolean onlyGPSLocation = false;
    static String driverPhone = "";
    static EditText dateEdit;
    static String lastSheduleTime = "";

    public void requestPermissions(String[] PERMISSIONS) {
        ActivityCompat.requestPermissions(this, PERMISSIONS,
                TDClientService.TDC_PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    public static boolean getUseFineLocation() {
        return useFineLocation;
    }

    public void configurePermissionsRequest(boolean denyLocation, boolean denyStorage) {

        if (denyLocation && denyStorage) {
            //showMyMsg("1");
            if (getUseFineLocation()) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                });
            } else {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        //Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                });
            }
        }
        else if (denyLocation) {
            //showMyMsg("2");
            if (getUseFineLocation()) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                });
            } else {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        //Manifest.permission.ACCESS_FINE_LOCATION
                });
            }
        } else {
            //showMyMsg("3");
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            });
        }
    }

    public boolean isGPSPermAllowed() {
        return ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED || !getUseFineLocation());
    }

    public void checkGPSPermission() {
        // Here, thisActivity is the current activity
        //
        final boolean denyLocation =
                ContextCompat.checkSelfPermission(this,
                      Manifest.permission.ACCESS_COARSE_LOCATION)
                      != PackageManager.PERMISSION_GRANTED ||
                (ContextCompat.checkSelfPermission(this,
                      Manifest.permission.ACCESS_FINE_LOCATION)
                      != PackageManager.PERMISSION_GRANTED && getUseFineLocation()),
                denyStorage = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED;
        if (denyLocation || denyStorage) {
            // Should we show an explanation?
            if (    ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                          Manifest.permission.ACCESS_FINE_LOCATION) && getUseFineLocation()) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        !denyLocation) &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        !denyStorage)  ) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Внимание")
                        .setMessage("Вами не разрешены доступы к геолокации и/или памяти. Функции карт и/или определения местоположения не будут работать пока вы не разрешите их!")
                        // кнопка "Yes", при нажатии на которую приложение закроется
                        .setPositiveButton("Ок",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int whichButton)
                                    {
                                        configurePermissionsRequest(denyLocation, denyStorage);
                                    }
                                })
                        .show();

            } else {

                // No explanation needed, we can request the permission.
                configurePermissionsRequest(denyLocation, denyStorage);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case TDClientService.TDC_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (    (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    ||
                        (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                        ||
                            (grantResults.length == 3
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED
                            && grantResults[2] == PackageManager.PERMISSION_GRANTED) ) {

                    /*AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("ВЫХОД ИЗ ПРОГРАММЫ")
                            .setMessage("При смене разрешения необходимо заново войти в приложение!")
                            // кнопка "Yes", при нажатии на которую приложение закроется
                            .setPositiveButton("Ок",
                                    new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int whichButton)
                                        {
                                            finish();
                                        }
                                    })
                            .show();*/
					finish();
                    startActivity(getIntent());
                    sendInfoBroadcast(ParamsAndConstants.ID_ACTION_START_GPS_DETECTING, "---");
                    backgroundCoordSearchCompleted = false;
                    isPermissionAllowed = true;
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("ПРЕДУПРЕЖДЕНИЕ")
                            .setMessage("Вами не разрешены доступы к геолокации и/или памяти. Функции карт и/или определения местоположения не будут работать пока вы не разрешите их в настройках! Необходимо заново войти в приложение! ")
                            // кнопка "Yes", при нажатии на которую приложение закроется
                            .setPositiveButton("Ок",
                                    new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int whichButton)
                                        {
                                            openApplicationSettings();
                                        }
                                    })
                            .show();
                }
                return;
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, TDClientService.PERMISSION_APP_CODE);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TDClientService.PERMISSION_APP_CODE) {

        }
        super.onActivityResult(requestCode, resultCode, data);
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
        //checkGPSPermission();
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
                if (tab.getPosition() == 1) {
                    if (hasMeGPSDetecting || hasMeGAdrDetecting)
                        showMeOnMap();
                    else
                        startGPSCoordsProcessing(true);
                }
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
                    String orderStatus = msg.getData().
                            getString(ParamsAndConstants.MSG_TEXT);
                    setTextViewStatus(orderStatus, orderStatus, "");
                }
                else if (msg.arg1 == ParamsAndConstants.SHOW_STATUS_STRING) {
                    try {
                        String status = msg.getData().getString("msg_text");
                        setTextViewStatus(status, status, "");
                    }   catch(Exception e)  {
                        showMyMsg("SHOW_STATUS_STRING!"+e.getMessage());
                    }
                }
                else if (msg.arg1 == ParamsAndConstants.SHOW_SECTOR_DETECT_INFO) {
                    /**
                     * detectData = {
                     *       'sectorId': sectorId,
                     *       'sectorName': sectorName,
                     *       'districtId': districtId,
                     *       'districtName': districtName,
                     *       'companyId': companyId,
                     *       'companyName': companyName,
                     *       'districtGeo': districtGeo,
                     *     };
                     */
                    try {
                        JSONObject resultJson = new JSONObject(msg.getData().
                                getString("msg_text"));
                        setSectDetectInfo("Фирма: " + resultJson.getString("companyName") +
                                "Район: " + resultJson.getString("districtName") +
                                "Сектор: " + resultJson.getString("sectorName"));

                        if (resultJson.has("resultJson")) {
                            districtGeo = resultJson.getString("districtGeo");
                        }
                    }   catch(Exception e)  {
                        showMyMsg("Неудачное чтение данных определения сектора!"+e.getMessage());
                    }
                }
                else if (msg.arg1 == ParamsAndConstants.SHOW_TARIF_AND_OPTIONS) {
                    /**
                     * {"command":"to_lst","t_cnt":"10","tid0":"1","tn0":"ГОРОД-Ст","tmt0":"10.00","txt0":"20.00",
                     * "ttpi0":"2","tdip0":"0","tstds0":"0","tdpt0":"0.00","tspt0":"0.00","tshn0":"Гст","otarid0":"3",
                     * "otplid0":"2","trarcnt0":"4","tralat0_0":"44.87430","tralon0_0":"37.29760","tralat0_1":
                     * "44.90140","tralon0_1":"37.28180","tralat0_2":"44.90750","tralon0_2":"37.35290","tralat0_3":"44.87700",
                     * "tralon0_3":"37.33400","tid1":"2","tn1":"ПРИГОРОД-Ст","tmt1":"12.00","txt1":"22.00","ttpi1":"2",
                     * "tdip1":"0","tstds1":"0","tdpt1":"0.00","tspt1":"0.00","tshn1":"Пгст","otarid1":"3","otplid1":"2","
                     * trarcnt1":"0","tid2":"3","tn2":"МЕЖГОРОД-Ст","tmt2":"17.00","txt2":"27.00","ttpi2":"2","tdip2":"0",
                     * "tstds2":"0","tdpt2":"0.00","tspt2":"0.00","tshn2":"Мгст","otarid2":"-1","otplid2":"-1","trarcnt2":"0",
                     * "tid3":"4","tn3":"ГОРОД-Пр","tmt3":"10.00","txt3":"20.00","ttpi3":"3","tdip3":"0","tstds3":"0","tdpt3":
                     * "0.00","tspt3":"0.00","tshn3":"Гпр","otarid3":"-1","otplid3":"-1","trarcnt3":"0","tid4":"5","tn4":"
                     * ПРИГОРОД-Пр","tmt4":"10.00","txt4":"20.00","ttpi4":"3","tdip4":"0","tstds4":"0","tdpt4":"0.00",
                     * "tspt4":"0.00","tshn4":"Пгпр","otarid4":"-1","otplid4":"-1","trarcnt4":"0","tid5":"6","tn5":"МЕЖГОРОД-Пр",
                     * "tmt5":"10.00","txt5":"20.00","ttpi5":"3","tdip5":"0","tstds5":"0","tdpt5":"0.00","tspt5":"0.00","
                     * tshn5":"Мгпр","otarid5":"-1","otplid5":"-1","trarcnt5":"0","tid6":"7","tn6":"ГОРОД-Эк","tmt6":"10.00",
                     * "txt6":"20.00","ttpi6":"1","tdip6":"0","tstds6":"0","tdpt6":"0.00","tspt6":"0.00","tshn6":"Гэк","
                     * otarid6":"-1","otplid6":"-1","trarcnt6":"0","tid7":"8","tn7":"ПРИГОРОД-Эк","tmt7":"10.00","txt7":"20.00",
                     * "ttpi7":"1","tdip7":"0","tstds7":"0","tdpt7":"0.00","tspt7":"0.00","tshn7":"Пгэк","otarid7":"-1",
                     * "otplid7":"-1","trarcnt7":"0","tid8":"9","tn8":"МЕЖГОРОД-Эк","tmt8":"10.00","txt8":"20.00",
                     * "ttpi8":"1","tdip8":"0","tstds8":"0","tdpt8":"0.00","tspt8":"0.00","tshn8":"Мгэк","otarid8":"-1",
                     * "otplid8":"-1","trarcnt8":"0","tid9":"12","tn9":"село1","tmt9":"11.00","txt9":"18.00","ttpi9":"2","
                     * tdip9":"0","tstds9":"0","tdpt9":"0.00","tspt9":"0.00","tshn9":"сл1","otarid9":"3","otplid9":"2",
                     * "trarcnt9":"0","op_cnt":"13","oid0":"1","on0":"ПЕРЕГРУЗ-Ст","oscf0":"1.50","oscm0":"0.00","otpi0":"2",
                     * "oshn0":"Пст","oid1":"2","on1":"БАГАЖ-Ст","oscf1":"1.00","oscm1":"50.00","otpi1":"2","oshn1":"Бст",
                     * "oid2":"3","on2":"ДОСТАВКА-Ст","oscf2":"1.00","oscm2":"300.00","otpi2":"2","oshn2":"Дст","oid3":"4","
                     * on3":"ЖИВОТНЫЕ-Ст","oscf3":"1.00","oscm3":"0.00","otpi3":"2","oshn3":"Жст","oid4":"5","on4":
                     * "ПЕРЕГРУЗ-Пр","oscf4":"1.00","oscm4":"0.00","otpi4":"3","oshn4":"ППр","oid5":"6","on5":"БАГАЖ-Пр","
                     * oscf5":"1.00","oscm5":"0.00","otpi5":"3","oshn5":"БПр","oid6":"7","on6":"ДОСТАВКА-Пр","oscf6":"1.00","
                     * oscm6":"0.00","otpi6":"3","oshn6":"ДПр","oid7":"8","on7":"ЖИВОТНЫЕ-Пр","oscf7":"1.00","oscm7":"0.00",
                     * "otpi7":"3","oshn7":"ЖПр","oid8":"9","on8":"ПЕРЕГРУЗ-эк","oscf8":"1.00","oscm8":"0.00","otpi8":"1",
                     * "oshn8":"Пэк","oid9":"10","on9":"БАГАЖ-эк","oscf9":"1.00","oscm9":"0.00","otpi9":"1","oshn9":"Бэк","
                     * oid10":"11","on10":"ДОСТАВКА-эк","oscf10":"1.00","oscm10":"0.00","otpi10":"1","oshn10":"Дэк","oid11":"12","
                     * on11":"ЖИВОТНЫЕ-эк","oscf11":"1.00","oscm11":"0.00","otpi11":"1","oshn11":"Жэк","oid12":"13","on12":
                     * "ТОЧКА-Ст","oscf12":"1.00","oscm12":"50.00","otpi12":"2","oshn12":"Тст",
                     * "tpl_cnt":"3","tpid0":"1","tpn0":"ЭКОНОМ","tpshn0":"эк","tpid1":"2","tpn1":"СТАНДАРТ","tpshn1":"ст",
                     * "tpid2":"3","tpn2":"ПРЕМИУМ","tpshn2":"пр","msg_end":"ok"}
                     */
                    try {
                        JSONObject resultJson = new JSONObject(msg.getData().
                            getString("msg_text"));
                        int tplansCnt = resultJson.getInt("tpl_cnt");
                        tariffPlanName = "";
                        tariffPlanId = 0;
                        tarifPlanChoice.removeAllViews();
                        //showMyMsg("cnt=" + tplansCnt);
                        for (int i = 0; i < tplansCnt; i++) {
                            RadioButton newRadioButton = new RadioButton(firstFragmentContext);
                            newRadioButton.setText(resultJson.getString("tpn" + i));
                            newRadioButton.setId(resultJson.getInt("tpid" + i));
                            tarifPlanChoice.addView(newRadioButton);
                        }

                        tarifPlanChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                        {
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                // checkedId is the RadioButton selected
                                RadioButton rb = (RadioButton)findViewById(checkedId);
                                //textViewChoice.setText("You Selected " + rb.getText());
                                tariffPlanName = rb.getText().toString();
                                tariffPlanId = rb.getId();
                                Toast.makeText(getApplicationContext(), rb.getText() + "::" + rb.getId(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }   catch(Exception e)  {
                        showMyMsg("Неудачное чтение данных тарифов и опций!" + e.getMessage());
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
                            String ords_dt = "", mapOrderInfo = "",
                                mapDriverInfo = "";
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

                                if(resultJson.has("dphn"+i)) {
                                    driverPhone = resultJson.getString("dphn"+i);
                                }

                                if(i==0) {
                                    try {
                                        //showMyMsg(resultJson.getString("odt"+i));
                                        showGMAddressIfEmpty(resultJson.getString("odt"+i).replace("(ONLINE)",""));
                                    } catch (Exception e) { }
                                }

                                if(i==0) {
                                    try {
                                        showEndAddressIfEmpty(resultJson.getString("oena"+i).replace("(ONLINE)",""));
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
                                    ords_dt = ords_dt + " извините, нет свободных машин";
                                }
                                else {
                                    if (rcst == 2)
                                        ords_dt = ords_dt + " в обработке";
                                    if (resultJson.has("ors" + i)) {
                                        ordersInfo = ordersInfo + "status" + resultJson.getInt("ors" + i);
                                        if (resultJson.getInt("ors" + i) == 0)
                                            ords_dt = ords_dt + " ищем машину";
                                        if (resultJson.getInt("ors" + i) == 8) {
                                            ords_dt = ords_dt + " за Вами отправлена машина";
                                            if ((!resultJson.has("opl" + i) || !(resultJson.getInt("opl" + i) == 1)) &&
                                                    resultJson.has("wtr" + i)) {
                                                ords_dt = ords_dt + " (вр. ожидания " + resultJson.getInt("wtr" + i) + " мин.)";
                                            }
                                        }
                                        if (resultJson.has("opl" + i))
                                        if (resultJson.getInt("opl" + i) == 1 && resultJson.getInt("ors" + i) == 8) {
                                            ords_dt = ords_dt + " ожидает выходите";
                                            ordersInfo = ordersInfo + "opl";
                                        }
                                        if (resultJson.getInt("ors" + i) == 26)
                                            ords_dt = ords_dt + " дан отчет " + resultJson.getString("osumm" + i);
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

                                        if (resultJson.has("oppr" + i)) {
                                            if (resultJson.getString("oppr" + i).length() > 0) {
                                                ords_dt = ords_dt + " Стоимость: " + resultJson.getString("oppr" + i);
                                            }
                                        }

                                        mapOrderInfo = ords_dt;

                                        if (resultJson.has("dphn" + i)) {
                                            if (resultJson.getString("dphn" + i).length() > 0) {
                                                String driverPhone = resultJson.getString("dphn" + i);
                                                ordersInfo = ordersInfo + driverPhone;
                                                driverPhone = " Телефон водителя: " + driverPhone;
                                                ords_dt += driverPhone;
                                                mapDriverInfo += driverPhone;
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
                                setTextViewStatus("У вас Заказов всего " + resultJson.getString("ocn")+": " + ords_dt,
                                        mapOrderInfo.length() > 0 ? mapOrderInfo : "Нет активных заказов",
                                        mapDriverInfo);
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
                    sendOrderRequest(msg.getData().getString("msg_text"),
                            msg.getData().getString("end_adr"),
                            msg.getData().getString("comment"),
                            msg.getData().getString("shedule_date"),
                            msg.getData().getInt("tariff_plan_id"));
                    hasMAOrdering=true;
                    hasOrderRequest=true;
                    hasMAOrderAdr=msg.getData().getString("msg_text");
                }   else if(msg.arg1 == ParamsAndConstants.MA_CANCELING)   {
                    sendOrderCancelRequest();
                }   else if(msg.arg1 == ParamsAndConstants.SHOW_GM_ADDRESS)   {
                    String gmAddress = msg.getData().getString("msg_text");
                    showGMAddress(gmAddress);
                    //showMyMsg(gmAddress);
                    if (msg.getData().containsKey("is_reverse_geocode") &&
                            msg.getData().getBoolean("is_reverse_geocode") &&
                            startMarker != null) {
                        setStartMarker(gmAddress);
                    }
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
                            handle.sendMessage(msg);

                        } catch (Exception ex) {
                            showMyMsg("Ошибка ID_ACTION_SHOW_STATUS_INFO: " + ex);
                        }
                        break;
                    case ParamsAndConstants.ID_ACTION_SHOW_SECTOR_DETECT_INFO:
                        try {
                            Message msg = new Message();
                            msg.arg1 = ParamsAndConstants.SHOW_SECTOR_DETECT_INFO;
                            Bundle bnd = new Bundle();
                            bnd.putString("msg_text", intent.getStringExtra(ParamsAndConstants.MSG_TEXT));
                            msg.setData(bnd);
                            handle.sendMessage(msg);

                        } catch (Exception ex) {
                            showMyMsg("Ошибка ID_ACTION_SHOW_SECTOR_DETECT_INFO: " + ex);
                        }
                        break;
                    case ParamsAndConstants.ID_ACTION_SHOW_TARIF_OPTIONS:
                        try {
                            Message msg = new Message();
                            msg.arg1 = ParamsAndConstants.SHOW_TARIF_AND_OPTIONS;
                            Bundle bnd = new Bundle();
                            bnd.putString("msg_text", intent.getStringExtra(ParamsAndConstants.MSG_TEXT));
                            msg.setData(bnd);
                            handle.sendMessage(msg);

                        } catch (Exception ex) {
                            showMyMsg("Ошибка ID_ACTION_SHOW_SECTOR_DETECT_INFO: " + ex);
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
                            if (activeCoordSearch || !backgroundCoordSearchCompleted) {

                                if (!activeCoordSearch) {
                                    backgroundCoordSearchCompleted = true;
                                }
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

                                if (intent.getStringExtra("provider").equals("net") && !onlyGPSLocation) {
                                    onlyGPSLocation = true;

                                    showNetDetectAlert();
                                }
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

    public void showNetDetectAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("ПРЕДУПРЕЖДЕНИЕ")
                .setMessage("Определена координата при помощи сетей, если она не точна, " +
                        "нажмите Определить местоположение чтобы найти ее через GPS!")
                // кнопка "Yes", при нажатии на которую приложение закроется
                .setPositiveButton("Ок",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {

                            }
                        })
                .show();
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

    public void showEndAddressIfEmpty(String txt)   {
        try {
            if(mSectionsPagerAdapter.firstTab.editTextToAdres.getText().length()<=0 ||
                    !mSectionsPagerAdapter.firstTab.editTextToAdres.getText().equals(txt))
                mSectionsPagerAdapter.firstTab.editTextToAdres.setText(txt);
        }   catch(Exception e)  {
            showMyMsg("showEndAddress: "+e.getMessage());
        }
    }

    public void setTextViewStatus(String txt, String orderInf, String driverInfo)   {
        try {
            textViewStatus.setText(txt);
        }   catch(Exception e)  {
            showMyMsg("setTextViewStatus: "+e.getMessage());
        }
        try {
            mapStatusView.setText(orderInf);
            driverInfoMapView.setText(driverInfo);
        }   catch(Exception e)  {
            showMyMsg("setMapsViewStatus error: "+e.getMessage());
        }
    }

    public void setSectDetectInfo(String txt)   {
        try {
            sectDetectInfo.setText(txt);
        }   catch(Exception e)  {
            showMyMsg("setSectDetectInfo: "+e.getMessage());
        }
    }

    public void sendOrderRequest(String start_adr, String end_adr, String comment, String sheduleDate, int tarPlanId)  {
        Intent intent = new Intent(INFO_ACTION);
        intent.putExtra(ParamsAndConstants.TYPE, ParamsAndConstants.ID_ACTION_GO_ORDERING);
        intent.putExtra(ParamsAndConstants.MSG_TEXT, start_adr);
        intent.putExtra("end_adr", end_adr);
        intent.putExtra("comment", comment);
        intent.putExtra("shedule_date", sheduleDate);
        intent.putExtra("tariff_plan_id", tarPlanId);
        sendBroadcast(intent);
    }

    public void sendOrderCancelRequest()  {
        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_GO_ORDER_CANCELING, "---");
    }

    public static String lastAdr="";
    public static String lastComment = "";

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

    public GeoPoint setStartMarker(String title) {
        if(startMarker!=null) {
            try {
                startMarker.getInfoWindow().close();
            } catch (Exception e) {

            }
            try {
                map.getOverlays().remove(startMarker);
            } catch (Exception e) {

            }
        }
        startMarker = null;

        GeoPoint startPoint = null;
        if(lastLat>0 && lastLon>0) {
            startPoint = new GeoPoint(lastLat, lastLon);

            //0. Using the Marker overlay
            startMarker = new Marker(map);
            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setTitle(title.length() > 0 ? title : (hasMeGAdrDetecting ? lastAdr : "Вы здесь!"));
            startMarker.setIcon(getResources().getDrawable(R.drawable.person).mutate());
            //startMarker.setImage(getResources().getDrawable(R.drawable.ic_launcher));
            startMarker.setInfoWindow(new MarkerInfoWindow(R.layout.bonuspack_bubble_black, map));
            startMarker.setDraggable(true);
            //startMarker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer());
            map.getOverlays().add(startMarker);
            startMarker.showInfoWindow();
        }

        return startPoint;
    }

    public void showMeOnMap()   {
        try {

            if (lastLat == ParamsAndConstants.defLat &&
                    lastLon == ParamsAndConstants.defLon) {
                //showMyMsg("Используются координаты по умолчанию: неточный адрес или не работает GPS!");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("ВНИМАНИЕ")
                        .setMessage("Используются координаты нас. пункта по умолчанию: неточный адрес или не работает GPS!")
                        // кнопка "Yes", при нажатии на которую приложение закроется
                        .setPositiveButton("Ок",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int whichButton)
                                    {
                                    }
                                })
                        .show();
            }

            if(!hasMeGAdrDetecting && lastLat>0 && lastLon>0)
                sendGeocodeHTTPRequest( lastLat, lastLon);

            if(startMarker!=null)
                try {
                    startMarker.getInfoWindow().close();
                } catch(Exception e) {

                }
                try {
                    map.getOverlays().remove(startMarker);
                } catch(Exception e) {

                }
            startMarker = null;

            if(driverMarker!=null)
                try {
                    driverMarker.getInfoWindow().close();
                } catch(Exception e) {

                }
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
                startMarker.setIcon(getResources().getDrawable(R.drawable.person).mutate());
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
                driverMarker.setIcon(getResources().getDrawable(R.drawable.taxi2).mutate());
                //driverMarker.setImage(getResources().getDrawable(R.drawable.ic_launcher));
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
                    BoundingBox oBB = new BoundingBox(latMax+0.01, lonMax+0.01, latMin-0.01, lonMin-0.01);
                    map.zoomToBoundingBox(oBB, false);
                }

            } else {
                if (startPoint!=null) {
                    mapController.setCenter(startPoint);
                }
            }

            //showMyMsg("OSM MAP SUCC!");
        }   catch(Exception e)  {
            showMyMsg("Ошибка отрисовки карты!"+e.getMessage());
        }

    }

    public void startGPSCoordsProcessing(boolean gadr_alternative) {
        final boolean gadraa = gadr_alternative;
        if(lastAdr.length()>2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Введен адрес Откуда!").setMessage("В поле откуда введен адрес, искать координату через GPS? Нет - искать по адресу.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startGPSCoordsProcessingInc(gadraa, true);
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            //sendReverseGeocodeHTTPRequest(tgadr);
                            startGPSDetectWithAddr(gadraa);
                        }
                    }).show();
        }   else {
            startGPSCoordsProcessingInc(gadr_alternative, false);
        }
    }

    public void startGPSDetectWithAddr(boolean gadr_alternative) {
        final boolean gadraa = gadr_alternative;
        final String regionAddr = districtGeo.length() > 0 ? districtGeo : ParamsAndConstants.REGION_DEFAULT;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поиск по адресу").setMessage("Искать в регионе '" + regionAddr + "'")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        //sendReverseGeocodeHTTPRequest(ParamsAndConstants.REGION_DEFAULT+" "+tgadr);
                        lastAdr = regionAddr + lastAdr;
                        startGPSCoordsProcessingInc(gadraa, false);
                    }
                })
                .setNegativeButton("Искать везде", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        //sendReverseGeocodeHTTPRequest(tgadr);
                        startGPSCoordsProcessingInc(gadraa, false);
                    }
                }).show();
    }

    public void startGPSCoordsProcessingInc(boolean gadr_alternative, boolean dontGetCoordByAdr) {
        //Создаем ProgressDialog
        Indicator = new ProgressDialog(this);
        //Настраиваем для ProgressDialog название его окна:
        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_START_GPS_DETECTING, "---");
        if(gadr_alternative && !dontGetCoordByAdr)
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
                                    String phoneNumber = input_text.getText().toString();

                                    if (phoneNumber.length() != 10) {
                                        showMyMsg("Длина номера не равна 10!");
                                        phoneDlgIsOpened=false;
                                        return;
                                    }

                                    if (useSMSInRegistration) {
                                        if (isSMSRegsLimitOverload()) {
                                            showMyMsg("Превышено количество попыток подтверждения!");
                                            return;
                                        }

                                        checkSMSRegistrationCode(phoneNumber);
                                        return;
                                    }
                                    phoneDlgIsOpened=false;

                                    try {
                                        SharedPreferences.Editor edt = prefs.edit();
                                        edt.putString("example_text", phoneNumber);
                                        edt.commit();
                                        checkGPSPermission();
                                    } catch (Exception pex) {
                                        showMyMsg("Неудачное присваивание настроек PHONE_NUM от клиента! " +
                                                pex.getMessage());
                                    }

                                }
                            }).show();

        }	catch(Exception e)	{
            showMyMsg("Ошибка вывода диалога: " + e.getMessage());
        }

    }

    public boolean isSMSRegsLimitOverload() {
        try {
            int attemptsCount = prefs.getInt(
                    "sms_regs_check_attempts_count", 0);
            boolean limitOverload = attemptsCount >= 3;
            if (!limitOverload) {
                SharedPreferences.Editor edt = prefs.edit();
                edt.putInt("sms_regs_check_attempts_count", attemptsCount + 1);
                edt.commit();
            }
            return limitOverload;
        } catch (Exception e) { }
        return false;
    }

    public String getRegistrationCode() {
        return  "" + (1000 + (int) (Math.random()*(9999 - 1000)));
    }

    public void checkSMSRegistrationCode(String phoneNumber) {
        final String regCode = getRegistrationCode();
        String password = "";
        String login = "";
        String stringUrl = "https://smsc.ru/sys/send.php?login=" + login +
                "&psw=" + password + "&phones=+7" + phoneNumber + "&mes=" +
                "Kod: " + regCode;

        sendHttpRequest(stringUrl);

        try	{
            AlertDialog.Builder inpBuilder = new AlertDialog.Builder(this);

            final EditText inputText = new EditText(this);
            inputText.setInputType(InputType.TYPE_CLASS_NUMBER);
            final String checkPhoneNumber = phoneNumber;

            inpBuilder.setView(inputText);
            inpBuilder.setTitle("Введите код подтверждения")
                    .setMessage("Подтверждение номера")
                    // кнопка "Yes", при нажатии на которую приложение закроется
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    phoneDlgIsOpened=false;
                                    if (!inputText.getText().toString().equals(regCode)) {
                                        showMyMsg("Неверный код подтверждения!");
                                        return;
                                    }

                                    try {
                                        SharedPreferences.Editor edt = prefs.edit();
                                        edt.putString("example_text", checkPhoneNumber);
                                        edt.commit();
                                        checkGPSPermission();
                                    } catch (Exception pex) {
                                        showMyMsg("Неудачное присваивание настроек PHONE_NUM от клиента! " +
                                                pex.getMessage());
                                    }


                                }
                            }).show();

        }	catch(Exception e)	{
            showMyMsg("Ошибка вывода диалога: " + e.getMessage());
        }
    }

    public void sendHttpRequest(String stringUrl) {
        final String requestUrl = stringUrl;
        Runnable httpRunnable = new Runnable() {

            public void showMsg(String msgText)    {
                Message msg = new Message();
                msg.arg1 = ParamsAndConstants.SHOW_MESSAGE_TOAST;
                Bundle bnd = new Bundle();
                bnd.putString("msg_text", msgText);
                msg.setData(bnd);
                handle.sendMessage(msg);
            }

            @Override
            public void run(){
                try {
                    URL obj = new URL(requestUrl);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");

                    int responseCode = con.getResponseCode();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                }
                catch (Exception e) {
                    showMsg("Ошибка HTTP-запроса! "+e.getMessage());
                }
            }
        };

        new Thread(httpRunnable).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGPSPermAllowed()) {
            sendInfoBroadcast(ParamsAndConstants.ID_ACTION_START_GPS_DETECTING, "---");
        }
        //this.checkGPSPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        phoneDlgIsOpened=false;
        try {
            Intent i = new Intent(getBaseContext(), TDClientService.class);
            startService(i);
            //startForegroundService(i);
            if (prefs.getString("example_text", "").length() == 10) {
                this.checkGPSPermission();
            }
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
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
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
        final String addres = "http://search.maps.sputnik.ru/search/addr?q=" +
                URLEncoder.encode(adress) + "&addr_limit=1";

        if(adress.length()>4)
            new Thread(new Runnable() {

                public void parseAnswer(String data) {
                    try {
                        JSONObject resultJson = (new JSONObject(data));

                        JSONArray resultJsonArray = resultJson.getJSONObject("result")
                                .getJSONArray("address").getJSONObject(0)
                                .getJSONArray("features").getJSONObject(0)
                                .getJSONObject("geometry").getJSONArray("geometries")
                                .getJSONObject(0).getJSONArray("coordinates");

                        if (resultJsonArray.length() == 2 && activeCoordSearch) {
                            lastLon = resultJsonArray.getDouble(0);
                            lastLat = resultJsonArray.getDouble(1);
                        } else {
                            showMsg("Не найдена координата для введенного адреса 1!" +
                                    resultJsonArray.getString(1) + "+" +
                                    resultJsonArray.getString(0) + "=" +
                                    resultJsonArray.length());
                        }

                        if (lastLat > 0 && lastLon > 0 && activeCoordSearch) {
                            Intent bintent = new Intent(INFO_ACTION);
                            bintent.putExtra(ParamsAndConstants.TYPE, ParamsAndConstants.ID_ACTION_SEND_CCOORDS);
                            bintent.putExtra("clat", lastLat);
                            bintent.putExtra("clon", lastLon);
                            sendBroadcast(bintent);
                            hasMeGAdrDetecting = true;
                            activeCoordSearch = false;
                            coordSearchDetectFromAdr = true;
                            Message msg = new Message();
                            msg.arg1 = ParamsAndConstants.SHOW_COORDS_INFO;
                            Bundle bnd = new Bundle();
                            bnd.putString("msg_text", "===");
                            msg.setData(bnd);
                            handle.sendMessage(msg);
                        } else {
                            showMsg("Не найдена координата для введенного адреса!");
                        }

                    } catch (Exception e) {
                        showMsg("Ошибка парсинга ответа Sputnik Geocoder" + e.getMessage());
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

                @Override
                public void run(){
                    try {
                        String url = addres;

                        URL obj = new URL(url);
                        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                        // optional default is GET
                        con.setRequestMethod("GET");

                        //add request header
                        con.setRequestProperty("User-Agent", "Dalvik");

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
                            parseAnswer(response.toString());
                        } catch (Exception ex) {
                            showMsg("Парсинг ответа обратного запроса Sputnik! "+ex.getMessage());
                        }
                    }
                    catch (Exception e) {
                        showMsg("Парсинг обратного запроса геокодера Sputnik! "+e.getMessage());
                    }
                }
            }).start();
    }

    public void sendReverseGeocodeHTTPRequestNominatim(String adress)	{
        final String addres = "https://nominatim.openstreetmap.org/search/" + URLEncoder.encode(adress) +
                "?format=json&addressdetails=1&limit=1&polygon_svg=1";
        showMyMsg(addres);
        if(adress.length()>4)
            new Thread(new Runnable() {

                public void parseAnswer(String data) {
                    try {
                        JSONObject resultJson = (new JSONArray(data)).getJSONObject(0);

                        if (resultJson.has("lat") && resultJson.has("lon") && activeCoordSearch) {
                            lastLon = strToDoubleDef(resultJson.getString("lon"), -1);
                            lastLat = strToDoubleDef(resultJson.getString("lat"), -1);
                        } else {
                            showMsg("Не найдена координата для введенного адреса 1!");
                        }

                        if (lastLat > 0 && lastLon > 0 && activeCoordSearch) {
                            Intent bintent = new Intent(INFO_ACTION);
                            bintent.putExtra(ParamsAndConstants.TYPE, ParamsAndConstants.ID_ACTION_SEND_CCOORDS);
                            bintent.putExtra("clat", lastLat);
                            bintent.putExtra("clon", lastLon);
                            sendBroadcast(bintent);
                            hasMeGAdrDetecting = true;
                            activeCoordSearch = false;
                            coordSearchDetectFromAdr = true;
                            Message msg = new Message();
                            msg.arg1 = ParamsAndConstants.SHOW_COORDS_INFO;
                            Bundle bnd = new Bundle();
                            bnd.putString("msg_text", "===");
                            msg.setData(bnd);
                            handle.sendMessage(msg);
                        } else {
                            showMsg("Не найдена координата для введенного адреса!");
                        }

                    } catch (Exception e) {
                        showMsg("Ошибка парсинга ответа Nominatim Geocoder" + e.getMessage());
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

                @Override
                public void run(){
                    try {
                        String url = addres;

                        URL obj = new URL(url);
                        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

                        // optional default is GET
                        con.setRequestMethod("GET");

                        //add request header
                        con.setRequestProperty("User-Agent", "Dalvik");

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

                        showMsg(response.toString());
                        try {
                            parseAnswer(response.toString());
                        } catch (Exception ex) {
                            showMsg("Парсинг ответа обратного запроса Nominatim! "+ex.getMessage());
                        }
                    }
                    catch (Exception e) {
                        showMsg("Парсинг обратного запроса геокодера Nominatim! "+e.getMessage());
                    }
                }
            }).start();
    }

    public void sendGeocodeHTTPRequest(double rlat, double rlon)	{
        final String addres = "https://nominatim.openstreetmap.org/reverse?" +
                "format=json&lat=" + rlat + "&lon=" + rlon + "&zoom=18&addressdetails=1";
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
                bnd.putBoolean("is_reverse_geocode", true);
                msg.setData(bnd);
                handle.sendMessage(msg);
            }

            @Override
            public void run(){
                try {
                    String url = addres;

                    URL obj = new URL(url);
                    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

                    // optional default is GET
                    con.setRequestMethod("GET");
                    //add request header
                    con.setRequestProperty("User-Agent", "Dalvik");

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
                        JSONObject addrJson = (new JSONObject
                                (response.toString())).getJSONObject("address");
                        hasMeGAdrDetecting=true;
                        showGMAdress(addrJson.getString("road") + " " +
                                addrJson.getString("house_number"));
                    } catch (Exception e) {
                        showMsg("Ошибка парсинга ответа Nominatim Geocoder"+e.getMessage());
                    }
                }
                catch (Exception e) {
                    showMsg("Запроса геокодера Nominatim! "+e.getMessage());
                }
            }
        }).start();
    }

    public static void startGpsDetecting() {
        Message msg = new Message();
        msg.arg1 = ParamsAndConstants.MA_GPS_DETECTING;
        Bundle bnd = new Bundle();
        bnd.putString("msg_text", "ddddd");
        msg.setData(bnd);
        handle.sendMessage(msg);
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            dateEdit.setText(year + "-" + ((month + 1) < 10 ? "0" : "") + (month + 1) + "-" +
                    (day < 10 ? "0" : "") + day);
        }
    }

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            lastSheduleTime = dateEdit.getText() + " " + (hourOfDay < 10 ? "0" : "") +
                    hourOfDay + ":" + (minute < 10 ? "0" : "") + minute + ":00";
            dateEdit.setText(lastSheduleTime);
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        public View fragmentViev;
        Button orderButton, cancelButton, gpsDetectButton,
                clearBtn, cancelMapButton;
        FloatingActionButton callButton;
        AutoCompleteTextView editTextFromAdres;
        EditText editTextToAdres, commentEditText;

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

        public void sendCancelRequest(FragmentActivity FActivity) {
            if (lastOrdersCount > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FActivity);

                builder.setTitle("ПОДТВЕРЖДЕНИЕ")
                        .setMessage("Вы хотите отменить заказ?")
                        // кнопка "Yes", при нажатии на которую приложение закроется
                        .setPositiveButton("Ок",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int whichButton)
                                    {
                                        Message msg = new Message();
                                        msg.arg1 = ParamsAndConstants.MA_CANCELING;
                                        Bundle bnd = new Bundle();
                                        bnd.putString("msg_text", "ddddd");
                                        msg.setData(bnd);
                                        handle.sendMessage(msg);
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
            }   else    {
                Toast toastErrorStartActivitySMS2 = Toast.
                        makeText(FActivity,
                                "Нет активных заказов!", Toast.LENGTH_LONG);
                toastErrorStartActivitySMS2.show();
            }
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

        public void showTruitonTimePickerDialog(View v) {
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getFragmentManager(), "timePicker");
        }

        public void showTruitonDatePickerDialog(View v) {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getFragmentManager(), "datePicker");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView=null;
            orderButton=null;


            if(getArguments().getInt(ARG_SECTION_NUMBER)==1)    {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                textViewStatus = (TextView) rootView.findViewById(R.id.textViewStatus);
                sectDetectInfo = (TextView) rootView.findViewById(R.id.sectDetectInfo);
                tarifPlanChoice = (RadioGroup) rootView.findViewById(R.id.tarifPlanChoice);
                firstFragmentContext = getContext();
                sendInfoBroadcast(ParamsAndConstants.ID_ACTION_GET_TARIF_AND_OPTIONS, "---");
                orderButton = (Button)rootView.findViewById(R.id.orderButton);
                gpsDetectButton = (Button)rootView.findViewById(R.id.gpsDetectButton);
                cancelButton = (Button)rootView.findViewById(R.id.cancelButton);
                editTextFromAdres = (AutoCompleteTextView)rootView.findViewById(R.id.editTextFromAdr);
                editTextToAdres = (EditText)rootView.findViewById(R.id.editTextToAdr);
                clearBtn = (Button)rootView.findViewById(R.id.btn_clear);
                callButton = (FloatingActionButton) rootView.findViewById(R.id.floatingActionButton);
                commentEditText = (EditText)rootView.findViewById(R.id.editComment);
                commentEditText.setText(lastComment);

                dateEdit = (EditText) rootView.findViewById(R.id.editSheduleDate);
                dateEdit.setText(lastSheduleTime);

                dateEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTruitonTimePickerDialog(v);
                        showTruitonDatePickerDialog(v);
                    }
                });

                callButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try	{

                            Intent dialIntent = new Intent(Intent.ACTION_DIAL,
                                    Uri.fromParts("tel", "+7" + driverPhone, null));
                            startActivity(dialIntent);
                        } catch(Exception cex)	{
                            Toast toastErrorStartDial = Toast.
                                    makeText(getActivity(),
                                            "Ошибка набора номера!", Toast.LENGTH_LONG);
                            toastErrorStartDial.show();
                        }
                    }
                });

                commentEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //here is your code
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // TODO Auto-generated method stub
                        lastComment = commentEditText.getText().toString();
                    }
                });

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
                                    //tarifPlanChoice.getCheckedRadioButtonId()
                                    String tplanName = tariffPlanName.length() > 0 ? "(" + tariffPlanName + ")" : "";
                                    bnd.putString("msg_text", editTextFromAdres.getText().toString() + tplanName);
                                    bnd.putString("end_adr", editTextToAdres.getText().toString());
                                    bnd.putString("comment", commentEditText.getText().toString());
                                    String dateStr = dateEdit.getText().toString();
                                    bnd.putString("shedule_date", dateStr.length() == 19 ? dateStr : "");
                                    bnd.putInt("tariff_plan_id", tariffPlanId);
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
                        startGpsDetecting();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_WAKE_UP_NEO,"---");
                        sendCancelRequest(getActivity());
                    }
                });

            }
            else if (getArguments().getInt(ARG_SECTION_NUMBER)==2) {
                rootView = inflater.inflate(R.layout.map_layout, container, false);

                mapStatusView = (TextView) rootView.findViewById(R.id.mapStatusView);
                driverInfoMapView = (TextView) rootView.findViewById(R.id.driverInfoMapView);

                cancelMapButton = (Button)rootView.findViewById(R.id.mapCancelButton);
                cancelMapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendInfoBroadcast(ParamsAndConstants.ID_ACTION_WAKE_UP_NEO,"---");
                        sendCancelRequest(getActivity());
                    }
                });

                try {
                    Configuration.getInstance().setUserAgentValue(getActivity().getPackageName());
                    map = (MapView) rootView.findViewById(R.id.map);
                    //map.setTileSource(TileSourceFactory.MAPNIK);
                    //map.setBuiltInZoomControls(true);
                    //map.setMultiTouchControls(true);
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
            if(position==0) {
                firstTab = (PlaceholderFragment) fr;
            }

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
