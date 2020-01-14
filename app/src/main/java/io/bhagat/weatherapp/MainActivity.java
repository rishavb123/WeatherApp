package io.bhagat.weatherapp;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private EditText editZip;
    private ListView weatherList;
    private ArrayList<String> arrayList;
    private CustomAdapter arrayAdapter;

    private ArrayList<String> db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editZip = findViewById(R.id.edit_zip);
        weatherList = findViewById(R.id.weather_list);

        arrayList = new ArrayList<>();
        arrayAdapter = new CustomAdapter(this, R.layout.list_view_template,arrayList);
        weatherList.setAdapter(arrayAdapter);
        db = new ArrayList<>();

        db.add("It is very very cold. Like its in the single digits. So its time to play Magic"); // 0-10
        db.add("It is still very cold so like don't go out. Just play Magic instead."); // 10-20
        db.add("Its below freezing right now. So since you can't go outside just play Magic"); // 20-30
        db.add("Its still a bit chilly. So like don't go outside, instead just play Magic"); // 30 - 40
        db.add("Don't you think it is the perfect weather to play Magic"); // 40-50
        db.add("Its a bit cold still but managable. GO PLAY MAGIC"); // 50-60
        db.add("Its pretty nice out now. So maybe play Magic outside"); // 60-70
        db.add("It is summer weather. This is great weather to play Magic outside."); // 70-80
        db.add("Its a bit hot out so just make sure to stay indoors and play Magic"); // 80-90
        db.add("Don't even think about going outside since it is really hot. Instead, just play Magic."); // 90 -100

        findViewById(R.id.main_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getAlpha() == 1)
                    v.setAlpha(0.7f);
                else
                    v.setAlpha(1);
            }
        });

        editZip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 5 && Integer.parseInt(s.toString()) > 0)
                    new GetWeather().execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    public void updateUI(JSONObject jsonObject) throws JSONException, NullPointerException {
        ((TextView) findViewById(R.id.location)).setText(jsonObject.getJSONObject("city").getString("name") + ", " + jsonObject.getJSONObject("city").getString("country"));
        JSONArray list = jsonObject.getJSONArray("list");
        arrayList.clear();
        int curIndex = 0;

        for(int i = 1; i <= 5; i++)
        {
            JSONObject dayWeather = list.getJSONObject(curIndex);
            Date date = new Date(1000*dayWeather.getLong("dt"));
            int day = date.getDay();
            double high = -99999;
            double low = 99999;
            if(i != 1)
            {
                ImageView imageView = findViewById(getResource("bottom_image_"+i, R.id.class));
                imageView.setImageResource(getResource(list.getJSONObject(curIndex+2).getJSONArray("weather").getJSONObject(0).getString("main").toLowerCase(), R.drawable.class));
            }
            while(curIndex < list.length() && (new Date(1000*dayWeather.getLong("dt"))).getDay() == day)
            {
                dayWeather = list.getJSONObject(curIndex);
                if(i == 1)
                {
                    arrayList.add((new Date(1000*dayWeather.getLong("dt"))).toString().split(" ")[3]+";"+dayWeather.getJSONObject("main").getDouble("temp"));
                }
                if(dayWeather.getJSONObject("main").getDouble("temp_min") < low)
                    low = dayWeather.getJSONObject("main").getDouble("temp_min");
                if(dayWeather.getJSONObject("main").getDouble("temp_max") > high)
                    high = dayWeather.getJSONObject("main").getDouble("temp_max");
                curIndex++;
            }
            ((TextView) findViewById(getResource("bottom_textView_"+i, R.id.class))).setText(date.toString().split(" ")[0] + " " + date.toString().split(" ")[1]  + " " + date.toString().split(" ")[2]  + " " + date.toString().split(" ")[5]  + " " + "\nHigh: " + high + "\nLow: " + low);
        }
        arrayAdapter.notifyDataSetChanged();

        TextView tv = findViewById(R.id.weather_stuff);

        tv.setText(list.getJSONObject(0).getJSONObject("main").getDouble("temp")+"\n"+db.get((int) list.getJSONObject(0).getJSONObject("main").getDouble("temp")/10)+"\n"+list.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("main"));


        ImageView imageView = findViewById(R.id.main_image);
        ImageView imageView2 = findViewById(R.id.bottom_image_1);
        imageView.setImageResource(getResource("b"+list.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("main").toLowerCase(), R.drawable.class));
        imageView2.setImageResource(getResource(list.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("main").toLowerCase(), R.drawable.class));
    }

    public int getResource(String resName, Class<?> c) {
        try {
            return c.getDeclaredField(resName).getInt(c.getDeclaredField(resName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @SuppressLint("StaticFieldLeak")
    public class GetWeather extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... zips) {
            try {
                String zip = zips[0];
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?APPID="+getString(R.string.open_weather_api_key)+"&zip="+zip+"&units=imperial");
                URLConnection urlConnection = url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String temp;
                StringBuilder total = new StringBuilder(1024);

                while((temp =  reader.readLine()) != null)
                {
                    total.append(temp).append('\n');
                }

                reader.close();

                JSONObject jsonObject = new JSONObject(total.toString());
                Log.d("TAG-THREAD", jsonObject.toString());
                return jsonObject;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                updateUI(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }

}