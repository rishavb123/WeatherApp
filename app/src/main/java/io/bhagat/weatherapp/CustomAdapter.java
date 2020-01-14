package io.bhagat.weatherapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<String> {

    private Context context;
    private int resource;
    private ArrayList<String> list;

    public CustomAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.list = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View adapterLayout = layoutInflater.inflate(resource, null);
        TextView time = adapterLayout.findViewById(R.id.time);
        TextView temp = adapterLayout.findViewById(R.id.temp);
        int t = Integer.parseInt(list.get(position).split(";")[0].split(":")[0]);
        int tt = (t == 12)? 12 : t%12;
        time.setText(Integer.toString(tt)+":00"+((t>=12)?"PM": "AM"));
        temp.setText(list.get(position).split(";")[1]+" F");

        return adapterLayout;
    }
}