# 클라이언트기반 도보 내비게이션 애플리케이션
#### 개요 : 오차가 있는 GPS를 실제 위치로 이동시키는 맵매칭 알고리즘을 개발하여 적용한 turn-by-turn 도보 내비게이션

  <br/>
  
## 🔸 서비스 기능
<p float="left" align="center">
  <img src="https://user-images.githubusercontent.com/66414115/152907175-a08975ba-974f-4292-94b8-3f87cd1e5f15.png" width="550" />
  <img src="https://s3.us-west-2.amazonaws.com/secure.notion-static.com/e8693002-6d4a-4ae8-9aff-765e3a82e121/Untitled.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220208%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220208T015448Z&X-Amz-Expires=86400&X-Amz-Signature=24a669d799ed5d12d48454b13d8bb70fa76030499f230ea79784fe444a251aef&X-Amz-SignedHeaders=host&response-content-disposition=filename%20%3D%22Untitled.png%22&x-id=GetObject" width="350" />
</p>
  
* HMM과 Viterbi 알고리즘을 이용하여 오차가 있는 GPS를 실제 위치로 이동시키는 맵매칭 알고리즘 구현  
* Dijkstra 알고리즘을 이용하여 최단 경로 찾기 기능 구현  
* 파악된 현재 위치를 바탕으로 적절한 타이밍에 안내를 하는 Turn-By-turn 안내기능 구현  
* 결과  
  - 기존 도보 내비게이션과 차별화된 알고리즘 적용으로 정확한 현재 위치 파악
    - 기존 HMM과 Viterbi 알고리즘만 사용한 알고리즘 보다 10%p 오차율이 감소한 것으로 확인
  - 음성 길 안내로 사용자의 불편함 감소와 시간 절약
<br/>
<br/>  

## 🔸 기술 스택
- Tool : Android
- anguage : Kotlin, JAVA
- API : NAVER MAP API
  
<br/>
<br/>

## 🔸 시스템 구조도
<P align="center" ><img src="https://s3.us-west-2.amazonaws.com/secure.notion-static.com/66d049cc-1e27-4524-817a-e04d3f57fc30/Untitled.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220208%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220208T021218Z&X-Amz-Expires=86400&X-Amz-Signature=000f62680241d74ed9269368e5249eb46dfcaf37dac71a25e720bc302422ed04&X-Amz-SignedHeaders=host&response-content-disposition=filename%20%3D%22Untitled.png%22&x-id=GetObject" width="550" height="300"/></P>

<br/>

## 🔸 적용 알고리즘(갈림길 알고리즘)
<P align="center" ><img src="https://user-images.githubusercontent.com/66414115/152906592-4b8b1e61-2fb7-4a1a-bfb4-0bf4c0d97535.png" width="700" /></>

<br/>
<br/>
  
## 🔸 시연영상
- youtube : [https://youtu.be/FhesmLq61jc](https://youtu.be/FhesmLq61jc)
- youtube (+ 설명) : [https://www.youtube.com/watch?v=HCWCosCenKc](https://www.youtube.com/watch?v=HCWCosCenKc)
  
<br/>
<br/>
  
## 🔸 논문

- 보행자 내비게이션 앱에서의 맵 매칭 정확도 향상을 위한 휴리스틱 알고리즘 : [https://www.dbpia.co.kr/journal/articleDetail?nodeId=NODE10583472](https://www.dbpia.co.kr/journal/articleDetail?nodeId=NODE10583472)

- pdf : [[학부생논문] 보행자 내비게이션 앱에서의 맵 매칭 정확도 향상을 위한 휴리스틱 알고리즘.pdf](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/6853f8e1-8cb4-4747-8606-2879c64f4470/학부생논문_보행자_내비게이션_앱에서의_맵_매칭_정확도_향상을_위한_휴리스틱_알고리즘.pdf)
  
<br/>
  
## 🔸 프로젝트 기간 & 팀원 역할

- 팀 구성 : 최윤혜, 박유림, 홍세정
- 프로젝트 기간 : 2020.06.29 ~ 2021,06,03
  
- 최윤혜 : 
    - FSW Viterbi 알고리즘 구현
    - turn-by-turn 경로 안내 구현
    - 앱 화면 구현
- 박유림 : 
    - 보완 알고리즘(1) : 벡터의 각도 차이를 이용한 알고리즘
    - 앱 화면 구현
    - 경로 찾기 구현
- 홍세정 : 
    - 보완 알고리즘(2) : 갈림길 이후 GPS 데이터와 링크 간 거리의 평균 이용한 알고리즘
    - 도로 데이터 생성
    - 지도 : 네이버 지도 API 및 현재 위치 받아오기
  
<br/>
