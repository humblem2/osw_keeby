# -*- coding: utf-8 -*-

import os
from flask import Flask
from flask import request
from flask import Response, json
from flask import jsonify # json 추가
from flask import render_template
from werkzeug.datastructures import FileStorage
from werkzeug import secure_filename
from flask import make_response

import pandas as pd  # 웹서버에 pip install

import sklearn.decomposition  # 필수 추가해 주기
import sklearn.lda  # 필수 추가해 주기
import sklearn.preprocessing  # 필수 추가해 주기 ==> scaling 쉽게 하기 위해서

import numpy as np  # 필수 추가해 주기

import sklearn.preprocessing  # 필수 추가해 주기
import sklearn.cluster  # 필수 추가해 주기
import sklearn.cross_validation  # 필수 추가해 주기

import random  # 난수 발생

from sqlalchemy import create_engine, desc, asc  # DB연동
from sqlalchemy.orm import sessionmaker, scoped_session  # DB연동
from user import Base, User # DB 연동


#  #####################################################################################################################
#  ####################################  [ 3rd-Party-Library(Module) import 하기 ]  ####################################
#  #####################################################################################################################


UPLOAD_FOLDER = '/home/ubuntu/android/'  # 서버 persistence layer 접근 가능 Directory

app = Flask(__name__)

app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER  # Directory 등록


#  분류기 한개 짜리	
@app.route('/predictoneclassifier/', methods=['POST'])  # Post방식만 요청URL 받겠다. 단일인입점은 없다. 
def predictoneclassifier():

    #  1 : ==> 이 부분 단말기에서 보낸 것 받는 것이므로 수정해야한다.  
    #  json에서 년/월/일 뺴서 할당
    exeyear = str(request.json['year'])
    exemonth = str(request.json['month'])
    exeday = str(request.json['day'])
    #  1 : ==> 이 부분 단말기에서 보낸 것 받는 것이므로 수정해야한다.  
    
    #  2 : ==> 이 부분 단말기(안드로이드)에서 보낸 것 받는 것.
    with open('/home/ubuntu/android/unseen.csv','w') as us:
        us.write(str(request.json['columns']).rstrip(','))
        us.write('\n')
        us.write(str(request.json['features']).rstrip(','))
    #  클라이언트에서 온 42차원 Feature와 ubuntu directory에 있는 42차원 featuer-set을 concat한다. Dataframw형식으로 데이터를 가공.
    df1 = pd.read_csv(os.path.join(app.config['UPLOAD_FOLDER'],'unseen.csv'))
    df2 = pd.read_csv(os.path.join(app.config['UPLOAD_FOLDER'],'ubuntudir.csv'))  # ubuntudir_new.csv 로 데이터모은뒤에 바꿔야 되나

    framesummation = [df1,df2]
    df12 = pd.concat(framesummation,ignore_index=True) #합침

    #  CSV파일 불러오기 :: Un-seen input-Data

    class_df = [] # 레이블 없음
    feature_df = df12

    #  행렬(numpy-array)로 변환
    feature_df = feature_df.values

    unSeenX = feature_df

    #  unSeenX ==> 차원축소 시켜 ==> 차원축소방법 = PCA
    pca_unSeenX, X_pca_unSeenX = run_PCA(unSeenX, 2)

    #  n-차원축소 된 new_data의 요소 [0]번째 Element
    new_data = X_pca_unSeenX[0]

    #  ############   데이터 준비 끝. End of ready to prediction ..
    

    #  #####################################################################
    #  ############  Classifier, ML 시작
    #  #####################################################################


    directory = os.path.join(app.config['UPLOAD_FOLDER'],'features_270count_change.csv') # features_270count_change.csv 로 데이터 늘린것 으로 바꿈 

    #  SVM-algorithm Constant
    C = 1.0
    
    #  피쳐 Load :: feature = (training-data+test-data) ==> 37-counts-Feature ==> 37Dimension
    X, y = load_data(directory)

    #  Feature ( 분류기(Model) 학습시킬 Data-set) 를 Dimension Reduction.
    pca, X_pca = run_PCA(X, 2)                  # PCA로 reduce ..
    
    # PCA로 차원축소 했을때, SVM의 커널 선택 ==> 완성된 분류기
    svc_linear_pca = run_linear_SVM(X_pca, y, C)
    
    # Predict
    result = svc_linear_pca.predict(new_data)  # 어떤 운동인지

    tempwhat = '' # 무슨운동인지 담는 변수

    with open('/home/ubuntu/keebylog/what.txt','w') as whatf:
        print >> whatf, result
    with open('/home/ubuntu/keebylog/what.txt','r') as ff:
        line = ff.readline()
        line = line.strip()
        line = line.lstrip("['")
        line = line.rstrip("]")
        line = line.rstrip("'")
        line = line.strip()
        tempwhat = line


    #  [전역변수]
    exercisename = '' # 무슨운동인지 다시 단말기에 보낼 값
    exercisecount = 0 # 그 운동 몇번 했는지 다시 단말기에 보낼 값
    exercisekcal = 0 # 그 운동 할때 소모된 칼로리량 단시 단말기에 보낼 값


    temp = str(request.json['time']).strip() # json 값 파싱
    time = float(temp) # casting

    #  Classifier 판단
    if tempwhat == 'db':
        exercisename = 'dumbell'
        exercisecount = round(time / 1.2)
        exercisekcal = exercisecount * 0.1
    elif tempwhat == 'pu':
        exercisename = 'pullup'
        exercisecount = round(time / 1.1)
        exercisekcal = exercisecount * 0.1
    else:
        exercisename = 'pullside'
        exercisecount = round(time / 1.1)
        exercisekcal = exercisecount * 0.1


    #  8
    #################################################################################################
    #  DB로 CRUD 할 부분 ORM툴 이용해서 mariaDB로 CRUD :: SQL-Alchemy도 OGNL 지원한다.
    #################################################################################################
    
    exename = str(exercisename) #  문자열로 캐스팅
    execount = str(exercisecount) #  문자열로 캐스팅
    exekcal = str(exercisekcal) #  문자열로 캐스팅 

    #  데이터베이스 연결
    engine = create_engine("mysql://root:sean@127.0.0.1/scoach", encoding='utf8', echo=False) #  데이터베이스 접속정보
    Base.metadata.bind = engine # Binding할 엔진 등록

    # DB 세션 생성
    DBSession = scoped_session(sessionmaker(autocommit=False, autoflush=False, bind=engine)) # autocommit안해준다. 바인딩할 엔진 정보 추가
    session = DBSession()  # 세션 생성

    # C  ==> INSERT
    usr = User('107', exename, execount, exekcal, exeyear, exemonth, exeday)
    session.add(usr)

    session.commit()
    session.close()

    # R  ==> SELECT 히스토리 기능 ==> OGNL 지원
    #history = session.query(user.exe_name, user.exe_count, user.exe_kcal).filter(user.exe_day=='14')

    #  [디버깅]
    with open('/home/ubuntu/keebylog/log.txt','a') as f:
        f.write('[운동결과]')
        f.write('@-----------------------------------@')
        print >> f, result
        f.write('@==========================================================================@')
        f.write(tempwhat)
        f.write('@여섯시 사십육분@')
        f.write(exercisename)

    #  9
    #################################################################################################
    #  response 만들기  ==> 단말기로 다시 보내야되. exercise~ 변수들 3개 ..
    #################################################################################################

    exercisename = str(unicode(exercisename))  # 한글인코딩처리 및 문자열 캐스팅 
    exercisecount = str(unicode(exercisecount))  # 한글인코딩처리 및 문자열 캐스팅
    exercisekcal = str(unicode(exercisekcal))    # 한글인코딩처리 및 문자열 캐스팅
    
    #  10 >> bpm에 맞는 MP3 노래 추천
    music_to_client = "neednotmusic" # 클라이언트로 추천할 음악 이름 == mp3 file name
    timesum = str(request.json['timesum']).strip()  #  운동 시간 합
    timesum = float(timesum) #  문자열을 숫자로 캐스팅
    
    
    file_dir = ''  # 읽어올 CSV파일 디렉토리명

    # 축적된 운동시간에 따라서 WorkFlow가 진행된다.
    if timesum > 30 and timesum <= 210:
        file_dir = os.path.join(app.config['UPLOAD_FOLDER'],'bpm70to100.csv')
        music_to_client = best_music(file_dir)

    elif timesum > 210 and timesum <= 390:
        file_dir = os.path.join(app.config['UPLOAD_FOLDER'],'bpm100to130.csv')
        music_to_client = best_music(file_dir)

    elif timesum > 390:
        file_dir = os.path.join(app.config['UPLOAD_FOLDER'],'bpm130to150.csv')
        music_to_client = best_music(file_dir)

    else:
        music_to_client = 'neednotmusic'
  
    # 추천 된 음악
    music_to_client = str(unicode(music_to_client)).strip()    
    
    # response객체
    response = jsonify({'exercisename':exercisename,'exercisecount':exercisecount,'exercisekcal':exercisekcal,'musicfromserver':music_to_client})

    return response

######################################################################################################
######################################################################################################
######################################################################################################
######################################################################################################
######################################################################################################

#  분류개 3개 짜리 ==> 차선
@app.route('/predict/', methods=['POST'])
def predict():
   
    #  2 : ==> 이 부분 단말기(안드로이드)에서 보낸 것 받는 것.
    with open('/home/ubuntu/android/unseen.csv','w') as us:
    	us.write(str(request.json['columns']).rstrip(','))
        us.write('\n')
        us.write(str(request.json['features']).rstrip(','))

    df1 = pd.read_csv(os.path.join(app.config['UPLOAD_FOLDER'],'unseen.csv'))
    df2 = pd.read_csv(os.path.join(app.config['UPLOAD_FOLDER'],'ubuntudir.csv'))

    framesummation = [df1,df2]
    df12 = pd.concat(framesummation,ignore_index=True) #합침

    #  CSV파일 불러오기 :: Un-seen input-Data
    with open('/home/ubuntu/keebylog/log.txt','a') as debug:
	debug.write('1111111111111111111111111111111111111')
    class_df = [] # 레이블 없음
    feature_df = df12

    #  행렬(numpy-array)로 변환
    feature_df = feature_df.values

    unSeenX = feature_df

    #  unSeenX ==> 차원축소 시켜 ==> 차원축소방법 = PCA
    pca_unSeenX, X_pca_unSeenX = run_PCA(unSeenX, 2)
    with open('/home/ubuntu/keebylog/log.txt','a') as debug:
	debug.write('2222222222222222222222222222222222222222222222')
    #  n-차원축소 된 new_data의 요소 [0]번째 Element
    new_data = X_pca_unSeenX[0]

    #  ############   데이터 준비 끝. End of ready to prediction ..



    #  #####################################################################
    #  ############  Classifier, ML 시작
    #  #####################################################################
   
    

    # 정수형변수
    # 에측된 클래스-라벨이 획득한 점수
    pu_score_sum = 0
    ps_score_sum = 0
    db_score_sum = 0

    # 문자형변수
    # dataframe 만들 csv파일 읽어올 내부 저장소 경로
    directory = ''

    #  ##################################################################
    #  ##################################################################
    #  ##################	분류기 3가지 학습 그리고 예측
    #  ##################################################################
    #  ##################################################################

    # 분류기1(pu & ps) 학습 그리고 예측
    directory = os.path.join(app.config['UPLOAD_FOLDER'],'features1pups_180count.csv') # 분류기1 운동2가지 pu ps 80개
    pre_predict = supervised_classifier(directory, new_data) #리턴값이 predict한 레이블
    if pre_predict == 'pu':
	pu_score_sum = pu_score_sum + 1 # pu에 점수 가중치 준다
    else:
	ps_score_sum = ps_score_sum + 1 # ps에 점수 가중치 준다


    # 분류기2(pu & db) 학습 그리고 예측
    directory = os.path.join(app.config['UPLOAD_FOLDER'],'features2pudb_180count.csv') # 분류기1 운동2가지 pu ps 80개
    pre_predict = supervised_classifier(directory, new_data) #리턴값이 predict한 레이블
    if pre_predict == 'pu':
        pu_score_sum = pu_score_sum + 1 # pu에 점수 가중치 준다
    else:
	db_score_sum = db_score_sum + 1 # db에 점수 가중치 준다


    # 분류기3(ps & db) 학습 그리고 예측
    directory = os.path.join(app.config['UPLOAD_FOLDER'],'features3psdb_180count.csv') # 분류기1 운동2가지 pu ps 80개
    pre_predict = supervised_classifier(directory, new_data) #리턴값이 predict한 레이블
    if pre_predict == 'ps':
	ps_score_sum = ps_score_sum + 1 # pu에 점수 가중치 준다
    else:
	db_score_sum = db_score_sum + 1 # db에 점수 가중치 준다

    #  #######################################################################################################################
    #  #######################################################################################################################
    #  ##################	unseen-data를 분류기 3가지 input하여  확률기반 점수기반 예측을 하였다.
    #  #######################################################################################################################
    #  #######################################################################################################################

    # 문자형변수
    # 최종 예측 운동 결과
    final_result = ''

    #  pu가 제일 높을 때
    if pu_score_sum > ps_score_sum:
	if pu_score_sum > db_score_sum:
	    final_result = 'pu'

    #  ps가 제일 높을 때
    if ps_score_sum > pu_score_sum:
	if ps_score_sum > db_score_sum:
	    final_result = 'ps'

    #  db가 제일 높을 때
    if db_score_sum > pu_score_sum:
	if db_score_sum > ps_score_sum:
	    final_result = 'db'


    #  디버깅
    with open('/home/ubuntu/keebylog/log.txt','a') as debug:
	debug.write('444444444444444444444444444444444444444444444444444444444')

    
    exercisename = ''
    exercisecount = 0
    exercisekcal = 0    

    temp = str(request.json['time']).strip()
    time = float(temp)

    if final_result == 'db':
    	exercisename = 'dumbell'
	    exercisecount = round(time / 1.2)
	    exercisekcal = exercisecount * 0.1    
    elif final_result == 'pu':
	    exercisename = 'pullup'
	    exercisecount = round(time / 1.3)
        exercisekcal = exercisecount * 0.1   
    else:
	    exercisename = 'pullside'
        exercisecount = round(time / 1.1)
        exercisekcal = exercisecount * 0.1   
    #  9
    #################################################################################################
    #  response 만들기  ==> 단말기로 다시 보내야되. exercise~ 변수들 3개 ..
    #################################################################################################

    #  [디버깅]
    with open('/home/ubuntu/keebylog/log.txt','a') as f:
    	f.write('[운동결과]')
	f.write('@------------final_result값 디버깅--------------@')
	f.write('@==========================================================================@')
        f.write(final_result)
	f.write('@ [ 2015-09-25 금요일  오전 2시 55분 ] @')
	f.write(exercisename)        
    
    exercisename = str(unicode(exercisename))
    exercisecount = str(unicode(exercisecount))   
    # response객체
    response = jsonify({'exercisename':exercisename,'exercisecount':exercisecount,'exercisekcal':exercisekcal})   

    return response

#  ubuntu directory에 있는 classifier supervised learning 을 위한 dataset load..
#  CSV파일 불러오기 :: Dataset(Summation of Feature) = training-data + testing-data
def load_data(directory):
    # 1
    wine_df = pd.read_csv(directory) # 읽어와서

    class_df = wine_df.pop('class')  # 라벨 제게
    feature_df = wine_df  #  재할당

    #  행렬로 변환시키기
    class_df = class_df.values
    feature_df = feature_df.values

    return feature_df, class_df

# PCA
def run_PCA(dataframe, num_components):
    # 2
    #######################################################
    pca = sklearn.decomposition.PCA(n_components=num_components)  # 축소할 차원 set
    pca.fit(dataframe)  # DF-data를 fitting 시킨다.

    pca_array = pca.transform(dataframe) # pca 한 결과
    #######################################################
    return pca, pca_array

#  SVM linear
def run_linear_SVM(X, y, C):
    # 4
    svc_linear = sklearn.svm.SVC(C=C, kernel='linear').fit(X, y)  # linear커널과 SVM을  데이터를 이용하여 분류기를 학습시킨다.

    return svc_linear


def run_rbf_SVM(X, y, C, gamma=0.7):
    # 5
    svc_rbf = sklearn.svm.SVC(C=C, kernel='rbf', gamma=gamma).fit(X, y)# rbf커널과 SVM을  데이터를 이용하여 분류기를 학습시킨다.

    return svc_rbf

# LDA
#  매개변수 설명
#  X ==> 피쳐들
#  y ==> 클래스 (라벨)
#  num_components ==> 축소될 차원의 수
def run_LDA(X, y, num_components):
    # 3
    lda = sklearn.lda.LDA(n_components=num_components) # 축소할 차원 set
    lda_array = lda.fit(X, y).transform(X) # DF-data 와 클래스 정보까지 고려하여 fitting 시킨다.

    return lda, lda_array

#  분류기 학습
def supervised_classifier(directory, new_data):
	#  #####################################################################
    #  #####################################################################
    #  ############  Classifier, ML 시작
    #  #####################################################################
    #  #####################################################################

    # 디버깅 파트
    with open('/home/ubuntu/keebylog/log.txt','a') as debug:
        debug.write('Classifier 지도학습 시키는 supervised_classifier(directory)함수 들어옴!!')

    #  SVM-algorithm Constant
    C = 1.0
    
    #  피쳐 Load :: feature = (training-data+test-data) ==> 42-counts-Feature ==> 42Dimension
	#  2가지 class로 라벨링된 CSV파일을 DataFrame으로 load하여, 분류기 3개로 분할하여 RandomForest 컨셉처럼 학습시킬 것.
    X, y = load_data(directory)

    # Split DATA for Cross-Validation Check :: Split Training-data and Test-data
    #X_train, X_test, y_train, y_test = sklearn.cross_validation.train_test_split(X, y, test_size=0.2, random_state=0)

    # 디버깅 파트
    with open('/home/ubuntu/keebylog/log.txt','a') as debug:
        debug.write('  supervised_classifier() inside ..  ')
    
    #  load한 dataframe형태의 Feature ( 분류기(Model) 학습시킬 Data-set) 를 Dimension Reduction. ==> LDA
    pca, X_pca = run_PCA(X, 2)                  # PCA로 reduce ..
    #lda, X_lda = run_LDA(X_train, y_train, 2)               # LDA로 reduce ..
    
   

    # SVM알고리즘으로 classifier supervised-learning..
   
    # PCA로 차원축소 했을때, SVM의 커널 선택 ==> 완성된 분류기
    svc_linear_pca = run_linear_SVM(X_pca, y, C) # 분류기 학습시킬데이터셋을 pca로 차원축소/ SVM / 커널 linear
    #svc_rbf_pca = run_rbf_SVM(X_pca, y, C) # 분류기 학습시킬데이터셋을 pca로 차원축소/ SVM / 커널 rbf

    # LDA로 차원축소 했을때, SVM의 커널 선택
    #svc_linear_lda = run_linear_SVM(X_lda, y_train, C) # 분류기 학습시킬데이터셋을 lda로 차원축소/ SVM / 커널 linear
    #svc_rbf_lda = run_rbf_SVM(X_lda, y, C) # 분류기 학습시킬데이터셋을 lda로 차원축소/ SVM / 커널 rbf

   
    # Predict
    result = svc_linear_pca.predict(new_data)
    #result = svc_rbf_pca.predict(new_data)
    #result = svc_linear_lda.predict(new_data)
    #result = svc_rbf_lda.predict(new_data)

    # 디버깅 파트
    with open('/home/ubuntu/keebylog/log.txt','a') as debug:
        debug.write('Classifier 지도학습 시키는 supervised_classifier(directory)함수 나간다.ㅠㅠ')

    #  ['cy'] 문자를==> cy 문자로 가공 하는 부분 
    tempwhat = '' # 무슨운동인지 담는 변수
    with open('/home/ubuntu/keebylog/what.txt','w') as whatf:
        print >> whatf, result
    with open('/home/ubuntu/keebylog/what.txt','r') as ff:
        line = ff.readline()
        line = line.strip()
        line = line.lstrip("['")
        line = line.rstrip("]")
        line = line.rstrip("'")
        line = line.strip()
        tempwhat = line
    with open('/home/ubuntu/keebylog/log.txt','a') as debug:
        debug.write('5555555555555555555555555555555555555555555555555555')
        debug.write('\n')
        debug.write('@tempwhat변수에 최종적으로 할당 된 것은 ??? @')
        debug.write(tempwhat)
 	
 	#  return-value
    return tempwhat

#  사용자의 운동시간에따른 적합한 bpm을 갖는 노래
def best_music(file_dir):

    filename_bpm_df = pd.read_csv(file_dir)

    filename_df = filename_bpm_df.pop('filename')
    bpm_df = filename_bpm_df.pop('bpm')

    #행렬로 변환시키기
    filename_df = filename_df.values
    bpm_df = bpm_df.values

    filename = filename_df
    bpm = bpm_df

    #  라인 계산  ##############################        ==> open()으로 바꾸기
    temp = open(file_dir, 'r') # 파일 객체
    lines = temp.readlines()
    linecount = 0
    for line in lines:
        linecount += 1

    # 파일 객체 닫아 준다.
    temp.close()
    ############################################
    randomIndex = random.randint(0, linecount-2) # 컬럼은 절대 선택될수 없어

    return filename[randomIndex]  #  문자열 = json에 put 할 추천 노래 이름


######################################################################################################

if __name__ == '__main__':
    app.run(host='52.89.71.89', port=80) # 52.89.14.137 
