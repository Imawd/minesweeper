package com.example.csci310minesweeperproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_page);

        TextView resultText = findViewById(R.id.resultText);
        TextView secondsUsed = findViewById(R.id.secondsUsed);
        Button restartButton = findViewById(R.id.restartButton);

        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        int timeUsed = intent.getIntExtra("secondsUsed", 0);

        resultText.setText("You " + result + "!");
        secondsUsed.setText("Time: " + timeUsed + " seconds");

        restartButton.setOnClickListener(v -> {
            Intent restartIntent = new Intent(ResultActivity.this, MainActivity.class);
            startActivity(restartIntent);
            finish();
        });
    }
}

