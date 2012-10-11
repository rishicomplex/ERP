package com.kgpcircles.kgp_erp;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class LoginActivity extends Activity {

	public static final String LOGIN_STATE_PREF = "login_state";
	public static final String LOGIN_STATE = "logged_in";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences loginPref = getSharedPreferences(LOGIN_STATE_PREF,
				MODE_PRIVATE);
		if (loginPref.getBoolean(LOGIN_STATE, false)) {
			Log.d("Update", "Login state is true");
			Intent intent = new Intent(this, TimeTableView.class);
			startActivity(intent);
			finish();
			return;
		}
		Log.d("Update", "Login state is either absent or false");
		loginPref.edit().putBoolean(LOGIN_STATE, false);
		deleteFile(CustomClasses.LOGIN_FILE);
		deleteFile(CustomClasses.TIME_TABLE_FILE);
		setContentView(R.layout.activity_login);
		EditText roll_no_view = (EditText) findViewById(R.id.login_roll_no);
		roll_no_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				Log.d("Update", "Focus changed");
				if (v.getId() != R.id.login_roll_no)
					return;
				
				if (hasFocus == false) {
					Log.d("Update", "Focus lost");
					new SetSecurityQuestion().execute(
							(EditText) findViewById(R.id.login_roll_no),
							(EditText) findViewById(R.id.login_sq));
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	private class SetSecurityQuestion extends
			AsyncTask<EditText, Void, String[]> {
		private EditText roll_no_view, sq_view;

		@Override
		protected String[] doInBackground(EditText... views) {
			roll_no_view = views[0];
			sq_view = views[1];
			return CustomClasses.getSecurityQuestion(roll_no_view
					.getEditableText().toString(), roll_no_view.getContext());
		}

		@Override
		protected void onPostExecute(String[] result) {
			if (result == null) {
				Log.d("Update", "Tried to fetch question, got null.");
				return;
			}
			sq_view.setHint(result[1]);
			sq_view.setTag(result[0]);
		}
	}

	private class LoginManager extends AsyncTask<View, Void, Boolean> {

		private Context viewContext;
		private View loginButton;
		private View progressBar;

		@Override
		protected Boolean doInBackground(View... views) {
			viewContext = views[0].getContext();
			loginButton = views[0];
			progressBar = views[1];
			return CustomClasses.get_time_table(viewContext);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result.booleanValue() == true) {
				SharedPreferences.Editor login_pref = getSharedPreferences(
						LOGIN_STATE_PREF, MODE_PRIVATE).edit();
				login_pref.putBoolean(LOGIN_STATE, true);
				login_pref.commit();
				Log.d("Update", "Login state is now true!");
				Intent intent = new Intent(getApplicationContext(),
						TimeTableView.class);
				startActivity(intent);
				finish();
			} else {

				progressBar.setVisibility(View.GONE);
				loginButton.setVisibility(View.VISIBLE);
				LinearLayout login_page = (LinearLayout) findViewById(R.id.login_page);
				login_page
						.setDescendantFocusability(LinearLayout.FOCUS_BEFORE_DESCENDANTS);
				new AlertDialog.Builder(viewContext)
						.setMessage(R.string.login_error)
						.setCancelable(false)
						.setNeutralButton("Okay",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
									}
								}).show();
			}

		}
	}

	public void login(View view) {
		LinearLayout login_page = (LinearLayout) findViewById(R.id.login_page);
		login_page.requestFocus();
		login_page
				.setDescendantFocusability(LinearLayout.FOCUS_BLOCK_DESCENDANTS);
		ProgressBar progress = (ProgressBar) findViewById(R.id.login_progress_bar);
		view.setVisibility(View.GONE);
		progress.setVisibility(View.VISIBLE);
		String roll_no = ((EditText) findViewById(R.id.login_roll_no))
				.getText().toString();
		String password = ((EditText) findViewById(R.id.login_password))
				.getText().toString();
		String sq = ((EditText) findViewById(R.id.login_sq)).getText()
				.toString();
		String U = (String) ((EditText) findViewById(R.id.login_sq)).getTag();
		store_login(roll_no, password, sq, U);
		new LoginManager().execute(view, progress);

	}

	public void onCheckboxClicked(View view) {
		EditText passwordView = (EditText) findViewById(R.id.login_password);
		EditText sqView = (EditText) findViewById(R.id.login_sq);
		if (((CheckBox) view).isChecked()) {
			passwordView
					.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			passwordView.setHint("");
			sqView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			sqView.setHint("");
		} else {
			passwordView.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			passwordView.setHint(R.string.password);
			sqView.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			sqView.setHint(R.string.security_question);
		}
	}

	private void store_login(String username, String password, String sq,
			String U) {
		try {
			LoginData loginData = new LoginData(username, password, sq, U);
			FileOutputStream fos = openFileOutput(CustomClasses.LOGIN_FILE,
					Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(loginData);
			Log.d("Success", "Login file written: ".concat(getFilesDir()
					.getAbsolutePath()));
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// private String getSQ(String roll_no);

}
