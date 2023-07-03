# Samsung Tizen OS Application
## My AI Persnol Trainer, 🔥S-Coach !
#### Project : MachineLearning based Samsung Healthcare-App
[The 9th OSS(Open Source Software) Grand Developers Challenge](https://www.oss.kr/notice/show/6008d9bc-66f0-4373-a9df-19a8973c7038) 삼성전자 기업제안과제 본선진출 `ML(Classifier) + Web(백엔드) + Smartwatch App` 프로젝트
* [[paper site1](https://www.semanticscholar.org/paper/Recognition-of-Anaerobic-based-on-Machine-Learning-Cho-Lee/b09e29cb5e33519be01e7fa9fe4d486903b73721)] / [[paper site2](https://www.researchgate.net/publication/312408156_Recognition_of_Anaerobic_based_on_Machine_Learning_using_Smart_Watch_Sensor_Data)]
* [[video](https://youtu.be/p5vPWqi1B6w)]
* [[about competition](https://www.oss.kr/notice/show/6008d9bc-66f0-4373-a9df-19a8973c7038)]

#### 프로젝트 
* [대회소개](https://www.oss.kr/notice/show/6008d9bc-66f0-4373-a9df-19a8973c7038)
* [시연영상](https://youtu.be/p5vPWqi1B6w)
* [발표자료](https://www.slideshare.net/SuHyunCho2/sws-56703648#1)
* [개발문서](https://www.slideshare.net/secret/bsfNKp1uR5Y1q8)
* [논문작성](https://www.slideshare.net/SuHyunCho2/recognition-of-anaerobic-based-on-machine-learning-using-smart-watch-sensor-data) [[paper site1](https://www.semanticscholar.org/paper/Recognition-of-Anaerobic-based-on-Machine-Learning-Cho-Lee/b09e29cb5e33519be01e7fa9fe4d486903b73721)] / [[paper site2](https://www.researchgate.net/publication/312408156_Recognition_of_Anaerobic_based_on_Machine_Learning_using_Smart_Watch_Sensor_Data)]

<br>


:rocket: This repo is part of the full project code.

## __*S-coach*__ 
AI personal trainer App based on Machine Learning using Samsung tizen smart watch.

### Note
* `삼성 타이젠 OS` 기반의 Gear(Gear2 , Gear S, Gear S2)  애플리케이션
* `tizen-sdk-2.3.1`
* Device Optimization completed on `samsung smartwatch` version([Samsung Galaxy Gear 2, Gear S, Gear S2](https://developer.samsung.com/galaxy-watch))
* Tizen app type: Companion(Operating with Samsung Galaxy S4(android 4.4))

### Project Summary
머신러닝 기반 인공지능 피트니스 헬스 코치 어플리케이션.
3축가속도센서 3축자이로스코프센서를 기반으로 사용자의 모션을 실시간 트래킹 및 스케쥴링하여 '무슨운동'을 '몇회'했는지 그리고 '칼로리 소모량'까지 스스로 알아서 판단하고 기록하여 관리.
또한 사용자가 운동을 시작한 후 심박수를 예상하여 운동을 촉진할수있도록 사용자의 예상된 평균심장박동수와 가장 비슷한 BPM에 해당되는 음악을 스스로 찾고 알아서 재생.

### About Train Models(__optimized__)
* Performance(Accuracy): about 96.7% for unseen data [2016. 10] 
* Model Sort: `Discriminative Model` ( **_P_** **( _y_ | _X_ )** ) for inference
* Learning Type: Classification on Supervised Learning.
* Using Dimension Reduction Skills e.g. PCA, LDA(Fisher's LDA)
* Using Kernel Tricks e.g. linear and rbf
* Hybrid Stacking Model based on SVM(Support Vector Machine) Framework and others 

### About Project Enviroments
* client side
  * python 3.4 / 2.7
  * tizen 2.3.1
  * java 8
  * android 4.4
  * windows 7
* server side
  * ubuntu 14
  * AWS EC2 free tier
  * flask 0.9
  * nginx 1.4.6
  * mariadb 5.5.44
  * uwsgi 1.9.17.1
  * sqlalchemy 0.15

### Reference
* [Samsung Developers](https://developer.samsung.com/forum/android/samsung-sdk?boardName=SDK&searchSubIdAll=&searchSubId=&searchType=ALL&listLines=40&searchText=tizen)
* [Samsung Tizen Forum](https://www.samsungtizenforum.com/)
* [book 1 ](http://www.hanbit.co.kr/store/books/look.php?p_code=E6459056874)(kor)
* [book 2 ](http://book.interpark.com/product/BookDisplay.do?_method=detail&sc.saNo=001&sc.prdNo=216774259&gclid=Cj0KCQiAurjgBRCqARIsAD09sg9bcVJnPhp9QvONk9QuGJJkgvZu5jHmehbMiu0mpuM1Kui5vAN8-kcaArDxEALw_wcB&product2017=true)(kor)
* [book 3 ](http://www.yes24.com/24/goods/16022321)(kor)
