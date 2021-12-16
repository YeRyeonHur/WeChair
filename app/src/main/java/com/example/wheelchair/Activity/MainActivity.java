package com.example.wheelchair.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wheelchair.DTO.EntranceInfo;
import com.example.wheelchair.DTO.MapPointDTO;
import com.example.wheelchair.DTO.NowBus;
import com.example.wheelchair.R;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Overlay.OnClickListener {
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private Geocoder geocoder;
    private static final int PERMISSION_REQUEST_CODE = 100;
    Button toiletButton, busStationButton, restaurantButton, navi;
    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;
    private InfoWindow mInfoWindow;
    private TextView slidingTextView;
    private SlidingUpPanelLayout slidingPaneLayout;
    private LinearLayout dragView;
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private Vector<MapPointDTO> mapPointDTOS = new Vector<MapPointDTO>();
    private Vector<Marker> activeMarkers;
    LatLng destLatLng = new LatLng(0.0, 0.0);  // 클릭한 마커 위치
    LatLng currentLatLng = new LatLng(0.0, 0.0);  //현재 위치
    private ProgressDialog progressDialog;

    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    int toilet_flag = 0;
    int bus_flag = 0;
    int res_flag = 0;
    boolean loading_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_activitiy);

        toiletButton = (Button) findViewById(R.id.toilet);
        busStationButton = (Button) findViewById(R.id.station);
        restaurantButton = (Button) findViewById(R.id.restaurant);
        navi = (Button) findViewById(R.id.navi);
        slidingPaneLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingPanel);
        dragView = (LinearLayout) findViewById(R.id.dragView);
        dragView.setClickable(false);
        slidingPaneLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        slidingTextView = (TextView) findViewById(R.id.slidingText);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        //api
        StrictMode.enableDefaults();

        progressDialog = new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (!loading_flag) {
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
        }
    }

    private void setMarker(@NonNull NaverMap naverMap, Marker marker, double lat, double lng) {
        marker.setPosition(new LatLng(lat, lng));
        marker.setOnClickListener(this);
        marker.setMap(naverMap);
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        getData();
        getBusData();
        kdata();
        LatLng initialPosition = new LatLng(35.88754486390442, 128.6117392305679);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        naverMap.moveCamera(cameraUpdate);
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(mLocationSource);
        mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        loading_flag = true;
        progressDialog.dismiss();

        // 카메라 이동 되면 호출 되는 이벤트
        naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int reason, boolean animated) {
                //freeActiveMarkers();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantRequest) {
        super.onRequestPermissionsResult(requestCode, permissions, grantRequest);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantRequest.length > 0 && grantRequest[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        LinearLayout toilet_restaurant_linearLayout, bus_linearLayout;
        toilet_restaurant_linearLayout = (LinearLayout) findViewById(R.id.linearLayoutToiletRes);
        bus_linearLayout = (LinearLayout) findViewById(R.id.linearLayoutBus);

        TextView tv_low_floor_bus = (TextView) findViewById(R.id.low_floor_bus);
        ImageView bus_re_data = (ImageView) findViewById(R.id.bus_autorenew);
        if (overlay instanceof Marker) {
            Marker marker = (Marker) overlay;

            MapPointDTO mapPointDTO = (MapPointDTO) marker.getTag();
            destLatLng = new LatLng(mapPointDTO.getLatitude(), mapPointDTO.getLongitude());
            if (mapPointDTO.getFaclTyCd() != "BUS") {
                if (mapPointDTO.hasInfo() == false) {
                    //화장실, 음식점 출입 정보
                    getDataFaclInfo(mapPointDTO);
                }
                ArrayList<EntranceInfo> entranceInfo = new ArrayList<>();
                boolean[] infoFlag = mapPointDTO.getInfo();
                for (int i = 0; i < 5; i++) {
                    if (infoFlag[i] == true) {
                        entranceInfo.add(new EntranceInfo(i));
                    }
                }
                bus_linearLayout.setVisibility(View.GONE);
                tv_low_floor_bus.setVisibility(View.GONE);
                bus_re_data.setVisibility(View.GONE);
                toilet_restaurant_linearLayout.setVisibility(View.VISIBLE);
                ListView listView = (ListView) findViewById(R.id.toilet_res_ListView);
                listView.setVisibility(View.VISIBLE);
                final EntranceAdapter entranceAdapter = new EntranceAdapter(this, entranceInfo);
                listView.setAdapter(entranceAdapter);

                slidingTextView.setText(mapPointDTO.getName());
                slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
            //버스 정류장 데이터 가져오기
            else {
                bus_re_data.setVisibility(View.VISIBLE);
                toilet_restaurant_linearLayout.setVisibility(View.GONE);
                bus_linearLayout.setVisibility(View.VISIBLE);
                ArrayList<NowBus> nowBuses = new ArrayList<NowBus>();
                nowBuses = getDataNowBus(mapPointDTO);
                //sort
                Collections.sort(nowBuses, sortByTime);
                ArrayList<NowBus> lowFloorBuses = new ArrayList<>();
                for (NowBus nowBus : nowBuses) {
                    if (nowBus.getBusType() == "저상버스")
                        lowFloorBuses.add(nowBus);
                }
                TextView tv_noBus = (TextView) findViewById(R.id.noBus);
                if (nowBuses.size() == 0) {
                    tv_noBus.setVisibility(View.VISIBLE);
                    tv_low_floor_bus.setVisibility(View.GONE);
                } else {
                    tv_low_floor_bus.setVisibility(View.VISIBLE);
                    tv_noBus.setVisibility(View.GONE);
                    ListView listView = (ListView) findViewById(R.id.busListView);
                    listView.setVisibility(View.VISIBLE);
                    final NowBusAdapter nowBusAdapter = new NowBusAdapter(this, nowBuses);
                    listView.setAdapter(nowBusAdapter);

                    tv_low_floor_bus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final NowBusAdapter nowBusAdapter = new NowBusAdapter(getApplicationContext(), lowFloorBuses);
                            nowBusAdapter.notifyDataSetChanged();
                            listView.setAdapter(nowBusAdapter);
                        }
                    });

                }
                slidingTextView.setText(mapPointDTO.getName());
                slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }

            return true;
        }
        return false;
    }

    private final static Comparator<NowBus> sortByTime = new Comparator<NowBus>() {
        @Override
        public int compare(NowBus o1, NowBus o2) {
            if (o1.getTime() < o2.getTime()) return -1;
            else if (o1.getTime() == o2.getTime()) return 0;
            else return 1;
        }
    };

    public void navibuttonclicked(View v) throws IOException {
        if(destLatLng.latitude==0.0 || destLatLng.longitude==0.0){
            return;
        }
        String current_addr="경북대학교 IT 2호관";
        String dest_addr;
        List<Address> dest_address = null;
        Geocoder geocoder = new Geocoder(this);
        dest_address=geocoder.getFromLocation(destLatLng.latitude,destLatLng.longitude,1);
        dest_addr=dest_address.get(0).getAddressLine(0).toString();
        String dest_utf8=new String(dest_addr.getBytes("UTF-16"),"UTF-8");
        String current_utf8=new String(current_addr.getBytes("UTF-16"),"UTF-8");
        String currentLatitude = String.valueOf(currentLatLng.latitude);
        String currentLongitude = String.valueOf(currentLatLng.longitude);
        String destLatitude = String.valueOf(destLatLng.latitude);
        String destLongitude = String.valueOf(destLatLng.longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("nmap://route/walk?slat=35.88782334898758&slng=128.61168685742604" +
                "&sname=" + current_addr +
                "&dlat=" + destLatitude + "&dlng=" + destLongitude +
                "&dname="+ dest_addr +
                "&appname=com.example.wheelchair"));
        startActivity(intent);
    }


    private void kdata() {
        MapPointDTO mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.88772056077337);
        mapPoint.setLongitude(128.61166490592902);
        mapPoint.setName("경북대학교 IT 2호관");
        boolean[] flag = new boolean[5];
        flag[0] = true;
        flag[1] = false;
        flag[2] = false;
        flag[3] = true;
        flag[4] = true;
        mapPoint.setInfo(flag);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.887478717604615);
        mapPoint.setLongitude(128.6127158402182);
        mapPoint.setName("경북대학교 IT 1호관");
        boolean[] flag1 = new boolean[5];
        flag1[0] = true;
        flag1[1] = true;
        flag1[2] = true;
        flag1[3] = true;
        flag1[4] = true;
        mapPoint.setInfo(flag1);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.889065493084395);
        mapPoint.setLongitude(128.61447372826044);
        mapPoint.setName("경북대학교 복지관");
        boolean[] flag2 = new boolean[5];
        flag2[0] = true;
        flag2[1] = true;
        flag2[2] = true;
        flag2[3] = true;
        flag2[4] = true;
        mapPoint.setInfo(flag2);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.88810008443131);
        mapPoint.setLongitude(128.61123623804312);
        mapPoint.setName("경북대학교 IT융복합관");
        boolean[] flag3 = new boolean[5];
        flag3[0] = true;
        flag3[1] = true;
        flag3[2] = true;
        flag3[3] = true;
        flag3[4] = true;
        mapPoint.setInfo(flag3);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.888788690975296);
        mapPoint.setLongitude(128.61364921554974);
        mapPoint.setName("경북대학교 박물관");
        boolean[] flag4 = new boolean[5];
        flag4[0] = true;
        flag4[1] = true;
        flag4[2] = true;
        flag4[3] = false;
        flag4[4] = true;
        mapPoint.setInfo(flag4);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.89198024097426);
        mapPoint.setLongitude(128.61125161671453);
        mapPoint.setName("경북대학교 글로벌프라자");
        boolean[] flag5 = new boolean[5];
        flag5[0] = false;
        flag5[1] = true;
        flag5[2] = true;
        flag5[3] = false;
        flag5[4] = false;
        mapPoint.setInfo(flag5);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.888202095991325);
        mapPoint.setLongitude(128.61045437522122);
        mapPoint.setName("경북대학교 IT 3호관");
        boolean[] flag6 = new boolean[5];
        flag6[0] = true;
        flag6[1] = true;
        flag6[2] = true;
        flag6[3] = false;
        flag6[4] = false;
        mapPoint.setInfo(flag6);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.88810008443131);
        mapPoint.setLongitude(128.61123623804312);
        mapPoint.setName("경북대학교 IT융복합관");
        boolean[] flag7 = new boolean[5];
        flag7[0] = true;
        flag7[1] = true;
        flag7[2] = true;
        flag7[3] = true;
        flag7[4] = true;
        mapPoint.setInfo(flag7);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.884717310984115);
        mapPoint.setLongitude(128.61038979722474);
        mapPoint.setName("대현어린이공원");
        boolean[] flag8 = new boolean[5];
        flag8[0] = false;
        flag8[1] = true;
        flag8[2] = false;
        flag8[3] = false;
        flag8[4] = true;
        mapPoint.setInfo(flag8);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.88402960531602);
        mapPoint.setLongitude(128.61223115185197);
        mapPoint.setName("신암초등학교");
        boolean[] flag9 = new boolean[5];
        flag9[0] = true;
        flag9[1] = true;
        flag9[2] = false;
        flag9[3] = true;
        flag9[4] = true;
        mapPoint.setInfo(flag9);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.88301572848548);
        mapPoint.setLongitude(128.61419795962772);
        mapPoint.setName("대구공업고등학교");
        boolean[] flag10 = new boolean[5];
        flag10[0] = true;
        flag10[1] = true;
        flag10[2] = true;
        flag10[3] = true;
        flag10[4] = true;
        mapPoint.setInfo(flag10);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.89368644748507);
        mapPoint.setLongitude(128.60341204840168);
        mapPoint.setName("대구체육관");
        boolean[] flag11 = new boolean[5];
        flag11[0] = true;
        flag11[1] = true;
        flag11[2] = true;
        flag11[3] = true;
        flag11[4] = true;
        mapPoint.setInfo(flag11);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.89422187888365);
        mapPoint.setLongitude(128.60691114298731);
        mapPoint.setName("산격초등학교");
        boolean[] flag12 = new boolean[5];
        flag12[0] = true;
        flag12[1] = true;
        flag12[2] = false;
        flag12[3] = true;
        flag12[4] = true;
        mapPoint.setInfo(flag12);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.89763428949772);
        mapPoint.setLongitude(128.60713221951193);
        mapPoint.setName("개나리공원");
        boolean[] flag13 = new boolean[5];
        flag13[0] = true;
        flag13[1] = true;
        flag13[2] = true;
        flag13[3] = false;
        flag13[4] = false;
        mapPoint.setInfo(flag13);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
        mapPoint = new MapPointDTO();
        mapPoint.setLatitude(35.8926474016669);
        mapPoint.setLongitude(128.59884707581702);
        mapPoint.setName("대구광역시청 별관");
        boolean[] flag14 = new boolean[5];
        flag14[0] = true;
        flag14[1] = true;
        flag14[2] = true;
        flag14[3] = true;
        flag14[4] = false;
        mapPoint.setInfo(flag14);
        mapPoint.setfaclTyCd("UC0A13");
        mapPointDTOS.add(mapPoint);
    }

    private void getData() {
        for (int page = 10; page < 20; page++) {
            try {
                URL url = new URL("http://apis.data.go.kr/B554287/DisabledPersonConvenientFacility/getDisConvFaclList?"
                        + "&pageNo=" + page + "&numOfRows=" + "1000" +
                        "&ServiceKey=lDq1uyoVjyWBPA1R3tj0E6HqMH5B4ifC1vLm%2Br%2FiHErs776rR48xQRYOOPsxMRAN7MaT6LBUFrUklPsU%2BMlB8Q%3D%3D&"); //검색 URL부분
                InputStream is = url.openStream();
                XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserCreator.newPullParser();
                parser.setInput(new InputStreamReader(is, "UTF-8"));

                int parserEvent = parser.getEventType();
                int estbdate = 0;
                double lat = 0.0, lng = 0.0;
                String name = null, cd = null, id = null;
                while (parserEvent != XmlPullParser.END_DOCUMENT) {
                    switch (parserEvent) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                            if (parser.getName().equals("estbDate")) { //title 만나면 내용을 받을수 있게 하자
                                parser.next();
                                estbdate = Integer.parseInt(parser.getText());
                            } else if (parser.getName().equals("faclLat")) { //title 만나면 내용을 받을수 있게 하자
                                parser.next();
                                lat = Double.parseDouble(parser.getText());
                            } else if (parser.getName().equals("faclLng")) { //title 만나면 내용을 받을수 있게 하자
                                parser.next();
                                lng = Double.parseDouble(parser.getText());
                            } else if (parser.getName().equals("faclNm")) { //title 만나면 내용을 받을수 있게 하자
                                parser.next();
                                name = parser.getText();
                            } else if (parser.getName().equals("faclTyCd")) {
                                parser.next();
                                cd = parser.getText();
                            } else if (parser.getName().equals("wfcltId")) {
                                parser.next();
                                id = parser.getText();
                            }
                            break;
                        case XmlPullParser.TEXT:
                            break;

                        case XmlPullParser.END_TAG:
                            if (parser.getName().equals("servList")) {
                                MapPointDTO mapPoint = new MapPointDTO();
                                mapPoint.setEstbDate(estbdate);
                                mapPoint.setLatitude(lat);
                                mapPoint.setLongitude(lng);
                                mapPoint.setName(name);
                                mapPoint.setWfcltId(id);
                                mapPoint.setfaclTyCd(cd);
                                mapPointDTOS.add(mapPoint);
                            }
                            break;
                    }
                    parserEvent = parser.next();
                }
            } catch (Exception e) {
            }
        }
    }

    private void getBusData() {
        try {
            String url_str = "http://openapi.tago.go.kr/openapi/service/BusSttnInfoInqireService/getSttnNoList?serviceKey=L2VM7f1PPrN4%2FiRYxA9H%2F47FcZ6L8Mp72fB67Gqj0YjzlKQ%2FgmqtTURCNbQf7e2jIaMkdordccx0dbQx3UmPeg%3D%3D&cityCode=22&" +
                    "numOfRows=" + "10000" + "&pageNo=1";
            URL url = new URL(url_str); //검색 URL부분
            InputStream is = url.openStream();
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            parser.setInput(new InputStreamReader(is, "UTF-8"));

            int parserEvent = parser.getEventType();
            double lat = 0.0, lng = 0.0;
            String name = null, nodeId = null;
            int nodeNum = 0;
            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                        if (parser.getName().equals("gpslati")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            lat = Double.parseDouble(parser.getText());
                        } else if (parser.getName().equals("gpslong")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            lng = Double.parseDouble(parser.getText());
                        } else if (parser.getName().equals("nodeid")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            nodeId = parser.getText();
                        } else if (parser.getName().equals("nodenm")) {
                            parser.next();
                            name = parser.getText();
                        } else if (parser.getName().equals("nodeno")) {
                            parser.next();
                            nodeNum = Integer.parseInt(parser.getText());
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("item")) {
                            MapPointDTO mapPointDTO = new MapPointDTO();
                            mapPointDTO.setLatitude(lat);
                            mapPointDTO.setLongitude(lng);
                            mapPointDTO.setWfcltId(nodeId);
                            mapPointDTO.setName(name);
                            mapPointDTO.setNodeNm(nodeNum);
                            mapPointDTO.setfaclTyCd("BUS");
                            mapPointDTOS.add(mapPointDTO);
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch (Exception e) {
            Log.i("test", String.valueOf(e));
        }
    }

    private void getDataFaclInfo(MapPointDTO mapPointDTO) {
        try {
            String wFaclId = mapPointDTO.getWfcltId();
            String url_str = "http://apis.data.go.kr/B554287/DisabledPersonConvenientFacility/getFacInfoOpenApiJpEvalInfoList?"
                    + "serviceKey=ILTqw3kO5xY0W9LmfQzwMcKHHOEqv4aXn3iBkRv6V7MDJLADpnXT4x6jJeNzx409g03rioaANmj%2BGSzTu6G9tA%3D%3D&wfcltId=" + wFaclId;

            URL url = new URL(url_str); //검색 URL부분
            InputStream is = url.openStream();
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            parser.setInput(new InputStreamReader(is, "UTF-8"));

            int parserEvent = parser.getEventType();
            String info;
            boolean[] info_flag_list = new boolean[5];
            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                        if (parser.getName().equals("evalInfo")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            info = parser.getText();
                            //경사로
                            if (info.contains("경사로")) {
                                info_flag_list[0] = true;
                            }
                            //대변기
                            if (info.contains("대변기")) {
                                info_flag_list[1] = true;
                            }
                            //엘리베이터
                            if (info.contains("엘리베이터")) {
                                info_flag_list[2] = true;
                            }
                            //장애인 주차장
                            if (info.contains("장애인전용주차구역")) {
                                info_flag_list[3] = true;
                            }
                            //접근로
                            if (info.contains("높이차이 제거") || info.contains("접근로")) {
                                info_flag_list[4] = true;
                            }
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("servList")) {
                            mapPointDTO.setInfo(info_flag_list);
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch (Exception e) {
        }
    }

    private ArrayList<NowBus> getDataNowBus(MapPointDTO mapPointDTO) {
        ArrayList<NowBus> buses = new ArrayList<NowBus>();
        try {
            String NodeCd = mapPointDTO.getWfcltId();
            String url_str = "http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?serviceKey=L2VM7f1PPrN4%2FiRYxA9H%2F47FcZ6L8Mp72fB67Gqj0YjzlKQ%2FgmqtTURCNbQf7e2jIaMkdordccx0dbQx3UmPeg%3D%3D&cityCode=22&" +
                    "nodeId=" + NodeCd;
            Log.i("test", NodeCd);
            URL url = new URL(url_str); //검색 URL부분
            InputStream is = url.openStream();
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            parser.setInput(new InputStreamReader(is, "UTF-8"));

            int parserEvent = parser.getEventType();
            String busName = null, busType = null;
            int arriveTime = 0;
            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                        if (parser.getName().equals("arrtime")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            arriveTime = Integer.parseInt(parser.getText());
                        } else if (parser.getName().equals("routeno")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            busName = parser.getText();
                        } else if (parser.getName().equals("vehicletp")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            busType = parser.getText();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("item")) {
                            Log.i("test", busName + "  " + arriveTime + "  " + busType);

                            NowBus nowBus = new NowBus(busName, arriveTime, busType);
                            nowBus.setBusNum(busName);
                            buses.add(nowBus);
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch (Exception e) {
            Log.i("test", String.valueOf(e));
        }

        return buses;
    }

    @SuppressLint("ResourceAsColor")
    public void buttonClicked(View v) {
        freeActiveMarkers();

        switch (v.getId()) {
            case R.id.toilet:
                bus_flag = 0;
                busStationButton.setBackgroundResource(R.drawable.round_button);
                res_flag = 0;
                restaurantButton.setBackgroundResource(R.drawable.round_button);
                if (toilet_flag == 0) {
                    toiletButton.setBackgroundResource(R.drawable.round_button_pressed);
                    toilet_flag = 1;
                } else {
                    toiletButton.setBackgroundResource(R.drawable.round_button);
                    toilet_flag = 0;
                }
                for (MapPointDTO obj : mapPointDTOS) {
                    double lat = obj.getLatitude();
                    double lng = obj.getLongitude();
                    LatLng tmp = new LatLng(lat, lng);
                    if (obj.getFaclTyCd().equals("UC0A13")) {
                        Marker marker = new Marker();
                        marker.setTag(obj);
                        marker.setIconTintColor(R.color.toilet);
                        setMarker(mNaverMap, marker, lat, lng);
                        activeMarkers.add(marker);
                    }
                }
                break;
            case R.id.restaurant:
                bus_flag = 0;
                busStationButton.setBackgroundResource(R.drawable.round_button);
                toilet_flag = 0;
                toiletButton.setBackgroundResource(R.drawable.round_button);
                if (res_flag == 0) {
                    restaurantButton.setBackgroundResource(R.drawable.round_button_pressed);
                    res_flag = 1;
                } else {
                    restaurantButton.setBackgroundResource(R.drawable.round_button);
                    res_flag = 0;
                }
                for (MapPointDTO obj : mapPointDTOS) {
                    double lat = obj.getLatitude();
                    double lng = obj.getLongitude();
                    LatLng tmp = new LatLng(lat, lng);
                    if (obj.getFaclTyCd().equals("UC0B01")) {
                        Marker marker = new Marker();
                        marker.setTag(obj);
                        marker.setIconTintColor(R.color.bus);
                        setMarker(mNaverMap, marker, lat, lng);
                        activeMarkers.add(marker);
                    }
                }
                break;
            case R.id.station:
                res_flag = 0;
                restaurantButton.setBackgroundResource(R.drawable.round_button);
                toilet_flag = 0;
                toiletButton.setBackgroundResource(R.drawable.round_button);
                if (bus_flag == 0) {
                    busStationButton.setBackgroundResource(R.drawable.round_button_pressed);
                    bus_flag = 1;
                } else {
                    busStationButton.setBackgroundResource(R.drawable.round_button);
                    bus_flag = 0;
                }
                for (MapPointDTO obj : mapPointDTOS) {
                    double lat = obj.getLatitude();
                    double lng = obj.getLongitude();
                    LatLng tmp = new LatLng(lat, lng);
                    if (obj.getFaclTyCd().equals("BUS")) {
                        Marker marker = new Marker();
                        marker.setTag(obj);
                        marker.setIconTintColor(R.color.restaurant);
                        setMarker(mNaverMap, marker, lat, lng);
                        activeMarkers.add(marker);
                    }
                }
                break;
        }

    }

    // 현재 카메라가 보고있는 위치
    public LatLng getCurrentPosition(NaverMap naverMap) {
        CameraPosition cameraPosition = naverMap.getCameraPosition();
        return new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }

    public boolean withinSightMarker(LatLng currentPosition, LatLng markerPosition) {
        LatLng tmp = getCurrentPosition(mNaverMap);
        double lat = tmp.latitude;
        double lng = tmp.longitude;
        double REFERANCE_LAT_X3 = 3 / lat;
        double REFERANCE_LNG_X3 = 3 / lng;
        boolean withinSightMarkerLat = Math.abs(currentPosition.latitude - markerPosition.latitude) <= REFERANCE_LAT_X3;
        boolean withinSightMarkerLng = Math.abs(currentPosition.longitude - markerPosition.longitude) <= REFERANCE_LNG_X3;
        return withinSightMarkerLat && withinSightMarkerLng;
    }

    // 지도상에 표시되고있는 마커들 지도에서 삭제
    private void freeActiveMarkers() {
        if (activeMarkers != null) {
            for (Marker activeMarker : activeMarkers) {
                activeMarker.setMap(null);
            }
        }
        activeMarkers = new Vector<Marker>();
    }
}
