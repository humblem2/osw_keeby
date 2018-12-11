# osw_keeby
OSS Grand Developers Challenge 본선 진출 프로젝트

[[paper](https://www.researchgate.net/publication/312408156_Recognition_of_Anaerobic_based_on_Machine_Learning_using_Smart_Watch_Sensor_Data)]
[[related code](https://github.com/humblem2/osw_keeby/)]
[[video](https://youtu.be/p5vPWqi1B6w)]

## __*S-coach*__ 
AI personal trainer App based on Machine Learning using Samsung tizen smart watch. 
("S" - This means " S " for Smart, Samsung and Univ. Soogsil.)

### 타이젠 OS 기반의 Gear(Gear2 , Gear S, Gear S2)  애플리케이션
* `tizen-sdk-2.3.1`
* Device Optimzation on gear version(2, S, S2)

### Project Summary
머신러닝 기반 인공지능 피트니스 헬스 코치 어플리케이션
3축가속도센서 3축자이로스코프센서를 기반으로 사용자의 모션을 실시간 트래킹 및 스케쥴링하여 '무슨운동'을 '몇회'했는지 그리고 '칼로리 소모량'까지 스스로 알아서 판단하고 기록하여 관리.
또한 사용자가 운동을 시작한 후 심박수를 예상하여운동을 촉진할수있도록 사용자의 예상된 평균심장박동수와 가장 비슷한 BPM에 해당되는 음악을 스스로 찾고 알아서 재생.

### About Models
* Performance(Accuracy): about 96.7% for unseen data [2016. 10] 
* Type: Classification on Supervised Learning.
* Using Dimension Reduction Skills e.g. PCA, LDA(Fisher's LDA)
* Using Kernel Tricks e.g. linear and rbf
* Hybrid Stacking Model based on SVM Framework and others 

### About Project Enviroments
- Using Python, Tizen, Java, Android, AWS
