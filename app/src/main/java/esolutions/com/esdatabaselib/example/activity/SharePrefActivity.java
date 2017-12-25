package esolutions.com.esdatabaselib.example.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import esolutions.com.esdatabaselib.R;
import esolutions.com.esdatabaselib.baseSharedPref.SharePrefManager;
import esolutions.com.esdatabaselib.example.source.sharePrefConfig.MainSecondSharePref;
import esolutions.com.esdatabaselib.example.source.sharePrefConfig.MainSharePref;

public class SharePrefActivity extends AppCompatActivity {
    SharePrefManager sharePrefManager;
    private EditText etMsp_string, etMsp_StringSet, etMsp_int, etMsp_long, etMsp_float,
            etMspSecond_string, etMspSecond_StringSet, etMspSecond_int, etMspSecond_long, etMspSecond_float;
    private CheckBox cbMsp_boolean;
    private CheckBox cbMspSecond_boolean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_pref);


        //set view
        etMsp_string = (EditText) findViewById(R.id.tvMsp_string);
        etMsp_StringSet = (EditText) findViewById(R.id.tvMsp_StringSet);
        etMsp_int = (EditText) findViewById(R.id.tvMsp_int);
        etMsp_long = (EditText) findViewById(R.id.tvMsp_long);
        etMsp_float = (EditText) findViewById(R.id.tvMsp_float);
        cbMsp_boolean = (CheckBox) findViewById(R.id.cbMsp_boolean);

        etMspSecond_string = (EditText) findViewById(R.id.tvMspSecond_string);
        etMspSecond_StringSet = (EditText) findViewById(R.id.tvMspSecond_StringSet);
        etMspSecond_int = (EditText) findViewById(R.id.tvMspSecond_int);
        etMspSecond_long = (EditText) findViewById(R.id.tvMspSecond_long);
        etMspSecond_float = (EditText) findViewById(R.id.tvMspSecond_float);
        cbMspSecond_boolean = (CheckBox) findViewById(R.id.tvMspSecond_boolean);


        //create share pref
        ArrayList<Class<?>> sharedPrefsConfigClasses = new ArrayList<>();
        sharedPrefsConfigClasses.add(MainSharePref.class);
        sharedPrefsConfigClasses.add(MainSecondSharePref.class);


        //init shared pref
        try {
            sharePrefManager = SharePrefManager.getInstance(this, sharedPrefsConfigClasses);
//            sharePrefManager.removeAllFileSharedPref();
//            SharePrefManager.getInstance(this, sharedPrefsConfigClasses);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            fillData();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void fillData() throws Exception {
        MainSharePref dataMainSharePref = (MainSharePref) sharePrefManager.getSharePrefObject(MainSharePref.class);
        if (dataMainSharePref != null) {
            etMsp_string.setText("");
            etMsp_StringSet.setText("");
            etMsp_int.setText("");
            etMsp_long.setText("");
            etMsp_float.setText("");


            etMsp_string.setHint(dataMainSharePref.mainString);
            etMsp_StringSet.setHint(dataMainSharePref.mainStringSet.toString());
            etMsp_int.setHint(String.valueOf(dataMainSharePref.mainInt));
            etMsp_long.setHint(String.valueOf(dataMainSharePref.mainLong));
            etMsp_float.setHint(String.valueOf(dataMainSharePref.mainFloat));
            cbMsp_boolean.setChecked(dataMainSharePref.mainBoolean);
        }


        MainSecondSharePref dataMainSecondSharePref = (MainSecondSharePref) sharePrefManager.getSharePrefObject(MainSecondSharePref.class);
        if (dataMainSharePref != null) {
            etMspSecond_string.setText("");
            etMspSecond_StringSet.setText("");
            etMspSecond_int.setText("");
            etMspSecond_long.setText("");
            etMspSecond_float.setText("");


            etMspSecond_string.setHint(dataMainSecondSharePref.mainSecondString);
            etMspSecond_StringSet.setHint(dataMainSecondSharePref.mainSecondStringSet.toString());
            etMspSecond_int.setHint(String.valueOf(dataMainSecondSharePref.mainSecondInt));
            etMspSecond_long.setHint(String.valueOf(dataMainSecondSharePref.mainSecondLong));
            etMspSecond_float.setHint(String.valueOf(dataMainSecondSharePref.mainSecondFloat));
            cbMspSecond_boolean.setChecked(dataMainSecondSharePref.mainSecondBoolean);
        }
    }

    public void onclickSetDataMainSharedPref(View view) {
        try {
            MainSharePref dataMainSharePref = new MainSharePref(
                    etMsp_string.getText().toString(),
                    new HashSet<String>(),
                    Integer.parseInt(etMsp_int.getText().toString()),
                    Long.parseLong(etMsp_long.getText().toString()),
                    Float.parseFloat(etMsp_float.getText().toString()),
                    cbMsp_boolean.isChecked());

            //write
            sharePrefManager.writeDataSharePref(MainSharePref.class, dataMainSharePref);


            //fill data
            fillData();


            //toast
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void onclickSetDataMainsecondSharedPref(View view) {
        try {
            MainSecondSharePref dataMainSecondSharePref = new MainSecondSharePref(
                    etMspSecond_string.getText().toString(),
                    new HashSet<String>(),
                    Integer.parseInt(etMspSecond_int.getText().toString()),
                    Long.parseLong(etMspSecond_long.getText().toString()),
                    Float.parseFloat(etMspSecond_float.getText().toString()),
                    cbMspSecond_boolean.isChecked());

            //write
            sharePrefManager.writeDataSharePref(MainSecondSharePref.class, dataMainSecondSharePref);


            //fill data
            fillData();


            //toast
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
