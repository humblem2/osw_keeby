# osw_keeby
The 9th OSS Grand Developers Challenge 본선 진출 프로젝트

[[paper](https://www.researchgate.net/publication/312408156_Recognition_of_Anaerobic_based_on_Machine_Learning_using_Smart_Watch_Sensor_Data)]
[[related code](https://github.com/humblem2/osw_keeby/)]
[[video](https://youtu.be/p5vPWqi1B6w)]

## __*S-coach*__ 
AI personal trainer App based on Machine Learning using Samsung tizen smart watch.

### Note
* `삼성 타이젠 OS` 기반의 Gear(Gear2 , Gear S, Gear S2)  애플리케이션
* `tizen-sdk-2.3.1`
* Device Optimization on `samsung galaxy gear` version(2, S, S2)

### Project Summary
머신러닝 기반 인공지능 피트니스 헬스 코치 어플리케이션.
3축가속도센서 3축자이로스코프센서를 기반으로 사용자의 모션을 실시간 트래킹 및 스케쥴링하여 '무슨운동'을 '몇회'했는지 그리고 '칼로리 소모량'까지 스스로 알아서 판단하고 기록하여 관리.
또한 사용자가 운동을 시작한 후 심박수를 예상하여 운동을 촉진할수있도록 사용자의 예상된 평균심장박동수와 가장 비슷한 BPM에 해당되는 음악을 스스로 찾고 알아서 재생.

### About Models
* Performance(Accuracy): about 96.7% for unseen data [2016. 10] 
* Type: Classification on Supervised Learning.
* Using Dimension Reduction Skills e.g. PCA, LDA(Fisher's LDA)
* Using Kernel Tricks e.g. linear and rbf
* Hybrid Stacking Model based on SVM Framework and others 

### About Project Enviroments
* Python 3.4
* Tizen
* Java 8
* Android
* AWS EC2 free tier
* Flask
* mariadb
* windows 7
* ubuntu 14

### Reference
* [Samsung Developers](https://developer.samsung.com/forum/android/samsung-sdk?boardName=SDK&searchSubIdAll=&searchSubId=&searchType=ALL&listLines=40&searchText=tizen)
* [Samsung Tizen Forum](https://www.samsungtizenforum.com/)
* [타이젠으로 웨어러블 앱 개발하기, 서영진, 한빛미디어](http://www.hanbit.co.kr/store/books/look.php?p_code=E6459056874)
* [Tizen 애플리케이션 프로그래밍, 서영진, 프리랙](http://book.interpark.com/product/BookDisplay.do?_method=detail&sc.saNo=001&sc.prdNo=216774259&gclid=Cj0KCQiAurjgBRCqARIsAD09sg9bcVJnPhp9QvONk9QuGJJkgvZu5jHmehbMiu0mpuM1Kui5vAN8-kcaArDxEALw_wcB&product2017=true)
* [웹 개발자를 위한 타이젠 모바일 프로그래밍, 이병옥, 엘비오](http://www.yes24.com/24/goods/16022321)
