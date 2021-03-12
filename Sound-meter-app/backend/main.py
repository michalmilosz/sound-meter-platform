# -*- coding: utf-8 -*-

from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from dataclasses import dataclass

# main
# konfiguracja bazy
app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = "postgresql://postgresql:postgresql@localhost:5432/postgresql"#"postgresql://{username}:{password}@{server_url}:{port}/{database_name}"
db = SQLAlchemy(app)
migrate = Migrate(app, db)


# domain
# klasy domenowe / tabele w bazie danych
@dataclass
class User(db.Model):
    __tablename__ = 'user'
    phone: str
    calibration: int

    login = db.Column(db.String(), primary_key=True)
    password = db.Column(db.String())
    phone = db.Column(db.String())
    calibration = db.Column(db.Integer)
    measurements = db.relationship('Measurement', backref='user', lazy=True)

    def __init__(self, login, password, calibration, phone):
        self.login = login
        self.password = password
        self.calibration = calibration
        self.phone = phone

    def __repr__(self):
        return f"User {self.login}"


@dataclass
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

    def __repr__(self):
        return f"Measurement {self.id}"


# controller
# endpointy do komunikacji backednu z aplikacją mobilną
# definicje metod HTTP (POST/GET)
# rejestracja usera
@app.route('/users/register', methods=['POST'])
def register():
    data = request.get_json()
    user = User(login=data['login'], password=data['password'], calibration=0, phone="")
    # db query
    registered_user = User.query.filter_by(login=user.login).first()
    #
    if registered_user is not None and registered_user.login == user.login:
        return {"message": f"The same user {user.login} exists."}, 400
    else:
        db.session.add(user)
        db.session.commit()
        return {"message": f"user {user.login} has been created successfully."}, 201


# logowanie usera
@app.route('/users/login', methods=['POST'])
def login():
    data = request.get_json()
    user = User(login=data['login'], password=data['password'], calibration=0, phone="")
    # db query
    registered_user = User.query.filter_by(login=user.login).first()
    #
    if registered_user is None or registered_user.password != user.password:
        return {"message": f"user {user.login} not authorized."}, 401
    else:
        return {
                    "login" : f"{registered_user.login}",
                    "calibration": f"{registered_user.calibration}",
                    "phone" : f"{registered_user.phone}"
               }, 200


# zapisywanie profilu usera (aktualizacja)
@app.route('/users/profile', methods=['POST'])
def update_profile():
    data = request.get_json()
    # db query
    registered_user = User.query.filter_by(login=data['login']).first()
    if registered_user is not None:
        registered_user.phone = data['phone']
        registered_user.calibration = data['calibration']
        db.session.commit()
        return {"message": f"User profile updated successfully."}, 200
    else:
        return {"message": f"User {data['login']} not found."}, 400


# pobieranie profilu usera
@app.route('/users/profile', methods=['GET'])
def get_user_profile():
    # db query
    registered_user = User.query.filter_by(login=request.args.get('login')).first()
    if registered_user is not None:
        return jsonify(registered_user), 200
    else:
        return {"message": f"User {request.args.get('login')} not found."}, 400


# zapis pomiaru
@app.route('/measurements/save', methods=['POST'])
def save_measurement():
    data = request.get_json()
    # db query
    registered_user = User.query.filter_by(login=data['login']).first()
    if registered_user is None:
        return {"message": f"User {data['login']} not found."}, 400
    else:
        measurement = Measurement(
            min=data['min'],
            max=data['max'],
            avg=data['avg'],
            gps_longitude=data['gps_longitude'],
            gps_latitude=data['gps_latitude'],
            user_login=data['login']
        )
        db.session.add(measurement)
        db.session.commit()
        return {"message": f"Saved successfully."}, 201


# pobieranie pomiarów dla danego usera
@app.route('/measurements/all', methods=['GET'])
def get_measurements_for_user():
    # db query
    registered_user = User.query.filter_by(login=request.args.get('login')).first()
    if registered_user is not None:
        measurements = Measurement.query.filter_by(user_login=request.args.get('login')).all()
        return jsonify(measurements), 200
    else:
        return {"message": f"User {request.args.get('login')} not found."}, 400

@app.route("/")
def home():
    return("Hello")
# main
# start aplikacji, konfiguracja adresu URL i portu
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=80)