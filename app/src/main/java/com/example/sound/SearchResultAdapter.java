
package com.example.sound;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private Context context;
    private List<SearchResult> results;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    public static class SearchResult {
        public String title;
        public Object thumbnail;
        public int originalPosition;
        public boolean isUrl;

        public SearchResult(String title, Object thumbnail, int position, boolean isUrl) {
            this.title = title;
            this.thumbnail = thumbnail;
            this.originalPosition = position;
            this.isUrl = isUrl;
        }
    }

    public SearchResultAdapter(Context context, OnSongClickListener listener) {
        this.context = context;
        this.results = new ArrayList<>();
        this.listener = listener;
    }

    public void updateResults(List<SearchResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_toggle, parent, false);
        return new ViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        SearchResult result = results.get(position);
//
//        holder.title.setText(result.title);
//        holder.toggle.setVisibility(View.GONE);
//
//        if (result.isUrl && context != null) {
//            Glide.with(context)
//                    .load((String) result.thumbnail)
//                    .placeholder(R.drawable.thumb_placeholder)
//                    .into(holder.thumbnail);
//        } else {
//            holder.thumbnail.setImageResource((Integer) result.thumbnail);
//        }
//
//        holder.itemView.setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onSongClick(result.originalPosition);
//            }
//        });
//    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResult result = results.get(position);

        holder.title.setText(result.title);
        holder.toggle.setVisibility(View.GONE);

        if (result.thumbnail instanceof Integer) {
            holder.thumbnail.setImageResource((Integer) result.thumbnail);
        } else if (result.thumbnail instanceof String && context != null) {
            Glide.with(context)
                    .load((String) result.thumbnail)
                    .placeholder(R.drawable.thumb_placeholder)
                    .into(holder.thumbnail);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(result.originalPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        View toggle;

        ViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumb);
            title = itemView.findViewById(R.id.title);
            toggle = itemView.findViewById(R.id.toggle);
        }
    }
}

//package com.example.sound;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.bumptech.glide.Glide;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
//
//    private Context context;
//    private List<SearchResult> results;
//    private OnSongClickListener listener;
//
//    public interface OnSongClickListener {
//        void onSongClick(int position);
//    }
//
//    public static class SearchResult {
//        public String title;
//        public Object thumbnail; // Can be Integer (resource) or String (URL)
//        public int originalPosition;
//        public boolean isUrl;
//
//        public SearchResult(String title, Object thumbnail, int position, boolean isUrl) {
//            this.title = title;
//            this.thumbnail = thumbnail;
//            this.originalPosition = position;
//            this.isUrl = isUrl;
//        }
//    }
//
//    public SearchResultAdapter(Context context, OnSongClickListener listener) {
//        this.context = context;
//        this.results = new ArrayList<>();
//        this.listener = listener;
//    }
//
//    public void updateResults(List<SearchResult> newResults) {
//        this.results = newResults;
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_song_toggle, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        SearchResult result = results.get(position);
//
//        holder.title.setText(result.title);
//        holder.toggle.setVisibility(View.GONE); // Hide toggle in search results
//
//        if (result.isUrl) {
//            Glide.with(context)
//                    .load((String) result.thumbnail)
//                    .placeholder(R.drawable.thumb_placeholder)
//                    .into(holder.thumbnail);
//        } else {
//            holder.thumbnail.setImageResource((Integer) result.thumbnail);
//        }
//
//        holder.itemView.setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onSongClick(result.originalPosition);
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return results.size();
//    }
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView thumbnail;
//        TextView title;
//        View toggle;
//
//        ViewHolder(View itemView) {
//            super(itemView);
//            thumbnail = itemView.findViewById(R.id.thumb);
//            title = itemView.findViewById(R.id.title);
//            toggle = itemView.findViewById(R.id.toggle);
//        }
//    }
//}