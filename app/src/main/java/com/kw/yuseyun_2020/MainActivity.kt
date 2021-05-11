package com.kw.yuseyun_2020;

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.kw.yuseyun_2020.Adjacent_List
import com.kw.yuseyun_2020.R
import com.kw.yuseyun_2020.Realtime_engine
import com.kw.yuseyun_2020.RoadNetwork
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class MainActivity : FragmentActivity(), OnMapReadyCallback {

    val permission_request = 99

    var sentenceList = arrayListOf(
            "[1]   dummy",
            "[2]   dummy",
            "[3]   dummy",
            "[4]   dummy",
            "[5]   dummy",
            "[6]   dummy",
            "[7]   dummy",
            "[8]   dummy"
    )


    private lateinit var naverMap: NaverMap
    var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    //권한 가져오기

    override fun onCreate(savedInstanceState: Bundle?) {
        //activity가 최초 실행 되면 이곳을 수행

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        if (isPermitted()) {
            startProcess()
        } else {
            ActivityCompat.requestPermissions(this, permissions, permission_request)
        }//권한 확인

        val mAdapter = MainRvAdapter(this, sentenceList)
        recyclerView.adapter = mAdapter

        val lm = LinearLayoutManager(this)
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)

    }

    fun isPermitted(): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }//권한을 허락 받아야함

    fun startProcess(){
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
                ?: MapFragment.newInstance().also {
                    fm.beginTransaction().add(R.id.map, it).commit()
                } //권한
        mapFragment.getMapAsync(this)
    } //권한이 있다면 onMapReady연결


    @UiThread
    override fun onMapReady(naverMap: NaverMap){
/*
        val cameraPosition = CameraPosition(
                LatLng(37.5666102, 126.9783881),  // 위치 지정
                16.0 // 줌 레벨
        )
        naverMap.cameraPosition = cameraPosition
        this.naverMap = naverMap

        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this) //gps 자동으로 받아오기
        setUpdateLocationListner() //내위치를 가져오는 코드
*/

        val dir = filesDir.absolutePath //파일절대경로
        Mapmatching_engine(naverMap).engine(naverMap, dir) //GPS 생성 매칭
        //FixedGPS(naverMap).fixengine(naverMap, dir) //고정된 GPS 매칭
        //engine 부분 옮기기
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            permission_request -> {
                var check = true
                for (grant in grantResults) {
                    if (grant != PERMISSION_GRANTED) {
                        check = false
                        break
                    }
                }
                if (check) {
                    startProcess()
                } else {
                    Toast.makeText(this, "권한을 승인해아지만 앱을사용가능", Toast.LENGTH_LONG).show()
                    finish()
                    finish()
                }
            }
        }
    }//권한 승인

    //내 위치를 가져오는 코드
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient //자동으로 gps값을 받아온다.
    lateinit var locationCallback: LocationCallback //gps 응답값을 가져온다.
    //lateinit: 나중에 초기화 해주겠다는 의미

    @SuppressLint("MissingPermission")
    fun setUpdateLocationListner() {
        val dir = filesDir.absolutePath //내부저장소 절대 경로
        val filename = "실제 GPS.txt"
        //writeTextFile(dir, filename, "애플리케이션 시작!\n")

        var roadNetwork = Adjacent_List(naverMap).Adjacent_List(naverMap,dir)

        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //높은 정확도
            interval = 1000 //1초에 한번씩 GPS 요청
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for ((i, location) in locationResult.locations.withIndex()) {
                    Log.d("location: ", "${location.latitude}, ${location.longitude}")
                    setLastLocation(location, roadNetwork)
                    val contents = location.latitude.toString() + "\t" + location.longitude.toString() + "\n"
                    //writeTextFile(dir, filename, contents)
                }
            }
        }
        //location 요청 함수 호출 (locationRequest, locationCallback)

        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
        )
    }//좌표계를 주기적으로 갱신

    fun setLastLocation(location: Location, roadNetwork: RoadNetwork?) {
        if (roadNetwork != null) {
            Realtime_engine().Real_engine(naverMap, location, roadNetwork)
        }
        val myLocation = LatLng(location.latitude, location.longitude)
        val marker = Marker()
        marker.position = myLocation

        marker.map = naverMap
        //마커
        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        naverMap.moveCamera(cameraUpdate)
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 5.0

        //marker.map = null
    }

    fun writeTextFile(directory:String, filename:String, content:String){
        val dir = File(directory)

        if(!dir.exists()){ //dir이 존재 하지 않을때
            dir.mkdirs() //mkdirs : 중간에 directory가 없어도 생성됨
        }

        val writer = FileWriter(directory + "/" + filename, true)
        //true는 파일을 이어쓰는 것을 의미

        //쓰기 속도 향상
        val buffer = BufferedWriter(writer)
        buffer.write(content)
        buffer.close()
    }


}

