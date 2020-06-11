package com.example.myquiz;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {
    public static final String EXTRA_SCORE="extraScore";

//    public static final long COUNTDOWN_IN_MILLIS=30000;

    public static final String KEY_SCORE="keyScore";
    public static final String KEY_QUESTION_COUNT="keyQuestionCount";
    public static final String KEY_MILLISLEFT="keyMillisLeft";
    public static final String KEY_ANSWERED="keyAnswered";
    public static final String KEY_QUESTION_LIST="keyQuestionList";
    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewCountDown;
    private RadioGroup rbGroup;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private Button buttonConfirmNext;
    private ArrayList<Question> questionList;

    private ColorStateList textColorDefaultRb;
    private ColorStateList textColorDefaultCd;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    private int questionCounter;
    private int questionCountotal;
    private Question currentQuestion;

    private int score;
    private boolean answered;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        setTitle("Tick tok");
        textViewQuestion=findViewById(R.id.text_view_question);
        textViewScore=findViewById(R.id.text_view_score);
        textViewQuestionCount=findViewById((R.id.text_view_question_count));
        textViewCountDown=findViewById(R.id.text_view_countdown);
        rbGroup=findViewById(R.id.radio_group);
        rb1=findViewById(R.id.radio_button1);
        rb2=findViewById(R.id.radio_button2);
        rb3=findViewById(R.id.radio_button3);
        buttonConfirmNext=findViewById(R.id.button_confirm_next);
        textColorDefaultRb=rb1.getTextColors();
        textColorDefaultCd=textViewCountDown.getTextColors();


        if(savedInstanceState == null) {
            QuizDbHelper dbHelper = new QuizDbHelper(this);
            questionList = dbHelper.getAllQuestions();
            questionCountotal = questionList.size();
            Collections.shuffle(questionList);
            showNextQuestion();
        }else{
            questionList=savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            questionCountotal=questionList.size();
            questionCounter=savedInstanceState.getInt(KEY_QUESTION_COUNT);
            currentQuestion=questionList.get(questionCounter-1);
            score=savedInstanceState.getInt(KEY_SCORE);
            timeLeftInMillis=savedInstanceState.getLong(KEY_MILLISLEFT);
            answered=savedInstanceState.getBoolean(KEY_ANSWERED);
            if(!answered){
                startCountDown();
            }else{
                updateCoundownText();
                showSolution();
            }
        }

        buttonConfirmNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!answered){
                    if(rb1.isChecked() || rb2.isChecked() || rb3.isChecked()){
                        checkAnswer();
                    }else{
                        Toast.makeText(QuizActivity.this, "Please Select an Answer", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    showNextQuestion();
                }
            }
        });
    }
    private  void showNextQuestion(){
        rb1.setTextColor(textColorDefaultRb);
        rb2.setTextColor(textColorDefaultRb);
        rb3.setTextColor(textColorDefaultRb);
        rbGroup.clearCheck();

        if(questionCounter<questionCountotal){
            currentQuestion=questionList.get(questionCounter);
            textViewQuestion.setText(currentQuestion.getQuestion());
            rb1.setText(currentQuestion.getOption1());
            rb2.setText(currentQuestion.getOption2());
            rb3.setText(currentQuestion.getOption3());

            questionCounter++;
            textViewQuestionCount.setText("Question:"+questionCounter+"/"+questionCountotal);
            answered=false;
            buttonConfirmNext.setText("Confirm");

            timeLeftInMillis=10000;
            startCountDown();
        }else{
            finishQuiz();
        }
    }
    private void startCountDown(){
        countDownTimer=new CountDownTimer(timeLeftInMillis,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis=millisUntilFinished;
                updateCoundownText();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis=0;
                updateCoundownText();
                checkAnswer();

            }
        }.start();
    }
    private void updateCoundownText(){
        int minutes=(int)(timeLeftInMillis/1000)/60;
        int seconds=(int)(timeLeftInMillis/1000) % 60;
        String timeFormatted=String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
        textViewCountDown.setText(timeFormatted);

        if(timeLeftInMillis<10000){
            textViewCountDown.setTextColor(Color.RED);
        }else{
            textViewCountDown.setTextColor(textColorDefaultCd);
        }
    }
    private void checkAnswer(){
        answered=true;
        countDownTimer.cancel();
        RadioButton rbSelected=findViewById(rbGroup.getCheckedRadioButtonId());
        int answerNr=rbGroup.indexOfChild(rbSelected)+1;
        if(answerNr==currentQuestion.getAnswerNr()){
            score++;
            textViewScore.setText("Score" +score);
        }
        showSolution();
    }
    private void showSolution(){
        rb1.setTextColor(Color.RED);
        rb2.setTextColor(Color.RED);
        rb3.setTextColor(Color.RED);
        switch ((currentQuestion.getAnswerNr())){
            case 1:
                rb1.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 1 is Correct");
                break;
            case 2:
                rb2.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 2 is Correct");
                break;
            case 3:
                rb3.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer 3 is Correct");
                break;
        }
        if(questionCounter<questionCountotal){
            buttonConfirmNext.setText("NEXT");
        }else{
            buttonConfirmNext.setText("THANK YOU");
        }
    }

    private void finishQuiz(){
        Intent resultIntent=new Intent();
        resultIntent.putExtra(EXTRA_SCORE,score);
        setResult(RESULT_OK,resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finishQuiz();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(countDownTimer!=null){
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCORE, score);
        outState.putInt(KEY_QUESTION_COUNT,questionCounter);
        outState.putLong(KEY_MILLISLEFT,timeLeftInMillis);
        outState.putBoolean(KEY_ANSWERED,answered);
        outState.putParcelableArrayList(KEY_QUESTION_LIST,questionList);
    }
}

