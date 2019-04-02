package ru.psdevelop.tdclientapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        //sendGeocodeHTTPRequest(0, 0);
        String https_url = "https://nominatim.openstreetmap.org/reverse?" +
                "format=json&lat=44.888613&lon=37.313288&zoom=18&addressdetails=1";
        //"https://nominatim.openstreetmap.org/";
        //"https://www.google.com/";
        URL url;
        try {

            url = new URL(https_url);
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", "Dalvik");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            //dumpl all cert info
            //print_https_cert(con);

            //dump all the content
            print_content(con);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(4, 2 + 2);
    }

    public void sendGeocodeHTTPRequest(double rlat, double rlon)	{
        final String addres = "http://nominatim.openstreetmap.org/reverse?" +
                "format=json&lat=44.888613&lon=37.313288&zoom=18&addressdetails=1";

        try {
            String url = addres;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

                    /*SSLContext sslContext = SSLContext.getInstance("TLS");

                    sslContext.init(null, null, null);
                    con.setSSLSocketFactory(sslContext.getSocketFactory());*/

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
                System.out.println(addrJson.getString("road") + " " +
                        addrJson.getString("house_number"));
            } catch (Exception e) {
                System.out.println("Ошибка парсинга ответа Nominatim Geocoder" +
                        e.getMessage() + "\n" + inputLine);
            }
        } catch (Exception e) {
            System.out.println("Запроса геокодера Nominatim! " + e.getMessage());
        }
    }

    private void print_https_cert(HttpsURLConnection con){

        if(con!=null){

            try {

                System.out.println("Response Code : " + con.getResponseCode());
                System.out.println("Cipher Suite : " + con.getCipherSuite());
                System.out.println("\n");

                Certificate[] certs = con.getServerCertificates();
                for(Certificate cert : certs){
                    System.out.println("Cert Type : " + cert.getType());
                    System.out.println("Cert Hash Code : " + cert.hashCode());
                    System.out.println("Cert Public Key Algorithm : "
                            + cert.getPublicKey().getAlgorithm());
                    System.out.println("Cert Public Key Format : "
                            + cert.getPublicKey().getFormat());
                    System.out.println("\n");
                }

            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }

        }

    }

    private void print_content(HttpsURLConnection con){
        if(con!=null){

            try {

                System.out.println("****** Content of the URL ********");
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(con.getInputStream()));

                String input;

                while ((input = br.readLine()) != null){
                    System.out.println(input);
                }
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /*public void sendReverseGeocodeHTTPRequestOld(String adress)	{
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

                    if(hasGAdrLat&&hasGAdrLon&&lastRevLon>0&&lastRevLat>0&&activeCoordSearch) {
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
                    } else {
                        showMsg("Не найдена координата для введенного адреса!");
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

    public void sendGoogleGeocodeHTTPRequest(double rlat, double rlon)	{
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
    }*/
}