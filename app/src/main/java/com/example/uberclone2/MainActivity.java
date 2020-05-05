package com.example.uberclone2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    enum State{
        SIGNUP, LOGIN
    }

    private State state;
    private Button btnSignUpLogin, btnOneTimeLogin;
    private EditText editUserName, editPassword, editPassengerOrDriver;
    private RadioButton radioButtonPassenger, radioButtonDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (ParseUser.getCurrentUser() != null){
            // transition
            transitionToPassengerActivity();
            trasitionToDriverActivity();
        }

        btnSignUpLogin = findViewById(R.id.btnSignUpLogin);
        btnOneTimeLogin = findViewById(R.id.btnOnTimeLogin);
        editUserName = findViewById(R.id.editUserName);
        editPassword = findViewById(R.id.editPassword);
        editPassengerOrDriver = findViewById(R.id.editPassengerOrDriver);
        radioButtonDriver = findViewById(R.id.rdbDriver);
        radioButtonPassenger = findViewById(R.id.rdbPassenger);

        state = State.SIGNUP;


        btnOneTimeLogin.setOnClickListener(MainActivity.this);
        btnSignUpLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == State.SIGNUP){
                    if (radioButtonDriver.isChecked() == false && radioButtonPassenger.isChecked() == false){
                        Toast.makeText(MainActivity.this, "Are You a Driver or Passenger?", Toast.LENGTH_SHORT).show();
                        // tidak mengeeksekusi code
                        return;
                    }
                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(editUserName.getText().toString());
                    appUser.setPassword(editPassword.getText().toString());
                    if (radioButtonDriver.isChecked()){
                        appUser.put("as", "Driver");
                    } else if (radioButtonPassenger.isChecked()){
                        appUser.put("as", "Passenger");
                    }
                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null){
                                Toast.makeText(MainActivity.this, "You signed", Toast.LENGTH_SHORT).show();
                                transitionToPassengerActivity();
                                trasitionToDriverActivity();
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                } else if (state == State.LOGIN){
                    ParseUser.logInInBackground(editUserName.getText().toString(), editPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null && e == null){
                                Toast.makeText(MainActivity.this, "You Login", Toast.LENGTH_SHORT).show();
                                transitionToPassengerActivity();
                                trasitionToDriverActivity();
                            }
                        }
                    });
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.itemLogin:
                if (state == State.SIGNUP){
                    state = State.LOGIN;
                    item.setTitle("SignUp");
                    btnSignUpLogin.setText("Login");
                } else if (state == State.LOGIN){
                    state = State.SIGNUP;
                    item.setTitle("Login");
                    btnSignUpLogin.setText("SignUp");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        if (editPassengerOrDriver.getText().toString().equals("Driver")
                || editPassengerOrDriver.getText().toString().equals("Passenger")){
            if (ParseUser.getCurrentUser() == null){
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user == null && e == null){
                            Toast.makeText(MainActivity.this, "You Are anymous", Toast.LENGTH_SHORT).show();
                            user.put("as", editPassengerOrDriver.getText().toString());
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null){
                                        transitionToPassengerActivity();
                                        trasitionToDriverActivity();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } else {
            Toast.makeText(MainActivity.this, "Are you a driver or a passenger?", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void transitionToPassengerActivity(){
        if (ParseUser.getCurrentUser() != null){
            if (ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
                startActivity(intent);
            }
        }
    }

    private void trasitionToDriverActivity(){
        if (ParseUser.getCurrentUser() != null){
            if (ParseUser.getCurrentUser().get("as").equals("Driver")){
                Intent intent = new Intent(MainActivity.this, DriverRequestListActivity.class);
                startActivity(intent);
            }
        }
    }



}
