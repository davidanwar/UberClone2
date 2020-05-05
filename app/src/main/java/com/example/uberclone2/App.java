package com.example.uberclone2;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("fiqrCWk39eKbbISWhR9uFT0D9IkO2t3f9NIH0lZf")
                // if defined
                .clientKey("oMESJzP7jCPhecVZBjkfzAW8uY7E8mnocZb6mqqD")
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}
