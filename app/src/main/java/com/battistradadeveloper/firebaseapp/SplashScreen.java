package com.battistradadeveloper.firebaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
	private int waktu_loading = 4000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent login = new Intent(SplashScreen.this, MainActivity.class);
				startActivity(login);
				finish();
			}
		},waktu_loading);
	}
}
