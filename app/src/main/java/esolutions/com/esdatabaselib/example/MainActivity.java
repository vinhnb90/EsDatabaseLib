package esolutions.com.esdatabaselib.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import esolutions.com.esdatabaselib.R;
import esolutions.com.esdatabaselib.SqlHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            SqlHelper.setupDB(this, ESDbConfig.class, new Class[]{
                    ClassRoom.class, Student.class});
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
