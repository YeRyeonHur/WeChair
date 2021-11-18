package com.example.wheelchair.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.wheelchair.DTO.MapPointDTO;
import com.example.wheelchair.R;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
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
    Button toiletButton,busStationButton,restaurantButton;
    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;
    private SlidingUpPanelLayout slidingPaneLayout;
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private ArrayList<MapPointDTO> mapPointDTOS = new ArrayList<MapPointDTO>();
    private MapPointDTO mapPoint = null;
    //api
    private TextView textView;
    private String data = "";
    private static final String[] PERMISSIONS ={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activitiy);

        toiletButton = (Button) findViewById(R.id.toilet);
        busStationButton = (Button) findViewById(R.id.busStation);
        restaurantButton = (Button) findViewById(R.id.restaurant);
        slidingPaneLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingPanel);

        textView = (TextView) findViewById(R.id.result);

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
        getData();


    }


    private void setMarker(@NonNull NaverMap naverMap, Marker marker, double lat, double lng){
        marker.setPosition(new LatLng(lat,lng));
        marker.setMap(naverMap);
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        getData();
        LatLng initialPosition = new LatLng(35.88754486390442, 128.6117392305679);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        naverMap.moveCamera(cameraUpdate);
        Marker marker1 = new Marker();
        setMarker(naverMap, marker1,35.88754486390442, 128.6117392305679);


        for(MapPointDTO obj : mapPointDTOS){
            Marker marker = new Marker();
            //LatLng latLng = new LatLng(obj.getLatitude(),obj.getLongitude());
            markers.add(marker);
            setMarker(naverMap, markers.get(markers.size()),obj.getLatitude(), obj.getLongitude());
        }
        //marker.setOnClickListener(this);
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(mLocationSource);
        //ActivityCompat.requestPermissions(this,PERMISSIONS, PERMISSION_REQUEST_CODE);

        mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);


        // 카메라 이동 되면 호출 되는 이벤트
        naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int reason, boolean animated) {
                freeActiveMarkers();
                // 정의된 마커위치들중 가시거리 내에있는것들만 마커 생성
                LatLng currentPosition = getCurrentPosition(naverMap);
                for(MapPointDTO mapPointDTO : mapPointDTOS){
                    LatLng mapPointLatLng = new LatLng(mapPointDTO.getLatitude(),mapPointDTO.getLongitude());
                    if(!withinSightMarker(currentPosition,mapPointLatLng)) continue;
                    Marker marker = new Marker();
                    marker.setPosition(mapPointLatLng);
                    marker.setMap(naverMap);
                    activeMarkers.add(marker);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantRequest) {

        super.onRequestPermissionsResult(requestCode, permissions, grantRequest);
        if(requestCode==PERMISSION_REQUEST_CODE){
            if(grantRequest.length>0 && grantRequest[0] == PackageManager.PERMISSION_GRANTED){
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }

    @Override
    public boolean onClick(@NonNull Overlay overlay) {
        if(overlay instanceof Marker){
            slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            return true;
        }
        return false;
    }
    String getData(){
        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL("http://apis.data.go.kr/B554287/DisabledPersonConvenientFacility/getDisConvFaclList?"
                    + "&pageNo=1&numOfRows=1000&ServiceKey=lDq1uyoVjyWBPA1R3tj0E6HqMH5B4ifC1vLm%2Br%2FiHErs776rR48xQRYOOPsxMRAN7MaT6LBUFrUklPsU%2BMlB8Q%3D%3D&"); //검색 URL부분
            InputStream is= url.openStream();
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            parser.setInput( new InputStreamReader(is, "UTF-8") );

            int parserEvent = parser.getEventType();
            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                        if(parser.getName().equals("servList")){
                            mapPoint = new MapPointDTO();
                        }
                        else if (parser.getName().equals("estbDate")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            buffer.append(parser.getText());
                            buffer.append("\n"); // 줄바꿈 문자 추가
                            mapPoint.setEstbDate(Integer.parseInt(parser.getText()));
                        }
                        else if (parser.getName().equals("faclLat")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            double tmp = Double.parseDouble(parser.getText());
                            buffer.append(tmp+"");
                            buffer.append("\n"); // 줄바꿈 문자 추가
                            mapPoint.setLatitude(Double.parseDouble(parser.getText()));
                        }
                        else if (parser.getName().equals("faclLng")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            double tmp = Double.parseDouble(parser.getText());
                            buffer.append(tmp+"");
                            buffer.append("\n"); // 줄바꿈 문자 추가
                            mapPoint.setLongitude(Double.parseDouble(parser.getText()));
                        }
                        else if (parser.getName().equals("faclNm")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            double tmp = Double.parseDouble(parser.getText());
                            buffer.append(tmp+"");
                            buffer.append("\n"); // 줄바꿈 문자 추가
                            mapPoint.setName(parser.getText());
                        }
                        break;
                    case XmlPullParser.TEXT: break;

                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("servList")) {
                            buffer.append("\n"); // 첫번째 검색결과종료 후 줄바꿈
                            mapPointDTOS.add(mapPoint);
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch (Exception e) {
        }
        buffer.append("파싱 종료 단계 \n");
        return buffer.toString();
    }

    public void buttonClicked(View v){
        switch(v.getId()){
            case R.id.new_data :
                // 쓰레드를 생성하여 돌리는 구간
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //data = getData(); // 하단의 getData 메소드를 통해 데이터를 파싱
                        getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for(MapPointDTO obj : mapPointDTOS){
                                    data = data + obj.getEstbDate();
                                }
                                for(MapPointDTO obj : mapPointDTOS){
                                    Marker marker = new Marker();
                                    LatLng latLng = new LatLng(obj.getLatitude(),obj.getLongitude());
                                    markers.add(marker);
                                    markersPosition.add(latLng);
                                    //setMarker(naverMap, markers.get(markers.size()),obj.getLatitude(), obj.getLongitude());
                                }
                                //textView.setText(mapPointDTOS.get(0).getEstbDate()+"");
                                //textView.setText(data);
                                textView.setText("파싱완료");
                            }
                        });
                    }
                }).start();


                break;
        }
    }


    // 마커 정보 저장시킬 변수들 선언
    private Vector<LatLng> markersPosition;
    private Vector<Marker> activeMarkers;

    // 현재 카메라가 보고있는 위치
    public LatLng getCurrentPosition(NaverMap naverMap) {
        CameraPosition cameraPosition = naverMap.getCameraPosition();
        return new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }

    // 선택한 마커의 위치가 가시거리(카메라가 보고있는 위치 반경 3km 내)에 있는지 확인
    public final static double REFERANCE_LAT = 1 / 109.958489129649955;
    public final static double REFERANCE_LNG = 1 / 88.74;
    public final static double REFERANCE_LAT_X3 = 3 / 109.958489129649955;
    public final static double REFERANCE_LNG_X3 = 3 / 88.74;
    public boolean withinSightMarker(LatLng currentPosition, LatLng markerPosition) {
        boolean withinSightMarkerLat = Math.abs(currentPosition.latitude - markerPosition.latitude) <= REFERANCE_LAT_X3;
        boolean withinSightMarkerLng = Math.abs(currentPosition.longitude - markerPosition.longitude) <= REFERANCE_LNG_X3;
        return withinSightMarkerLat && withinSightMarkerLng;
    }

    // 지도상에 표시되고있는 마커들 지도에서 삭제
    private void freeActiveMarkers() {
        if (activeMarkers == null) {
            activeMarkers = new Vector<Marker>();
            return;
        }
        for (Marker activeMarker: activeMarkers) {
            activeMarker.setMap(null);
        }
        activeMarkers = new Vector<Marker>();
    }
}