package com.kgpcircles.kgp_erp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class TimeTableView extends Activity {

	private static final String DEBUG_TAG = "Debugger";
	// private TextView tView;
	private LinearLayout[] dayViews;
	private Context viewContext;
	private boolean refreshing;
	
	
	private void set_refreshing(boolean yes) {
		refreshing = yes;
		ActivityCompat.invalidateOptionsMenu(this);
	}

	// An async task that calls TimeTable.update_time_table() to first update
	// the time table
	// to file if necessary, and then returns a time_table
	private class UpdateTimeTable extends
			AsyncTask<Context, Void, SubjectSlot[][]> {
		@Override
		protected SubjectSlot[][] doInBackground(Context... contexts) {
			
			if (CustomClasses.get_time_table(contexts[0]))
				try {
					FileInputStream fis = new FileInputStream(getFilesDir().getAbsolutePath().concat("/").concat(CustomClasses.TIME_TABLE_FILE));
					ObjectInputStream ois = new ObjectInputStream(fis);
					SubjectSlot[][] time_table = (SubjectSlot[][]) ois.readObject();
					ois.close();
					
					return time_table;
				} catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			else
				return null;
		}

		@Override
		protected void onPostExecute(SubjectSlot[][] time_table) {
			set_refreshing(false);
			update_view(time_table);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_table);
		viewContext = findViewById(R.id.table).getContext();
		refreshing = false;
		// tView = (TextView) findViewById(R.id.front_page_html);
		// if file exists, call update_view directly. Otherwise, the async task
		// is started...
		try {
			FileInputStream fis = new FileInputStream(getFilesDir().getAbsolutePath().concat("/").concat(CustomClasses.TIME_TABLE_FILE));
			ObjectInputStream ois = new ObjectInputStream(fis);
			SubjectSlot[][] time_table = (SubjectSlot[][]) ois.readObject();
			update_view(time_table);
			ois.close();
		} catch(FileNotFoundException e) {
				new UpdateTimeTable().execute(viewContext);
				Log.d("Error", "File not found at: ".concat(getFilesDir().getAbsolutePath()));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(16)
	private void update_view(SubjectSlot[][] time_table) {

		dayViews = new LinearLayout[5];
		dayViews[0] = (LinearLayout) findViewById(R.id.Monday);
		dayViews[1] = (LinearLayout) findViewById(R.id.Tuesday);
		dayViews[2] = (LinearLayout) findViewById(R.id.Wednesday);
		dayViews[3] = (LinearLayout) findViewById(R.id.Thursday);
		dayViews[4] = (LinearLayout) findViewById(R.id.Friday);

		Resources res = getResources();

		int colors[] = { R.color.DimGray, R.color.FireBrick, R.color.Gold,
				R.color.Lavender, R.color.LightSeaGreen, R.color.LightSalmon,
				R.color.LightSteelBlue, R.color.LightCoral, R.color.Beige,
				R.color.LavenderBlush, R.color.Linen };
		int newColorIndex = 0, subjectColor;
		HashMap<String, Integer> subjectColors = new HashMap<String, Integer>();
		for (int i = 0; i < 5; ++i) {
			dayViews[i].removeAllViews();
			for (int j = 0; j < 9; ++j) {
				float weight = 1f;
				String subject_code = time_table[i][j].subject_code;
				TextView slot = (TextView) getLayoutInflater().inflate(
						R.layout.subject, null);
				// new TextView(dayViews[i].getContext(),
				// findViewById(R.id.sample_subject)., R.style.SubjectFont);
				slot.setText(subject_code);
				while (j < 8
						&& subject_code
								.compareTo(time_table[i][j + 1].subject_code) == 0) {
					weight++;
					j++;
				}
				slot.setLayoutParams(new TableLayout.LayoutParams(0, 0, weight));
				if (subject_code.matches("\\w\\w.*")) { // if it's actually a subject, and not
											// a break
					if (subjectColors.containsKey(subject_code))
						subjectColor = ((Integer) subjectColors
								.get(subject_code)).intValue();
					else {
						subjectColor = colors[newColorIndex++];
						subjectColors.put(subject_code, subjectColor);
					}
					// float[] radii = {6,6,6,6,6,6,6,6};
					// ShapeDrawable sd1 = new ShapeDrawable(new
					// RoundRectShape(radii, null, null));
					// sd1.getPaint().setColor(subjectColor);
					// sd1.getPaint().setStyle(Style.FILL);
					// sd1.getPaint().setStrokeWidth(1);

					// shape.getPaint().setColor(subjectColor);
					Drawable shape = res
							.getDrawable(R.drawable.subject_background);
					shape.setColorFilter(res.getColor(subjectColor),
							PorterDuff.Mode.MULTIPLY);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						slot.setBackground(shape);
					}
					else
						slot.setBackgroundDrawable(shape);

				}
				dayViews[i].addView(slot);
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_time_table, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			set_refreshing(true);
			new UpdateTimeTable().execute(viewContext);
			return true;
		case R.id.menu_sign_out:
			SharedPreferences.Editor login_pref = getSharedPreferences(
					LoginActivity.LOGIN_STATE_PREF, MODE_PRIVATE).edit();
			login_pref.putBoolean(LoginActivity.LOGIN_STATE, false);
			login_pref.commit();
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		if (refreshing) {
			menu.findItem(R.id.menu_refresh_progress).setVisible(true);
			menu.findItem(R.id.menu_refresh).setVisible(false);
		}
		else {
			menu.findItem(R.id.menu_refresh).setVisible(true);
			menu.findItem(R.id.menu_refresh_progress).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}
}
