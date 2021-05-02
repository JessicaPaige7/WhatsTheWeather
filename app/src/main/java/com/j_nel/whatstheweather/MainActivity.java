package com.j_nel.whatstheweather;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Variables
    private List<WeatherModel> weatherModelList = new ArrayList<>();
    private WeatherContentAdapter weatherContentAdapter;
    private String strWeatherMapURL, strBaseURL, strWeatherMapAPIKey, strCurrentTemperature;
    private JSONArray jsonArray;
    private JSONObject jsonObject, joTemp, joWeather;
    private LocationFinder locationFinder;
    private double dbLatitude, dbLongitude;
    private Boolean IsFirstSearch = true;
    ProgressBar pbLoadingWeatherData;
    TextView tvCurrentWeather, tvCurrentDayOfTheWeek;
    FloatingActionButton fabRefreshLocation;
    RecyclerView rvWeatherListItems;
    Context context;

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception E) {
            Toast.makeText(getApplicationContext(), R.string.location_permission, Toast.LENGTH_SHORT).show();
            E.printStackTrace();
        }

        FindUserCurrentLocation();
        rvWeatherListItems = findViewById(R.id.rvWeatherListItems);
        fabRefreshLocation = findViewById(R.id.fabRefreshLocation);
        tvCurrentWeather = findViewById(R.id.tvCurrentWeather);
        tvCurrentDayOfTheWeek = findViewById(R.id.tvCurrentDayOfTheWeek);
        pbLoadingWeatherData = findViewById(R.id.pbLoadingWeatherData);
        pbLoadingWeatherData.setVisibility(View.VISIBLE);

        if (IsFirstSearch) {
            BuildUpTheURL();
            IsFirstSearch = false;
        }
        fabRefreshLocation.setOnClickListener(view -> {
            pbLoadingWeatherData.setVisibility(View.VISIBLE);
            BuildUpTheURL();
        });
    }

    public void BuildUpTheURL() {
        URL url = WeatherMapURL(Double.toString(dbLatitude), Double.toString(dbLongitude));
        if (url != null) {
            GetTheWeather getTheWeather = new GetTheWeather();
            getTheWeather.execute(url);
        } else {
            Toast.makeText(getApplicationContext(), R.string.invalid_url, Toast.LENGTH_SHORT).show();
        }
    }

    public void FindUserCurrentLocation() {
        locationFinder = new LocationFinder(MainActivity.this);
        if (locationFinder.canUserGetLocation()) {
            dbLatitude = locationFinder.getUserLatitude();
            dbLongitude = locationFinder.getUserLongitude();
        } else {
            locationFinder.showSettingsAlert();
        }
    }

    private URL WeatherMapURL(String Latitude, String Longitude) {
        //Create WeatherMap URL to retrieve data
        strBaseURL = getString(R.string.web_service_url);
        strWeatherMapAPIKey = getString(R.string.api_key);
        try {
            strWeatherMapURL = strBaseURL + "?lat=" + URLEncoder.encode(Latitude, "UTF-8") + "&lon=" + URLEncoder.encode(Longitude, "UTF-8") + "&exclude=hourly,minutely&units=metric&appid=" + strWeatherMapAPIKey;
            return new URL(strWeatherMapURL);
        } catch (Exception E) {
            E.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.invalid_url, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private class GetTheWeather extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(URL... URLS) {
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) URLS[0].openConnection();
                int intResponse = httpURLConnection.getResponseCode();

                if (intResponse == HttpURLConnection.HTTP_OK) {
                    StringBuilder stringBuilder = new StringBuilder();
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                    } catch (IOException E) {
                        E.printStackTrace();
                    }
                    return new JSONObject(stringBuilder.toString());
                } else {
                    Toast.makeText(getApplicationContext(), R.string.json_error, Toast.LENGTH_SHORT).show();
                }
            } catch (IOException | JSONException E) {
                Toast.makeText(getApplicationContext(), R.string.cannot_find_current_location, Toast.LENGTH_SHORT).show();
                E.printStackTrace();
            } finally {
                httpURLConnection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject WeatherObject) {
            ConvertJSONObjectToArrayList(WeatherObject);

            weatherContentAdapter = new WeatherContentAdapter(weatherModelList);
            if (weatherContentAdapter.getItemCount() > 0) {
                rvWeatherListItems.setLayoutManager(new LinearLayoutManager(context));
                rvWeatherListItems.setItemAnimator(new DefaultItemAnimator());
                rvWeatherListItems.setAdapter(weatherContentAdapter);
                pbLoadingWeatherData.setVisibility(View.INVISIBLE);
                weatherContentAdapter.notifyDataSetChanged();
                rvWeatherListItems.smoothScrollToPosition(0);
            } else {

            }
        }

        private void ConvertJSONObjectToArrayList(JSONObject WeatherForecast) {
            weatherModelList = new ArrayList<>();
            try {
                JSONObject Sunshine = WeatherForecast.getJSONObject("current");
                if (Sunshine != null && Sunshine.length() > 0) {
                    NumberFormat numberFormat = NumberFormat.getInstance();
                    numberFormat.setMaximumFractionDigits(0);
                    strCurrentTemperature = numberFormat.format(Sunshine.getLong("temp"))+ "\u00B0C";
                    tvCurrentWeather.setText(strCurrentTemperature);
                    tvCurrentDayOfTheWeek.setText(WeatherModel.ConvertTimestampToWeekday(Sunshine.getLong("dt")));
                }
                jsonArray = WeatherForecast.getJSONArray("daily");
                for (int x = 0; x < jsonArray.length(); x++) {
                    jsonObject = jsonArray.getJSONObject(x);
                    joTemp = jsonObject.getJSONObject("temp");
                    joWeather = jsonObject.getJSONArray("weather").getJSONObject(0);
                    weatherModelList.add(new WeatherModel(
                            joTemp.getDouble("min"),
                            joTemp.getDouble("max"),
                            jsonObject.getLong("dt"),
                            jsonObject.getDouble("humidity"),
                            joWeather.getString("icon"),
                            joWeather.getString("description")
                    ));
                }
            } catch (JSONException E) {
                E.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.json_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}