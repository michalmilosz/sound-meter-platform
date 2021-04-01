
import datetime
import os

from flask import Flask, render_template, redirect, url_for, request, jsonify
from forms import SignupForm

from models import User, Measurement
from database import db_session

import json

app = Flask(__name__)
app.secret_key = os.environ['APP_SECRET_KEY']


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
@app.route('/users/register', methods=['POST'])
def register():
    data = request.get_json()
    user = User(login=data['login'], password=data['password'], min_v=0, max_v=0, min_db=0, max_db=0, phone="")
    # db query
    registered_user = User.query.filter_by(login=user.login).first()
    #
    if registered_user is not None and registered_user.login == user.login:
        return {"message": f"The same user {user.login} exists."}, 400
    else:
        db_session.add(user)
        db_session.commit()
        return {"message": f"user {user.login} has been created successfully."}, 201


# logowanie usera
@app.route('/users/login', methods=['POST'])
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
@app.route('/users/profile', methods=['POST'])
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
        db_session.commit()
        return {"message": f"User profile updated successfully."}, 200
    else:
        return {"message": f"User {data['login']} not found."}, 400


# pobieranie profilu usera
@app.route('/users/profile', methods=['GET'])
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
        db_session.add(measurement)
        db_session.commit()
        return {"message": f"Saved successfully."}, 201


# pobieranie pomiarów dla danego usera
@app.route('/measurements/all', methods=['GET'])
def get_measurements_for_user():
    # db query
    registered_user = User.query.filter_by(login=request.args.get('login')).first()
    if registered_user is not None:
        measurements = Measurement.query.filter_by(user_login=request.args.get('login')).all()
        return jsonify(to_json(measurements)), 200
    else:
        return {"message": f"User {request.args.get('login')} not found."}, 400

@app.route("/")
def home():
    return render_template('index.html')
    # form = SignupForm()
    # if form.validate_on_submit():
    #     signup = Signups(name=form.name.data, email=form.email.data, date_signed_up=datetime.datetime.now())
    #     db_session.add(signup)
    #     db_session.commit()
    #     return redirect(url_for('success'))
    # return render_template('signup.html', form=form)
# main
# start aplikacji, konfiguracja adresu URL i portu
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5090)