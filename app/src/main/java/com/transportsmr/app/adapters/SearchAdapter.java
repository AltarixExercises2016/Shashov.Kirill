package com.transportsmr.app.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.transportsmr.app.R;
import com.transportsmr.app.model.DaoSession;

/**
 * Created by kirill on 24.12.16.
 */
public class SearchAdapter extends CursorAdapter {
    private DaoSession daoSession;

    public SearchAdapter(Context context, Cursor c, boolean autoRequery, DaoSession daoSession) {
        super(context, c, autoRequery);
        this.daoSession = daoSession;
    }

    public class ViewHolder {
        private TextView title;
        private TextView street;
        private TextView direction;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();

        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.search_item, parent, false);
        holder.title = (TextView) view.findViewById(R.id.search_item_title);
        holder.street = (TextView) view.findViewById(R.id.search_item_street);
        holder.direction = (TextView) view.findViewById(R.id.search_item_direction);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        //Stop stop = daoSession.getStopDao().readEntity(cursor, cursor.getPosition());
        holder.title.setText(cursor.getString(2));
        holder.street.setText(cursor.getString(4));
        holder.direction.setText(cursor.getString(6));
    }
}