package com.example.yourskeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

public class PleaseMain_act extends AppCompatActivity
        implements OnMapReadyCallback {

    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;
    private double lat,lon;

    private static final int PERMISSION_REQUEST_CODE = 1000;

    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_please_main);

        //지도 객체 생성하기
        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map_fragment);
        if(mapFragment == null){
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.map_fragment, mapFragment).commit();

        }

        //getMapAsync 호출해 비동기로 onMapReady 콜백 메서드 호출
        //onMapReady에서 NaverMap 객체를 받음.
        mapFragment.getMapAsync(this);

        //위치를 반환하는 구현체인 FusedLocationSource 생성
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);


    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {


        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(locationSource);

        // 권한 확인, 결과는 onRequestPermissionResult 콜백 메서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                Toast.makeText(getApplicationContext(),
                        lat+", "+lon, Toast.LENGTH_SHORT).show();
            }
        });
        Marker marker1 = new Marker();
        marker1.setPosition(new LatLng(37.4974358, 126.9530382));
        marker1.setMap(naverMap);
        Marker marker2 = new Marker();
        marker2.setPosition(new LatLng(37.4985318, 126.9580722));
        marker2.setMap(naverMap);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request code와 권한 획득 여부 확인
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }
}