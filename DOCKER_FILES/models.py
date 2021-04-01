
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
    __tablename__ = 'users'
    phone: str
    max_v: float
    min_db: float
    max_db: float

    login = Column(String(), primary_key=True)
    password = Column(String())
    phone = Column(String())
    min_v = Column(Float)
    max_v = Column(Float)
    min_db = Column(Float)
    max_db = Column(Float)
    measurements = relationship('Measurement', backref='users', lazy=True)

    def __init__(self, login, password, min_v, max_v, min_db, max_db, phone):
        self.login = login
        self.password = password
        self.min_v = min_v
        self.max_v = max_v
        self.min_db = min_db
        self.max_db = max_db
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
    user_login = Column(String(), ForeignKey('users.login'), nullable=False)

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