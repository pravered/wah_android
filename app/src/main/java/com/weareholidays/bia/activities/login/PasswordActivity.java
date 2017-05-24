package com.weareholidays.bia.activities.login;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.utils.ViewUtils;

public class PasswordActivity extends AppCompatActivity {

    private TextView email;
    private EditText password;
    private EditText confirm;
    ParseCustomUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        setup();
    }

    private void setup() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = (TextView) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        confirm = (EditText) findViewById(R.id.confirm);

        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
        confirm.setTypeface(Typeface.DEFAULT);
        confirm.setTransformationMethod(new PasswordTransformationMethod());

        currentUser = (ParseCustomUser) ParseUser.getCurrentUser();
        if (currentUser != null) {
            email.setText(currentUser.getEmail());
        }

        Button continueBtn = (Button) findViewById(R.id.continue_btn);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCheck();
            }
        });
    }

    private void resetCheck(){

        boolean isEmpty = false;

        if(TextUtils.isEmpty(password.getText())){
            password.setError(getString(R.string.required));
            isEmpty = true;
        }
        else {
            password.setError(null);
        }
        if(TextUtils.isEmpty(confirm.getText())){
            confirm.setError(getString(R.string.required));
            isEmpty = true;
        }
        else{
            confirm.setError(null);
        }

        if(isEmpty)
            return;

        if(password.getText().length() < 6){
            password.setError("Password should be at least 6 characters");
            return;
        }

        if(confirm.getText().toString().equals(password.getText().toString())){
            if (ViewUtils.isNetworkAvailable(this)) {
                confirm.setError(null);
                currentUser.setPassword(confirm.getText().toString());
                currentUser.saveInBackground();
                ViewUtils.hideKeyboard(this);
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra(HomeActivity.SHOW_TAB, HomeActivity.PROFILE_TAB);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Password updated", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No working internet connection found!!", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            confirm.setError(getString(R.string.password_mismatch));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
