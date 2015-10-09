from flask import Flask
from flask.ext.sqlalchemy import SQLAlchemy
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, Integer, String
from sqlalchemy import create_engine
from sqlalchemy import ForeignKey
from sqlalchemy.orm import relationship, backref



Base = declarative_base()


#   Domain(Value Object) Definetion
class User(Base):

    __tablename__ = 'users' #  테이블 이름

    #definition columns mapping field
    index = Column(Integer, autoincrement=True, primary_key=True) # 테이블의 시퀀스 
    user_id = Column(String(30))  # 기기 id
    exe_name = Column(String(30)) #  판별된 운동종류
    exe_count = Column(String(30)) #  운동 횟수
    exe_kcal = Column(String(30))  # 운동으로인한 칼로리 소모량
    exe_year = Column(String(30))  #  운동 한 년도
    exe_month = Column(String(30))  # 운동 한 달
    exe_day = Column(String(30))  # 운동 한 일


    #  Constructor
    def __init__(self, user_id, exe_name, exe_count, exe_kcal, exe_year, exe_month, exe_day):
        self.user_id = user_id
        self.exe_name = exe_name
        self.exe_count = exe_count
        self.exe_kcal = exe_kcal
        self.exe_year = exe_year
        self.exe_month = exe_month
        self.exe_day = exe_day

    # setter getter function
    #  This returns general information of user-class ==> 마리아DB scoach 데이터베이스 > users 테이블에 맵핑되는  Object
    def __repr__(self):
        return "<User('%s','%s','%s','%s','%s','%s','%s')>" % (self.user_id, self.exe_name, self.exe_count, self.exe_kcal, self.exe_year, self.exe_month, self.exe_day)




engine = create_engine("mysql://root:sean@127.0.0.1/scoach", encoding='utf8', echo=True) #  DB 정보
Base.metadata.create_all(engine) #  엔진 객체 meta정보로 추가


