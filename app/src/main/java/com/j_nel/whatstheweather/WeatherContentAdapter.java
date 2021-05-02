package com.j_nel.whatstheweather;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherContentAdapter extends RecyclerView.Adapter<WeatherContentAdapter.ViewHolder> {
    //Variables
    protected Map<String, Bitmap> bitmaps = new HashMap<>();
    protected List<WeatherModel> weatherModelList;

    //Constructor
    public WeatherContentAdapter(List<WeatherModel> WeatherModelList) {
        weatherModelList = WeatherModelList;
    }

    @NonNull
    @Override
    public WeatherContentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup Parent, int ViewType) {
        View view = LayoutInflater.from(Parent.getContext()).inflate(R.layout.list_item, Parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder ViewHolder, final int Position) {
        //Set data on all the view items listed below.
        WeatherModel weatherModel = weatherModelList.get(Position);
        ViewHolder.tvMaximumTemperature.setText(weatherModel.strMaximumTemperature);
        ViewHolder.tvMinimumTemperature.setText(weatherModel.strMinimumTemperature);
        ViewHolder.tvDayOfTheWeek.setText(weatherModel.strDayOfTheWeek);
        if (bitmaps.containsKey(Position)) {
            ViewHolder.ivWeatherIcon.setImageBitmap(bitmaps.get(Position));
        } else {
            if (!weatherModel.strWeatherIconURL.trim().toUpperCase().equals("N/A")) {
                new LoadWeatherIconTask(ViewHolder.ivWeatherIcon).execute(weatherModel.strWeatherIconURL);
            }
        }
    }

    @Override
    public int getItemCount() {
        return weatherModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWeatherIcon;
        TextView tvMaximumTemperature, tvMinimumTemperature, tvDayOfTheWeek, tvHumidity;
        CardView cvListItem;

        public ViewHolder(View View) {
            super(View);
            ivWeatherIcon = View.findViewById(R.id.ivWeatherIcon);
            tvDayOfTheWeek = View.findViewById(R.id.tvDayOfTheWeek);
            tvMaximumTemperature = View.findViewById(R.id.tvMaximumTemperature);
            tvMinimumTemperature = View.findViewById(R.id.tvMinimumTemperature);
            cvListItem = View.findViewById(R.id.cvListItem);
        }
    }

    private class LoadWeatherIconTask extends AsyncTask<String, Void, Bitmap> {
        //Retrieve weather icon for each item using an AsyncTask.
        private final ImageView imageView;
        private final Map<String, Object> additionalProperties = new HashMap<>();

        public LoadWeatherIconTask(ImageView ImageView) {
            imageView = ImageView;
        }

        @Override
        protected Bitmap doInBackground(String... Strings) {
            Bitmap bitmap = null;
            HttpURLConnection connection;
            try {
                URL url = new URL(Strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                try (InputStream inputStream = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    additionalProperties.put(Strings[0], bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap Bitmap) {
            imageView.setImageBitmap(Bitmap);
        }
    }
}