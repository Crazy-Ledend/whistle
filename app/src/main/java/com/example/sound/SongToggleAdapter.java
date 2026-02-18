package com.example.sound;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongToggleAdapter extends RecyclerView.Adapter<SongToggleAdapter.ViewHolder> {

    private final Context context;
    private final String[] titles;
    private final int[] thumbnailResIds;
    private final String[] thumbnailUrls;
    private String[] songUrls; // ADD THIS
    private final boolean[] enabled;
    private final boolean useUrls;
    private List<String> urlList;


    private List<String> titleList;
    private List<Object> thumbList;
    private List<Boolean> enabledList;


    // Offline constructor (resource IDs)
    public SongToggleAdapter(String[] titles, int[] thumbnails, boolean[] enabled) {
        this.context = null;
        this.titles = titles;
        this.thumbnailResIds = thumbnails;
        this.thumbnailUrls = null;
        this.songUrls = null;
        this.enabled = enabled;
        this.useUrls = false;

        buildListsFromArrays();
    }

    // Online constructor (URLs)
    public SongToggleAdapter(Context context, String[] titles, String[] thumbnailUrls, String[] songUrls, boolean[] enabled) {
        this.context = context;
        this.titles = titles;
        this.thumbnailResIds = null;
        this.thumbnailUrls = thumbnailUrls;
        this.songUrls = songUrls;
        this.enabled = enabled;
        this.useUrls = true;

        buildListsFromArrays();
    }

    // Convert arrays → lists (used for reorder)
    private void buildListsFromArrays() {
        int minSize = titles.length;

        if (useUrls) {
            minSize = Math.min(minSize, thumbnailUrls.length);
        } else {
            minSize = Math.min(minSize, thumbnailResIds.length);
        }

        minSize = Math.min(minSize, enabled.length);

        titleList = new ArrayList<>();
        enabledList = new ArrayList<>();
        thumbList = new ArrayList<>();
        urlList = new ArrayList<>();

        // Use only valid consistent data
        for (int i = 0; i < minSize; i++) {
            titleList.add(titles[i]);
            enabledList.add(enabled[i]);
            thumbList.add(useUrls ? thumbnailUrls[i] : thumbnailResIds[i]);
            if (songUrls != null && i < songUrls.length) { // Also add bounds check
                urlList.add(songUrls[i]);
            }
        }
    }


    // Convert lists → arrays (after reorder)
    private void applyListsBackToArrays() {
        for (int i = 0; i < titleList.size(); i++) {
            titles[i] = titleList.get(i);
            enabled[i] = enabledList.get(i);

            if (urlList != null && i < urlList.size()) { // ✅ Add null check
                songUrls[i] = urlList.get(i);
            }

            if (useUrls) {
                thumbnailUrls[i] = (String) thumbList.get(i);
            } else {
                thumbnailResIds[i] = (Integer) thumbList.get(i);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_toggle, parent, false);
        return new ViewHolder(view);
    }

    // GPT'S FIX
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        holder.title.setText(titleList.get(position));
//        holder.toggle.setChecked(enabledList.get(position));
//
//        if (useUrls) {
//            Glide.with(context)
//                    .load((String) thumbList.get(position))
//                    .placeholder(R.drawable.thumb_placeholder)
//                    .into(holder.thumbnail);
//        } else {
//            holder.thumbnail.setImageResource((Integer) thumbList.get(position));
//        }
//
//        holder.toggle.setOnCheckedChangeListener((button, checked) -> {
//            enabledList.set(position, checked);
//        });
//    }

    // Claude's fix
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(titleList.get(position));

        // ✅ Remove listener BEFORE setting checked state
        holder.toggle.setOnCheckedChangeListener(null);
        holder.toggle.setChecked(enabledList.get(position));

        if (useUrls) {
            Glide.with(context)
                    .load((String) thumbList.get(position))
                    .placeholder(R.drawable.thumb_placeholder)
                    .into(holder.thumbnail);
        } else {
            holder.thumbnail.setImageResource((Integer) thumbList.get(position));
        }

        // ✅ Set listener AFTER checked state is set
        holder.toggle.setOnCheckedChangeListener((button, checked) -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                enabledList.set(currentPos, checked);
                enabled[currentPos] = checked; // Update the array passed to adapter

                // ✅ CRITICAL: Update MainActivity's songEnabled array
                if (context instanceof MainActivity) {
                    ((MainActivity) context).updateSongEnabledState(currentPos, checked);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return titleList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        SwitchCompat toggle;

        ViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumb);
            title = itemView.findViewById(R.id.title);
            toggle = itemView.findViewById(R.id.toggle);
        }
    }

    // DRAG TO REORDER SUPPORT
    public void moveItem(int fromPos, int toPos) {
        if (fromPos == toPos) return;

        // bounds protection
        if (fromPos < 0 || fromPos >= titleList.size()) return;
        if (toPos < 0 || toPos >= titleList.size()) return;

        Collections.swap(titleList, fromPos, toPos);
        Collections.swap(thumbList, fromPos, toPos);
        Collections.swap(enabledList, fromPos, toPos);
        Collections.swap(urlList, fromPos, toPos);

        notifyItemMoved(fromPos, toPos);

        ((MainActivity) context).updatePlayerPlaylist(titleList, thumbList, enabledList, urlList);

        if (orderListener != null) {
            orderListener.onOrderChanged(titleList, thumbList, enabledList, urlList);
        }

        applyListsBackToArrays(); // KEEP this
    }

    // HELPERS
    public interface OnOrderChangedListener {
        void onOrderChanged(List<String> titles, List<Object> thumbnails, List<Boolean> enabled, List<String> songUrls);
    }

    private OnOrderChangedListener orderListener;

    public void setOrderChangedListener(OnOrderChangedListener listener) {
        this.orderListener = listener;
    }

    // Select All button Helpers
    public boolean areAllSelected() {
        for (Boolean enabled : enabledList) {
            if (!enabled) {
                return false;
            }
        }
        return true;
    }

    public boolean areAllUnselected() {
        for (Boolean enabled : enabledList) {
            if (enabled) {
                return false;
            }
        }
        return true;
    }

    public void selectAll() {
        for (int i = 0; i < enabledList.size(); i++) {
            enabledList.set(i, true);
            enabled[i] = true;

            // Update MainActivity's array
            if (context instanceof MainActivity) {
                ((MainActivity) context).updateSongEnabledState(i, true);
            }
        }
        notifyDataSetChanged();
    }

    public void unselectAll() {
        for (int i = 0; i < enabledList.size(); i++) {
            enabledList.set(i, false);
            enabled[i] = false;

            // Update MainActivity's array
            if (context instanceof MainActivity) {
                ((MainActivity) context).updateSongEnabledState(i, false);
            }
        }
        notifyDataSetChanged();
    }
}