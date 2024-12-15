from flask import Flask, render_template, request, redirect, session, flash
from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///quiz_app.db'
app.config['SECRET_KEY'] = 'your_secret_key_here'
db = SQLAlchemy(app)

# Database Models
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(150), unique=True, nullable=False)
    password = db.Column(db.String(150), nullable=False)
    is_admin = db.Column(db.Boolean, default=False)

class Quiz(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(150), nullable=False)
    questions = db.relationship('Question', backref='quiz', lazy=True)

class Question(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    content = db.Column(db.String(300), nullable=False)
    answer = db.Column(db.String(100), nullable=False)
    quiz_id = db.Column(db.Integer, db.ForeignKey('quiz.id'), nullable=False)

# Routes
@app.route('/')
def home():
    return render_template('home.html', quizzes=Quiz.query.all())

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        is_admin = 'is_admin' in request.form  # Checkbox will return True if checked

        if User.query.filter_by(username=username).first():
            flash('Username already exists. Please choose a different username.', 'error')
            return redirect('/register')

        hashed_password = generate_password_hash(password)
        new_user = User(username=username, password=hashed_password, is_admin=is_admin)
        db.session.add(new_user)
        db.session.commit()
        flash('Registration successful! Please login.', 'success')
        return redirect('/login')

    return render_template('register.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        user = User.query.filter_by(username=username).first()

        if user and check_password_hash(user.password, password):
            session['user_id'] = user.id
            session['is_admin'] = user.is_admin
            flash('Login successful!', 'success')
            return redirect('/')
        else:
            flash('Invalid username or password.', 'error')

    return render_template('login.html')

@app.route('/logout')
def logout():
    session.clear()
    flash('Logged out successfully.', 'success')
    return redirect('/')

@app.route('/create_quiz', methods=['GET', 'POST'])
def create_quiz():
    if not session.get('is_admin'):
        flash('Access denied. Admins only.', 'error')
        return redirect('/')

    if request.method == 'POST':
        title = request.form['title']
        new_quiz = Quiz(title=title)
        db.session.add(new_quiz)
        db.session.commit()
        flash('Quiz created successfully!', 'success')
        return redirect('/')

    return render_template('create_quiz.html')

@app.route('/quiz/<int:quiz_id>', methods=['GET', 'POST'])
def take_quiz(quiz_id):
    quiz = Quiz.query.get_or_404(quiz_id)
    if request.method == 'POST':
        score = 0
        for question in quiz.questions:
            user_answer = request.form.get(str(question.id))
            if user_answer and user_answer.strip().lower() == question.answer.strip().lower():
                score += 1
        flash(f'You scored {score}/{len(quiz.questions)}!', 'info')
        return render_template('result.html', score=score, total=len(quiz.questions))

    return render_template('quiz.html', quiz=quiz)

@app.route('/add_question/<int:quiz_id>', methods=['GET', 'POST'])
def add_question(quiz_id):
    if not session.get('is_admin'):
        flash('Access denied. Admins only.', 'error')
        return redirect('/')

    quiz = Quiz.query.get_or_404(quiz_id)
    if request.method == 'POST':
        content = request.form['content']
        answer = request.form['answer']

        if not content or not answer:
            flash('Question content and answer are required.', 'error')
            return redirect(f'/add_question/{quiz_id}')

        new_question = Question(content=content, answer=answer, quiz_id=quiz_id)
        db.session.add(new_question)
        db.session.commit()
        flash('Question added successfully!', 'success')
        return redirect(f'/quiz/{quiz_id}')

    return render_template('add_question.html', quiz=quiz)

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(debug=True)