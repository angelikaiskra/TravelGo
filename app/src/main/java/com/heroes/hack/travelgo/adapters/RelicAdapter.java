package com.heroes.hack.travelgo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.objects.Relic;

import java.util.List;

/**
 * Created by Angelika Iskra on 20.10.2018.
 */
public class RelicAdapter extends ArrayAdapter<Relic> {

    public RelicAdapter(@NonNull Context context, List<Relic> relics) {
        super(context, 0, relics);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.relic_list_item, parent, false);
        }
        Relic currentRelic = getItem(position);

        TextView idView = (TextView) listItemView.findViewById(R.id.idView);
        idView.setText(String.valueOf(currentRelic.getId()));

        TextView identificationView = (TextView) listItemView.findViewById(R.id.identificationView);
        identificationView.setText(currentRelic.getIdentification());

        TextView placenameView = (TextView) listItemView.findViewById(R.id.placenameView);
        placenameView.setText(currentRelic.getPlaceName());

        return listItemView;
    }

}
