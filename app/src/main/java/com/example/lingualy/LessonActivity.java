package com.example.lingualy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lingualy.R;



import com.example.lingualy.databinding.ActivityLessonBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Random;

public class LessonActivity extends AppCompatActivity {

    ActivityLessonBinding binding;
    ArrayList<Question> questions;
    int index = 0;
    Question question;
    CountDownTimer timer;
    FirebaseFirestore database;
    int correctAnswers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLessonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        questions =new ArrayList<>();
        database = FirebaseFirestore.getInstance();

        final String catId = getIntent().getStringExtra("catId");

        Random random = new Random();
        final int rand = random.nextInt(10);

        database.collection("categories")
                .document(catId)
                .collection("questions")
                .whereGreaterThanOrEqualTo("index", rand)
                .orderBy("index")
                .limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().size() < 5){
                            database.collection("categories")
                                    .document(catId)
                                    .collection("questions")
                                    .whereLessThanOrEqualTo("index", rand)
                                    .orderBy("index")
                                    .limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for(DocumentSnapshot snapshot : queryDocumentSnapshots){
                                                    Question question = snapshot.toObject(Question.class);
                                                    questions.add(question);
                                                }
                                            setNextQuestion();
                                        }
                                    });
                        } else {
                            for(DocumentSnapshot snapshot : queryDocumentSnapshots){
                                Question question = snapshot.toObject(Question.class);
                                questions.add(question);
                            }
                            setNextQuestion();
                        }
                    }
                });

        resetTimer();

        Button button = findViewById(R.id.btnQuit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LessonActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    void resetTimer() {
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.timer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                // Timer has finished, navigate back to MainActivity
                Intent intent = new Intent(LessonActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finish the current activity to prevent going back to it
            }
        };
    }

    void showAnswer(){
        if(question.getAnswer().equals(binding.optionA.getText().toString()))
            binding.optionA.setBackground(getResources().getDrawable(R.drawable.option_right));
        else if(question.getAnswer().equals(binding.optionB.getText().toString()))
            binding.optionB.setBackground(getResources().getDrawable(R.drawable.option_right));
        else if(question.getAnswer().equals(binding.optionC.getText().toString()))
            binding.optionC.setBackground(getResources().getDrawable(R.drawable.option_right));
        else if(question.getAnswer().equals(binding.optionD.getText().toString()))
            binding.optionD.setBackground(getResources().getDrawable(R.drawable.option_right));
    }

    void setNextQuestion() {
        if(timer != null)
            timer.cancel();
        timer.start();
        if(index < questions.size()){
            binding.questionCounter.setText(String.format("%d/%d", (index+1), questions.size()));
            question = questions.get(index);
            binding.question.setText(question.getQuestion());
            binding.optionA.setText(question.getOptionA());
            binding.optionB.setText(question.getOptionB());
            binding.optionC.setText(question.getOptionC());
            binding.optionD.setText(question.getOptionD());
        }
    }

    void checkAnswer(TextView textView){
        String selectedAnswer = textView.getText().toString();
        if(selectedAnswer.equals(question.getAnswer())){
            correctAnswers++;
            textView.setBackground(getResources().getDrawable(R.drawable.option_right));
        } else {
            showAnswer();
            textView.setBackground(getResources().getDrawable(R.drawable.option_wrong));
        }
    }

    void reset(){
        binding.optionA.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.optionB.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.optionC.setBackground(getResources().getDrawable(R.drawable.option_unselected));
        binding.optionD.setBackground(getResources().getDrawable(R.drawable.option_unselected));
    }

    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.optionA || viewId == R.id.optionB || viewId == R.id.optionC || viewId == R.id.optionD) {
            if (timer != null) {
                timer.cancel();
            }
            TextView selected = (TextView) view;
            checkAnswer(selected);
        } else if (viewId == R.id.btnNext) {
            if (index < questions.size()) {
                index++;
                reset();
                setNextQuestion();
            } else {
                // Start ResultActivity
                Intent intent = new Intent(LessonActivity.this, ResultActivity.class);
                intent.putExtra("correct", correctAnswers);
                intent.putExtra("total", questions.size());
                startActivity(intent);
                //Toast.makeText(this, "Pembelajaran Selesai", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void onBackPressed(){
    }
}