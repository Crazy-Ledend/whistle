
package com.example.sound;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "offline_songs")
public class OfflineSong {

    @PrimaryKey
    @NonNull
    public String id; // e.g. "song_0", "song_1"

    public String title;
    public String audioFileName;  // e.g. "song_0.ogg"
    public String thumbFileName;  // e.g. "song_0.png"
    public String audioPath;      // full path to audio file
    public String thumbPath;      // full path to thumb file
    public int sortOrder;         // order in playlist

    public OfflineSong(@NonNull String id, String title,
                       String audioFileName, String thumbFileName,
                       String audioPath, String thumbPath,
                       int sortOrder) {
        this.id = id;
        this.title = title;
        this.audioFileName = audioFileName;
        this.thumbFileName = thumbFileName;
        this.audioPath = audioPath;
        this.thumbPath = thumbPath;
        this.sortOrder = sortOrder;
    }
}