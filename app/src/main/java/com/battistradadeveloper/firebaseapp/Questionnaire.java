package com.battistradadeveloper.firebaseapp;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class Questionnaire extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_questionnaire);

		//Actionbar and its title
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("Questionare Form");
		//enable back button
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);

		WebView webView = findViewById(R.id.webView);
		webView.loadUrl("https://forms.gle/qmRnTNmhzXDRH9VG8");

	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed(); //go previous activity
		return super.onSupportNavigateUp();
	}
}
