package com.kw.yuseyun_2020

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_first.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder

class FirstActivity : FragmentActivity(), OnMapReadyCallback {

    var in_depature = "";
    var in_destination = "";

    val permission_request = 99

    //private val candidate: Candidate = Candidate()
    lateinit var naverMap: NaverMap

    var permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    )// 권한 가져오기

    override fun onCreate(savedInstanceState: Bundle?) { //액티비티가 최초 실행 되면 이곳을 수행한다.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        if (isPermitted()) {
                startProcess()
            } else {
                ActivityCompat.requestPermissions(this, permissions, permission_request)
        }//권한 확인

        map_matching_button.visibility = View.INVISIBLE

        map_matching_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        route_finding_button.setOnClickListener{
            in_depature = depature.text.toString();
            in_destination = destination.text.toString();
            if(in_depature.equals("")){
                var t1 = Toast.makeText(this,"출발지를 입력해 주셔야죠~",Toast.LENGTH_SHORT);
                t1.show();
            }
            if(in_destination.equals("")){
                var t1 = Toast.makeText(this,"도착지를 입력해 주셔야죠~",Toast.LENGTH_SHORT);
                t1.show();
            }
            var path = pathFind()
            printNodesToPath()
            printStartAndEndPOIs()
            map_matching_button.visibility = View.VISIBLE
        }
    }

    //권한을 허락 받아야함
    private fun isPermitted(): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }



    fun startProcess() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
                ?: MapFragment.newInstance().also {
                    fm.beginTransaction().add(R.id.map, it).commit()
                } //권한
        mapFragment.getMapAsync(this) //안드로이드 연결 //onMapReady연결
    }//권한이 있다면 onMapReady연결


    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        val cameraPosition = CameraPosition(
                LatLng(37.618235, 127.061945),  // 위치 지정
                16.0 // 줌 레벨
        )
        naverMap.cameraPosition = cameraPosition
        this.naverMap = naverMap
        main() //file
    }
    //맵을 생성할 준비가 되었을 때 가장 먼저 호출되는 오버라이드 메소드

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
    lateinit var locationCallback: LocationCallback //gps응답 값을 가져온다.
    //lateinit: 나중에 초기화 해주겠다는 의미

    @SuppressLint("MissingPermission")
    fun setUpdateLocationListner() {
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
                    setLastLocation(location)
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

    fun setLastLocation(location: Location) {
        val myLocation = LatLng(37.618235, 127.061945)
        val marker = Marker()
        marker.position = myLocation
        //marker.captionText = "위도: ${location.latitude}, 경도: ${location.longitude}"
        marker.map = naverMap
        //마커
        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        naverMap.moveCamera(cameraUpdate)
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 5.0
        //marker.map = null
    }

    fun main(){}

    fun pathFind() {
        val dir = filesDir.absolutePath //파일절대경로
        FileIO.setDir(dir)
        FileIO.generateRoadNetwork()

        var routeObject = Mapmatching_engine(naverMap)
        var route: ArrayList<Int>
        var startNodeID = RoadNetwork.getNodeIDByPoiName(in_depature)
        var endNodeID = RoadNetwork.getNodeIDByPoiName(in_destination)
        RoadNetwork.startPOI = in_depature
        RoadNetwork.endPOI = in_destination
        route = routeObject.for_route(naverMap, dir, startNodeID, endNodeID);

        // 0513 유네 추가 .. 뒤로가기로 여러번의 테스트 가능하도록 데이터 비움
        if (!RoadNetwork.getRouteNodeArrayList().isEmpty()) RoadNetwork.routeNodeArrayList.clear();
        for (i in 0..route.size - 1) {
            RoadNetwork.routeNodeArrayList.add(RoadNetwork.getNode(route.get(i)));
        }
        var str: StringBuilder? = StringBuilder()
        str?.append("Node : ")
        for (i in 1..route.size) {
            str?.append(route.get(i - 1))
            str?.append(" ")
        }
        str?.append("\n")
        str?.append("Length : ")
        str?.append(routeObject.route_length)
        var t1 = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        t1.show()
    }

    fun printNodesToPath() {
        val path = PathOverlay()
        var pathArr = ArrayList<LatLng>()
        var i = 0
        for (node in RoadNetwork.getRouteNodeArrayList()) {
            pathArr.add(LatLng(RoadNetwork.getNode(node.nodeID).coordinate.y, RoadNetwork.getNode(node.nodeID).coordinate.x))
        }
        path.coords = pathArr
        path.map = naverMap
        path.setColor(Color.rgb(3,107,252));
        path.setWidth(30);
        path.outlineColor = Color.LTGRAY
        path.outlineWidth = 5
        path.patternImage = OverlayImage.fromResource(R.drawable.route_pattern)
    }
    fun printStartAndEndPOIs() {
        // 출발점 표시
        val marker_s = Marker() //좌표
        var poi_s = RoadNetwork.getPOI(RoadNetwork.getPOIIDByPoiName(RoadNetwork.startPOI)).coordinate
        marker_s.position = LatLng(
                poi_s.y,
                poi_s.x
        ) //poi 좌표 출력
        //marker_s.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
        //marker_s.iconTintColor = Color.GREEN //색 덧입히기
        marker_s.icon = OverlayImage.fromResource(R.drawable.start_poi)
        marker_s.width = 100
        marker_s.height = 120
        marker_s.map = naverMap //navermap에 출력
        //marker_s.captionText = "출발"

        // 도착점 표시
        val marker_e = Marker() //좌표
        var poi_e = RoadNetwork.getPOI(RoadNetwork.getPOIIDByPoiName(RoadNetwork.endPOI)).coordinate
        marker_e.position = LatLng(
                poi_e.y,
                poi_e.x
        ) //poi 좌표 출력
        //marker_e.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
        //marker_e.iconTintColor = Color.GREEN //색 덧입히기
        marker_e.icon = OverlayImage.fromResource(R.drawable.end_poi)
        marker_e.width = 100
        marker_e.height = 120
        marker_e.map = naverMap //navermap에 출력
        //marker_e.captionText = "도착"
    }
}