package com.digitalforce.datingapp.social.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.digitalforce.datingapp.R;
import com.digitalforce.datingapp.constants.DatingConstants;
import com.digitalforce.datingapp.utils.ToastCustom;
import com.digitalforce.datingapp.view.LoginActivity;


public class LoginTwitter {

	private Twitter twitter;
	private Context context;
	private RequestToken requestToken = null;
	private AccessToken accessToken;
	private String oauth_url,oauth_verifier,profile_url;
	private Dialog auth_dialog;
	private WebView web;
	private ProgressDialog progress;
	private Bitmap bitmap;
	public LoginTwitter(Context context)
	{
		this.context = context;
		intializeVariable(context);
	}

	public void intializeVariable(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(DatingConstants.CONSUMER_KEY, DatingConstants.CONSUMER_SECRET);
		new TokenGet().execute();
	}

	private class TokenGet extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... args) {

			try {
				requestToken = twitter.getOAuthRequestToken();
				oauth_url = requestToken.getAuthorizationURL();
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return oauth_url;
		}
		@Override
		protected void onPostExecute(String oauth_url) {
			if(oauth_url != null){
				Log.e("URL", oauth_url);
				auth_dialog = new Dialog(context);
				auth_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); 

				auth_dialog.setContentView(R.layout.auth_dialog);
				web = (WebView)auth_dialog.findViewById(R.id.webv);
				web.getSettings().setJavaScriptEnabled(true);
				web.loadUrl(oauth_url);
				web.setWebViewClient(new WebViewClient() {
					boolean authComplete = false;
					@Override
					public void onPageStarted(WebView view, String url, Bitmap favicon){
						super.onPageStarted(view, url, favicon);
					}

					@Override
					public void onPageFinished(WebView view, String url) {
						super.onPageFinished(view, url);
						if (url.contains("oauth_verifier") && authComplete == false){
							authComplete = true;
							Log.e("Url",url);
							Uri uri = Uri.parse(url);
							oauth_verifier = uri.getQueryParameter("oauth_verifier");

							auth_dialog.dismiss();
							new AccessTokenGet().execute();
						}else if(url.contains("denied")){
							auth_dialog.dismiss();
							Toast.makeText(context, "Sorry !, Permission Denied", Toast.LENGTH_SHORT).show();


						}
					}
				});
				auth_dialog.show();
				auth_dialog.setCancelable(true);



			}else{

				Toast.makeText(context, "Sorry !, Network Error or Invalid Credentials", Toast.LENGTH_SHORT).show();


			}
		}
	}

	private class AccessTokenGet extends AsyncTask<String, String, User> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = new ProgressDialog(context);
			progress.setMessage("Fetching Data ...");
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setIndeterminate(true);
			progress.show();

		}


		@Override
		protected User doInBackground(String... args) {

			User user = null;
			try {

				accessToken = twitter.getOAuthAccessToken(requestToken, oauth_verifier); 		   
				user = twitter.showUser(accessToken.getUserId());


			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	    
			return user;
		}
		@Override
		protected void onPostExecute(User user) {

			progress.hide();

			if(user!=null){

				String name =user.getName();
				profile_url = user.getOriginalProfileImageURL();
				String twitterId = ""+user.getId();
				String user_name = user.getScreenName();
				/*ToastCustom.makeText(context, 
						"name = "+name+" "+"profile_url = "+profile_url+" "+"twitterId = "+twitterId+" "+"user_name = "+user_name, 
						Toast.LENGTH_LONG);*/
				
				((LoginActivity)context).doSocialLogin(user_name);
				
				
			}
		}


	}

}
