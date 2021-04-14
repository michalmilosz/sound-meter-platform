from flask import Blueprint, render_template, request, flash, redirect, url_for
from .models import User
from . import db, bcrypt
from flask_login import login_user, login_required, logout_user, current_user



auth = Blueprint('auth', __name__)


@auth.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        if "login" in request.form:
            login = request.form.get('login')
            password = request.form.get('password')

            user = User.query.filter_by(login=login).first()
            if user:
                if bcrypt.check_password_hash(user.password, password):
                    flash('Logged in successfully!', category='success')
                    login_user(user, remember=True)
                    return redirect(url_for('views.home'))
                else:
                    flash('Incorrect password, try again.', category='error')
            else:
                flash('Login does not exist.', category='error')
        elif "sign-up" in request.form:
            return redirect(url_for('auth.sign_up'))

    return render_template("login.html", user=current_user)


@auth.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('auth.login'))


@auth.route('/sign-up', methods=['GET', 'POST'])
def sign_up():
    if request.method == 'POST':
        if "sign-up" in request.form:
            login = request.form.get('login')
            password1 = request.form.get('password1')
            password2 = request.form.get('password2')

            user = User.query.filter_by(login=login).first()
            if user:
                flash('User with that login already exists.', category='error')
            elif len(login) < 6:
                flash('Login must be greater than 5 character.', category='error')
            elif password1 != password2:
                flash('Passwords don\'t match.', category='error')
            elif len(password1) < 7:
                flash('Password must be at least 7 characters.', category='error')
            else:
                password=str(bcrypt.generate_password_hash(password1,10))[2:-1]
                new_user = User(login=login, password=password, min_v=0, max_v=0, min_db=0, max_db=0, phone="")
                db.session.add(new_user)
                db.session.commit()
                login_user(new_user, remember=True)
                flash('Account created!', category='success')
                return redirect(url_for('views.home'))

    return render_template("sign_up.html", user=current_user)