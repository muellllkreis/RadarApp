package com.radarapp.mjr9r.radar.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.radarapp.mjr9r.radar.R;
import com.radarapp.mjr9r.radar.fragments.BookmarkFragment.OnListFragmentInteractionListener;
import com.radarapp.mjr9r.radar.helpers.TimeAgo;
import com.radarapp.mjr9r.radar.model.DropMessage;

import java.util.Date;
import java.util.List;


public class DropMessageRecyclerViewAdapter extends RecyclerView.Adapter<DropMessageRecyclerViewAdapter.ViewHolder> {

    private final List<DropMessage> mValues;
    private final OnListFragmentInteractionListener mListener;

    public DropMessageRecyclerViewAdapter(List<DropMessage> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_bookmark, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        String timeFromNow = TimeAgo.toDuration(new Date().getTime() - mValues.get(position).getDate().getTime());
        holder.mDateView.setText(timeFromNow);
        holder.mContentView.setText(mValues.get(position).getContent());
        holder.mFilterView.setText(mValues.get(position).getFilter().getName());
        holder.mIconView.setImageResource(mValues.get(position).getFilter().getIconID());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    // mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDateView;
        public final TextView mFilterView;
        public final TextView mContentView;
        public final ImageView mIconView;
        public DropMessage mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDateView = (TextView) view.findViewById(R.id.bookmark_list_date);
            mContentView = (TextView) view.findViewById(R.id.bookmark_list_content);
            mFilterView = (TextView) view.findViewById(R.id.bookmark_list_filter);
            mIconView = (ImageView) view.findViewById(R.id.bookmark_list_icon);
        }

        @Override
        public String toString() {
            return super.toString(); //+ " '" + mContentView.getText() + "'";
        }
    }
}
