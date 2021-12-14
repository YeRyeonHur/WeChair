package com.example.wheelchair.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Overlay.OnClickListener {
    private static final int PERMISSION_REQUEST_CODE = 100;
    Button toiletButton, busStationButton, restaurantButton;
    ImageView img_ramp;
    ImageView img_elevator;
    ImageView img_stair;
    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;
    private InfoWindow mInfoWindow;
    private TextView slidingTextView;
    private SlidingUpPanelLayout slidingPaneLayout;
    private LinearLayout dragView;
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private Vector<MapPointDTO> mapPointDTOS = new Vector<MapPointDTO>();
    private Vector<Marker> activeMarkers;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activitiy);

        toiletButton = (Button) findViewById(R.id.toilet);
        busStationButton = (Button) findViewById(R.id.station);
        restaurantButton = (Button) findViewById(R.id.restaurant);
        slidingPaneLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingPanel);
        dragView = (LinearLayout) findViewById(R.id.dragView);
        dragView.setClickable(false);
        slidingPaneLayout.setFadeOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        slidingTextView = (TextView) findViewById(R.id.slidingText);
        img_elevator = (ImageView) findViewById(R.id.img_elevator);
        img_ramp = (ImageView) findViewById(R.id.img_ramp);
        img_stair = (ImageView) findViewById(R.id.img_stair);

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
        LatLng initialPosition = new LatLng(35.88754486390442, 128.6117392305679);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        naverMap.moveCamera(cameraUpdate);
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(mLocationSource);
        mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);


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

    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        LinearLayout toilet_restaurant_linearLayout, bus_linearLayout;
        toilet_restaurant_linearLayout = (LinearLayout) findViewById(R.id.linearLayoutToiletRes);
        bus_linearLayout = (LinearLayout) findViewById(R.id.linearLayoutBus);

        if (overlay instanceof Marker) {
            Marker marker = (Marker) overlay;

            MapPointDTO mapPointDTO = (MapPointDTO) marker.getTag();


            if (mapPointDTO.getFaclTyCd() != "BUS") {
                if (mapPointDTO.hasInfo() == false) {
                    //화장실, 음식점 출입 정보
                    getDataFaclInfo(mapPointDTO);
                }
                bus_linearLayout.setVisibility(View.GONE);
                toilet_restaurant_linearLayout.setVisibility(View.VISIBLE);
                boolean[] infoFlag = mapPointDTO.getInfo();
                // 승강설비
                if (infoFlag[0]) {
                    img_elevator.setImageResource(R.drawable.elevator);
                } else {
                    img_elevator.setImageResource(R.drawable.x);
                }
                // 높이차이 제거, 접근로 (접근 편이)
                if (infoFlag[1]) {
                    img_ramp.setImageResource(R.drawable.ramp);
                } else {
                    img_ramp.setImageResource(R.drawable.x);
                }
                // 계단
                if (infoFlag[2]) {
                    img_stair.setImageResource(R.drawable.stair);
                } else {
                    img_stair.setImageResource(R.drawable.x);
                }

                slidingTextView.setText(mapPointDTO.getName());
                slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
            //버스 정류장 데이터 가져오기
            else {
                toilet_restaurant_linearLayout.setVisibility(View.GONE);
                bus_linearLayout.setVisibility(View.VISIBLE);
                ArrayList<NowBus> nowBuses = new ArrayList<NowBus>();
                nowBuses = getDataNowBus(mapPointDTO);
                Log.i("test",mapPointDTO.getName());
                for(NowBus bus : nowBuses){
                    Log.i("test",bus.getBusNum()+"  "+bus.getBusType()+"  "+bus.getTime());
                }
                ListView listView = (ListView) findViewById(R.id.busListView);
                final NowBusAdapter nowBusAdapter = new NowBusAdapter(this, nowBuses);
                listView.setAdapter(nowBusAdapter);
                slidingTextView.setText(mapPointDTO.getName());
                slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }

            return true;
        }
        return false;
    }

    private void getData() {
        try {
            URL url = new URL("http://apis.data.go.kr/B554287/DisabledPersonConvenientFacility/getDisConvFaclList?"
                    + "&pageNo=1&numOfRows=1000&ServiceKey=lDq1uyoVjyWBPA1R3tj0E6HqMH5B4ifC1vLm%2Br%2FiHErs776rR48xQRYOOPsxMRAN7MaT6LBUFrUklPsU%2BMlB8Q%3D%3D&"); //검색 URL부분
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
            Log.i("test",String.valueOf(e));
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
                            if (info.contains("승강설비")) {
                                info_flag_list[0] = true;
                            }
                            if (info.contains("높이차이 제거") || info.contains("접근로")) {
                                info_flag_list[1] = true;
                            }
                            if (info.contains("계단")) {
                                info_flag_list[2] = true;
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
            Log.i("test",NodeCd);
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
                            Log.i("test",busName+"  "+arriveTime+"  "+busType);

                            NowBus nowBus = new NowBus(busName,arriveTime,busType);
                            nowBus.setBusNum(busName);
                            buses.add(nowBus);
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch (Exception e) {
            Log.i("test",String.valueOf(e));
        }

        return buses;
    }

    public void buttonClicked(View v) {
        freeActiveMarkers();
        switch (v.getId()) {
            case R.id.toilet:
                for (MapPointDTO obj : mapPointDTOS) {
                    double lat = obj.getLatitude();
                    double lng = obj.getLongitude();
                    LatLng tmp = new LatLng(lat, lng);
                    if (obj.getFaclTyCd().equals("UC0A13")) {
                        Marker marker = new Marker();
                        marker.setTag(obj);
                        marker.setIconTintColor(Color.BLUE);
                        setMarker(mNaverMap, marker, lat, lng);
                        activeMarkers.add(marker);
                    }
                }
                break;
            case R.id.restaurant:
                for (MapPointDTO obj : mapPointDTOS) {
                    double lat = obj.getLatitude();
                    double lng = obj.getLongitude();
                    LatLng tmp = new LatLng(lat, lng);
                    if (obj.getFaclTyCd().equals("UC0B01")) {
                        Marker marker = new Marker();
                        marker.setTag(obj);
                        marker.setIconTintColor(Color.GREEN);
                        setMarker(mNaverMap, marker, lat, lng);
                        activeMarkers.add(marker);
                    }
                }
                break;
            case R.id.station:
                Toast.makeText(this, "station", Toast.LENGTH_SHORT).show();
                for (MapPointDTO obj : mapPointDTOS) {
                    double lat = obj.getLatitude();
                    double lng = obj.getLongitude();
                    LatLng tmp = new LatLng(lat, lng);
                    if (obj.getFaclTyCd().equals("BUS")) {
                        Marker marker = new Marker();
                        marker.setTag(obj);
                        marker.setIconTintColor(Color.RED);
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