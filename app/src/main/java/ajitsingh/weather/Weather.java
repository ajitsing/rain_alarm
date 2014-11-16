package ajitsingh.weather;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ajitsingh.weather.util.DataFetchListener;
import ajitsingh.weather.util.DataFetcher;
import ajitsingh.weather.util.NotificationUtils;
import ajitsingh.weather.util.SystemUiHider;


public class Weather extends Activity {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final boolean TOGGLE_ON_CLICK = true;
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private SystemUiHider mSystemUiHider;
    private JSONObject weatherInfo;
    private Context activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateWeatherInfo();

        setContentView(R.layout.activity_weather);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        findViewById(R.id.get_weather_data_button).setOnTouchListener(mDelayHideTouchListener);
        setUpService();
    }

    private void updateWeatherInfo() {
        new LongOperation().execute();
    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("weatherUpdate"));
        updateWeatherInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private void setUpService() {
        Intent intent = new Intent(getBaseContext(), WeatherService.class);
        startService(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String weatherInfoString = intent.getStringExtra("weatherInfo");

            try {
                weatherInfo = new JSONObject(weatherInfoString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            updateWeatherInfo();
            Log.d("receiver", "Got message: " + weatherInfo);
        }
    };

    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }

            setUpService();
            updateWeatherInfo();
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void setWeatherIcon(JSONObject weatherInfo) {
        TextView filename = (TextView)findViewById(R.id.fullscreen_content);
        filename.setText("");

        int actualId = 0;
        long sunrise = 0;
        long sunset = 0;
        try {
            JSONObject details = weatherInfo.getJSONArray("weather").getJSONObject(0);
            actualId = details.getInt("id");
            sunrise = weatherInfo.getJSONObject("sys").getLong("sunrise") * 1000;
            sunset = weatherInfo.getJSONObject("sys").getLong("sunset") * 1000;
        } catch (JSONException e) {
            e.printStackTrace();
        }


        int id = actualId / 100;
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                updateViewAndNotify(filename, "SUNNY");
            } else {
                updateViewAndNotify(filename, "CLEAR NIGHT");
            }
        } else {
            switch (id) {
                case 2:
                    updateViewAndNotify(filename, "THUNDER");
                    if(filename.getText() != "THUNDER")NotificationUtils.vibrateFor(this, 4000);
                    break;
                case 3:
                    updateViewAndNotify(filename, "DRIZZLE");
                    if(filename.getText() != "DRIZZLE")NotificationUtils.vibrateFor(this, 4000);
                    break;
                case 7:
                    updateViewAndNotify(filename, "FOGGY");
                    break;
                case 8:
                    updateViewAndNotify(filename, "CLOUDY");
                    break;
                case 6:
                    updateViewAndNotify(filename, "SNOWY");
                    break;
                case 5:
                    updateViewAndNotify(filename, "RAINY");
                    if(filename.getText() != "RAINY")NotificationUtils.vibrateFor(this, 5000);
                    break;
            }
        }
    }

    private void updateViewAndNotify(TextView filename, String weather) {
        if(filename.getText() != weather){
            filename.setText(weather);
            NotificationUtils.sendNotification(this, "Weather Update", weather);
        }
    }

    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... values) {
            DataFetcher.fetchWeatherInfo(new DataFetchListener() {
                @Override
                public void onDataFetch(JSONObject data) {
                    weatherInfo = data;
                }
            });

            return "success";
        }

        @Override
        protected void onPostExecute(String result) {
            setWeatherIcon(weatherInfo);
        }

    }
}
