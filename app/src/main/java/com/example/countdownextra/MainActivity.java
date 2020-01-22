package com.example.countdownextra;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText InputMinutes;
    private TextView CountDownView;
    private Button ButtonSet;
    private Button ButtonStartPause;
    private Button ButtonReset;
    private DatabaseAdapter databaseAdapter;
    private CountDownTimer CntDwnTimer;
    private boolean IsTimerRunning;
    private long StartTimeInMilliseconds;
    private long TimeLeftInMilliseconds;
    private long EndingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseAdapter = new DatabaseAdapter(getApplicationContext());
        databaseAdapter = databaseAdapter.open();
        InputMinutes = findViewById(R.id.input_minutes);
        CountDownView = findViewById(R.id.countdown);

        ButtonSet = findViewById(R.id.button_set);
        ButtonStartPause = findViewById(R.id.button_start_pause);
        ButtonReset = findViewById(R.id.button_reset);

        ButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = InputMinutes.getText().toString();
                if (input.length() == 0) {
                    Toast.makeText(MainActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = Long.parseLong(input) * 60000;
                if (millisInput == 0) {
                    Toast.makeText(MainActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                setTime(millisInput);
                InputMinutes.setText("");
            }
        });

        ButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsTimerRunning) {
                    Pause();
                } else {
                    StartTmr();
                }
            }
        });

        ButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset();
            }
        });
    }

    private void setTime(long milliseconds) {
        StartTimeInMilliseconds = milliseconds;
        Reset();
        closeInput();
    }

    private void StartTmr() {
        EndingTime = System.currentTimeMillis() + TimeLeftInMilliseconds;

        CntDwnTimer = new CountDownTimer(TimeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeLeftInMilliseconds = millisUntilFinished;
                TextUpdate();
            }

            @Override
            public void onFinish() {
                IsTimerRunning = false;
                Refresh();
            }
        }.start();

        IsTimerRunning = true;
        Refresh();
    }

    private void Pause() {
        CntDwnTimer.cancel();
        IsTimerRunning = false;
        Refresh();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);
        databaseAdapter.insertEntry(formattedDate,(int)(TimeLeftInMilliseconds / 1000) / 3600, (int)((TimeLeftInMilliseconds / 1000) % 3600) / 60, (int)(TimeLeftInMilliseconds / 1000) % 60,(int)TimeLeftInMilliseconds);

    }

    private void Reset() {
        TimeLeftInMilliseconds = StartTimeInMilliseconds;
        TextUpdate();
        Refresh();
    }

    private void TextUpdate() {
        int hours = (int) (TimeLeftInMilliseconds / 1000) / 3600;
        int minutes = (int) ((TimeLeftInMilliseconds / 1000) % 3600) / 60;
        int seconds = (int) (TimeLeftInMilliseconds / 1000) % 60;
        int millisecs = (int) (TimeLeftInMilliseconds);

        String formattedTime;
        if (hours > 0) {
            formattedTime = String.format(Locale.getDefault(),
                    "%d:%02d:%02d:%02d", hours, minutes, seconds, millisecs );
        } else {
            formattedTime = String.format(Locale.getDefault(),
                    "%02d:%02d:%02d", minutes, seconds, millisecs);
        }

        CountDownView.setText(formattedTime);
    }

    private void Refresh() {
        if (IsTimerRunning) {
            InputMinutes.setVisibility(View.INVISIBLE);
            ButtonSet.setVisibility(View.INVISIBLE);
            ButtonReset.setVisibility(View.INVISIBLE);
            ButtonStartPause.setText("Pause");
        } else {
            InputMinutes.setVisibility(View.VISIBLE);
            ButtonSet.setVisibility(View.VISIBLE);
            ButtonStartPause.setText("Start");

            if (TimeLeftInMilliseconds < 1000) {
                ButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                ButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (TimeLeftInMilliseconds < StartTimeInMilliseconds) {
                ButtonReset.setVisibility(View.VISIBLE);
            } else {
                ButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void closeInput() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager InputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            InputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putLong("StartTimeInMilliseconds", StartTimeInMilliseconds);
        editor.putLong("TimeLeftInMilliseconds", TimeLeftInMilliseconds);
        editor.putBoolean("IsTimerRunning", IsTimerRunning);
        editor.putLong("EndingTime", EndingTime);

        editor.apply();

        if (CntDwnTimer != null) {
            CntDwnTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);

        StartTimeInMilliseconds = preferences.getLong("StartTimeInMilliseconds", 600000);
        TimeLeftInMilliseconds = preferences.getLong("TimeLeftInMilliseconds", StartTimeInMilliseconds);
        IsTimerRunning = preferences.getBoolean("IsTimerRunning", false);

        TextUpdate();
        Refresh();

        if (IsTimerRunning) {
            EndingTime = preferences.getLong("EndingTime", 0);
            TimeLeftInMilliseconds = EndingTime - System.currentTimeMillis();

            if (TimeLeftInMilliseconds < 0) {
                TimeLeftInMilliseconds = 0;
                IsTimerRunning = false;
                TextUpdate();
                Refresh();
            } else {
                StartTmr();
            }
        }
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        databaseAdapter.close();
    }
}