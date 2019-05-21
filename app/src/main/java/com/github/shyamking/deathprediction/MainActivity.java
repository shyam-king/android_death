package com.github.shyamking.deathprediction;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int actualYear;
    Button guessButton, hackermodeButton, resetButton;
    EditText yearInput;
    LinearLayout container;
    TextView resultText, conclusionText;
    ImageView personSnap;
    int optimalGuesses = 7;
    int maxGuesses = 15;
    int currentGuess = 1;
    Activity activity;
    SharedPreferences settings;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        guessButton = findViewById(R.id.guessButton);
        hackermodeButton = findViewById(R.id.hackermodeButton);
        resetButton = findViewById(R.id.resetButton);
        yearInput = findViewById(R.id.yearInput);
        container = findViewById(R.id.container);
        personSnap = findViewById(R.id.personSnap);
        activity = this;
        resultText = findViewById(R.id.result);
        conclusionText = findViewById(R.id.conclusion);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        reset();

        guessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int diff = Integer.valueOf(yearInput.getText().toString()) - actualYear;
                currentGuess ++;

                if (diff < 0) {
                    resultText.setText(R.string.result_early);
                }
                else if (diff > 0) {
                    resultText.setText(R.string.result_late);
                } else {
                    resultText.setText(R.string.result_exact);
                    if (currentGuess <= optimalGuesses) {
                        conclusionText.setText(R.string.conclusion_good);
                    }
                    else {
                        conclusionText.setText(R.string.conclusion_bad);
                    }
                    guessButton.setEnabled(false);
                    int score = settings.getInt("score", 0);
                    score++;
                    settings.edit().putInt("score", score).apply();
                }

                if (currentGuess >= maxGuesses) {
                    guessButton.setEnabled(false);
                    conclusionText.setText(R.string.conclusion_fail);
                }

                diff = Math.abs(diff);
                int red, green;
                red = green = 0;

                if (diff < 25) {
                    green = Math.max((int)((float)(25-diff)/25 * 255), 100);
                }
                else {
                    red = Math.max(Math.min(255, (int) ((float) diff / 75 * 255)), 100);
                }
                container.setBackgroundColor(Color.rgb(red, green,0));
            }
        });

        hackermodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "Take a snap of the person", Toast.LENGTH_SHORT).show();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            personSnap.setImageBitmap(imageBitmap);
            reset();
        }
    }

    protected void reset() {
        currentGuess = 0;
        if(resultText !=null)
            resultText.setText("");
        if (conclusionText != null) {
            conclusionText.setText("");
        }
        actualYear = (int)(1 + Math.random()*100);
        guessButton.setEnabled(true);
        container.setBackgroundColor(Color.rgb(255,255,255));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.actions_showScore:
                if (settings.contains("score")) {
                    Toast.makeText(activity,"Your correct number of guesses so far: " + settings.getInt("score",0), Toast.LENGTH_LONG).show();
                }
                else  {
                    Toast.makeText(activity, "You are taking the test for the first time!", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.actions_resetScore:
                settings.edit().putInt("score", 0).apply();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
}
