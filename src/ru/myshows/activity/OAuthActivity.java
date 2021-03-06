package ru.myshows.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import ru.myshows.util.Settings;
import ru.myshows.util.TwitterUtil;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Georgy Gobozov
 * Date: 14.10.13
 */
public class OAuthActivity extends Activity {


    public static final int OAUTH_TWITTER = 1;
    public static final int OAUTH_FACEBOOK = 2;
    public static final int OAUTH_VK = 3;


    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth);

        int type = getIntent().getIntExtra("type", -1);


        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(Settings.TWITTER_CALLBACK_URL)){

            String arg = uri.getQueryParameter(Settings.TWITTER_OAUTH_VERIFIER);
            new TwitterGetAccessTokenTask().execute(arg);

        }

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearCache(true);


        switch (type) {
            case OAUTH_VK:

                String api_id = "2155950";
                final String redirect_url = "https://oauth.vk.com/blank.html";
                String settings = "";
                String url = "https://oauth.vk.com/authorize?client_id=" + api_id + "&display=mobile&scope=" + settings + "&redirect_uri=" + URLEncoder.encode(redirect_url) + "&response_type=token&v=5.2";

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                        parseUrl(url, redirect_url);
                    }
                });


                webView.loadUrl(url);
                break;

            case OAUTH_FACEBOOK:
                String app_id = "568473939831654";
                String secret = "cb0262c6051d38e11674871822be646f";
                final String redirectUrl = "https://www.facebook.com/connect/login_success.html";

                String facebookUrl = "https://www.facebook.com/dialog/oauth?client_id=" + app_id  + /*"&client_secret=" + secret +*/ "&redirect_uri=" + URLEncoder.encode(redirectUrl) + "&response_type=token";

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                        parseUrl(url, redirectUrl);
                    }
                });

                webView.loadUrl(facebookUrl);

                // to get user_id
                // http://stackoverflow.com/questions/3546677/how-to-get-the-facebook-user-id-using-the-access-token
                break;




            case OAUTH_TWITTER:

                new TwitterAuthenticateTask().execute();

        }

    }

    private void parseUrl(String url, String urlParseTo) {
        try {
            if (url == null)
                return;
            Log.i("MyShows", "url=" + url);
            //if (url.startsWith("https://oauth.vk.com/blank.html")) {
            if (url.startsWith(urlParseTo)) {
                if (!url.contains("error=")) {
                    String[] auth = parseRedirectUrl(url);
                    Intent intent = new Intent();
                    Log.i("MyShows", "vk token =" + auth[0]);
                    Log.i("MyShows", "vk user_id =" + auth[1]);
                    intent.putExtra("token", auth[0]);
                    intent.putExtra("user_id", Long.parseLong(auth[1]));
                    setResult(Activity.RESULT_OK, intent);
                } else {
                    Log.i("MyShows", "ERROR url=" + url);
                }
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] parseRedirectUrl(String url) throws Exception {
        //url is something like http://api.vkontakte.ru/blank.html#access_token=66e8f7a266af0dd477fcd3916366b17436e66af77ac352aeb270be99df7deeb&expires_in=0&user_id=7657164
        String access_token = extractPattern(url, "access_token=(.*?)&");
        Log.i("MyShows", "access_token=" + access_token);
        String user_id = extractPattern(url, "user_id=(\\d*)");
        Log.i("MyShows", "user_id=" + user_id);
        if (user_id == null || user_id.length() == 0 || access_token == null || access_token.length() == 0)
            throw new Exception("Failed to parse redirect url " + url);
        return new String[]{access_token, user_id};
    }

    public static String extractPattern(String string, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        if (!m.find())
            return null;
        return m.toMatchResult().group(1);
    }

    class TwitterAuthenticateTask extends AsyncTask<String, String, RequestToken> {

        @Override
        protected void onPostExecute(RequestToken requestToken) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()));
            startActivity(intent);
        }

        @Override
        protected RequestToken doInBackground(String... params) {
            return TwitterUtil.getInstance().getRequestToken();
        }
    }

    class TwitterGetAccessTokenTask extends AsyncTask<String, String, AccessToken> {

        @Override
        protected void onPostExecute(AccessToken accessToken) {
            Log.d("MyShows", "access twitter token = " + accessToken.getToken());
            Log.d("MyShows", "access twitter secret = " + accessToken.getTokenSecret());
            Log.d("MyShows", "access twitter login = " + accessToken.getScreenName());


        }

        @Override
        protected AccessToken doInBackground(String... params) {

            Twitter twitter = TwitterUtil.getInstance().getTwitter();
            RequestToken requestToken = TwitterUtil.getInstance().getRequestToken();
           // SharedPreferences sharedPreferences = Settings.getPreferences();

            if (params[0] != null) {
                try {
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, params[0]);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString(Settings.TWITTER_OAUTH_TOKEN, accessToken.getToken());
//                    editor.putString(Settings.TWITTER_OAUTH_TOKEN_SECRET, accessToken.getTokenSecret());
//                    editor.putBoolean(Settings.TWITTER_IS_LOGGED_IN, true);
//                    editor.commit();
                    return accessToken;
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
//            } else {
//                String accessTokenString = sharedPreferences.getString(Settings.TWITTER_OAUTH_TOKEN, "");
//                String accessTokenSecret = sharedPreferences.getString(Settings.TWITTER_OAUTH_TOKEN_SECRET, "");
//                AccessToken accessToken = new AccessToken(accessTokenString, accessTokenSecret);
//                return accessToken;
            }

            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }




}
