package com.gc.history.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gc.history.R;
import com.gc.history.entity.History;

import java.util.List;

public class HistoryAdapter extends BaseRecyclerAdapter<History> {

    public HistoryAdapter(Context context, List<History> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_time_line, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((HistoryViewHolder) holder).title.setText(list.get(position).getTitle());
        ((HistoryViewHolder) holder).date.setText(list.get(position).getDate());
        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    listener.onClick(v, pos);
                }
            });
        }
    }

    protected class HistoryViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView date;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.id_history_title);
            date = (TextView) itemView.findViewById(R.id.id_history_time);
        }
    }
}
