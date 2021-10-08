package com.example.wheelchair;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Overlay.OnClickListener {
    private static final int PERMISSION_REQUEST_CODE = 100;
    Button toiletButton,busStationButton,restaurantButton;
    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;
    private SlidingUpPanelLayout slidingPaneLayout;
    private static final String[] PERMISSIONS ={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activitiy);

        toiletButton = (Button)findViewById(R.id.toilet);
        busStationButton = (Button)findViewById(R.id.busStation);
        restaurantButton = (Button)findViewById(R.id.restaurant);
        slidingPaneLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingPanel);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map_fragment);

        if(mapFragment==null){
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment,mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        mLocationSource = new FusedLocationSource(this,PERMISSION_REQUEST_CODE);


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
}