package com.kw.yuseyun_2020

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.ERROR
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.kw.yuseyun_2020.R
import com.kw.yuseyun_2020.Realtime_engine
import com.kw.yuseyun_2020.RoadNetwork
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.MarkerIcons
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : FragmentActivity(), OnMapReadyCallback {

    val permission_request = 99

    lateinit var tts:TextToSpeech

    lateinit var mAdapter: MainRvAdapter
    val lm = LinearLayoutManager(this)

    lateinit var ArrOfGuidance: ArrayList<Guidance>

    var index = 1;


    // map matching 관련 ///////////////
    // GPS points와 routePoints를 저장할 ArrayList생성
    var gpsPointArrayList: ArrayList<GPSPoint> = ArrayList()
    var routePointArrayList = ArrayList<Point>() // 실제 경로의 points!

    var matchingCandiArrayList: ArrayList<Candidate> = ArrayList()


    // window size만큼의 t-window, ... , t-1, t에서의 candidates의 arrayList
    var arrOfCandidates: ArrayList<ArrayList<Candidate>> = ArrayList()
    var subGPSs: ArrayList<GPSPoint> = ArrayList()

    //ArrayList<Point> subRPA = new ArrayList<>(); // 비터비 내부 보려면 이것도 주석 해제해야! (subRoadPointArrayList)
    // GPSPoints 생성
    //ArrayList<Point> subRPA = new ArrayList<>(); // 비터비 내부 보려면 이것도 주석 해제해야! (subRoadPointArrayList)
    // GPSPoints 생성
    var timestamp = 0

    // map matching 관련 ///////////////
    //private val candidate: Candidate = Candidate()
    private lateinit var naverMap: NaverMap

    var permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    )// 권한 가져오기

    override fun onCreate(savedInstanceState: Bundle?) { //액티비티가 최초 실행 되면 이곳을 수행한다.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        if (isPermitted()) {
            startProcess()
        } else {
            ActivityCompat.requestPermissions(this, permissions, permission_request)
        }//권한 확인

        dataInit()
        //도로네트워크 생성해야됨

        ArrOfGuidance = TBTLogic.returnFinalGuidances(RoadNetwork.getRouteNodeArrayList())
        index = 1
        for (g in ArrOfGuidance) {
            if (g.direction == -2) g.index = "출발"
            else if (g.direction == -3) g.index = "도착"
            else {
                g.index = index.toString()
                index++
            }
        }

        System.out.println(ArrOfGuidance)

        var nodeIDs = ArrayList<Int>()
        for (g in ArrOfGuidance) {
            nodeIDs.add(g.nodeID)
        }
        setRV()
        setAdapter()

        main_panel.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                //
            }

            override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
                //onPanelSlide(main_panel, 0.0f)
                if (main_panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    val lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                    recyclerView.layoutParams = lp
                    //System.out.println("open!")
                } else {
                    val lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 200)
                    recyclerView.layoutParams = lp
                    //System.out.println("closed!")
                }
            }
        })

        // textToSpeech 초기화
        tts = TextToSpeech(this, object: TextToSpeech.OnInitListener {
            override fun onInit (status :Int) {
                if (status != ERROR) {
                    tts.language = Locale.KOREAN
                }
            }
        })
    }
    fun setAdapter() {
        //mAdapter.addSentences(generateSentenceList());
        mAdapter.addGuidances(ArrOfGuidance);
    }

    fun setRV() {
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        var g = ArrayList<Guidance>()
        mAdapter = MainRvAdapter(this, g)
        recyclerView.adapter = mAdapter
    }

    fun isPermitted(): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }//권한을 허락 받아야함

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
        this.naverMap = naverMap

        val cameraPosition = CameraPosition(
                LatLng(37.618235, 127.061945),  // 위치 지정
                16.0 // 줌 레벨
        )
        naverMap.cameraPosition = cameraPosition
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this) //gps 자동으로 받아오기
        setUpdateLocationListner() //내위치를 가져오는 코드


        val dir = filesDir.absolutePath //파일절대경로
        //Mapmatching_engine(naverMap).engine(naverMap, dir) //GPS 생성 매칭
        //FixedGPS(naverMap).fixengine(naverMap, dir) //고정된 GPS 매칭
        //engine 부분 옮기기

        printNodes(ArrOfGuidance)
        printNodesToPath()
        printStartAndEndPOIs()

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
        val dir = filesDir.absolutePath //내부저장소 절대 경로
        val filename = "실제 GPS.txt"
        //writeTextFile(dir, filename, "애플리케이션 시작!\n")

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

    fun setLastLocation(location: Location) {

        Realtime_engine().Real_engine(naverMap, location)
        //실제 GPS 하나씩 받아서 넘겨주기


        // 음성 턴바이턴 안내
        speechGuidance()

        val myLocation = LatLng(location.latitude, location.longitude)

        val locationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true
        locationOverlay.position = LatLng(location.latitude, location.longitude)
        locationOverlay.bearing = 90f
        locationOverlay.iconWidth = 80
        locationOverlay.iconHeight = 80
        locationOverlay.circleRadius = 300

    }
    fun speechGuidance () {
        var currentPoint = FSWViterbi.getMatched_sjtp().get(FSWViterbi.getMatched_sjtp().size-1).point

        for(routeNode in RoadNetwork.routeNodeArrayList) {
            if (Calculation.calDistance(currentPoint, routeNode.coordinate) < 10) {
                for (g in ArrOfGuidance) {
                    if (g.nodeID == routeNode.nodeID && !g.isPassed) {
                        // 음성안내
                        Toast.makeText(this, g.speechSentence,Toast.LENGTH_LONG).show()
                        tts.setPitch(1.0f)      // 음성 톤은 기본 설정
                        tts.setSpeechRate(1.0f)   // 읽는 속도를 0.5빠르기로 설정
                        // editText에 있는 문장을 읽는다.
                        tts.speak(g.speechSentence,TextToSpeech.QUEUE_FLUSH, null);

                        var RVposition  = 0
                        // 리사이클러뷰 특정위치로 스크롤
                        if (g.index.contentEquals("출발")){
                            RVposition = 0
                        } else if(g.index.contentEquals("도착")) {
                            RVposition = ArrOfGuidance.size-1
                        } else {
                            RVposition = Integer.parseInt(g.index)
                        }

                        recyclerView.scrollToPosition(RVposition)

                        // 지나간 안내라고 표시하기
                        g.isPassed  = true

                    }
                }
            }
        }
    }
    fun dataInit() {
        System.out.println("===== [YSY] Map-matching PilotTest 2 =====")

        // 파일에서 읽어와 도로네트워크 생성
        //FileIO.generateRoadNetwork()
        RoadNetwork.printRoadNetwork(); // POI 잘 읽은 것 확인 완료!

        val testNo = 4 // 여기만 바꿔주면 됨 (PilotTest 2는 data 1만 존재)
        //routePointArrayList = RoadNetwork.routePoints(testNo)

        // Link와 Node를 바탕으로 Adjacent List 구축
        val heads: ArrayList<AdjacentNode> = ArrayList()
        for (i in RoadNetwork.nodeArrayList.indices) {
            val headNode = AdjacentNode(RoadNetwork.nodeArrayList[i])
            heads.add(headNode)
            val adjacentLink: MutableList<Pair<Link, Int>>? =
                    RoadNetwork.getLink1(headNode.node.nodeID) //mutableList?
            if (adjacentLink != null) { //안전하게 하기 위함
                if (adjacentLink.size == 0) continue
            }
            var ptr = headNode
            if (adjacentLink != null) { //안전하게 하기 위함
                for (j in adjacentLink.indices) {
                    val addNode = AdjacentNode(
                            RoadNetwork.getNode(adjacentLink[j].second), adjacentLink[j].first
                    )
                    ptr.nextNode = addNode
                    ptr = ptr.nextNode
                }
            }
        }
        //신기한 사실 = get,set 함수를 불러오지 않아도 알아서 불러옴
        //여기까지 도로네트워크 생성

    }

    fun printNodes(guidances: ArrayList<Guidance>) {
        var i = 0;
        for (g in guidances) {
            if (g.direction == -2 || g.direction == -3) continue;
            val marker = Marker() //좌표
            var node = RoadNetwork.getNode(g.nodeID)
            marker.position = LatLng(
                    node.coordinate.y,
                    node.coordinate.x
            ) //node 좌표 출력
            marker.icon = OverlayImage.fromResource(R.drawable.guidance_node)
            //marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
            //marker.iconTintColor = Color.BLACK //색 덧입히기
            marker.width = 80
            marker.height = 80
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
            marker.captionText = "" + (i + 1)
            marker.captionAlign = Align.Center
            //marker.captionColor = Color.WHITE
            marker.captionTextSize = 16f
            i++
        }
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
        path.setColor(Color.rgb(3, 107, 252));
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