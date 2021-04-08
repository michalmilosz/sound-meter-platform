import os
import datetime

from flask import Blueprint, Flask, render_template, redirect, url_for, request, jsonify
from .forms import SignupForm

from .models import User, Measurement
from . import db

import json

rest_api = Blueprint('rest_api',__name__)

def to_json(list_of_strings):
    result = "["
    tmp = 0
    for x in list_of_strings:
        print(json.loads(str(x)))
        if tmp !=0 :
            result = result + "," + str(x) 
        else:
            result = result + str(x)
        tmp+=1
    result += "]"
    return json.loads(result)
    
# controller
# endpointy do komunikacji backednu z aplikacją mobilną
# definicje metod HTTP (POST/GET)
# rejestracja usera

@rest_api.route('/users/register', methods=['POST'])
def register():
    data = request.get_json()
    user = User(login=data['login'], password=data['password'], min_v=0, max_v=0, min_db=0, max_db=0, phone="")
    # db query
    registered_user = User.query.filter_by(login=user.login).first()
    #
    if registered_user is not None and registered_user.login == user.login:
        return {"message": f"The same user {user.login} exists."}, 400
    else:
        db.add(user)
        db.commit()
        return {"message": f"user {user.login} has been created successfully."}, 201
# logowanie usera
@rest_api.route('/users/login', methods=['POST'])
def login():
    data = request.get_json()
    user = User(login=data['login'], password=data['password'], min_v=0, max_v=0, min_db=0, max_db=0, phone="")
    # db query
    registered_user = User.query.filter_by(login=user.login).first()
    #
    if registered_user is None or registered_user.password != user.password:
        return {"message": f"user {user.login} not authorized."}, 401
    else:
        return {
                    "login" : f"{registered_user.login}",
                    "min_v": f"{registered_user.min_v}",
                    "max_v": f"{registered_user.max_v}",
                    "min_db": f"{registered_user.min_db}",
                    "max_db": f"{registered_user.max_db}",
                    "phone" : f"{registered_user.phone}"
            }, 200
# zapisywanie profilu usera (aktualizacja)
@rest_api.route('/users/profile', methods=['POST'])
def update_profile():
    data = request.get_json()
    # db query
    registered_user = User.query.filter_by(login=data['login']).first()
    if registered_user is not None:
        registered_user.phone = data['phone']
        registered_user.min_v = data['min_v']
        registered_user.max_v = data['max_v']
        registered_user.min_db = data['min_db']
        registered_user.max_db = data['max_db']
        db.commit()
        return {"message": f"User profile updated successfully."}, 200
    else:
        return {"message": f"User {data['login']} not found."}, 400
# pobieranie profilu usera
@rest_api.route('/users/profile', methods=['GET'])
def get_user_profile():
    # db query
    registered_user = User.query.filter_by(login=request.args.get('login')).first()
    if registered_user is not None:
        #return jsonify(registered_user), 200
        return jsonify(User=registered_user.login,
                    Phone=registered_user.phone,
                    Min_V=registered_user.min_v,
                    Max_V=registered_user.max_v,
                    Min_db=registered_user.min_db,
                    Max_db=registered_user.max_db), 200
    else:
        return {"message": f"User {request.args.get('login')} not found."}, 400
# zapis pomiaru
@rest_api.route('/measurements/save', methods=['POST'])
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
        db.add(measurement)
        db.commit()
        return {"message": f"Saved successfully."}, 201
# pobieranie pomiarów dla danego usera
@rest_api.route('/measurements/all', methods=['GET'])
def get_measurements_for_user():
    # db query
    registered_user = User.query.filter_by(login=request.args.get('login')).first()
    if registered_user is not None:
        measurements = Measurement.query.filter_by(user_login=request.args.get('login')).all()
        return jsonify(to_json(measurements)), 200
    else:
        return {"message": f"User {request.args.get('login')} not found."}, 400