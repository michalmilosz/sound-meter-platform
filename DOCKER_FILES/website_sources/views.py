from flask import Blueprint, render_template, request, flash, jsonify, redirect, url_for
from flask_login import login_required, current_user
from flask_googlemaps import GoogleMaps, Map
from .models import Measurement, User
from . import db
import json

views = Blueprint('views',__name__)

@views.route('/', methods=['GET'])
@login_required
def home():
    measurements = Measurement.query.filter_by(user_login=current_user.login).all()
    points = [{ 'lat': -35.344, 'lng': 131.036 }, { 'lat': -15.344, 'lng': 131.036 },{ 'lat': -25.344, 'lng': 131.036 }]
    return render_template("home.html", user=current_user, points=points, measurements=measurements)

@views.route('/settings', methods=['GET'])
@login_required
def settings():
    return render_template("settings.html", user=current_user)

