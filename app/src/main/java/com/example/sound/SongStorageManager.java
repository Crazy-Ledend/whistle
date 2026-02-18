
package com.example.sound;

import android.content.Context;
import java.io.File;

public class SongStorageManager {

    private static final String SONGS_FOLDER = "offline_songs";
    private static final String THUMBS_FOLDER = "offline_thumbs";

    private final File songsDir;
    private final File thumbsDir;

    public SongStorageManager(Context context) {
        // Create dedicated folders inside app's internal storage
        songsDir = new File(context.getFilesDir(), SONGS_FOLDER);
        thumbsDir = new File(context.getFilesDir(), THUMBS_FOLDER);

        // Create folders if they don't exist
        if (!songsDir.exists()) songsDir.mkdirs();
        if (!thumbsDir.exists()) thumbsDir.mkdirs();
    }

    public File getSongsDir() { return songsDir; }
    public File getThumbsDir() { return thumbsDir; }

    public File getAudioFile(int index) {
        return new File(songsDir, "song_" + index + ".ogg");
    }

    public File getThumbFile(int index) {
        return new File(thumbsDir, "song_" + index + ".png");
    }

    public boolean isAudioDownloaded(int index) {
        return getAudioFile(index).exists();
    }

    public boolean isThumbDownloaded(int index) {
        return getThumbFile(index).exists();
    }

    public boolean isSongFullyDownloaded(int index) {
        return isAudioDownloaded(index) && isThumbDownloaded(index);
    }

    // Delete a song and its thumbnail
    public void deleteSong(int index) {
        File audio = getAudioFile(index);
        File thumb = getThumbFile(index);
        if (audio.exists()) audio.delete();
        if (thumb.exists()) thumb.delete();
    }
}