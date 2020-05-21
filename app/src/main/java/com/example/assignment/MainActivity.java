package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    CircularProgressIndicator circularProgressIndicator;
    Button button;
    EditText progressValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circularProgressIndicator = findViewById(R.id.circular_progress);
        button = findViewById(R.id.button);
        progressValue = findViewById(R.id.progressValue);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circularProgressIndicator.setCurrentProgress(Integer.parseInt(progressValue.getText().toString()));
            }
        });
        circularProgressIndicator.setMaxProgress(100);
        circularProgressIndicator.setCurrentProgress(0);
        circularProgressIndicator.setShouldDrawDot(true);
        circularProgressIndicator.setProgressColor(getResources().getColor(R.color.colorAccent));
        circularProgressIndicator.setDotColor(getResources().getColor(R.color.orangeColor));
        circularProgressIndicator.setDotWidthDp(20);
    }
}
