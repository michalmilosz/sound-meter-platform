
from database import Base
from sqlalchemy import Column, Integer, String, Float, ForeignKey
from sqlalchemy.types import DateTime
from sqlalchemy.orm import relationship

class Signups(Base):
    """
    Example Signups table
    """
    __tablename__ = 'signups'
    id = Column(Integer, primary_key=True)
    name = Column(String(256))
    email = Column(String(256), unique=True)
    date_signed_up = Column(DateTime())

class User(Base):
    __tablename__ = 'user'
    phone: str
    calibration: int

    login = Column(String(), primary_key=True)
    password = Column(String())
    phone = Column(String())
    calibration = Column(Integer)
    measurements = relationship('Measurement', backref='user', lazy=True)

    def __init__(self, login, password, calibration, phone):
        self.login = login
        self.password = password
        self.calibration = calibration
        self.phone = phone

    def __repr__(self):
        return f"User {self.login}"


class Measurement(Base):
    __tablename__ = 'measurement'
    id: int
    min: float
    max: float
    avg: float
    gps_longitude: float
    gps_latitude: float

    id = Column(Integer, primary_key=True, autoincrement=True)
    min = Column(Float)
    max = Column(Float)
    avg = Column(Float)
    gps_longitude = Column(Float)
    gps_latitude = Column(Float)
    user_login = Column(String(), ForeignKey('user.login'), nullable=False)

    def __init__(self, min, max, avg, gps_longitude, gps_latitude, user_login):
        self.min = min
        self.max = max
        self.avg = avg
        self.gps_latitude = gps_latitude
        self.gps_longitude = gps_longitude
        self.user_login = user_login
    # def __repr__(self):
    #     return "<%s %r>" % (self.__class__.__name__, self.id)
    def __repr__(self):
        return "{"+f'''"id":{self.id}, "min":{self.min},"max":{self.max},"avg":{self.avg},"gps_longitude":{self.gps_longitude},"gps_latitude":{self.gps_latitude}'''+"}"