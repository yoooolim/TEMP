package com.kw.yuseyun_2020

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
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
import com.kw.yuseyun_2020.Adjacent_List
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

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class MainActivity : FragmentActivity(), OnMapReadyCallback {

    val permission_request = 99


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

        //dataInit()

        ArrOfGuidance = TBTLogic.returnFinalGuidances(RoadNetwork.getRouteNodeArrayList())

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
                    System.out.println("open!")
                } else {
                    val lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 800)
                    recyclerView.layoutParams = lp
                    System.out.println("closed!")
                }
            }
        })

    }


    fun generateSentenceList(): ArrayList<String> {
        var sentenceList = ArrayList<String>();

        for (g in ArrOfGuidance) {
            var temp = ""
            if (g.direction != -2 && g.direction != -3) {
                temp += "[$index] "
                temp += g.sentence
                index++
            } else if (g.direction == -2) {
                temp += "[출발] "
                temp += g.sentence
            } else if (g.direction == -3) {
                temp += "[도착] "
                temp += g.sentence
            }
            sentenceList.add(temp);
            System.out.println("sentence: ${temp}")
        }
        return sentenceList;
    }

    fun setAdapter() {
        mAdapter.addSentences(generateSentenceList());
    }

    fun setRV() {
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        var strarr = ArrayList<String>()
        mAdapter = MainRvAdapter(this, strarr);
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
        val cameraPosition = CameraPosition(
                LatLng(37.618235, 127.061945),  // 위치 지정
                16.0 // 줌 레벨
        )
        naverMap.cameraPosition = cameraPosition
        this.naverMap = naverMap
        /*val marker = Marker()
        marker.position = LatLng(37.618235, 127.061945)
        marker.map = naverMap*/
        main() //file
        /*fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this) //gps 자동으로 받아오기
        setUpdateLocationListner() //내위치를 가져오는 코드*/

        val dir = filesDir.absolutePath //파일절대경로
        Mapmatching_engine(naverMap).engine(naverMap, dir) //GPS 생성 매칭
        //FixedGPS(naverMap).fixengine(naverMap, dir) //고정된 GPS 매칭
        //engine 부분 옮기기
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

        var roadNetwork = Adjacent_List(naverMap).Adjacent_List(naverMap, dir)

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
        //marker.captionText = "위도: ${location.latitude}, 경도: ${location.longitude}"
        marker.map = naverMap
        //마커
        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        naverMap.moveCamera(cameraUpdate)
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 5.0
        //marker.map = null
    }

    fun dataInit() {
        System.out.println("===== [YSY] Map-matching PilotTest 2 =====")
        val dir = filesDir.absolutePath //파일절대경로
        FileIO.setDir(dir)

        // 파일에서 읽어와 도로네트워크 생성
        FileIO.generateRoadNetwork()
        RoadNetwork.printRoadNetwork(); // POI 잘 읽은 것 확인 완료!

        val testNo = 4 // 여기만 바꿔주면 됨 (PilotTest 2는 data 1만 존재)
        routePointArrayList = RoadNetwork.routePoints(testNo)

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

    //자바 복붙하면 자동으로 코틀린으로 바꿔줌.. 신기해
    // pilot test 1-2의 main 가져옴
    fun main() {
        // 1: 원래 하던대로 (표준편차 4)  | 2: x혹은 y좌표만 uniform하게(hor, ver, dia에 따라서)
        // 3: x, y 모두 uniform하게     | 4: 교수님이 말한 평균 4 방식

        // test 번호에 맞는 routePoints생성


        val gpsGenMode = 2
        println("Fixed Sliding Window Viterbi (window size: 3)")
        for (i in routePointArrayList.indices step (5)) {
            var point: Point = routePointArrayList.get(i)
            println("routePoint: " + point)
            printPoint(point, Color.YELLOW)
        }

        for (i in routePointArrayList.indices step (5)) {
            // 오래 걸리는 작업 수행부분
            var point: Point = routePointArrayList.get(i)
            val gpsPoint = GPSPoint(
                    timestamp,
                    point,
                    gpsGenMode,
                    3,
                    RoadNetwork.getLink(point.linkID).itLooksLike
            )
            printPoint(gpsPoint.point, Color.RED) // 생성된 GPS출력(빨간색)

            println("[MAIN] GPS: $gpsPoint")
            gpsPointArrayList.add(gpsPoint)
            timestamp++
            //System.out.println(gpsPoint); //gps point 제대로 생성 되는지 확인차 넣음
            val candidates: ArrayList<Candidate> = ArrayList()
            candidates.addAll(
                    Candidate.findRadiusCandidate(
                            gpsPointArrayList, matchingCandiArrayList,
                            gpsPoint.point, 50, timestamp, emission, transition
                    )
            )

            //세정 비터비x 출력
            //printMatched(matchingCandiArrayList, Color.GRAY, 50 )

            println(">>>> [MAIN] candidates <<<<")
            for (candidate in candidates) {
                println("  $candidate")
            }
            println(">>>>>>>>>>>>>><<<<<<<<<<<<<")

            ///////////// FSW VITERBI /////////////
            subGPSs.add(gpsPoint)
            arrOfCandidates.add(candidates)

            //subRPA.add(point); // 비터비 내부 보려면 이것도 주석 해제해야!

            ///////////////////matching 진행하는 부분분//////////////////
            //처음 부분 3번은 제일 가까운 candidate에 매칭 (ep)
            if (timestamp == 1) {
                // 마지막 candidates 중 prob가 가장 높은 것 max_last_candi에 저장
                var max_last_candi: Candidate? = Candidate()
                var max_prob = 0.0
                for (candidate in candidates) {
                    if (max_prob < candidate.ep) {
                        max_prob = candidate.ep
                        max_last_candi = candidate
                    }
                }
                FSWViterbi.setMatched_sjtp(max_last_candi) //가장 ep가 높은 candidate 매칭

                Emission.Emission_Median(FSWViterbi.getMatched_sjtp().get(0))
                //median값 저장

            } else if (timestamp <= 3) {
                // 마지막 candidates 중 prob가 가장 높은 것 max_last_candi에 저장
                var max_last_candi: Candidate? = Candidate()
                var max_prob = 0.0
                for (candidate in candidates) {
                    if (max_prob < candidate.ep) {
                        max_prob = candidate.ep
                        max_last_candi = candidate
                    }
                }
                FSWViterbi.setMatched_sjtp(max_last_candi) //가장 ep가 높은 candidate 매칭

                var tp = 0.0;
                tp = Transition.Transition_pro(subGPSs[timestamp - 2].point, subGPSs[timestamp - 1].point, FSWViterbi.getMatched_sjtp().get(timestamp - 2), FSWViterbi.getMatched_sjtp().get(timestamp - 1))
                FSWViterbi.getMatched_sjtp().get(timestamp - 2).setTp(tp)

                Emission.Emission_Median(FSWViterbi.getMatched_sjtp().get(timestamp - 1)) //매칭된 candidate의 median값 저장
                Transition.Transition_Median(FSWViterbi.getMatched_sjtp().get(timestamp - 2)) //매칭된 candidate와 그 전 매칭된 tp의 median값 저장

                if (timestamp == 3) {
                    subGPSs.clear()
                    arrOfCandidates.clear()
                    //저장되어있던 gps, candidates 삭제
                }
            } else {
                if (subGPSs.size == wSize) {
                    println("===== VITERBI start ====")
                    /*println("----- yhtp ------")
                    FSWViterbi.generateMatched(
                            tp_matrix,
                            wSize,
                            arrOfCandidates,
                            gpsPointArrayList, *//* subRPA, subGPSs,*//*
                            transition,
                            timestamp,
                            roadNetwork,
                            "yh",
                    )*/
                    println("----- sjtp ------")
                    FSWViterbi.generateMatched(
                            wSize,
                            arrOfCandidates,
                            gpsPointArrayList,
                            transition,
                            timestamp
                    )
                    subGPSs.clear()
                    arrOfCandidates.clear()
                    //subRPA.clear(); // 비터비 내부 보려면 이것도 주석 해제해야!
                    subGPSs.add(gpsPoint)
                    arrOfCandidates.add(candidates)
                    //subRPA.add(point); // 비터비 내부 보려면 이것도 주석 해제해야!

                    println("===== VITERBI end ====")
                }
                ///////////////////////////////////////
            }
        }
        // yhtp 이용해서 구한 subpath 출력
        //FSWViterbi.printSubpath(wSize, "yh")

        // sjtp 이용해서 구한 subpath 출력
        //FSWViterbi.printSubpath(wSize, "sj")

        // origin->생성 gps-> yhtp 이용해서 구한 matched 출력 및 정확도 확인
        //FSWViterbi.test(roadNetwork, "yh")

        // origin->생성 gps-> sjtp 이용해서 구한 matched 출력 및 정확도 확인
        FSWViterbi.test("sj")

        // 윤혜tp와 세정tp비교!
        //FSWViterbi.compareYHandSJ()

        // 어차피 결과가 같아서 출력은 하나만
        fun writeTextFile(directory: String, filename: String, content: String) {
            val dir = File(directory)

            if (!dir.exists()) { //dir이 존재 하지 않을때
                dir.mkdirs() //mkdirs : 중간에 directory가 없어도 생성됨
            }

            val writer = FileWriter(directory + "/" + filename, true)
            //true는 파일을 이어쓰는 것을 의미

            //쓰기 속도 향상
            val buffer = BufferedWriter(writer)
            buffer.write(content)
            buffer.close()
        }

        /*
        yh_tp.setOnClickListener{
            printMatched(FSWViterbi.getMatched_yhtp(), Color.BLUE, 50) // 윤혜 매칭: 파란색
        }*/

        // 경로안내 출력


        /*var i: Int = 0;
        *//*for (c in FSWViterbi.getMatched_sjtp()) {
            println("$i] matched: $c")
            printPoint(c.point, Color.BLUE);
            i++;
        }*/

        /*for (c in FSWViterbi.getMatched_sjtp()) {
            printPoint(c.point);
        }*/

    }

    fun printPoint(point: Point, COLOR: Int) {
        val marker = Marker() //좌표
        marker.position = LatLng(
                point.y,
                point.x
        )
        marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
        marker.iconTintColor = COLOR //색 덧입히기
        marker.width = 30
        marker.height = 30
        // 마커가 너무 커서 크기 지정해줌
        marker.map = naverMap //navermap에 출력
        var cameraUpdate = CameraUpdate.scrollAndZoomTo(
                LatLng(
                        point.y,
                        point.x
                ), 17.0
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동

    }

    // 생성된 GPS를 지도 위에 출력하는 함수
    fun printsubGPSs(subGPSs: ArrayList<GPSPoint>) {
        for (i in subGPSs.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                    subGPSs.get(i).x,
                    subGPSs.get(i).y
            ) //node 좌표 출력
            marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
            marker.iconTintColor = Color.RED //색 덧입히기
            marker.width = 20
            marker.height = 20
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollTo(
                LatLng(
                        subGPSs.get(0).x,
                        subGPSs.get(0).y
                )
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동
    }

    fun printMatched(matched: ArrayList<Candidate>, COLOR: Int, SIZE: Int) {
        for (i in matched.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                    matched.get(i).point.y,
                    matched.get(i).point.x
            ) //node 좌표 출력
            marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
            marker.iconTintColor = COLOR //색 덧입히기
            marker.width = SIZE
            marker.height = SIZE
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollAndZoomTo(
                LatLng(
                        matched.get(0).point.y,
                        matched.get(0).point.x
                ), 18.0
        )
        naverMap.moveCamera(cameraUpdate)

        //카메라 이동
    }

    fun removematched(matched: ArrayList<Candidate>) {
        for (i in matched.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                    matched.get(i).point.y,
                    matched.get(i).point.x
            ) //node 좌표

            marker.map = null
        }
    }

    //Node(좌표)를 지도위에 출력하는 함수
    fun getNodePrint(roadNetwork: RoadNetwork) {
        for (i in RoadNetwork.nodeArrayList.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                    RoadNetwork.getNode(i).coordinate.x,
                    RoadNetwork.getNode(i).coordinate.y
            ) //node 좌표 출력
            marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
            marker.iconTintColor = Color.BLACK //색 덧입히기
            marker.width = 30
            marker.height = 50
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollTo(
                LatLng(
                        RoadNetwork.getNode(0).coordinate.x, RoadNetwork.getNode(
                        0
                ).coordinate.y
                )
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동
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
            marker.icon = OverlayImage.fromResource(R.drawable.guidance_node_green)
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
        path.setColor(Color.rgb(255, 192, 0));
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