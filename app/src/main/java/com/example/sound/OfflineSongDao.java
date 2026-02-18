
package com.example.sound;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;
import com.example.sound.OfflineSong;

@Dao
public interface OfflineSongDao {

    // Get all songs ordered by sortOrder
    @Query("SELECT * FROM offline_songs ORDER BY sortOrder ASC")
    List<OfflineSong> getAllSongs();

    // Get a single song by ID
    @Query("SELECT * FROM offline_songs WHERE id = :id")
    OfflineSong getSongById(String id);

    // Check if song exists
    @Query("SELECT COUNT(*) FROM offline_songs WHERE id = :id")
    int exists(String id);

    // Insert or replace
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OfflineSong song);

    // Delete a song
    @Delete
    void delete(OfflineSong song);

    // Delete by ID
    @Query("DELETE FROM offline_songs WHERE id = :id")
    void deleteById(String id);

    // Get count
    @Query("SELECT COUNT(*) FROM offline_songs")
    int getCount();
}
