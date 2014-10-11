package com.digitalforce.datingapp.view;

import org.json.JSONException;
import org.json.JSONObject;


import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalforce.datingapp.DatingApplication;
import com.digitalforce.datingapp.R;
import com.digitalforce.datingapp.constants.ApiEvent;
import com.digitalforce.datingapp.constants.AppConstants;
import com.digitalforce.datingapp.constants.DatingUrlConstants;
import com.digitalforce.datingapp.model.UserInfo;
import com.digitalforce.datingapp.persistance.DatingAppPreference;
import com.digitalforce.datingapp.social.facebook.OnProfileListener;
import com.digitalforce.datingapp.social.facebook.ProfileHelper;
import com.digitalforce.datingapp.utils.ToastCustom;
import com.digitalforce.datingapp.utils.Validation;
import com.farru.android.network.ServiceResponse;
import com.farru.android.utill.Utils;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.Permission.Type;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;



public class LoginActivity extends BaseActivity implements OnClickListener,OnLoginListener, ProfileHelper{

	private EditText medtCoolName, medtPassword;
	private TextView mtxtNewUser, mtxtForgotPassword;
	private ImageView mimgFb, mimgTw;
	private Button mbtnLogin;
	private boolean mSignInClicked;
	private boolean mIntentInProgress;

	public SimpleFacebook mSimpleFacebook;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_login);

		medtCoolName = (EditText) findViewById(R.id.edt_login_cool_name);
		medtPassword = (EditText) findViewById(R.id.edt_login_password);
		mtxtNewUser = (TextView) findViewById(R.id.txt_login_forgot_password);
		mtxtForgotPassword = (TextView) findViewById(R.id.txt_login_new_user);
		mbtnLogin = (Button) findViewById(R.id.btn_login_lets_go);
		mimgFb = (ImageView) findViewById(R.id.img_login_facebook);
		mimgTw = (ImageView) findViewById(R.id.img_login_twitter);


		mtxtNewUser.setOnClickListener(this);
		mtxtForgotPassword.setOnClickListener(this);
		mbtnLogin.setOnClickListener(this);
		mimgTw.setOnClickListener(this);
		mimgFb.setOnClickListener(this);


	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_login_lets_go:
			if(checkValidation())
			{

				String postData = getRequestJson(medtCoolName.getText().toString(),medtPassword.getText().toString());
				Log.e("Post Data", postData);
				postData(DatingUrlConstants.LOGIN_URL, ApiEvent.LOGIN_EVENT, postData);
			}

			break;
		case R.id.txt_login_forgot_password:
			//ToastCustom.underDevelopment(this);
			Intent intentForgot = new Intent(this, ForgotpsswordActivity.class);
			startActivity(intentForgot);
			break;
		case R.id.txt_login_new_user:
			//ToastCustom.underDevelopment(this);
			Intent intentSign = new Intent(this, SignUpActivity.class);
			startActivity(intentSign);
			break;
		case R.id.img_login_facebook:
			//ToastCustom.underDevelopment(this);
			loginWithFb();
			break;
		case R.id.img_login_twitter:
			ToastCustom.underDevelopment(this);
			break;


		default:
			break;
		}
	}

	private boolean checkValidation()
	{
		boolean valid = true;
		if(!Validation.hasText(medtCoolName))
			valid = false;
		if(!Validation.hasText(medtPassword))
			valid = false;

		return valid;
	}
	protected void onStart() {
		super.onStart();
		//mGoogleApiClient.connect();
	}

	protected void onStop() {
		super.onStop();
		/*if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}*/
	}


	@Override
	public void onEvent(int eventId, Object eventData) {
		// TODO Auto-generated method stub

	}



	@Override
	protected void updateUi(ServiceResponse serviceResponse) {
		if(serviceResponse!=null){
			switch (serviceResponse.getErrorCode()) {
			case ServiceResponse.SUCCESS:
				onSuccess(serviceResponse);
				break;
			case ServiceResponse.MESSAGE_ERROR:
				showCommonError(serviceResponse.getErrorMessages());
				break;
			default:
				showCommonError(null);
				break;
			}
		}else{
			showCommonError(null);
		}



	}

	private void onSuccess(ServiceResponse serviceResponse){
		switch (serviceResponse.getEventType()) {
		case ApiEvent.LOGIN_EVENT:
		case ApiEvent.LOGIN_FB_GMAIL_EVENT:
			UserInfo userInfo = (UserInfo)serviceResponse.getResponseObject();
			if(userInfo!=null){
				showCommonError(serviceResponse.getBaseModel().getSuccessMsg()+" UserId: "+userInfo.getUserId()); // its only for testing
				navigateToHomeScreen();
			}else{
				showCommonError(null);
			}
			break;

		default:
			break;
		}
	}


	

	private void loginWithFb() {
		showProgressDialog();
		
		 Permission[] perm = new Permission[3];
	        perm[0] = Permission.BASIC_INFO;
	        perm[1] = Permission.USER_BIRTHDAY;
	        perm[2] = Permission.EMAIL;
	        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
	                .setAppId(AppConstants.FB_APP_ID)
	                .setNamespace(AppConstants.APP_NAME_SPACE)
	                .setPermissions(perm)
	                .setAskForAllPermissionsAtOnce(false)
	                .build();
	    SimpleFacebook.setConfiguration(configuration);
		mSimpleFacebook = SimpleFacebook.getInstance(this);
		
		mSimpleFacebook.login(this);
	}

	@Override
	public void onFail(String reason) {
		Log.e(AppConstants.LOG_TAG, "Failed to login");
		Utils.displayToast(this,"Unable to connect to Facebook, Please try again later.");
		removeProgressDialog();
	}

	@Override
	public void onException(Throwable throwable) {
		Log.e(AppConstants.LOG_TAG, "Bad thing happened", throwable);
		Utils.displayToast(this,"Unable to connect to Facebook, Please try again later.");
		removeProgressDialog();
	}

	@Override
	public void onThinking() {
		showProgressDialog();
	}

	@Override
	public void onLogin() {
		if (null != mSimpleFacebook) {
			OnProfileListener listener= new OnProfileListener(this);
			mSimpleFacebook.getProfile(listener);
		}
	}

	@Override
	public void onNotAcceptingPermissions(Permission.Type type) {
		removeProgressDialog();
	}

	public void onProfile(Profile profile) {
		removeProgressDialog();
		doFbGmailLogin(profile.getEmail());
	}

	public void onProfileFail(String reason) {
		removeProgressDialog();
		Utils.displayToast(this, "Profile updation failed. Please try again.");

	}

	public void onProfileException(Throwable th) {
		removeProgressDialog();
		Utils.displayToast(this, "Profile updation failed. Please try again later.");
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
	}
	
	private void doFbGmailLogin(String email){
		String postData = getRequestJson(email,null);
		Log.e("Post Data", postData);
		postData(DatingUrlConstants.LOGIN_WITH_FB_GMAIL_URL, ApiEvent.LOGIN_FB_GMAIL_EVENT, postData);
	}
	
	
	private String getRequestJson(String email,String password){

		//{"email":"shaan@gmail.com", "password":"12345", "lat":"743872432", "long":"3749382", "device":"89748937432784937498hjjk38343"}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.putOpt("email", email);
			if(password!=null)
			   jsonObject.putOpt("password", password);
			
			jsonObject.putOpt("lat", DatingAppPreference.getString(DatingAppPreference.USER_DEVICE_LATITUDE, "0.0", this));
			jsonObject.putOpt("long", DatingAppPreference.getString(DatingAppPreference.USER_DEVICE_LONGITUDE, "0.0", this));
			jsonObject.putOpt("device", Utils.getDeviceEMI(this));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.e("Login Request", jsonObject.toString());
		return jsonObject.toString();
	}
	
	private void navigateToHomeScreen(){
		
	}

}
