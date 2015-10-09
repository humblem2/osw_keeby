package com.samsung.android.sdk.accessory.example.helloaccessoryprovider;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

/**
 * Created by user on 2015-08-02.
 */
public class ClientSideActivity extends Activity implements View.OnClickListener
{

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /// Field Definition
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Button m_btnSendMsg; // 버튼
    private HelloAccessoryProviderService m_helloService; //HelloAccessoryProviderService 내의 함수를 사용하기 위해 인스턴스화 한다.
    private boolean m_bound = false; //HelloAccessroyProviderService 를 bind 했는지 여부를 가지는 boolean
    String dir = null; // 내부저장소 디렉토리 담을 곳. 2015-09-11 금 4:40
    File file = null; // 파일 객체
    public final static String MYTAG2 = "shTest02"; // 디버깅 태그라벨

    /**
     * ServiceConnection 인터페이스를 구현하는 객체를 생성
     *
     * @ this is field
     */
    private ServiceConnection mConnection = new ServiceConnection()
    {

        /**
         * Service랑 바인딩 끊기
         *
         * @return void
         */
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            // TODO Auto-generated method stub
            Log.d(MYTAG2, "[액티비티] onServiceDisconnected() 메소드 들어옴 ");
            m_bound = false; //  m_bound 초기화
        }

        /**
         * Service랑 바인딩 연결
         *
         * @return void
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d(MYTAG2, "[액티비티] onServiceConnected() 메소드 들어옴 ");

            HelloAccessoryProviderService.LocalBinder binder = (HelloAccessoryProviderService.LocalBinder) service; // LocalBinder객체 할당
            m_helloService = binder.getService(); // 서비스 객체 가져옴
            m_bound = true; // true로 바꿔줌 . 서비스랑 연결 됬으니깐.
        }

    };


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /// Method Definition
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 액티비티가 시작되면 서비스에 연결
     *
     * @return void
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(MYTAG2,"[액티비티] onCreate() 메소드 들어옴.");

        dir = getApplicationContext().getFilesDir().getAbsolutePath(); // 내부저장소 절대 경로
        Log.d(MYTAG2,"[액티비티] onCreate() 메소드 안쪽 상황 . 내부저장소 디렉토리 만듦.");

        dir = "/data/temp/test"; // 경로
        file = new File(dir); // 파일 생성
        boolean mkdirSuccess = file.mkdir(); // 디렉토리 만든것
        Log.d(MYTAG2, "[액티비티] onCreate() 메소드 안쪽 상황 . 내부저장소-디렉토리에 파일 생성 중..");
        if(mkdirSuccess)
        {
            Log.d(MYTAG2,"[액티비티] onCreate() 메소드 안쪽 상황 .내부저장소-디렉토리에 파일 생성 성공! ");
        }
        else
        {
            Log.d(MYTAG2,"[액티비티] onCreate() 메소드 안쪽 상황 .내부저장소-디렉토리에 파일 생성 실패.. ");
        }

        Log.d(MYTAG2,"[액티비티] onCreate() 메소드 나옴.");

    }
    // end of onCreate() method


    /**
     * 액티비티가 시작되면 서비스에 연결
     *
     * @return void
     */
    @Override
    protected void onStart()
    {
        // TODO Auto-generated method stub
        super.onStart();

        Log.d(MYTAG2, "[액티비티] onStart() 메소드 들어옴.");
        Log.d(MYTAG2, "[액티비티] onStart() 메소드 안쪽 상태 ==> 액티비티가 실행됬다는것 ==> 액티비티랑 서비스 연결");

        Intent intent = new Intent(this, HelloAccessoryProviderService.class); // 인텐트 생성

        intent.putExtra("dir", dir); // 액티비티에서 서비스로 값 전달. 서비스로 전달 할 값 SET .

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE); // 데이터 공유/전달
    }
    // end of onStart() method


    /**
     * onResume
     *
     * @return void
     */
    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(MYTAG2, "[액티비티] onResume() 메소드 들어옴.");

        m_btnSendMsg = (Button)findViewById(R.id.sendMsg); // 버튼 생성
        m_btnSendMsg.setOnClickListener(this); // 버튼 생성

    }
    // end of onResume() method


    /**
     * onPause
     *
     * @return void
     */
    @Override
    protected void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();
    }
    // end of onPause() method

    /**
     * 액티비티가 종료되면 서비스 연결을 해제
     *
     * @return void
     */
    @Override
    protected void onStop()
    {
        // TODO Auto-generated method stub
        super.onStop();
        Log.d(MYTAG2, "[액티비티] onStop() 메소드 들어옴");
        if(m_bound) // m_bound객체가  true이면
        {
            Log.d(MYTAG2,"[액티비티] onStop() 메소드 안쪽 if문 ===> m_bound가 true니깐 실행");

            unbindService(mConnection); // 서비스 바인딩
            m_bound=false; // 다시 초기화
        }
    }
    // end of onStop() method


    /**
     * Destory
     *
     * @return void
     */
    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d(MYTAG2, "[액티비티] onDestroy() 메소드 들어옴!!!!!!!!!!!!!!!!");
    }
    // end of onDestory() method


    /**
     * 버튼들이 클릭되면 m_bound로 서비스 연결 여부를 확인하고 HelloAccessoryProviderService의 sendMsg 메서드를 호출
     *
     * @return void
     */
    @Override
    public void onClick(View view)
    {
        Log.d(MYTAG2, "[액티비티] onClick() 메소드 들어옴!!!!!!!!!!!!!!!!");

        if ( view == m_btnSendMsg)
        {
            Log.d(MYTAG2, "[액티비티] onClick() 메소드 안쪽........ #1 ");

            if (m_bound)
            {
                Log.d(MYTAG2, "[액티비티] onClick() 메소드 안쪽........ #2 ");
            }
        }
    }
    // end of onClick() method


} // end of class (ClientSideActivity)
