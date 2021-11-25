package com.example.wheelchair.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Overlay.OnClickListener {
    private static final int PERMISSION_REQUEST_CODE = 100;
    Button toiletButton, busStationButton, restaurantButton;
    ImageView new_data;
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
        busStationButton = (Button) findViewById(R.id.busStation);
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
        mInfoWindow = new InfoWindow();
        mInfoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                Marker marker = infoWindow.getMarker();
                MapPointDTO mapPointDTO = (MapPointDTO) marker.getTag();
                if (mapPointDTO.getFaclTyCd().equals("UC0A13")) {
                    return "화장실";
                }
                else if (mapPointDTO.getFaclTyCd().equals("UC0B01")){
                    return "식당";
                }
                else {
                    return null;
                }
            }
        });

        getData();
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
        if (overlay instanceof Marker) {
            Marker marker = (Marker) overlay;
            MapPointDTO mapPointDTO = (MapPointDTO) marker.getTag();
            slidingTextView.setText(mapPointDTO.getName());
            slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

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
            int estbdate = 0, id = 0;
            double lat = 0.0, lng = 0.0;
            String name = null, cd = null;
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
                            lng = Double.parseDouble(parser.getText());
                        } else if (parser.getName().equals("faclLng")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            lat = Double.parseDouble(parser.getText());
                        } else if (parser.getName().equals("faclNm")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            name = parser.getText();
                        } else if (parser.getName().equals("faclTyCd")) {
                            parser.next();
                            cd = parser.getText();
                        } else if (parser.getName().equals("wfcltId")) {
                            parser.next();
                            id = Integer.parseInt(parser.getText());
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