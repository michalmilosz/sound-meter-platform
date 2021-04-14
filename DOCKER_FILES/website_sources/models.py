
# from database import Base
from . import db
from sqlalchemy.sql import func
from flask_login import UserMixin

class Signups(db.Model):
    """
    Example Signups table
    """
    __tablename__ = 'signups'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(256))
    email = db.Column(db.String(256), unique=True)
    date_signed_up = db.Column(db.DateTime())

class User(db.Model, UserMixin):
    __tablename__ = 'user'
    __table_args__ = {'extend_existing': True} 
    phone: str
    max_v: float
    min_db: float
    max_db: float
    
    id = db.Column(db.Integer, primary_key=True)
    login = db.Column(db.String(150), unique=True)
    password = db.Column(db.String())
    phone = db.Column(db.String())
    min_v = db.Column(db.Float)
    max_v = db.Column(db.Float)
    min_db = db.Column(db.Float)
    max_db = db.Column(db.Float)
    measurements = db.relationship('Measurement', backref='user', lazy=True)

    # def __init__(self, login, password, min_v, max_v, min_db, max_db, phone):
    #     self.login = login
    #     self.password = password
    #     self.min_v = min_v
    #     self.max_v = max_v
    #     self.min_db = min_db
    #     self.max_db = max_db
    #     self.phone = phone

    def __repr__(self):
        return f"User {self.login}"


class Measurement(db.Model):
    __tablename__ = 'measurement'
    id: int
    min: float
    max: float
    avg: float
    gps_longitude: float
    gps_latitude: float

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    min = db.Column(db.Float)
    max = db.Column(db.Float)
    avg = db.Column(db.Float)
    gps_longitude = db.Column(db.Float)
    gps_latitude = db.Column(db.Float)
    user_login = db.Column(db.String(), db.ForeignKey('user.login'), nullable=False)

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

