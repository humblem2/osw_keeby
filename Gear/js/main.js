

/**
 * S코치 
 * consumer 타입 앱
 * 기어2, 기어S, 기어S2 기반 앱.
 * @author 조수현
 * tizen sdk 2.3.1 ver 
 * (c) ~ 2014, 조수현
 */

////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
/// Field Definition
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
var SAAgent = null; // SAAgent 객체 할당 변수. 안드로이드 단말기 찾는 객체
var SASocket = null; // 안드로이드 단말기와 연결하는 소켓
var CHANNELID = 104; // 소켓연결 채널 ID
var ProviderAppName = "HelloAccessoryProvider"; // 앱이름
var endButtonStatus = "0"; // 정지버튼 클릭 안된상태 
var finalValueFromAndroid = "0"; // 단말기에서 기어로 서버로부터의결과값 보낸 상태. 동시에 SAP통신 소켓 아직 안 닫힌 상태 ==> "1"  



/**
 * classpath:index.html 의 resultBoard라는 id에 Element생성
 *
 * @param log_string 안드로이드 단말기에서 받은 Data
 * 
 * onreceive() function에서 Dependecy한다.
 */
function createHTML(log_string)
{
	var log = document.getElementById('resultBoard'); // resultBoard라는 id에
	log.innerHTML = log_string; // 엘리먼트 생성
}


/**
 * gear단말기에서 안드로이드 단말기로 SAP 소켓 연결요청 할때 Error 발생시 Callback function
 *
 * @param err 에러 정보
 * 
 * onreceive() function에서 Dependecy한다.
 */
function onerror(err) 
{
	console.log("err [" + err + "]"); // 콘솔창에 디버깅
}



// peerAgentFindCallback변수가 dependency
// agentCallback  콜백 객체
var agentCallback = {
	onconnect : function(socket) {
		SASocket = socket; // socket(소켓)객체 담는다.
		
		// 성공실패시 행 될 함수
		SASocket.setSocketStatusListener(function(reason){
			console.log("Service connection lost, Reason : [" + reason + "]");  
			
			disconnect(); 
			
		});
		
		
		fetch(); //  추가 ==> 운동하기버튼 클릭과 동시에 센서값 획득 
		timer = setInterval(fetch,refresh); // fetch()라는 함수가 refresh(0.2초)마다 실행됨.
		realTimer = setInterval(setTimerTest,refresh);//setTimerTest() 함수가 refresh(0.2초)마다 실행됨.
		
		
	},
	onerror : onerror
};

// onsuccess()함수에서 peerAgentFindCallback변수 dependency
var peerAgentFindCallback = {
	onpeeragentfound : function(peerAgent) {
		try 
		{
			if (peerAgent.appName == ProviderAppName) 
			{
				SAAgent.setServiceConnectionListener(agentCallback); // 리스너 대기 상태 
				SAAgent.requestServiceConnection(peerAgent); // 연결 요청
			} 
			else 
			{
				alert("Not expected app!! : " + peerAgent.appName); // 에러시 얼럿창
			}
			// end of if-else
		} 
		catch(err)
		{
			console.log("exception [" + err.name + "] msg[" + err.message + "]"); // 예외처리
		}
		// end of try-catch
	},
	onerror : onerror
}

/**
 * classpath:index.html의 'START'버튼 클릭시 실행되는 function. 이 함수가 onsuccess()함수 dependency
 * @param agents 접속할 기기 정보
 * connect() function이 Dependecy.
 */
function onsuccess(agents) 
{
	try 
	{
		if (agents.length > 0) 
		{
			SAAgent = agents[0]; // 첫번째 요소를 객체에 할당
			
			SAAgent.setPeerAgentFindListener(peerAgentFindCallback); // peerAgentFindCallback 리스닝 중.. 대기
			SAAgent.findPeerAgents(); // 피어 단말을 찾음.
		}
		else
		{
			alert("Not found SAAgent!!"); // 에러시 얼럿창
		}
		// end of if-else
	} 
	catch(err)
	{
		console.log("exception [" + err.name + "] msg[" + err.message + "]"); // 예외처리
	}
	// end of try-catch
	
}
//end of onsuccess() function

/**
 * classpath:index.html의 'START'버튼 클릭시 실행되는 Callback function. 이 함수가 onsuccess()함수 dependency
 * @param 없음
 */
function connect() 
{
	// 안드로이드 에서 모든 데이터가 왔고 동시에 END버튼이 눌렸었더라면,
	if(finalValueFromAndroid == "1" && endButtonStatus == "1") 
	{
		
		// 초기화 작업
		// 이 변수들 값 초기상태인 0으로바꾸고(0의 의미: disconnect버튼 누르지 않고 소켓(=SASocket)은 닫혀(null 그리고 .close() 인 상태) 있다)
		finalValueFromAndroid = "0";
		endButtonStatus = "0"; // end-button 안눌린상태라는 의미로 초기화
		
		
		// 초기화 작업
		SAAgent = null; // Agent객체 초기화
		SASocket.close(); // 소켓 닫고
		SASocket = null; // null로 초기화
		
		try 
		{
			// SAP통신을 위한 소켓 연결
			webapis.sa.requestSAAgent(onsuccess, function (err) {
				console.log("err [" + err.name + "] msg[" + err.message + "]");
			});
			webapis.sa.requestSAAgent(onsuccess, function (err) {
				console.log("err [" + err.name + "] msg[" + err.message + "]");
			});
			
			// ==> Keeby추가 시작 2015-09-29-(화)
			tau.changePage("#beExercising"); // "real time plotting and exercise continue" 페이지로 이동
			
			// Real-time-plotting(RTP객체) 생성
			smoothie = new SmoothieChart({millisPerPixel:5,horizontalLines:[{color:'#ffffff',lineWidth:1,value:0},{color:'#880000',lineWidth:2,value:3333},{color:'#880000',lineWidth:2,value:-3333}]});
			
			// TimeSeries 객체
			tempAx = new TimeSeries();
			tempRotx =  new TimeSeries();
			
			// TimeSeries객체에 append
			threadRTP = setInterval(function() {
				tempAx.append(new Date().getTime(), ax); // 시간 , 가속도X
				tempRotx.append(new Date().getTime(), rotx); // 시간 , 자이로X
			},refresh);
			
			smoothie.addTimeSeries(tempAx, { strokeStyle: 'rgba(0, 255, 0, 1)', fillStyle: 'rgba(100, 255, 100, 0.2)', lineWidth: 5 }); // RTP캔버스 디자인 및 속성 setting
			smoothie.addTimeSeries(tempRotx, { strokeStyle:'rgb(255, 0, 255)', fillStyle:'rgba(255, 0, 255, 0.3)', lineWidth:5 }); // RTP캔버스 디자인 및 속성 setting
			
			smoothie.streamTo(document.getElementById("mycanvas"),refresh); // html Element에 RTP객체(canvas객체) 생성
			
		} 
		catch(err) 
		{
			console.log("exception [" + err.name + "] msg[" + err.message + "]");
		}
		// end of try-catch
	} // end of if
	else // 처음 START버튼 누르면
	{
		
		// SAP통신을 위한 소켓 연결
		try 
		{
			webapis.sa.requestSAAgent(onsuccess, function (err) {
				console.log("err [" + err.name + "] msg[" + err.message + "]");
			});
		} 
		catch(err) 
		{
			console.log("exception [" + err.name + "] msg[" + err.message + "]");
		}
		// end of try-catch
		
		tau.changePage("#beExercising"); // "real time plotting and exercise continue" 페이지로 이동
		
		// Real-time-plotting
		smoothie = new SmoothieChart({millisPerPixel:5,horizontalLines:[{color:'#ffffff',lineWidth:1,value:0},{color:'#880000',lineWidth:2,value:3333},{color:'#880000',lineWidth:2,value:-3333}]});
		
		// TimeSeries 객체
		tempAx = new TimeSeries();
		tempRotx =  new TimeSeries();
		
		// TimeSeries객체에 append
		threadRTP = setInterval(function() {
			tempAx.append(new Date().getTime(), ax); // 시간 , 가속도X
			tempRotx.append(new Date().getTime(), rotx); // 시간 , 자이로X
		},refresh);
		
		smoothie.addTimeSeries(tempAx, { strokeStyle: 'rgba(0, 255, 0, 1)', fillStyle: 'rgba(100, 255, 100, 0.2)', lineWidth: 5 }); // RTP캔버스 디자인 및 속성 setting
		smoothie.addTimeSeries(tempRotx, { strokeStyle:'rgb(255, 0, 255)', fillStyle:'rgba(255, 0, 255, 0.3)', lineWidth:5 }); // RTP캔버스 디자인 및 속성 setting
		
		smoothie.streamTo(document.getElementById("mycanvas"),refresh); // html Element에 RTP객체(canvas객체) 생성
		
	} 
	// end of else 
		
	
} //  end of connect() function


/**
 * classpath:index.html의 'END'버튼 클릭시 실행되는 Callback function. 이 함수가 onsuccess()함수 dependency
 * 종료버튼 눌러도 소켓은 안닫힌다.
 * 그럼 언제 닫히냐? 다시 운동 시작하려고
 * connect()버튼 눌렀을때 잠깐 닫혔다가 다시 소켓 연다.
 * @param 없음
 */
function disconnect() 
{
	
	// "real time plotting and exercise continue" 페이지로 이동
	tau.changePage("#main"); 
	
	
	try 
	{
		if (SASocket != null) // 소켓객체가 존재하고
		{
			if(endButtonStatus == "0") // 종료버튼 누른적이 없으면
			{
				// 종료버튼 눌렀다("1")
				endButtonStatus = "1"; 
				
				if(endButtonStatus == 1) // 종료버튼 눌렀으면
				{
					SASocket.sendData(CHANNELID, endButtonStatus); // 종료버튼 눌렀다는 데이터 단말기로 보낸다
				}
				
				// 센서값 획득 기능 하는 함수 중지
				clearInterval(timer); // ==> js쓰레드인 setInterval 해제 timer객체 중지 
				
				// RTP(리얼타임플롯팅) 중지
				clearInterval(threadRTP); // 
				
				// RTP 관련 지역변수들 초기화 작업
				var smoothie = null; // RTP 차트 객체
				var tempAx = ""; // RTP 가속도x
				var tempRotx = ""; // RTP 자이로x
				var threadRTP = null; // 쓰레드객체
				
				finalValueFromAndroid = "1"; // 안드로이드가 보낸 데이터 문제없이 받았다는 상태로 setting
				
			} // end of if
			
		} // end of if
	} // end of try
	catch(err)
	{
		console.log("exception [" + err.name + "] msg[" + err.message + "]");
	}
	// end of try-catch
}

/**
 * channelID에 접속한 기기로부터 data 받는다
 * data 받아서 HTML엘리먼트 생성
 * @param channelId 접속한 안드로이드 단말기 채널 ID
 * @param data 안드로이드로부터 받은 Data
 * 
 */
function onreceive(channelId, data) 
{
	
	createHTML(data); // data 받아서 HTML엘리먼트 생성
	
}

/**
 * 소켓(SASocket) 열리고
 * 안드로이드 단말기와 데이터 주고 (받는) 부분
 */
function fetch() 
{
	try 
	{
		SASocket.setDataReceiveListener(onreceive); // SASocket변수에 리스너 담는다.
		
		// 센서값 추가부분 시작
		var sendAccelMsg = ax+' '+ay+' '+az+' '+rotx+' '+roty+' '+rotz+' '+currentTime; // 센서값 6개와 시간이 흐른 정도 
		
		// 센서값인 sendAccelMsg
		SASocket.sendData(CHANNELID, sendAccelMsg); // 호스트 디바이스인 안드로이드 단말기로 데이터를 실시간으로 보내는 함수 ==>  sendData(int,DOMString)

	} 
	catch(err) 
	{
		console.log("exception [" + err.name + "] msg[" + err.message + "]");
	}
	
}// end of fetch() function


////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
/// Field Definition
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
var refresh = 200; // 0.2초
var timer; // 쓰레드 객체 저장

var realTimer;  // 쓰레드 객체 저장
var currentTime = 0; // 운동시간


/**
 * 운동시간 측정 함수
 */
function setTimerTest()
{
	currentTime += 0.2;
}

/**
 * 앱을 처음 키면 LOAD
 */
window.onload = function () {
	
	
    // add eventListener for tizenhwkey
    document.addEventListener('tizenhwkey', function(e) {
        if(e.keyName == "back") // 뒤로가기 이벤트시 
            tizen.application.getCurrentApplication().exit(); // 앱 종료
    });
};


////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
/// Field Definition
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
var ax; // 가속도센서 X축
var ay; // 가속도센서 Y축
var az; // 가속도센서 Z축
var rotx; // 자이로센서 X축
var roty; // 자이로센서 Y축
var rotz; // 자이로센서 Z축


/**
 * 이벤트리스너로 등록
 * tizen sdk 2.3.1 devicemotion API
 * classpath:congig.xml 
 * <tizen:privilege name="http://tizen.org/feature/sensor.accelerometer"/> ,
 * <tizen:privilege name="http://tizen.org/feature/sensor.gyroscope"/> 추가.
 * 
 * 센서값 6개 획득 해서 변수에 할당
 */
window.addEventListener('devicemotion',function(e){
	ax = e.accelerationIncludingGravity.x;
	ay = -e.accelerationIncludingGravity.y;
	az = -e.accelerationIncludingGravity.z;
	rotx = e.rotationRate.alpha;
	roty = e.rotationRate.beta;
	rotz = e.rotationRate.gamma;
});

////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
/// Field Definition
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
var smoothie; // 리얼타임플로팅(RTP 파형) 객체
var tempAx; // RTP 가속도 X
var tempRotx; // RTP 자이로 Y

var threadRTP; // RTP 쓰레드 객체 할당 할 곳. 쓰레드 제어하기위해서 



