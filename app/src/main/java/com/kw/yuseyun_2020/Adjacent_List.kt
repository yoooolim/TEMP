/*package com.kw.yuseyun_2020

import android.graphics.Color
import android.util.Pair
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons

class Adjacent_List(naverMap: NaverMap) {
    private lateinit var naverMap: NaverMap

    fun Adjacent_List(naverMap: NaverMap, dir: String): RoadNetwork? {
        val testNo = 1 // 여기만 바꿔주면 됨 (PilotTest 2는 data 1만 존재)

        val fileIO = FileIO(dir) // 파일에서 읽어와 도로네트워크 생성
        val roadNetwork = fileIO.generateRoadNetwork()

        // Link와 Node를 바탕으로 Adjacent List 구축
        val heads: ArrayList<AdjacentNode> = ArrayList()
        for (i in roadNetwork.nodeArrayList.indices) {
            val headNode = AdjacentNode(roadNetwork.nodeArrayList[i])
            heads.add(headNode)
            val adjacentLink: MutableList<Pair<Link, Int>>? =
                    roadNetwork.getLink1(headNode.node.nodeID) //mutableList?
            if (adjacentLink != null) { //안전하게 하기 위함
                if (adjacentLink.size == 0) continue
            }
            var ptr = headNode
            if (adjacentLink != null) { //안전하게 하기 위함
                for (j in adjacentLink.indices) {
                    val addNode = AdjacentNode(
                            roadNetwork.getNode(adjacentLink[j].second), adjacentLink[j].first
                    )
                    ptr.nextNode = addNode
                    ptr = ptr.nextNode
                }
            }
        }
        //도로네트워크 생성

        //getNodePrint(roadNetwork, naverMap) //노드 출력
        return roadNetwork
    }

    fun dataInit(naverMap: NaverMap, dir: String) {

        System.out.println("===== [YSY] Map-matching PilotTest 2 =====")
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

}
*/
