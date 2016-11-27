package com.transportsmr.app.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.transportsmr.app.R;

/**
 * Created by kirill on 26.11.2016.
 */
public class StopsRecyclerAdapter extends RecyclerView.Adapter<StopsRecyclerAdapter.ViewHolder> {

    private String[] mDataset;

    // ����� view holder-� � ������� �������� �� �������� ������ �� ������ �������
    // ���������� ������ ������
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // ��� ����� ������� ������ �� ������ TextView
        public TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.tv_recycler_item);
        }
    }

    // �����������
    public StopsRecyclerAdapter(String[] dataset) {
        mDataset = dataset;
    }

    // ������� ����� views (���������� layout manager-��)
    @Override
    public StopsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_stop_item, parent, false);

        // ��� ����� ���������� ������ �������� ������� (size, margins, paddings � ��.)

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // �������� ������� ���������� view (���������� layout manager-��)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mTextView.setText(mDataset[position]);

    }

    // ���������� ������ ������ (���������� layout manager-��)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
