package com.samsung.android.sdk.accessory.example.helloaccessoryprovider;

// JavaSE
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import javax.security.cert.X509Certificate;

// Android
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

// SAP
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAAuthenticationToken;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

// HttpPost
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;



/**
 * 삼성 갤럭시 기어 앱 Type중 Companion앱.
 * 안드로이드 단말기에서 백그라운드 Service로
 * 수행되는 Class Defenition.
 * @apptype companion
 * @role provider
 * @since 2015-10-02
 * @author user 조수현
 * @version Scoach 5.0
 */
public class HelloAccessoryProviderService extends SAAgent
{
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /// Field Definition
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Socket socket;  //소켓생성 ==> 관련된것 다 주석 처리함... 2015-09-09 (수)
    private BufferedWriter br;
    public final static String MYTAG2 = "shTest02";
    private String ip = "103.49.44.7"; // IP  ==> 유동적 ==> 데이터 모을 때 *.java 프로그램(ServerSide.java)과 통신.
    private int port = 8080; // PORT번호 ==>  주의: Tomcat(WAS)이 Access하는 port번호랑 겹치므로 TOMCAT 중단하고 해야 됨.

    public static final int HELLOACCESSORY_CHANNEL_ID = 104; // 앱 채널 ID
    public static final String TAG = "HelloAccessoryProviderService"; // 앱이름 태그
    public Boolean isAuthentication = false;// 기어 제품과 연결된 상태 저장
    public Context mContext = null;// 컨텍스트(Context) 객체 할당
    public HelloAccessoryProviderConnection mMyConnection; // HelloAccessoryProviderConnection 객체. 기어와 연결 할때 필요
    private final IBinder m_binder = new LocalBinder(); // Ibinder 객체. 인턴트로 액티비티티와 서비스 연결 및 자원 공유

    HashMap<Integer, HelloAccessoryProviderConnection> mConnectionsMap = null; // mConnectionsMap 객체
    HashMap<Integer, HelloAccessoryProviderConnection> mConnectionsMapCopy = null; // mConnectionsMap 객체 재 할당

    PriorityQueue<String> priorityQueue = new PriorityQueue<String>(); // SAP통신으로 기어2로부터 0.2초마다 넘어오는 7개-unseen-data(String형식)
    int noAccessCsvFile = 0; // 종료버튼 눌렀을 때, priorityQueue의 모든 Data-point를 CSV파일에 모두 Write하고, 파일에 접근(Access) 안하는 상태.
    int endButtonStatus = 0; // 기어2의 종료버튼 클릭 여부. 0 ==> 클릭 안됨 / 1 ==> 클릭 됨

    BufferedReader in = null; //stream 타입의 문자를 읽어서 저장할 수 있는 함수. ==>  완성된 CSV파일 읽을때 쓸꺼임 . 그다음은 웹서버로 파일자체를 보냄
    BufferedWriter bw = null; // CSV파일로 출력

    String columnName = "meanAccX,deviationAccX,varianceAccX,minAccX,maxAccX,amplitudeAccX,rmsAccx,"
            +"meanAccY,deviationAccY,varianceAccY,minAccY,maxAccY,amplitudeAccY,rmsAccY,"
            +"meanAccZ,deviationAccZ,varianceAccZ,minAccZ,maxAccZ,amplitudeAccZ,rmsAccZ,"
            +"meanGyroX,deviationGyroX,varianceGyroX,minGyroX,maxGyroX,amplitudeGyroX,rmsGyroX,"
            +"meanGyroY,deviationGyroY,varianceGyroY,minGyroY,maxGyroY,amplitudeGyroY,rmsGyroY,"
            +"meanGyroZ,deviationGyroZ,varianceGyroZ,minGyroZ,maxGyroZ,amplitudeGyroZ,rmsGyroZ,"; // Featrue CSV파일 Column명 ==> CSV파일에 write할것. 42-Demension

    double summationExerciseDuringTime = 0.; // 운동시간 모으는 필드
    List<Double> exerciseDuringTime = new ArrayList<Double>(); // 운동 지속 시간.
    double duringExerciseSummation = 0.; // exerciseDuringTime 필드를 summation(축적)

    String musicFromServer = ""; // 서버에서 추천 받은 노래 이름

    MediaPlayer mMediaPlayer = null; // MediaPlayer 객체
    String mMp3Path = ""; // musicFromServer를 재 할당
    String DEST_MP3_DIRECTORY = "/storage/emulated/legacy/Music/"; // 노래 파일 있는 Directory
    String result = null; // Intent로 Activity에서 보낸 값 get 할 필드 . onBind()에서 받는다.

    String DEST_DIRECTORY = null; // 내부 저장소에 File Read/Wirte 하는 directory 할당.

    private String exerciseName = null; // 운동 종류
    private String exerciseCount = null; // 운동 횟수
    private String exerciseKcal = null; // 소모 칼로리

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /// Constructor Definition
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public HelloAccessoryProviderService()
    {
        super(TAG, HelloAccessoryProviderConnection.class);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /// Method Definition
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////
    // getter setter method
    /////////////////////////

    public void setExerciseName(String exerciseName)
    {
        this.exerciseName = exerciseName;
    }
    public String getExerciseName()
    {
        return this.exerciseName;
    }

    public void setExerciseCount(String exerciseCount)
    {
        this.exerciseCount = exerciseCount;
    }
    public String getExerciseCount()
    {
        return  this.exerciseCount;
    }
    public void setExerciseKcal(String exerciseKcal)
    {
        this.exerciseKcal = exerciseKcal;
    }
    public String getExerciseKcal()
    {
        return this.exerciseKcal;
    }
    public void setMusicFromServer(String musicFromServer)
    {
        this.musicFromServer = musicFromServer;
    }
    public String getMusicFromServer()
    {
        return this.musicFromServer;
    }

    /////////////////////////////////////////
    // custom method (User-Defenition method)
    /////////////////////////////////////////

    /**
     * 각종 초기화 작업
     * strict mode 추가
     * @return void
     */
    @Override
    public void onCreate()
    {
        super.onCreate();

        SA mAccessory = new SA(); // SA 객체 생성

        try
        {
            mAccessory.initialize(this); // SA 객체 초기화
        }
        catch (SsdkUnsupportedException e)
        {
            // Error Handling
            e.printStackTrace();
        }
        catch (Exception e1)
        {
            // Error Handling
            e1.printStackTrace();
            stopSelf(); // 강제 스톱
        }

        /// 안드로이드 쓰레드 정책으로 인한 strict mode 추가
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /// 데이터 모을때 PC와의 연결 소켓 만드는 쓰레드 시작
        MainThread mainThread = new MainThread(); // 쓰레드 객체 생성
        mainThread.setDaemon(true); // 데몬 쓰레드로 설정
        mainThread.start(); // 쓰레드 시작

        //미디어플레이어를 초기화 = 미디어플레이어 객체 생성
        mMediaPlayer = new MediaPlayer();


    } //  end  of  onCreate()


    /**
     * PC 소켓서버(.java 프로그램)와 연결하는 소켓 생성 메소드
     * @param ip 아이피 주소
     * @param port 포트 번호
     * @return void
     */
    public void setSocket(String ip, int port) throws IOException
    {
        try
        {
            Log.d(MYTAG2,":: [자바 서버소켓하고 연결되는 socket객체 생성] ::");

            socket = new Socket(ip, port); // 해당 ip,port번호로 서버소켓에 연결하기위한 소켓 객체 생성
            br = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // 소켓서버로 데이터 출력하는 객체 생성
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }// end of setSocket() method

    /**
     * 데이터 모을 때 PC의 자바프로그램과 연결되는 소켓 생성 하는 쓰레드
     * @see com.samsung.android.sdk.accessory.example.helloaccessoryprovider.HelloAccessoryProviderService.MainThread
     * @see java.lang.Thread
     * @see java.lang.Runnable
     * @ this is class for using thread
     */
    class MainThread extends Thread
    {
        public void run()
        {
            try
            {
                setSocket(ip,port); // setSocket() method 호출
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            // end of try-catch
        }
        // end of run() method
    }
    // end of class (MainThread)


    /**
     * 서버로부터의 사용자 심박수와 연관된 BPM을 가진 노래(MP3) 재생 쓰레드
     * @ this is Thread for MediaPlayer Object in Android
     */
    Runnable mRun = new Runnable() {
        public void run()
        {
            try
            {
                // 미디어플레이어 재생
                mMediaPlayer.setDataSource(DEST_MP3_DIRECTORY+mMp3Path); // 노래 set
                mMediaPlayer.prepare(); // 대기
                mMediaPlayer.start(); // 시작

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            // end of try-catch
        }
        //end of run() method
    };

    /**
     * 액티비티와 Binding
     * @param arg0 서비스에 바인딩 할 인텐트 객체
     * @return IBinder m_binder
     */
    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.d(MYTAG2,"onBind() 메소드 들어옴");
        Log.d(MYTAG2,"액티비티에서 심은 값 , get 하려고 준비중 .. ");
        result = arg0.getStringExtra("dir"); // 액티비티에서 보낸 값 K로 get 한다.
        Log.d(MYTAG2,"액티비티에서 받은 값 >> ");
        Log.d(MYTAG2,result);

        return m_binder; // 바인더 객체 리턴
    }

    /**
     * 피어 기기 찾았을때 응답
     * @param arg0 SAP통신 Agent객체(SAPeerAgent객체)
     * @param arg1 응답시 성공 혹은 실패 정보
     * @return void
     */
    @Override
    protected void onFindPeerAgentResponse(SAPeerAgent arg0, int arg1)
    {
        // 필요시 정의
    }

    /**
     * 기어와 연결 되기위한 인증 절차
     * @param uPeerAgent  SAP통신 Agent객체(SAPeerAgent객체)
     * @param authToken 인증 토큰 객체
     * @param error 인증 시도시 에러 정보
     * @return void
     */
    protected void onAuthenticationResponse(SAPeerAgent uPeerAgent, SAAuthenticationToken authToken, int error)
    {
        Log.d(MYTAG2,"onAuthenticationResponse() 메소드 들어옴");

        if (authToken.getAuthenticationType() == SAAuthenticationToken.AUTHENTICATION_TYPE_CERTIFICATE_X509)
        {
            mContext = getApplicationContext(); // application 모든 자원(정보) 객체
            byte[] myAppKey = getApplicationCertificate(mContext); // application 인증서 객체

            if (authToken.getKey() != null)
            {
                boolean matched = true;
                if (authToken.getKey().length != myAppKey.length) // 앱키 없으면
                {
                    matched = false; // matched에 false 할당
                }
                else
                {
                    for (int i = 0; i < authToken.getKey().length; i++)
                    {
                        if (authToken.getKey()[i] != myAppKey[i])
                        {
                            matched = false;
                        }
                    }
                }
                if (matched) // 매칭 됬으면
                {
                    acceptServiceConnectionRequest(uPeerAgent); // uPeerAgent 연결 요청 수락
                }
            }
        }
        else if (authToken.getAuthenticationType() == SAAuthenticationToken.AUTHENTICATION_TYPE_NONE)
        {
            Log.d(MYTAG2, "#####6");
            Log.e(TAG, "onAuthenticationResponse : CERT_TYPE(NONE)");
        }
    }


    /**
     * 기어와 연결 성공시 callback method
     * @param peerAgent 연결할 Samsung Accessory Protocol 통신 피어 단말기(갤럭시 기어) 정보 객체
     * @param thisConnection SAP통신 SASocket 소켓 객체
     * @param result 연결 시도 결과
     * @return void
     */
    @Override
    protected void onServiceConnectionResponse(SAPeerAgent peerAgent, SASocket thisConnection, int result)
    {
        // 기어 단말기와 연결 성공 이면
        if (result == CONNECTION_SUCCESS)
        {
            // 그리고 thisConnection객체가 정상적으로 있다면
            if (thisConnection != null)
            {
                // thisConnection객체를 HelloAccessoryProviderConnection로 casting하여 할당. SASocket과 HelloAccessoryProviderConnection는 Generalization-Specialization 관계
                mMyConnection = (HelloAccessoryProviderConnection) thisConnection;

                if (mConnectionsMap == null)
                {
                    Log.d(MYTAG2,"@@@@@3");
                    mConnectionsMap = new HashMap<Integer, HelloAccessoryProviderConnection>(); // HashMap 객체 생성. HelloAccessoryProviderConnection객체를 Value로 set
                    mConnectionsMapCopy = mConnectionsMap; // 객체 복사
                }

                // mMyConnection 속성 set
                mMyConnection.mConnectionId = (int) (System.currentTimeMillis() & 255);
                mConnectionsMap.put(mMyConnection.mConnectionId, mMyConnection); // ConnectionId를 Key로 Connection객체라는 Value를 HashMap에 set

                mConnectionsMapCopy = mConnectionsMap;// 객체 복사

                // 액티비티를 보여주기 위해 startActivity하는 부분 추가
                Intent dialogIntent = new Intent(getBaseContext(), ClientSideActivity.class); // 액티비티와 서비스의 데이터 공유를 위한 인텐트 설정
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 인텐트에 Flag설정 추가
                getApplication().startActivity(dialogIntent); // Activity를 실행(Show)


            }
            // end of if

        }// end of if
        else if (result == CONNECTION_ALREADY_EXIST)
        {
            String temp = "onServiceConnectionResponse,CONNECTION_ALREADY_EXIST";
            Log.e(TAG, temp);
        }
        // end of if else

        // Here is no else

    }// end of onServiceConnectionResponse() method



    /**
     * 서비스(Service)와 액티비티(Activity) 바인딩
     * @param peerAgent 연결할 Samsung Accessory Protocol 통신 피어 단말기(갤럭시 기어) 정보 객체
     * @return void
     */
    @Override
    protected void onServiceConnectionRequested(SAPeerAgent peerAgent)
    {
        Log.d(MYTAG2,"onServiceConnectionRequested() 메소드 호출됨");

        isAuthentication = false; // 초기상태

        if (isAuthentication)
        {
            Log.d(MYTAG2,"#1");
            Toast.makeText(getBaseContext(), "Authentication On!", Toast.LENGTH_SHORT).show();
            authenticatePeerAgent(peerAgent);
        }
        else
        {
            Log.d(MYTAG2,"#2");
            Toast.makeText(getBaseContext(), "삼성 Gear와 연결 됬습니다.", Toast.LENGTH_SHORT).show(); // 연결시 액티비티에 Toast 메세지
            acceptServiceConnectionRequest(peerAgent);
        }
    }


    /**
     * 기어와의 연결에 필요한 인증 절차를 위한 ApplicationCertificate 획득 하는 method
     * @param context ApplicationContext 객체
     * @return byte[] cert
     */
    private static byte[] getApplicationCertificate(Context context)
    {
        Log.d(MYTAG2,"getApplicationCertificate() 메소드 호출됨");

        // context객체가 없으면
        if (context == null)
        {
            return null;
        }

        byte[] cert = null;
        String packageName = context.getPackageName(); // 기어가 연결요청한 안드로이드 앱 패키지 이름

        // context객체가 있으면
        if (context != null)
        {
            try
            {
                // 패키지 정보 할당
                PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                if (pkgInfo == null)
                {
                    return null;
                }

                // 인증된 서명 객체 획득
                Signature[] sigs = pkgInfo.signatures;
                if (sigs == null)
                {
                    Log.d(MYTAG2,"액티비티 연결");
                }
                else
                {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509"); // CertificateFactory에서 싱글톤 방식으로 CertificateFactory객체 생성
                    ByteArrayInputStream stream = new ByteArrayInputStream(sigs[0].toByteArray());
                    X509Certificate x509cert = X509Certificate.getInstance(stream); // X509Certificate객체 생성

                    cert = x509cert.getPublicKey().getEncoded(); // 인증서 인코딩 하여 할당
                }
            }
            catch (NameNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (CertificateException e)
            {
                e.printStackTrace();
            }
            catch (javax.security.cert.CertificateException e)
            {
                e.printStackTrace();
            }
            // end of try-catch

        } // end of if


        return cert; // 인증 코드 리턴

    } // end of getApplicationCertificate() method


    /**
     * 액티비티와 바인딩(Binding)
     * @see android.os.Binder
     * @see android.os.IBinder
     */
    public class LocalBinder extends Binder
    {
        /**
         * @return HelloAccessoryProviderService object
         */
        public HelloAccessoryProviderService getService()
        {
            Log.d(MYTAG2,"getService() 메소드 들어옴");
            return HelloAccessoryProviderService.this;
        }
    }

    /**
     * // 기어와 SAP통신을 위한 HelloAccessoryProviderConnection 연결 상태
     * @see com.samsung.android.sdk.accessory.SASocket
     * @version 2.3.1
     */
    public class HelloAccessoryProviderConnection extends SASocket
    {

        /// Field
        private int mConnectionId; // connection ID

        /// Constructor 초기화
        public HelloAccessoryProviderConnection()
        {
            super(HelloAccessoryProviderConnection.class.getName()); // SASocket의 Connection객체 생성
        }

        /**
         * Error 발생했을 때 필요한 작업 작성용..
         * @param channelId 접속 할 기어2 단말기 ID
         * @param errorString 접속시 에러 정보 - String
         * @param error  접속시 에러 정보 - int
         * @return void
         */
        @Override
        public void onError(int channelId, String errorString, int error)
        {
            // 에러 실행시킬 작업 필요시 작성 ..
        }


        /**
         * 기어2의 '종료 버튼' 눌렀을 때, '실행' 되는 method
         * 모아 뒀던 unseen-data(priorityQueue)를
         * 단말기 경로에 CSV(Comma Serperated Values)형식으로 Write
         * 웹서버와 HttpPost통신. request / response
         * json객체 set/get
         * 6개의 센서값 웹서버로 보내긴 Feature값(42차원) pre-processing
         * @return void
         */
        public void onEndButtonClicked()
        {
            /// onEndButtonClicked() method에서 사용되는 변수들임  ==> Feature관련 필드들
            double mean = 0.; // 평균
            double variance = 0.; // 분산
            double deviation = 0.; // 표준편차
            double min = 0.; // 최대값
            double max = 0.; // 최소값
            double amplitude = 0.; // 진폭
            double rms = 0.; // RMS값(Root Mean Square)
            double time = 0.; // 운동시간

            DEST_DIRECTORY = "/storage/emulated/legacy/"; // 내부저장소에 접근 하는 Directory

            try
            {
                // 해당경로(내부저장소)에 파일(CSV) 만들어 준다.<생성>
                bw = new BufferedWriter(new FileWriter(DEST_DIRECTORY+"unseendataFeature.csv",true));
                Log.d(MYTAG2,"csv file make SUCCESS.");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            noAccessCsvFile = 1; // 이 method에 진입 했다는 것은 CSV파일에 접근 불가.(writable상태 no readable..)

            try
            {
                bw.append(columnName); // 초기 한번만 Column명 출력
                bw.append("\n"); // 개행

                // priorityQueue에서 하나씩 뽑아서 CSV파일로 Write
                String[] splitPriorityQueue = null;
                int size = priorityQueue.size(); // 다 모은 raw-data 튜플 갯수

                //  priorityQueue를 Column별로 가공
                List<Double[]> list =  HelloAccessoryProviderService.columnArray(priorityQueue);

                int count = 0; // 운동시간 계산 타이밍

                // 운동시간은 독립적으로 계산.
                // 42차원(6 x 7) ==> Label까지 고려하면 43차원(43-Demension)의 Feature-Data.
                // Feature ==> 평균 / 분산 / 편차 / 최대값 / 최소값 / ㅣ 최대값 - 최소값 ㅣ= 진폭 / RMS
                for (Double[] doubles : list) // 차례대로 한 Column씩 빼서
                {
                    // 운동시간 계산
                    if(count == 6)
                    {
                        // 운동시간 계산
                        time = HelloAccessoryProviderService.calDuringExercise(doubles); // 데이터 모을때 쓰임.
                        break;
                    }

                    // CSV파일에 담길 42-Demension(42차원)의 Features Calculate method set
                    mean = HelloAccessoryProviderService.mean(doubles);
                    deviation = HelloAccessoryProviderService.standardDeviation(doubles, 0);
                    variance = HelloAccessoryProviderService.variance(doubles);
                    min = HelloAccessoryProviderService.valueMinMaxAmp(doubles, 0);
                    max = HelloAccessoryProviderService.valueMinMaxAmp(doubles, 1);
                    amplitude = HelloAccessoryProviderService.valueMinMaxAmp(doubles, 2);
                    rms = HelloAccessoryProviderService.valueRMS(doubles);

                    // CSV파일에 출력
                    bw.append(Double.toString(mean) + ",").append(deviation+",").append(variance+",").append(min+",").append(max+",").append(amplitude+",").append(rms+",");


                    count++;

                }// end of for


                // json객체 선언
                JSONObject json = new JSONObject();

                // 컬럼 별 6가지 raw-data가 담길 List
                List<String[]> columnList = new ArrayList<String[]>();
                columnList = HelloAccessoryProviderService.ColumnListForJson();

                bw.append("\n"); // 개행
                bw.flush(); // CSV파일로 unseendate에 대한 Feature(47차원) 생성 완료.
                bw.close(); // 자원 닫기

                // 다시 CSV파일 읽어 ==> 한줄(컬럼들)씩 읽어서 json에 put
                try
                {
                    /// json객체에 set할 file read
                    in = new BufferedReader(new FileReader(new File(DEST_DIRECTORY + "unseendataFeature.csv")));


                    //************ 디버깅 용 *************
                    String[] jsonKey = {"columns","features"};
                    int i = 0;

                    String line = null;
                    while((line = in.readLine()) != null) // 한줄씩 읽어서.
                    {
                        if(i == 0)
                        {
                            json.put(jsonKey[0], line); // json 에 set할 value에 해당 되는 K 생성
                            i++;
                        }
                        else
                        {
                            json.put(jsonKey[1],line); // json 에 set할 value에 해당 되는 K 생성
                        }

                    }

                    // 운동시간 계산
                    double exerciseDuringTimeMax = Collections.max(exerciseDuringTime); // 운동 처음 시간
                    double exerciseDuringTimeMin = Collections.min(exerciseDuringTime); // 운동 종료 시간
                    double duringExercise = exerciseDuringTimeMax -exerciseDuringTimeMin; // 차이

                    // 운동시 어느 임계시간에 도달하면 MP3파일 재생
                    duringExerciseSummation += duringExercise; // 단위: (초)

                    json.put("time", Double.toString(duringExercise)); // 운동시간
                    json.put("timesum",Double.toString(duringExerciseSummation)); // 운동시간 축적

                    // 현재 년/월/일
                    Calendar calendar = Calendar.getInstance();
                    json.put("year",Integer.toString(calendar.get(Calendar.YEAR))); // 년
                    json.put("month",Integer.toString(calendar.get(Calendar.MONTH)+1)); // 월
                    json.put("day",Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))); // 일

                    // json 끝

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        in.close(); // 자원 닫는다.
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                // end of try-catch-finally


                /// 웹서버로 보내는 부분 시작
                HttpClient client = new DefaultHttpClient(); // HttpClient객체 생성
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 20000); // Timeout Limit settting

                HttpResponse response; // resuqest에 대한 response 할당할 객체

                try
                {
                    /// ==> 분류기 3개
                    // HttpPost post = new HttpPost("http://52.89.71.89/predict/"); // 바뀌면 re-complie

                    /// ==> 분류기 1개
                    /// ==> 웹서버 URI. 요청 URL
                    HttpPost post = new HttpPost("http://52.89.71.89/predictoneclassifier/"); // HttpPost 객체 생성

                    StringEntity se = new StringEntity( json.toString() ); // HttpPost로 보낼 데이터(json) set.
                    se.setContentType( new BasicHeader( HTTP.CONTENT_TYPE, "application/json" ) ); // HTTP Header 설정
                    post.setEntity(se); // StringEntity객체 할당

                    response = client.execute(post);// 웹서버로 request하고  바로 resonse 받는다.

                    /*Checking response */
                    HttpEntity entity = response.getEntity(); // response에서 Entity객체 get 한다.

                    // 웹서버에서 전달 받은 response에 Entitiy가 있으면
                    if (entity != null)
                    {
                        String retSrc = EntityUtils.toString(entity); // String으로 캐스팅

                        // parsing JSON
                        JSONObject result = new JSONObject(retSrc); //Convert String to JSON Object

                        /// response 처리 ==> Casting (Object to String 으로)
                        String exerciseName = (String)result.get("exercisename"); // 무슨 운동
                        String exerciseCount = (String)result.get("exercisecount"); // 몇회
                        String exerciseKcal = (String)result.get("exercisekcal"); // 몇 칼로리
                        String musicFromServer = (String)result.get("musicfromserver"); //  서버 추천 음악

                        /// 안드로이드에서 기어로 결과값 보낼값 setting ussing setter getter method
                        /// Field에 값 셋팅. VO(Value-Object)객체 처럼 이용 한다.
                        setExerciseName(exerciseName.trim());
                        setExerciseCount(exerciseCount.trim());
                        setExerciseKcal(exerciseKcal.trim());
                        setMusicFromServer(musicFromServer.trim());

                        /// "서버에서 추천받은 음악이름이 있으면( 값이 "neednotmusic" 이 아니면.. )" 밑에 로직 실행.
                        if(getMusicFromServer() != "neednotmusic") // "서버에서 추천받은 음악이름이 있으면" 밑에 로직 실행됨. 서버에서 받은게 없으면 혹은 리셋됬으면 이거 안 통해
                        {
                            /// 노래 재생중일때는 밑에 로직 수행 x . 즉, 노래 재생중 아닐 때만 밑에 로직 수행
                            if(  !( mMediaPlayer.isPlaying() )  )
                            {

                                //미디어플레이어를 초기화 = 미디어플레이어 객체 생성
                                mMediaPlayer = new MediaPlayer();

                                mMp3Path = getMusicFromServer(); // 추천 음악 setting
                                File mp3file = new File(DEST_MP3_DIRECTORY+mMp3Path);

                                //mp3파일이 있으면
                                if (mp3file.exists())
                                {
                                    //쓰레드실행
                                    new Thread(mRun).start();

                                }
                                // end of if
                            }
                            // end of if

                        }
                        // end of if

                    }
                    // end of if

                }// end of try
                catch(Exception e)
                {
                    Log.d(MYTAG2,":: [Error] ::");
                    e.printStackTrace();
                }
                // end of try-catch

            }// end of try
            catch (Exception e)
            {
                e.printStackTrace();

            } // end of try - catch
            finally
            {
                try
                {
                    bw.close(); // 자원 닫기
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            // end of finally

            noAccessCsvFile = 0; //  기어에서 넘어온 값들이 위에있는 logic들 정상적으로 거쳐서 CSV파일에 담기고 난 뒤, writable안하는 상태를 0으로 표시

            // CSV파일(Feature==47차원)자체를 read() 읽어서 .. 웹서버로 보낼준비
            if(noAccessCsvFile == 0)
            {

                try
                {
                    // 이용한 CSV파일 내부경로에서 삭제 할 준비
                    in = new BufferedReader(new FileReader(new File(DEST_DIRECTORY + "unseendataFeature.csv")));

                    //************ 디버깅 용 *************
                    String line = null;
                    while((line = in.readLine()) != null)
                    {
                        Log.d(MYTAG2,line);
                    }
                    //************ 디버깅 용 *************

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        // 해당 디렉토리 CSV파일 삭제
                        File willBeDeletedFile = new File(DEST_DIRECTORY + "unseendataFeature.csv"); // 삭제 할 파일
                        boolean deleteSuccessStatus = willBeDeletedFile.delete(); // 파일 삭제

                        //************ 디버깅 용 *************
                        if(deleteSuccessStatus)
                        {
                            Log.d(MYTAG2,":: [내부저장소 디렉토리에서 파일 삭제 성공] ::");
                        }
                        else
                        {
                            Log.d(MYTAG2,":: [내부저장소 디렉토리에서 파일 삭제] ::");
                        }
                        //************ 디버깅 용 *************

                        in.close(); // 자원 닫는다

                    } // end of try
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    // end of try-catch
                }
                // end of finally


            }// end of if


            //  priorityQueue 초기화
            priorityQueue = null;
            priorityQueue = new PriorityQueue<String>();


        } // end of if


        /**
         * onReviece() 호출 될 떄 ==> fetch()함수 안에 있는 SASocket객체.
         * SASocket.sendData(채널 , 전달값) Function ==> fetch()함수가
         * 0.2초마다 호출되서 실행됨.
         * 기어2(HelloAccessoryConsumer)에서 SAP통신으로 0.2초 마다 마다,
         * 자이로스코프(X,Y,Z) / 가속도(X,Y,Z) / 센서 발생 주기(시간=t) ..
         * 총 7가지 unseen-data 받는다.
         * 갤럭시 기어의 sendData(CHANNELID, sendAccelMsg) 실행 될때 마다,
         * 콜백함수처럼 실행
         * @param channelId 접속 기어 단말기 ID
         * @param data 기어 단말기에서 받은 Data
         * @return void
         */
        @Override
        public void onReceive(int channelId, byte[] data) // data가 기어가 주는 값..
        {
            // 단순 운동 시간 summation 계산
            summationExerciseDuringTime = summationExerciseDuringTime + 0.2; // onReceive() method 통과 할 때마다 지역변수에 0.2초씩 할당
            exerciseDuringTime.add(summationExerciseDuringTime); // List에 추가

            //// 주석처리 ::  ==> 데이터 모으기 위해 부분
            //PrintWriter out = new PrintWriter(br, true); // onReceive() scope 안에서만 쓰는 변수 out ==> PC로 출력하기 위한.

            // 기어 단말기에서 온 데이터
            String return_msg = new String(data);

            // 만약 기어2의 종료버튼 눌렀다면..
            if( (new String(data)).equals("1") )
            {
                // [Process 1] :: CSV파일 경로에 만들고 ==> 피처계산하여 42차원 WRITE ==> CSV파일 자체 읽어서 ==> 웹서버로 보내고 ==> CSV삭제
                onEndButtonClicked(); // 이 method 실행!!

                int clearQue = priorityQueue.size();

                // [Process 2] :: (CSV파일 웹서버로 정상적으로 보냈다면 ..) Collection.PriorityQueue 객체 초기화. 왜냐면 다음의 데이터 받을 준비..
                priorityQueue.clear(); // Removes all of the elements from this priority queue.

                // 초기화
                exerciseDuringTime = null;
                exerciseDuringTime = new ArrayList<Double>();


                // 기어로 보낼 데이터 생성하는 Business Logic 수행
                HelloAccessoryProviderConnection uHandler = mConnectionsMapCopy.get(Integer.parseInt(String.valueOf(mConnectionId)));
                String strToUpdateUI = "";

                // Field 3개(exerciseName, exerciseCount, exerciseKcal)에 값이 null이 아니면, 3개 필드 동시에 null값이 아닌 어떤 '값들'(무슨운동/몇회 /몇칼로리)이 들어 있으면..
                if(getExerciseName() != null && getExerciseCount() != null && getExerciseKcal() != null) // 3개값의 boolean여부 다 확인해서 다 true 면, logic 실행
                {

                    strToUpdateUI += "<div><b>"+getExerciseName()+"</b></div>";
                    strToUpdateUI += "<div><b>"+getExerciseCount()+" 회</b></div>";
                    strToUpdateUI += "<div><b>"+getExerciseKcal()+" Kcal</b></div>";

                }

                onSend2(strToUpdateUI); // 기어로 보냄

                // 무슨운동 / 몇회 / 몇칼로리 ==> 3개 field 초기화
                setExerciseName(null);
                setExerciseCount(null);
                setExerciseKcal(null);

            } // end of if

            // ==> 데이터 모을 땐 잠시 주석 풀고 PC의 자바프로그램(소켓 서버)으로 출력
            //out.println(return_msg);

            priorityQueue.add(return_msg); // onRecive() method 들어올때마다 Queue에 담는다. (약 0.2초 주기)

        }// end of onReceive() method

        /**
         * 종료버튼 클릭하면 서버로직 에서 판단된 데이터를 받아서 "기어2, 기어S, 기어S2" 로 전송
         * @param a_message 기어 단말기로 보낼 DATA
         * @return void
         */
        public void onSend2 (String a_message)
        {
            Log.d(MYTAG2,":: [onSend2() 메소드 들어옴] ::");

            // 보낼 DATA
            final String message = a_message;

            // 접속 되어있는 기어 단말기 ID Map에 set한 객체
            final HelloAccessoryProviderConnection uHandler = mConnectionsMapCopy.get(Integer.parseInt(String.valueOf(mConnectionId)));

            // uHandler객체 없으면
            if(uHandler == null)
            {
                return; // 종료
            }

            // 쓰레드 생성. 기어로 데이터 보내는 쓰레드 작업 단위
            new Thread(new Runnable() {
                public void run()
                {
                    try
                    {
                        uHandler.send(HELLOACCESSORY_CHANNEL_ID, message.getBytes()); // 보낼 단말기 ID와 보낼 DATA 필요
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    // end of try-catch
                }
                // end of run() method

            }).start(); // 쓰레드 시작

            Log.d(MYTAG2, "onSend2() 메소드 나 감....");

        } //  end of onSend2() method


        /**
         * 종료버튼 클릭하면 서버로직 에서 판단된 데이터를 받아서 "기어2, 기어S, 기어S2" 로 전송
         * 필요시 다른 데이터를 이 method 로 보내면 됨
         * @param a_message 기어 단말기로 보낼 DATA
         * @return void
         */
        /// onSend() 메소드 추가 Keeby ==> 필요시 다른 데이터를 이 method 로 보내면 됨
        public  void onSend (String a_message)
        {
            // 보낼 DATA
            final String message = a_message;

            final HelloAccessoryProviderConnection uHandler = mConnectionsMap.get(Integer.parseInt(String.valueOf(mConnectionId))); // 접속 되어있는 기어 단말기 ID Map에 set한 객체

            // uHandler객체 없으면
            if(uHandler == null)
            {
                return; // 종료
            }

            // 쓰레드 생성. 기어로 데이터 보내는 쓰레드 작업 단위
            new Thread(new Runnable() {
                public void run()
                {

                    try
                    {
                        uHandler.send(HELLOACCESSORY_CHANNEL_ID, message.getBytes()); // 보낼 단말기 ID와 보낼 DATA 필요
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    // end of try-catch

                } // end of run()
            }).start(); // 쓰레드 시작

        }
        // end of onSend() method

        /**
         * Connection객체 destory됬을때
         * @param errorCode 에러 코드
         * @return void
         */
        @Override
        protected void onServiceConnectionLost(int errorCode)
        {
            // mConnectionsMap이 있으면
            if (mConnectionsMap != null)
            {
                mConnectionsMap.remove(mConnectionId); // 해당 id의 connection 객체 제거
            }
        }
        // end of onServiceConnectionLost() method


    } // end of HelloAccessoryProviderConnection



    /**
     * json의 K값으로 이용될 문자열 값 setting 하는 method
     * @return List<String[]>
     */
    public static List<String[]> ColumnListForJson()
    {
        // String array 초기화
        String[] columnArrayAccX = {"meanAccX","deviationAccX","varianceAccX","minAccX","maxAccX","amplitudeAccX","rmsAccX"}; // AccX 컬럼 관련 피쳐 7개
        String[] columnArrayAccY = {"meanAccY","deviationAccY","varianceAccY","minAccY","maxAccY","amplitudeAccY","rmsAccY"}; // AccY 컬럼 관련 피쳐 7개
        String[] columnArrayAccZ = {"meanAccZ","deviationAccZ","varianceAccZ","minAccZ","maxAccZ","amplitudeAccZ","rmsAccZ"}; // AccZ 컬럼 관련 피쳐 7개
        String[] columnArrayGyroX = {"meanGyroX","deviationGyroX","varianceGyroX","minGyroX","maxGyroX","amplitudeGyroX","rmsGyroX"}; // GyroZ 컬럼 관련 피쳐 7개
        String[] columnArrayGyroY = {"meanGyroY","deviationGyroY","varianceGyroY","minGyroY","maxGyroY","amplitudeGyroY","rmsGyroY"}; // GyroZ 컬럼 관련 피쳐 7개
        String[] columnArrayGyroZ = {"meanGyroZ","deviationGyroZ","varianceGyroZ","minGyroZ","maxGyroZ","amplitudeGyroZ","rmsGyroZ"}; // GyroZ 컬럼 관련 피쳐 7개

        // Column 할당 할 List 선언
        List<String[]> columnList = new ArrayList<String[]>();

        // List에 Array set
        columnList.add(0,columnArrayAccX); // K = V 로 add
        columnList.add(1,columnArrayAccY); // K = V 로 add
        columnList.add(2,columnArrayAccZ); // K = V 로 add
        columnList.add(3,columnArrayGyroX); // K = V 로 add
        columnList.add(4,columnArrayGyroY); // K = V 로 add
        columnList.add(5,columnArrayGyroZ); // K = V 로 add

        // 리턴
        return columnList;
    }

    /**
     * Collection객체들을 Column별로 쪼개어, javaSE의 array로 만드는 method
     * @param tempPriorityQueue 기어2에서 어떤 시간동안 획득한 raw-data(센서값)
     * @return List<Double[]>
     */
    public static List<Double[]> columnArray(PriorityQueue<String> tempPriorityQueue)
    {

        Double[] accX; // 가속도 X
        Double[] accY; // 가속도 Y
        Double[] accZ; // 가속도 Z

        Double[] gyroX; // 자이로 X
        Double[] gyroY; // 자이로 Y
        Double[] gyroZ; // 자이로 Z

        Double[] tempTime; // 운동 시간

        String oneLine = null; // PriorityQueue에서 한줄씩 읽을 값 할당

        List<Double> accXList = new ArrayList<Double>(); // 가속도 X
        List<Double> accYList = new ArrayList<Double>(); // 가속도 Y
        List<Double> accZList = new ArrayList<Double>(); // 가속도 Z

        List<Double> gyroXList = new ArrayList<Double>(); // 자이로 X
        List<Double> gyroYList = new ArrayList<Double>(); // 자이로 Y
        List<Double> gyroZList = new ArrayList<Double>(); // 자이로 Z

        List<Double> timeList = new ArrayList<Double>(); // 운동 시간



        while(true)
        {
            oneLine = tempPriorityQueue.poll(); // 한줄 읽어서  poll : Retrieves and removes the head of this queue, or returns null if this queue is empty.

            // 더 이상 PriorityQueue에서 get할 값 없다면
            if(oneLine == null)
            {
                break; // 종료
            }

            // 공백으로 split하여 array에 할당
            String[] tempArray = null;
            tempArray = oneLine.trim().split(" ");


            for(int i = 0 ; i < tempArray.length ; i++)
            {

                // 요소를 double로 캐스팅후 계속 Column별로 list에 추가.
                switch (i)
                {
                    case 0: accXList.add(Double.parseDouble(tempArray[i])); break;
                    case 1: accYList.add(Double.parseDouble(tempArray[i])); break;
                    case 2: accZList.add(Double.parseDouble(tempArray[i])); break;
                    case 3: gyroXList.add(Double.parseDouble(tempArray[i])); break;
                    case 4: gyroYList.add(Double.parseDouble(tempArray[i])); break;
                    case 5: gyroZList.add(Double.parseDouble(tempArray[i])); break;

                    case 6: timeList.add(Double.parseDouble(tempArray[i])); break; // 운동 시간
                }

            }// end of for

        }// end of while



        /// 리스트를 배열로 .. Converting

        // 3축가속도센서값
        accX = accXList.toArray(new Double[accXList.size()]);
        accY = accYList.toArray(new Double[accYList.size()]);
        accZ = accZList.toArray(new Double[accZList.size()]);
        // 3축자이로센서값
        gyroX = gyroXList.toArray(new Double[gyroXList.size()]);
        gyroY = gyroYList.toArray(new Double[gyroYList.size()]);
        gyroZ = gyroZList.toArray(new Double[gyroZList.size()]);

        tempTime = timeList.toArray(new Double[timeList.size()]); // 운동 시간


        /// 배열로 바뀐 값들 리스트에 넣어서 리턴 할 준비
        List<Double[]> list = new ArrayList<Double[]>();
        list.add(accX);
        list.add(accY);
        list.add(accZ);
        list.add(gyroX);
        list.add(gyroY);
        list.add(gyroZ);

        list.add(tempTime); // 운동 시간


        return list; // Cloumn을 배열(Array)로

    }

    /**
     * 산술 평균 계산 method
     * @param array 기어2에서 어떤 시간동안 획득한 raw-data(센서값)을 Column별로 나눈 값
     * @return double
     */
    public static double mean(Double[] array)
    {
        double sum = 0.0; // 합

        for (int i = 0; i < array.length; i++)
        {
            sum += array[i];
        }

        // 산술 평균
        return sum / array.length;
    }

    /**
     * 표준-편차 계산 method
     * @param array 기어2에서 어떤 시간동안 획득한 raw-data(센서값)
     * @param option 0 == 모집단의 표준편차 , 1 == 표본집단의 표준편차
     * @return double
     */
    public static double standardDeviation(Double[] array, int option)
    {
        if (array.length < 2) return Double.NaN; // 매개변수로 전달된 값이 1개이하이면 Double.NaN을 리턴

        double sum = 0.0; // 편차 제곱의 총합
        double deviation = 0.0; // 표준편차
        double diff;
        double meanValue = mean(array); // 평균(Mean) 계산

        // 분산 계산
        for (int i = 0; i < array.length; i++)
        {
            diff = array[i] - meanValue;
            sum += (diff * diff);
        }

        deviation = Math.sqrt(sum / (array.length - option));

        return deviation;
    }

    /**
     * 분산 계산 method
     * @param array 기어2에서 어떤 시간동안 획득한 raw-data(센서값)을 Column별로 나눈 값
     * @return double
     */
    public static double variance(Double[] array)
    {
        // 편차
        double deviation = standardDeviation(array, 0);

        // 분산
        double variance = Math.pow(deviation, 2);

        return variance;
    }

    /**
     * 최대값, 최소값 , 진폭 계산 method
     * @param array 기어2에서 어떤 시간동안 획득한 raw-data(센서값)을 Column별로 나눈 값
     * @param option
     *              option이 0 ==> minimum return,
     *              option이 1 ==> maximum return,
     *              option이 2 ==> 최대값과 최소값의 차이(진폭=amplitude).
     * @return double
     */
    public static double valueMinMaxAmp(Double[] array, int option)
    {
        Arrays.sort(array); // 오름차순sorting
        double value = 0.;

        if(option == 0) // option이 0이면
        {
            value = array[0]; // 최소값 return
        }
        else if(option == 1)
        {
            value = array[array.length-1]; // 최대값 return
        }
        else
        {
            value = Math.abs((array.length-1)-array[0]); // 진폭 return
        }

        return value;
    }
    /**
     * RMS(Root Mean Square) 계산 method
     * @param array 기어2에서 어떤 시간동안 획득한 raw-data(센서값)을 Column별로 나눈 값
     * @return double
     */
    public static double valueRMS(Double[] array)
    {
        double sum = 0.0; // 합
        double rootMean = 0.0; // RM
        double rootMeanSquare = 0.0; // RMS

        // 제곱의 합
        for (int i = 0; i < array.length; i++)
        {
            sum += Math.pow(array[i], 2);
        }

        rootMean = sum / array.length; // RM
        rootMeanSquare = Math.sqrt(rootMean); // RMS

        return rootMeanSquare;
    }

    /**
     * 운동 시간 계산 method
     * @param array 기어2에서 어떤 시간동안 획득한 raw-data(센서값)을 Column별로 나눈 값
     * @return double
     */
    public static double calDuringExercise(Double[] array)
    {
        // 배열 길이 확인
        int arraySize = array.length;

        // Sorting
        Arrays.sort(array);

        // 운동시간
        double duringExercise = 0.;
        duringExercise = ( array[arraySize - 1] - array[0] ); // TO-DO 나중에 json에 set하기 전 String으로 casting.

        return duringExercise;
    }

}// end of Class (HelloAccessoryProvideService)
