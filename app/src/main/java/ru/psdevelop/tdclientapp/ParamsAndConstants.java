package ru.psdevelop.tdclientapp;

/**
 * Created by Станислав on 27.02.2016.
 */
public abstract class ParamsAndConstants {
    public static double defLat=44.878208;
    public static double defLon=37.314103;
    public static String srvHost="http://192.168.1.90:8081";
    public static final String REGION_DEFAULT = " Анапский район Анапа ";
    public static final String PLACES_DEFAULT = " Анапский район ";
    public static final String PLACE_REPLACE1 = " Анапа, ";
    public static final String PLACE_REPLACE2 = " Анапский район, ";
    public static final String PLACE_REPLACE3 = " Краснодарский край, ";
    public static final String PLACE_REPLACE4 = " Россия ";
    public static String gm_key = "";
    public static String mapHtml="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
            "<html><head>"+
            "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />" +
            "<script src=\"http://code.jquery.com/jquery-2.2.1.min.js\"></script> " +
            "<script src=\"http://maps.google.com/maps/api/js?sensor=false\"></script> " +
            "</head><body style=\"padding:0px;margin:0px;\">" +
            "<div id='err' style=\"position:absolute;z-index:100;height:40px;width:100%;background-color:yellow;\">Подождите, загрузка карты...</div><div id=\"map-canvas\" style=\"width:100%;height:100%;min-height:480px;background-color:#fff;\">Загрузка...</div>" +
            "<script> document.getElementById('map-canvas').innerHTML='gggg';  " +
            "try { var latlng = new google.maps.LatLng(44.878208, 37.314103); " +
            "var myOptions = { " +
            "zoom: 13, " +
            "center: latlng, " +
            "mapTypeId: google.maps.MapTypeId.ROADMAP, " +
            "maxZoom: 13 " +
            " }; " +
            "var map = new google.maps.Map(document.getElementById(\"map-canvas\"), myOptions); google.maps.event.addListenerOnce(map, 'idle', function(){ $('#err').hide();  }); " +
            "var bounds = new google.maps.LatLngBounds(); " +
            "var myLat=***___lastLat; " +
            "var myLon=***___lastLon; " +
            "var cl_marker = new google.maps.Marker({ " +
            "position: new google.maps.LatLng(myLat, myLon), " +
            "map: map, " +
            "title: \"Вы находитесь здесь!\" " +
            "}); " +
            "bounds.extend(cl_marker.position); var lastGAdr='***___lastGAdr'; " +
            "var addres = \"http://maps.googleapis.com/maps/api/geocode/json?latlng=\"+myLat+\",\"+myLon+\"&sensor=false&language=ru\"; " +
            "var contentString='<div id=\"content\" style=\"min-height:70px;\"><center><div>Вы здесь</div><div></div></center></div>'; " +
            "$.getJSON( addres, { }, function(data) { " +
            "try { " +
            "adr=data['results'][0]['formatted_address'].replace('Россия,',''); " +
            "contentString = '<div id=\"content\" style=\"min-height:70px;\"><center><div>'+(lastGAdr.length>0?lastGAdr:adr)+'</div><div></div></center></div>'; " +
            "} catch(e) { } " +
            "var infowindow = new google.maps.InfoWindow({ " +
            "content: contentString " +
            "}); " +
            "infowindow.open(map,cl_marker); " +
            "}).fail(function() { " +
            "var infowindow = new google.maps.InfoWindow({ " +
            "content: contentString " +
            "}); " +
            "infowindow.open(map,cl_marker); " +
            "}); " +
            " ***___drivers_markers " +
            "map.fitBounds(bounds); " +
            //" try { " +
            //" window.droid.print('sss'); " +
            //" } catch(e) { " +
            //" document.getElementById('err').innerHTML='window.droid.print'; " +
            //" } " +
            "document.getElementsByTagName('div')[0].style.height = 'auto'; " +
            "document.getElementsByTagName('div')[0].style.width = 'auto'; " +
            " } catch(e) { document.getElementById('err').innerHTML='Ошибка отрисовки карты'+e; } " +
            " </script>"+
            "</body></html>";



    public final static int SHOW_MESSAGE_TOAST = 1;
    public final static int SHOW_STATUS_INFO = 2;
    public final static int SHOW_DECLINE_INFO = 3;
    public final static int SHOW_COORDS_INFO = 4;
    public final static int MA_CHECK_STATUSES = 5;
    public final static int MA_ORDERING = 12;
    public final static int MA_CANCELING = 13;
    public final static int MA_GPS_DETECTING = 14;
    public final static int SHOW_GM_ADDRESS = 19;
    public final static int MA_SEND_INFO_BCAST = 20;
    public final static int MA_SET_STAT_TEXTVIEW = 23;
    public final static int SHOW_STATUS_STRING = 25;
    public final static int SHOW_SECTOR_DETECT_INFO = 32;
    public final static int SHOW_TARIF_AND_OPTIONS = 35;

    public static final String TYPE = "type";
    public static final String MSG_TEXT = "msg_text";
    public static final int ID_ACTION_SHOW_SERVICE_INFO = 0;
    public static final int ID_ACTION_SHOW_STATUS_INFO = 1;
    public static final int ID_ACTION_SHOW_COORD_INFO = 9;
    public static final int ID_ACTION_START_GPS_DETECTING = 10;
    public static final int ID_ACTION_GO_ORDERING = 11;
    public static final int ID_ACTION_GO_ORDER_CANCELING = 15;
    public static final int ID_ACTION_WAKE_UP_NEO = 18;
    public static final int ID_ACTION_SET_STATUS_TEXTVIEW = 22;
    public static final int ID_ACTION_SHOW_STATUS_STRING = 24;
    public static final int ID_ACTION_SET_HISTORY_ADR = 26;
    public static final int ID_ACTION_SEND_CCOORDS = 27;
    public static final int ID_ACTION_SHOW_SECTOR_DETECT_INFO = 31;
    public static final int ID_ACTION_SHOW_TARIF_OPTIONS = 34;
    public static final int ID_ACTION_GET_TARIF_AND_OPTIONS = 36;

    public final static int CHECK_CONNECT = 5;
    public final static int RECEIVE_AUTH = 2;
    public final static int RECEIVER_CLSTAT = 3;
    public final static int REQUEST_CLSTAT = 4;
    public final static int CHECK_STATUSES = 8;
    public final static int REQ_DECLINE = 16;
    public final static int SERVER_OVERLOAD = 17;
    public final static int DISCONNECT_SOCKIO = 21;
    public final static int RECEIVE_SECTOR_DETECT = 30;
    public final static int RECEIVE_TARIFS_OPTIONS = 33;
}
