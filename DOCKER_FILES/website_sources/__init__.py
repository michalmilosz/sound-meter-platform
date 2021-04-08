import os
import datetime

from flask import Flask, render_template, redirect, url_for, request, jsonify
from .forms import SignupForm

from flask_login import LoginManager
from flask_sqlalchemy import SQLAlchemy
from os import path

from sqlalchemy import create_engine
from sqlalchemy.orm import scoped_session, sessionmaker
from sqlalchemy.ext.declarative import declarative_base
import json


user = os.environ['POSTGRES_USER']
pwd = os.environ['POSTGRES_PASSWORD']
db_name = os.environ['POSTGRES_DB']
host = 'db'
port = '5432'

db = SQLAlchemy()

def create_app():
    app = Flask(__name__)
    app.secret_key = os.environ['APP_SECRET_KEY']    
    app.config["SECRET_KEY"] = os.environ['APP_SECRET_KEY'] 
    app.config["SQLALCHEMY_DATABASE_URI"] = f"postgresql://{user}:{pwd}@{host}:{port}/{db_name}"
    db.init_app(app)

    from .auth import auth
    from .views import views
    from .rest_api import rest_api

    app.register_blueprint(rest_api, url_prefix='/')
    app.register_blueprint(views, url_prefix='/')
    app.register_blueprint(auth, url_prefix='/')

    from .models import Users, Note, User, Measurement

    create_database(app)
    
    login_manager = LoginManager()
    login_manager.login_view = 'auth.login'
    login_manager.init_app(app)

    @login_manager.user_loader
    def load_user(id):
        return Users.query.get(int(id))


    @app.route("/home")
    def home():
        return render_template('index.html')
        # form = SignupForm()
        # if form.validate_on_submit():
        #     signup = Signups(name=form.name.data, email=form.email.data, date_signed_up=datetime.datetime.now())
        #     db_session.add(signup)
        #     db_session.commit()
        #     return redirect(url_for('success'))
        # return render_template('signup.html', form=form)
    return app

def create_database(app):
    db.create_all(app=app)
    print('Created Database!')