package com.example.wheelchair.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.wheelchair.R;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
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



public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Overlay.OnClickListener {
    private static final int PERMISSION_REQUEST_CODE = 100;
    Button toiletButton,busStationButton,restaurantButton;
    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;
    private SlidingUpPanelLayout slidingPaneLayout;
    private ArrayList<Marker> markers = new ArrayList<Marker>();

    //api
    private TextView textView;
    private String data;
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

    }


    private void setMarker(@NonNull NaverMap naverMap, Marker marker, double lat, double lng){
        marker.setPosition(new LatLng(lat,lng));
        marker.setMap(naverMap);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Marker marker = new Marker();
        setMarker(naverMap, marker,35.890408676085485, 128.61199646266655);
        Marker marker1 = new Marker();
        setMarker(naverMap, marker1,35.88754486390442, 128.6117392305679);

        marker.setOnClickListener(this);
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(mLocationSource);
        //ActivityCompat.requestPermissions(this,PERMISSIONS, PERMISSION_REQUEST_CODE);


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
                        if (parser.getName().equals("estbDate")) { //title 만나면 내용을 받을수 있게 하자
                            parser.next();
                            buffer.append(parser.getText());
                            buffer.append("\n"); // 줄바꿈 문자 추가
                        }
                        break;
                    case XmlPullParser.TEXT: break;

                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("item")) {
                            buffer.append("\n"); // 첫번째 검색결과종료 후 줄바꿈
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
                        data = getData(); // 하단의 getData 메소드를 통해 데이터를 파싱
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(textView.getText() + data);
                            }
                        });
                    }
                }).start();
                break;
        }
    }

}