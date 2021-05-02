package com.j_nel.whatstheweather;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

class WeatherModel {
    //Variables
    public final String strMinimumTemperature, strMaximumTemperature, strDayOfTheWeek, strHumidity, strWeatherIconURL, strDescription;

    //Constructor
    public WeatherModel(double MinimumTemperature, double MaximumTemperature, long DayOfTheWeek, double Humidity, String WeatherIconURL, String Description) {
        //Format the temperatures from doubles to rounded integers
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);

        strMinimumTemperature = numberFormat.format(MinimumTemperature)+ "\u00B0C";
        strMaximumTemperature = numberFormat.format(MaximumTemperature)+ "\u00B0C" + " \\";
        strDayOfTheWeek = ConvertTimestampToWeekday(DayOfTheWeek);
        strHumidity = NumberFormat.getPercentInstance().format(Humidity / 100.00);
        strWeatherIconURL = "https://openweathermap.org/img/w/" + WeatherIconURL + ".png";
        strDescription = Description;
    }

    public static String ConvertTimestampToWeekday(long TimeStamp) {
        //Convert the timestamp to a day of the week
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeStamp * 1000);
        TimeZone timeZone = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy");
        return simpleDateFormat.format(calendar.getTime());
    }
}