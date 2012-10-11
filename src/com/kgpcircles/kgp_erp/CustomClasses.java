package com.kgpcircles.kgp_erp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

class LoginData implements Serializable {
	private static final long serialVersionUID = -6002646120292233362L;
	public String RollNo;
	public String Password;
	public String U;
	public String SQ;

	public LoginData(String username, String password, String sq, String U) {
		this.RollNo = username;
		this.Password = password;
		this.SQ = sq;
		this.U = U;
	}
}

class SubjectSlot implements Serializable {
	private static final long serialVersionUID = -3026739067398219223L;
	public String subject_code;
	public String venue;
	public int duration;

	public SubjectSlot(String s_code, String venue, int duration) {
		this.subject_code = s_code;
		this.venue = venue;
		this.duration = duration;
	}
}

class CustomClasses {

	private static final String ERP_URL = "https://www.google.com/";
	// "https://erp.iitkgp.ernet.in/IIT_ERP2";
	private static final String ERP_REDIRECT_URL = "https://erp.iitkgp.ernet.in/IIT_ERP2/welcome.jsp";
	private static final String ERP_LOGIN_URL = "https://erp.iitkgp.ernet.in/SSOAdministration/auth.htm";
	private static final String ERP_LISTING = "https://erp.iitkgp.ernet.in/IIT_ERP2/welcome.jsp?module_id=16";
	private static final String ERP_TIMETABLE_FORM = "https://erp.iitkgp.ernet.in/IIT_ERP2/welcome.jsp?module_id=16&menu_id=40";
	private static final String ERP_TIMETABLE_VIEW = "https://erp.iitkgp.ernet.in/Acad/student/view_stud_time_table.jsp";
	private static final String ERP_GET_SQ = "https://erp.iitkgp.ernet.in/SSOAdministration/getSecurityQuestion.htm?user_id=!USER_ID!&rand_id=0";
	public static final String TIME_TABLE_FILE = "time_table.dat";
	public static final String LOGIN_FILE = "login_data.dat";
	private static Context cContext;

	// private static String PROXY_HOST = "http://144.16.192.247";
	// private static Integer PROXY_PORT = 8080;
	// Make HTTPClient accept all certificates
	private static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	private static HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			// DefaultHttpClient rClient =
			return new DefaultHttpClient(ccm, params);
			// ProxySelectorRoutePlanner routePlanner = new
			// ProxySelectorRoutePlanner(
			// rClient.getConnectionManager().getSchemeRegistry(),
			// ProxySelector.getDefault());
			// rClient.setRoutePlanner(routePlanner);
			// return rClient;
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultHttpClient();
		}
	}

	private static SubjectSlot[][] get_time_table_object(Document time_table_doc) { //
		SubjectSlot[][] r_time_table = new SubjectSlot[5][9];
		String[][] time_table = new String[5][9];
		int offset = 0;
		for (int i = 0; i < 5; ++i) {
			Element row = time_table_doc.select("table table table tbody")
					.first().child(i + offset + 1);

			int j_offset = 0;
			int min_row_height = 10, row_height = 0;
			int day_row_height = Integer.parseInt(row.child(0).attr("rowspan"));
			for (int j = 0; j < 9; ++j) {
				Element column = row.child(j + 1 - j_offset);

				int class_length = Integer.parseInt(column.attr("colspan")); // when
																				// one
																				// class
																				// spans
																				// columns
				if (class_length > 1) {
					for (int k = j; k < j + class_length; ++k) {
						time_table[i][k] = column.html();
						j_offset++;
					}
					j_offset -= 1;
					j += j_offset;
				} else
					time_table[i][j] = column.html();
				if ((row_height = Integer.parseInt(column.attr("rowspan"))) < min_row_height)
					min_row_height = row_height;
			}
			if (min_row_height != day_row_height) // means there are multiple
													// subjects in the same time
													// slot
				offset += 1;
		}
		for (int i = 0; i < 5; ++i)
			for (int j = 0; j < 9; ++j) {
				Pattern subject_pattern = Pattern
						.compile("(<.*?>)(.*?)(<.*?>)(.*?)(<?)");
				Matcher m = subject_pattern.matcher(time_table[i][j]);
				m.find();
				String subject_name = m.group(2);
				if (subject_name.startsWith("&")) // to figure out breaks in the
													// timetable
					subject_name = "";
				String venue = m.group(4);
				r_time_table[i][j] = new SubjectSlot(subject_name, venue, 1);
			}
		return r_time_table;
	}

	public static Boolean get_time_table(Context aContext) {
		cContext = aContext;
		try {
			// download time_table, save
			// it
			Log.d("Update", "Trying to download timetable!");
			SubjectSlot[][] time_table = downloadTimeTable();
			Log.d("Update", "Timetable downloaded, now saving it...");
			FileOutputStream fos = cContext.openFileOutput(TIME_TABLE_FILE,
					Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(time_table);
			Log.d("Success", "File written: ".concat(cContext.getFilesDir()
					.getAbsolutePath()));
			return true;
		} catch (Exception e) {
			Log.d("Error", "Caught Exception");
			return false;
		}
	}

	private static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private static boolean hasActiveInternetConnection(Context context) {
		if (isNetworkAvailable(context)) {
			try {
				HttpURLConnection urlc = (HttpURLConnection) (new URL(
						"http://www.google.com").openConnection());
				urlc.setRequestProperty("User-Agent", "Test");
				urlc.setRequestProperty("Connection", "close");
				urlc.setConnectTimeout(15000);
				urlc.connect();
				return (urlc.getResponseCode() == 200);
			} catch (IOException e) {
				Log.e("Error", "Error checking internet connection", e);
			}
		} else {
			Log.d("Error", "No network available!");
		}
		return false;
	}

	public static String[] getSecurityQuestion(String rollNo, Context context) {
		if (hasActiveInternetConnection(context)) {
			try {
				DefaultHttpClient httpClient = (DefaultHttpClient) getNewHttpClient();
				HttpGet sq_page = new HttpGet(ERP_GET_SQ.replace("!USER_ID!",
						rollNo));
				HttpResponse response = httpClient.execute(sq_page);
				String responseText = EntityUtils.toString(response.getEntity()).replaceAll("\n", "");
				if (!responseText.contains(":"))
					return null;
				return responseText.split(":");
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	// download the time_table and save it to local file
	private static SubjectSlot[][] downloadTimeTable() throws Exception {

		// Login data
		FileInputStream fis = new FileInputStream(cContext.getFilesDir()
				.getAbsolutePath().concat("/").concat(CustomClasses.LOGIN_FILE));
		ObjectInputStream ois = new ObjectInputStream(fis);
		LoginData loginData = (LoginData) ois.readObject();
		ois.close();

		// Some basic checks on the login form
		if (loginData.RollNo.length() < 4 || loginData.Password.length() == 0
				|| loginData.SQ.length() == 0)
			throw new Exception();

		// Check to make sure we have Internet

		if (!hasActiveInternetConnection(cContext)) {
			Log.d("Network Error", "Error: No network connection :( ");
			throw new Exception();
		} else {
			Log.d("Update", "Successfully able to connect to Google");
		}

		// Initialize the httpClient, and set the cookieStore and
		// localContext
		DefaultHttpClient httpClient = (DefaultHttpClient) getNewHttpClient();
		CookieStore cookieStore = new BasicCookieStore();
		HttpContext localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		// Go to the ERP Home page to get any cookies
		HttpGet home_page = new HttpGet(ERP_URL);
		HttpResponse response = httpClient.execute(home_page, localContext);

		// Then create a HttpPost object with the login data, and POST it
		HttpPost login_post = new HttpPost(ERP_LOGIN_URL);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);

		nameValuePairs.add(new BasicNameValuePair("user_id", loginData.RollNo));
		nameValuePairs.add(new BasicNameValuePair("password",
				loginData.Password));
		nameValuePairs.add(new BasicNameValuePair("answer", loginData.SQ));
		nameValuePairs.add(new BasicNameValuePair("question_id", loginData.U));
		login_post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		response = httpClient.execute(login_post, localContext);

		// Now parse the response to get the weird POST data that finally
		// redirects to ERP
		String login_redirect_thingy = EntityUtils.toString(response
				.getEntity());
		Document doc = Jsoup.parse(login_redirect_thingy);
		Elements redirect_inputs = doc.getElementsByTag("form").first()
				.getElementsByTag("input");
		String ssoToken = redirect_inputs.get(0).attr("value");
		String sessionToken = redirect_inputs.get(1).attr("value");
		// Create the necessary HttpPost object
		HttpPost httpPost = new HttpPost(ERP_REDIRECT_URL);
		List<NameValuePair> redirect_form_values = new ArrayList<NameValuePair>(
				2);
		redirect_form_values.add(new BasicNameValuePair("ssoToken", ssoToken));
		redirect_form_values.add(new BasicNameValuePair("sessionToken",
				sessionToken));
		httpPost.setEntity(new UrlEncodedFormEntity(redirect_form_values));
		response = httpClient.execute(httpPost, localContext);
		// Successfully logged in by this time! Obviously some error
		// handling will need to be
		// added here in case log in is unsuccessful

		// Again, parse the timetable page to get the weird POST data that
		// they seem to
		// use to authenticate sessions :-/
		httpPost = new HttpPost(ERP_TIMETABLE_FORM);
		response = httpClient.execute(httpPost, localContext);

		doc = Jsoup.parse(EntityUtils.toString(response.getEntity()));
		String jscript_string = doc.select("body table td script").toString();
		Pattern sso_pattern = Pattern.compile("(ssoToken.*?value.*?')(.+)(';)");
		Matcher m = sso_pattern.matcher(jscript_string);
		m.find();
		ssoToken = m.group(2);
		Pattern menu_id_pattern = Pattern
				.compile("(menu_id.*?value.*?')(.+)(';)");
		m = menu_id_pattern.matcher(jscript_string);
		m.find();
		String menu_id = m.group(2);
		// returnString = doc.toString();

		httpPost = new HttpPost(ERP_TIMETABLE_VIEW);
		List<NameValuePair> timetable_form_values = new ArrayList<NameValuePair>(
				2);
		timetable_form_values.add(new BasicNameValuePair("ssoToken", ssoToken));
		timetable_form_values.add(new BasicNameValuePair("menu_id", ssoToken));
		httpPost.setEntity(new UrlEncodedFormEntity(timetable_form_values));
		response = httpClient.execute(httpPost, localContext);
		String time_table_html = EntityUtils.toString(response.getEntity());

		// Okay, we've got the time table in html form. Now to parse and
		// display it beautifully.
		// Should abstract this out to a function

		doc = Jsoup.parse(time_table_html);
		return get_time_table_object(doc);

		// List<Cookie> cookies = cookieStore.getCookies();
		//
		// Log.d("Login Cookie", cookies_to_string(cookies));
		//
		// Header[] headerResponses = response.getAllHeaders();
		// String headerResponse = "";
		// for (Header h : headerResponses)
		// headerResponse += h.toString();
		//
		// HttpGet erp_listing = new HttpGet(ERP_LISTING);
		//
		//
		// response = httpClient.execute(erp_listing, localContext);
		//
		//
		// Log.d(DEBUG_TAG, "response stat code " +
		// response.getStatusLine().getStatusCode());

		// return returnString;
	}

}