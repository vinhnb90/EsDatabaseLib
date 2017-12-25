package esolutions.com.esdatabaselib.example.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import esolutions.com.esdatabaselib.R;


public class BeginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
    }

    public void clickSharePref(View view) {
        startActivity(new Intent(BeginActivity.this, DatabaseActivity.class));
    }

    public void clickDatabase(View view) {
        startActivity(new Intent(BeginActivity.this, SharePrefActivity.class));
    }
}
