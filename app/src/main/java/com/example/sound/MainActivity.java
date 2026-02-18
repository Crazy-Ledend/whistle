package com.example.sound;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
//import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

// Notif controls
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

// Perms
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// Lock Screen
import android.support.v4.media.session.MediaSessionCompat;

// Online Streaming
import java.net.URL;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Search bar
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.AutoCompleteTextView;
import android.view.inputmethod.InputMethodManager;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private MediaPlayer mediaPlayer;
    private long lastGestureTime = 0;

    private boolean[] songEnabled;

//    private int[] songs = {
//            R.raw.song1, R.raw.song2, R.raw.song3,
//            R.raw.song4, R.raw.song5, R.raw.song6,
//            R.raw.song7, R.raw.song8, R.raw.song9,
//            R.raw.song10, R.raw.song11, R.raw.song12,
//            R.raw.song13
//    };

//    private int[] thumbnails = {
//            R.drawable.thumb1, R.drawable.thumb2, R.drawable.thumb3,
//            R.drawable.thumb4, R.drawable.thumb5, R.drawable.thumb6,
//            R.drawable.thumb7, R.drawable.thumb8, R.drawable.thumb9,
//            R.drawable.thumb10, R.drawable.thumb11, R.drawable.thumb12,
//            R.drawable.thumb13
//    };

//    private String[] titles = {
//            "Vaadi Pulla Vaadi 💔", "Senthamizh Penne 🌺", "Perfect 👌🏼",
//            "Fairytale 🪽", "Dhom Dhom 🦋", "Oththa Thamara 🪷",
//            "Film Out 🎬", "Farewell Neverland 🏝️", "Falling Flower 🌸",
//            "Doughnut 🍩", "Change Your Ticket Home ✈️", "XO 💖", "Lovesick Girls 💔"
//    };

    // Offline songs from downloads
    private String[] offlineAudioPaths = null;
    private String[] offlineThumbPaths = null;
//    private int offline_songs_length;

    private String[] onlineUrls = new String[0];

    private String[] onlineTitles = new String[0];
    private String[] onlineThumbs = new String[0];

    private int currentIndex = 0;
    private boolean isLooping = false;

    private ImageView songThumbnail;
    private TextView songTitle, currentTime, totalTime;
    private Slider seekBar;
    private MaterialButton playBtn, pauseBtn, loopBtn, prevBtn, nextBtn;

    private SensorManager sensorManager;
    private float accelCurrent, accelLast;
    private final float shakeThreshold = 10f;

    private static final int NOTIF_PERMISSION_CODE = 1001;

    // Notif actions
    private static final String ACTION_NEXT = "ACTION_NEXT";
    private static final String ACTION_PREV = "ACTION_PREV";
    private static final String ACTION_PAUSE = "ACTION_PAUSE";

    private final Handler handler = new Handler();

    // Lock Screen access
    private MediaSessionCompat mediaSession;

    // Downloads
    private MaterialButton downloadBtn;

    // Audio Focus
    private AudioFocusManager audioFocusManager;

    // Search bar
    private View searchBarContainer;
    private AutoCompleteTextView searchInput;
    private MaterialButton searchBackBtn, searchClearBtn;
    private RecyclerView searchResultsList;
    private LinearLayout searchNoResults;
    private SearchResultAdapter searchResultAdapter;
    private boolean isSearchVisible = false;

    // DB Handlers
    private AppDatabase db;
    private SongStorageManager storageManager;

    // SONG REORDERS
//    private List<String> playTitles = new ArrayList<>();
//    private List<Object> playThumbs = new ArrayList<>();
//    private List<Boolean> playEnabled = new ArrayList<>();
//    private List<String> playUrls = new ArrayList<>();
//    private List<Integer> playRawIds = new ArrayList<>();


    private String BASEURL = "https://raw.githubusercontent.com/Crazy-Ledend/whistle-audio-files/main/";

    private boolean isSongDownloaded(int index) {
        return getDownloadedSongFile(index).exists();
    }

    private File getDownloadedSongFile(int index) {
        return new File(getFilesDir(), "song_" + index + ".ogg");
    }

    private int getDownloadedCount() {
        int count = 0;
        for (int i = 0; i < onlineUrls.length; i++) {
            if (isSongDownloaded(i)) count++;
        }
        return count;
    }

    // Download Song
    // Claude fix
//    private void downloadCurrentSong() {
//        if (!isOnline()) {
//            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        int index = currentIndex;
//        File audioFile = storageManager.getAudioFile(index);
//        File thumbFile = storageManager.getThumbFile(index);
//
//        if (audioFile.exists()) {
//            Toast.makeText(this, "Already downloaded", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show();
//
//        new Thread(() -> {
//            try {
//                // Download audio file
//                downloadFile(onlineUrls[index], audioFile);
//
//                // Download thumbnail
//                String thumbUrl = "https://raw.githubusercontent.com/Crazy-Ledend/whistle-audio-files/main/thumbs/thumb"
//                        + (index + 1) + ".png";
//                downloadFile(thumbUrl, thumbFile);
//
//                // ✅ Save to database
//                OfflineSong song = new OfflineSong(
//                        "song_" + index,
//                        onlineTitles[index],
//                        audioFile.getName(),
//                        thumbFile.getName(),
//                        audioFile.getAbsolutePath(),
//                        thumbFile.getAbsolutePath(),
//                        index
//                );
//                db.offlineSongDao().insert(song);
//
//                runOnUiThread(() ->
//                        Toast.makeText(this, "✓ Downloaded: " + onlineTitles[index], Toast.LENGTH_SHORT).show()
//                );
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                // Clean up partial downloads
//                if (audioFile.exists()) audioFile.delete();
//                if (thumbFile.exists()) thumbFile.delete();
//
//                runOnUiThread(() ->
//                        Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
//                );
//            }
//        }).start();
//    }

    private void downloadCurrentSong() {
        if (!isOnline()) {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
            return;
        }

        int index = currentIndex;
        File audioFile = storageManager.getAudioFile(index);
        File thumbFile = storageManager.getThumbFile(index);

        if (audioFile.exists()) {
            Toast.makeText(this, "Already downloaded", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                downloadFile(onlineUrls[index], audioFile);

                String thumbUrl = "https://raw.githubusercontent.com/Crazy-Ledend/whistle-audio-files/main/thumbs/thumb"
                        + (index + 1) + ".png";
                downloadFile(thumbUrl, thumbFile);

                OfflineSong song = new OfflineSong(
                        "song_" + index,
                        onlineTitles[index],
                        audioFile.getName(),
                        thumbFile.getName(),
                        audioFile.getAbsolutePath(),
                        thumbFile.getAbsolutePath(),
                        index
                );
                db.offlineSongDao().insert(song);

                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "✓ Downloaded: " + onlineTitles[index],
                            Toast.LENGTH_SHORT).show();
                    updateDownloadButton(); // ✅ Refresh button
                });

            } catch (Exception e) {
                e.printStackTrace();
                if (audioFile.exists()) audioFile.delete();
                if (thumbFile.exists()) thumbFile.delete();

                runOnUiThread(() ->
                        Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // Older one
//    private void downloadCurrentSong() {
//        if (!isOnline()) {
//            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        int index = currentIndex;
//        File outFile = getDownloadedSongFile(index);
//
//        if (outFile.exists()) {
//            Toast.makeText(this, "Already downloaded", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show();
//
//        new Thread(() -> {
//            try {
//                java.net.URL url = new java.net.URL(onlineUrls[index]);
//                java.net.HttpURLConnection conn =
//                        (java.net.HttpURLConnection) url.openConnection();
//
//                conn.connect();
//
//                java.io.InputStream in = conn.getInputStream();
//                java.io.FileOutputStream out =
//                        new java.io.FileOutputStream(outFile);
//
//                byte[] buffer = new byte[4096];
//                int len;
//
//                while ((len = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, len);
//                }
//
//                out.close();
//                in.close();
//                conn.disconnect();
//
//                String thumbUrl =
//                        "https://raw.githubusercontent.com/Crazy-Ledend/whistle-audio-files/main/thumbs/thumb"
//                                + (index + 1) + ".png";
//
//                downloadFile(
//                        thumbUrl,
//                        new File(getFilesDir(), "song_" + index + ".png")
//                );
//
//                runOnUiThread(() ->
//                        Toast.makeText(this, "Downloaded", Toast.LENGTH_SHORT).show()
//                );
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() ->
//                        Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
//                );
//            }
//        }).start();
//    }

    private void downloadFile(String urlStr, File outFile) throws Exception {
        java.net.URL url = new java.net.URL(urlStr);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.connect();

        java.io.InputStream in = conn.getInputStream();
        java.io.FileOutputStream out = new java.io.FileOutputStream(outFile);

        byte[] buffer = new byte[4096];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

//    private boolean isRawSong(int index) {
//        return index < songs.length;
//    }


//    private void playSongByIndex(int index) {
//        if (mediaPlayer != null) mediaPlayer.release();
//
//        new Thread(() -> {
//            OfflineSong dbSong = db.offlineSongDao().getSongById("song_" + index);
//
//            runOnUiThread(() -> {
//                try {
//                    if (dbSong != null && new File(dbSong.audioPath).exists()) {
//                        // Play from downloaded folder
//                        mediaPlayer = new MediaPlayer();
//                        mediaPlayer.setDataSource(dbSong.audioPath);
//                        mediaPlayer.prepare();
//                        preparePlayer(mediaPlayer);
//                    } else if (index < songs.length) {
//                        // Fall back to raw resource
//                        mediaPlayer = MediaPlayer.create(this, songs[index]);
//                        preparePlayer(mediaPlayer);
//                    } else {
//                        Toast.makeText(this, "Song not found", Toast.LENGTH_SHORT).show();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }).start();
//    }

    private void playFromPath(String path) {
        if (path == null || !new File(path).exists()) {
            Toast.makeText(this, "Song file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaPlayer != null) mediaPlayer.release();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            preparePlayer(mediaPlayer);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void playOnlineSong(String url) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            // RESET UI BEFORE ASYNC PREPARE
            resetSeekBarUI();

            mediaPlayer.setDataSource(url);

            mediaPlayer.setOnPreparedListener(mp -> {
                preparePlayer(mp);   // pass mp explicitly
            });

//            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
//                Toast.makeText(this, "Streaming error", Toast.LENGTH_SHORT).show();
//                return true;
//            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Streaming failed", Toast.LENGTH_SHORT).show();
        }
    }


    private void preparePlayer(MediaPlayer mp) {
        int duration = mp.getDuration();

        if (duration > 0) {
            seekBar.setValueTo(duration);
            totalTime.setText(formatTime(duration));
        } else {
            seekBar.setValueTo(1f);
            totalTime.setText("--:--");
        }

        mp.setOnCompletionListener(player -> {
            if (!isLooping) {
                nextSong();
            }
        });

        mp.setLooping(isLooping);

        // ✅ Request audio focus before playing
        if (audioFocusManager.requestAudioFocus()) {
            mp.start();
            showNotification();
            playBtn.setVisibility(View.GONE);
            pauseBtn.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Could not get audio focus", Toast.LENGTH_SHORT).show();
        }
    }

//    private void preparePlayer(MediaPlayer mp) {
//        int duration = mp.getDuration();
//
//        if (duration > 0) {
//            seekBar.setValueTo(duration);
//            totalTime.setText(formatTime(duration));
//        } else {
//            seekBar.setValueTo(1f);
//            totalTime.setText("--:--");
//        }
//
//        // ✅ FIX: Only call nextSong() if NOT looping
//        mp.setOnCompletionListener(player -> {
//            if (!isLooping) {
//                nextSong();
//            }
//            // If looping, MediaPlayer handles it automatically - do nothing
//        });
//
//        // ✅ Apply loop state to MediaPlayer
//        mp.setLooping(isLooping);
//
//        mp.start();
//        showNotification();
//
//        playBtn.setVisibility(View.GONE);
//        pauseBtn.setVisibility(View.VISIBLE);
//    }

    private void resetSeekBarUI() {
        seekBar.setValueFrom(0f);
        seekBar.setValueTo(1f);
        seekBar.setValue(0f);

        currentTime.setText("0:00");
        totalTime.setText("--:--");
    }

    private void startSeekBarUpdate() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int pos = mediaPlayer.getCurrentPosition();
                    int max = (int) seekBar.getValueTo();
                    if (pos >= 0 && pos <= max) {
                        seekBar.setValue(pos);
                        currentTime.setText(formatTime(pos));
                    }
                }
                handler.postDelayed(this, 500);
            }
        }, 0);
    }

    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private String formatTimeSeekBar(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format(" %02d:%02d ", minutes, seconds);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        accelLast = accelCurrent;
        accelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = accelCurrent - accelLast;

        long now = System.currentTimeMillis();

        // prevent spam gestures
        if (now - lastGestureTime < 1200) return;

        // SHAKE → NEXT SONG
        if (delta > shakeThreshold) {
            lastGestureTime = now;
            nextSong();
            return;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Stop and release media player
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // ✅ Cancel notification
        cancelNotification();

        // Clean up audio focus manager
        if (audioFocusManager != null) {
            audioFocusManager.release();
        }

        // Unregister broadcast receiver
        try {
            unregisterReceiver(notifReceiver);
        } catch (Exception ignored) {}

        // Deactivate media session
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }

        super.onDestroy();
    }

//    @Override
//    protected void onDestroy() {
//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//
//        // ✅ Clean up audio focus manager
//        if (audioFocusManager != null) {
//            audioFocusManager.release();
//        }
//        try {
//            unregisterReceiver(notifReceiver);
//        } catch (Exception ignored) {
//        }
//
//        super.onDestroy();
//    }

    @Override
    protected void onStop() {
        super.onStop();

        // Optional: Cancel notification when app goes to background
        // Remove this if you want notification to persist in background
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            cancelNotification();
        }
    }

    // Notif controls
    private void createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    "music",
                    "Music Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }
    }

    private PendingIntent nextIntent() {
        Intent intent = new Intent(ACTION_NEXT);
        intent.setPackage(getPackageName()); // target your app explicitly
        return PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent prevIntent() {
        Intent intent = new Intent(ACTION_PREV);
        intent.setPackage(getPackageName());
        return PendingIntent.getBroadcast(
                this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent pauseIntent() {
        Intent intent = new Intent(ACTION_PAUSE);
        intent.setPackage(getPackageName());
        return PendingIntent.getBroadcast(
                this, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }


    private void togglePlayPause() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            pauseBtn.setVisibility(View.GONE);
            playBtn.setVisibility(View.VISIBLE);
        } else {
            mediaPlayer.start();
            playBtn.setVisibility(View.GONE);
            pauseBtn.setVisibility(View.VISIBLE);
        }

        showNotification(); // refresh icon
    }

    private final BroadcastReceiver notifReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent == null || intent.getAction() == null) return;

                    Toast.makeText(context,
                            "ACTION: " + intent.getAction(),
                            Toast.LENGTH_SHORT).show();

                    switch (intent.getAction()) {
                        case ACTION_NEXT:
                            nextSong();
                            break;
                        case ACTION_PREV:
                            previousSong();
                            break;
                        case ACTION_PAUSE:
                            togglePlayPause();
                            break;
                    }
                }
            };

    // Perms
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIF_PERMISSION_CODE
                );
            }
        }
    }

    // Lock Screen access

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "MusicPlayerSession");
        mediaSession.setActive(true);
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    // FIXED: JSON is an array at root level, not an object with "songs" property

    private void loadOnlineSongsFromJson() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Loading online songs...", Toast.LENGTH_SHORT).show();
        });

        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(
                        "https://raw.githubusercontent.com/Crazy-Ledend/whistle-audio-files/main/songs.json"
                );

                Log.d("MusicPlayer", "Fetching: " + url.toString());

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("User-Agent", "Android Music Player");
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d("MusicPlayer", "Response Code: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP Error: " + responseCode);
                }

                reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                String jsonString = json.toString();
                Log.d("MusicPlayer", "JSON Length: " + jsonString.length());

                // FIX: Parse as JSONArray directly (not JSONObject)
                JSONArray arr = new JSONArray(jsonString);
                int n = arr.length();

                Log.d("MusicPlayer", "Found " + n + " songs");

                if (n == 0) {
                    throw new Exception("Songs array is empty");
                }

                String[] t = new String[n];
                String[] u = new String[n];
                String[] th = new String[n];

                for (int i = 0; i < n; i++) {
                    JSONObject o = arr.getJSONObject(i);

                    t[i] = o.getString("title");
                    u[i] = "https://raw.githubusercontent.com/Crazy-Ledend/whistle-audio-files/main/songs/"
                            + o.getString("audio");
                    th[i] = "https://raw.githubusercontent.com/Crazy-Ledend/whistle-audio-files/main/thumbs/"
                            + o.getString("thumb");

                    Log.d("MusicPlayer", "Song " + i + ": " + t[i]);
                }

                runOnUiThread(() -> {
                    onlineTitles = t;
                    onlineUrls = u;
                    onlineThumbs = th;

                    Log.d("MusicPlayer", "Online songs loaded: " + onlineUrls.length);

                    initSongEnabledArray();
                    initPlayer();

                    Toast.makeText(this,
                            "✓ Loaded " + n + " online songs",
                            Toast.LENGTH_LONG).show();
                });

            } catch (java.net.SocketTimeoutException e) {
                Log.e("MusicPlayer", "Timeout", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Connection timeout", Toast.LENGTH_LONG).show();
                    fallbackToOffline();
                });

            } catch (java.net.UnknownHostException e) {
                Log.e("MusicPlayer", "No internet", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "No internet. Using offline mode.", Toast.LENGTH_LONG).show();
                    fallbackToOffline();
                });

            } catch (java.io.IOException e) {
                Log.e("MusicPlayer", "Network error", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    fallbackToOffline();
                });

            } catch (org.json.JSONException e) {
                Log.e("MusicPlayer", "JSON error", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    fallbackToOffline();
                });

            } catch (Exception e) {
                Log.e("MusicPlayer", "Error", e);
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    fallbackToOffline();
                });

            } finally {
                try {
                    if (reader != null) reader.close();
                    if (conn != null) conn.disconnect();
                } catch (Exception e) {
                    Log.e("MusicPlayer", "Cleanup error", e);
                }
            }
        }).start();
    }

//    private void fallbackToOffline() {
//        songEnabled = new boolean[songs.length];
//        for (int i = 0; i < songEnabled.length; i++) {
//            songEnabled[i] = true;
//        }
//        initPlayer();
//    }

    private void fallbackToOffline() {
        new Thread(() -> {
            List<OfflineSong> dbSongs = db.offlineSongDao().getAllSongs();

            runOnUiThread(() -> {
                if (dbSongs != null && !dbSongs.isEmpty()) {
                    int n = dbSongs.size();

                    onlineTitles = new String[n];
                    onlineThumbs = new String[n]; // reuse for consistency
                    onlineUrls = new String[n];   // reuse for consistency
                    offlineAudioPaths = new String[n];
                    offlineThumbPaths = new String[n];

                    for (int i = 0; i < n; i++) {
                        OfflineSong s = dbSongs.get(i);
                        onlineTitles[i] = s.title;
                        offlineAudioPaths[i] = s.audioPath;
                        offlineThumbPaths[i] = s.thumbPath;
                    }

                    songEnabled = new boolean[n];
                    Arrays.fill(songEnabled, true);

                    Toast.makeText(this,
                            "Offline: " + n + " downloaded songs",
                            Toast.LENGTH_SHORT).show();

                    initPlayer();

                } else {
                    // ✅ No downloaded songs at all
                    Toast.makeText(this,
                            "No internet and no downloaded songs available",
                            Toast.LENGTH_LONG).show();

                    // Disable all playback controls
                    playBtn.setEnabled(false);
                    pauseBtn.setEnabled(false);
                    nextBtn.setEnabled(false);
                    prevBtn.setEnabled(false);
                    songTitle.setText("No songs available");
                }
            });
        }).start();
    }

    private void initSongEnabledArray() {
        int size = getQueueSize();
        if (songEnabled == null || songEnabled.length != size) {
            boolean[] newArray = new boolean[size];
            if (songEnabled != null) {
                System.arraycopy(songEnabled, 0, newArray, 0,
                        Math.min(songEnabled.length, size));
            }
            for (int i = songEnabled == null ? 0 : songEnabled.length; i < size; i++) {
                newArray[i] = true;
            }
            songEnabled = newArray;
        }
    }

//    private int getQueueSize() {
//        if (isOnline() && onlineUrls != null && onlineUrls.length > 0) {
//            return onlineUrls.length;
//        }
//        return songs.length;
//    }
    private int getQueueSize() {
        if (onlineUrls != null && onlineUrls.length > 0) {
            return onlineUrls.length;
        }
        if (offlineAudioPaths != null && offlineAudioPaths.length > 0) {
            return offlineAudioPaths.length;
        }
        return 0;
    }

//    private void initPlayer() {
//        int queueSize = getQueueSize();
//
//        if (queueSize == 0) {
//            Toast.makeText(this, "No songs available", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        initSongEnabledArray();
//
//        if (currentIndex >= queueSize) {
//            currentIndex = 0;
//        }
//
//        if (!songEnabled[currentIndex]) {
//            nextSong();
//            return;
//        }
//
//        if (isOnline() && onlineUrls != null && onlineUrls.length > 0) {
//            playOnlineSong(onlineUrls[currentIndex]);
//            loadThumbnailByIndex(currentIndex);
//        } else {
//            playSongByIndex(currentIndex);
//            loadThumbnailByIndex(currentIndex);
//        }
//        loadTitleByIndex(currentIndex);
//    }

    private void initPlayer() {
        int queueSize = getQueueSize();

        if (queueSize == 0) {
            Toast.makeText(this, "No songs available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-enable controls in case they were disabled
        playBtn.setEnabled(true);
        nextBtn.setEnabled(true);
        prevBtn.setEnabled(true);

        initSongEnabledArray();

        if (currentIndex >= queueSize) {
            currentIndex = 0;
        }

        if (!songEnabled[currentIndex]) {
            nextSong();
            return;
        }

        // ✅ Online mode
        if (isOnline() && onlineUrls != null && onlineUrls.length > 0
                && onlineUrls[currentIndex] != null) {
            playOnlineSong(onlineUrls[currentIndex]);
        }
        // ✅ Offline mode (from DB)
        else if (offlineAudioPaths != null && currentIndex < offlineAudioPaths.length) {
            playFromPath(offlineAudioPaths[currentIndex]);
        } else {
            Toast.makeText(this, "Song not available", Toast.LENGTH_SHORT).show();
            return;
        }

        loadThumbnailByIndex(currentIndex);
        loadTitleByIndex(currentIndex);
    }

//    private void loadTitleByIndex(int index) {
//        if (isOnline() && onlineTitles != null && index < onlineTitles.length) {
//            songTitle.setText(onlineTitles[index]);
//        } else if (!isOnline() && index < titles.length) {
//            songTitle.setText(titles[index]);
//        } else {
//            if (index < titles.length) {
//                songTitle.setText(titles[index]);
//            }
//            else {
//                songTitle.setText("Unknown Song");
//            }
//        }
//    }

    private void loadTitleByIndex(int index) {
        if (onlineTitles != null && index < onlineTitles.length
                && onlineTitles[index] != null && !onlineTitles[index].isEmpty()) {
            songTitle.setText(onlineTitles[index]);
        } else {
            // ✅ Try DB directly as fallback
            new Thread(() -> {
                OfflineSong dbSong = db.offlineSongDao().getSongById("song_" + index);
                runOnUiThread(() -> {
                    if (dbSong != null && dbSong.title != null) {
                        songTitle.setText(dbSong.title);
                    } else {
                        songTitle.setText("Unknown Song");
                    }
                });
            }).start();
        }

        updateDownloadButton();
    }

//    private String getTitleForIndex(int index) {
//        if (isOnline() && onlineTitles != null && index < onlineTitles.length) {
//            return onlineTitles[index];
//        } else if (!isOnline() && index < titles.length) {
//            return titles[index];
//        } else if (index < titles.length) {
//            return titles[index];
//        }
//
//        return "Unknown Song";
//    }

    private String getTitleForIndex(int index) {
        if (onlineTitles != null && index < onlineTitles.length) {
            String t = onlineTitles[index];
            if (t != null && !t.isEmpty()) return t;
        }

        // ✅ Fallback: check DB directly
        if (db != null) {
            OfflineSong dbSong = db.offlineSongDao().getSongById("song_" + index);
            if (dbSong != null && dbSong.title != null) return dbSong.title;
        }

        return "Unknown Song";
    }

    // Search Bar

    private void showSearchBar() {
        if (isSearchVisible) return;

        isSearchVisible = true;
        searchBarContainer.setVisibility(View.VISIBLE);

        // Animate slide in from right
        searchBarContainer.animate()
                .translationX(0)
                .setDuration(300)
                .start();

        // Focus search input and show keyboard
        searchInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
        }

        // Setup autocomplete
        String[] songList = onlineTitles;
//        String[] songList = isOnline() && onlineTitles != null && onlineTitles.length > 0
//                ? onlineTitles : titles;

        ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                songList
        );
        searchInput.setAdapter(autocompleteAdapter);

        // Disable default dropdowns
        searchInput.setOnClickListener(v -> {
            // Don't show dropdown on click
        });

        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            // Don't show dropdown on focus
        });
    }

    private void hideSearchBar() {
        if (!isSearchVisible) return;

        isSearchVisible = false;

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }

        // Animate slide out to right
        searchBarContainer.animate()
                .translationX(searchBarContainer.getWidth())
                .setDuration(300)
                .withEndAction(() -> {
                    searchBarContainer.setVisibility(View.GONE);
                    searchInput.setText("");
                    searchResultsList.setVisibility(View.GONE);
                    searchNoResults.setVisibility(View.GONE);
                })
                .start();
    }

    private void performSearch(String query) {
        String[] songList;
        Object[] thumbList;
        boolean isOnlineMode;

        if (isOnline() && onlineTitles != null && onlineTitles.length > 0) {
            songList = onlineTitles;
            thumbList = onlineThumbs;
            isOnlineMode = true;
        } else {
            songList = onlineTitles;
//            songList = titles;
//            thumbList = new Object[thumbnails.length];
//            for (int i = 0; i < thumbnails.length; i++) {
//                thumbList[i] = thumbnails[i];
            thumbList = new Object[offlineThumbPaths.length];
            for (int i = 0; i < offlineThumbPaths.length; i++) {
                thumbList[i] = offlineThumbPaths[i];
            }
            isOnlineMode = false;
        }

        List<SearchResultAdapter.SearchResult> filteredResults = new ArrayList<>();

        for (int i = 0; i < songList.length; i++) {
            if (songList[i].toLowerCase().contains(query)) {
                filteredResults.add(new SearchResultAdapter.SearchResult(
                        songList[i],
                        thumbList[i],
                        i,
                        isOnlineMode
                ));
            }
        }

        if (filteredResults.isEmpty()) {
            searchResultsList.setVisibility(View.GONE);
            searchNoResults.setVisibility(View.VISIBLE);
        } else {
            searchResultsList.setVisibility(View.VISIBLE);
            searchNoResults.setVisibility(View.GONE);
            searchResultAdapter.updateResults(filteredResults);
        }
    }

//    private void showSearchDialog() {
//        BottomSheetDialog dialog = new BottomSheetDialog(this);
//        View view = getLayoutInflater().inflate(R.layout.search_dialog, null);
//
//        AutoCompleteTextView searchBox = view.findViewById(R.id.searchBox);
//        RecyclerView searchResults = view.findViewById(R.id.searchResults);
//        TextView noResultsText = view.findViewById(R.id.noResultsText);
//
//        searchResults.setLayoutManager(new LinearLayoutManager(this));
//
//        // Get song list based on online/offline mode
//        String[] songList;
//        Object[] thumbList;
//        boolean isOnlineMode;
//
//        if (isOnline() && onlineTitles != null && onlineTitles.length > 0) {
//            songList = onlineTitles;
//            thumbList = onlineThumbs;
//            isOnlineMode = true;
//        } else {
//            songList = titles;
//            thumbList = new Object[thumbnails.length];
//            for (int i = 0; i < thumbnails.length; i++) {
//                thumbList[i] = thumbnails[i];
//            }
//            isOnlineMode = false;
//        }
//
//        // Setup autocomplete adapter
//        android.widget.ArrayAdapter<String> autocompleteAdapter =
//                new android.widget.ArrayAdapter<>(
//                        this,
//                        android.R.layout.simple_dropdown_item_1line,
//                        songList
//                );
//        searchBox.setAdapter(autocompleteAdapter);
//
//        // Setup search results adapter
//        SearchResultAdapter resultAdapter = new SearchResultAdapter(this, position -> {
//            // When a song is clicked, play it
//            currentIndex = position;
//            initPlayer();
//            if (mediaPlayer != null) {
//                mediaPlayer.start();
//                showNotification();
//                playBtn.setVisibility(View.GONE);
//                pauseBtn.setVisibility(View.VISIBLE);
//            }
//            dialog.dismiss();
//            Toast.makeText(this, "Playing: " + getTitleForIndex(position), Toast.LENGTH_SHORT).show();
//        });
//        searchResults.setAdapter(resultAdapter);
//
//        // Search functionality
//        searchBox.addTextChangedListener(new android.text.TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String query = s.toString().toLowerCase().trim();
//
//                if (query.isEmpty()) {
//                    searchResults.setVisibility(View.GONE);
//                    noResultsText.setVisibility(View.GONE);
//                    return;
//                }
//
//                // Filter songs
//                List<SearchResultAdapter.SearchResult> filteredResults = new ArrayList<>();
//
//                for (int i = 0; i < songList.length; i++) {
//                    if (songList[i].toLowerCase().contains(query)) {
//                        filteredResults.add(new SearchResultAdapter.SearchResult(
//                                songList[i],
//                                thumbList[i],
//                                i,
//                                isOnlineMode
//                        ));
//                    }
//                }
//
//                if (filteredResults.isEmpty()) {
//                    searchResults.setVisibility(View.GONE);
//                    noResultsText.setVisibility(View.VISIBLE);
//                } else {
//                    searchResults.setVisibility(View.VISIBLE);
//                    noResultsText.setVisibility(View.GONE);
//                    resultAdapter.updateResults(filteredResults);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(android.text.Editable s) {}
//        });
//
//        // Show all songs initially
//        searchBox.setOnFocusChangeListener((v1, hasFocus) -> {
//            if (hasFocus && searchBox.getText().toString().isEmpty()) {
//                searchBox.showDropDown();
//            }
//        });
//
//        dialog.setContentView(view);
//        dialog.show();
//
//        // Auto-focus search box and show keyboard
//        searchBox.requestFocus();
//        android.view.inputmethod.InputMethodManager imm =
//                (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (imm != null) {
//            imm.showSoftInput(searchBox, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestNotificationPermission();

        // UI initialization
        songThumbnail = findViewById(R.id.songThumbnail);
        songTitle = findViewById(R.id.songTitle);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        seekBar = findViewById(R.id.seekBar);
        playBtn = findViewById(R.id.playBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        loopBtn = findViewById(R.id.loopBtn);
        prevBtn = findViewById(R.id.prevBtn);
        downloadBtn = findViewById(R.id.downloadBtn);
        nextBtn = findViewById(R.id.nextBtn);

        searchBarContainer = findViewById(R.id.searchBarContainer);
        searchInput = findViewById(R.id.searchInput);
        searchBackBtn = findViewById(R.id.searchBackBtn);
        searchClearBtn = findViewById(R.id.searchClearBtn);
        searchResultsList = findViewById(R.id.searchResultsList);
        searchNoResults = findViewById(R.id.searchNoResults);

        searchResultsList.setLayoutManager(new LinearLayoutManager(this));

        // Search Bar --------------------------------------------------------------
        // Setup search button click
        findViewById(R.id.searchBtn).setOnClickListener(v -> showSearchBar());

        // Setup back button (close search)
        searchBackBtn.setOnClickListener(v -> hideSearchBar());

        // Setup clear button
        searchClearBtn.setOnClickListener(v -> {
            searchInput.setText("");
            searchClearBtn.setVisibility(View.GONE);
        });

        // Setup search adapter
        searchResultAdapter = new SearchResultAdapter(this, position -> {
            currentIndex = position;
            initPlayer();
            if (mediaPlayer != null) {
                mediaPlayer.start();
                showNotification();
                playBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);
            }
            hideSearchBar();
            Toast.makeText(this, "Playing: " + getTitleForIndex(position), Toast.LENGTH_SHORT).show();
        });
        searchResultsList.setAdapter(searchResultAdapter);

        // Setup search text watcher
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();

                // Show/hide clear button
                searchClearBtn.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);

                if (query.isEmpty()) {
                    searchResultsList.setVisibility(View.GONE);
                    searchNoResults.setVisibility(View.GONE);
                    return;
                }

                performSearch(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSearchVisible) {
                    // If search is visible, close it
                    hideSearchBar();
                } else {
                    // Otherwise, use default back behavior
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Storage Manager Init
        storageManager = new SongStorageManager(this);

        // DB Init
        db = AppDatabase.getInstance(this);

        // ---------------------------------------------------------------------------------------------------

        initMediaSession();

        // Audio Focus ---------------------------------------------------------------------------------------
        audioFocusManager = new AudioFocusManager(this, new AudioFocusManager.AudioFocusListener() {
            @Override
            public void onAudioFocusGain() {
                // Resume playback and restore volume
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    mediaPlayer.start();
                    playBtn.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.VISIBLE);
                    showNotification();
                }
            }

            @Override
            public void onAudioFocusLoss() {
                // Permanent loss - pause
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    pauseBtn.setVisibility(View.GONE);
                    playBtn.setVisibility(View.VISIBLE);
                    showNotification();
                }
            }

            @Override
            public void onAudioFocusLossTransient() {
                // Temporary loss (phone call, camera) - pause
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    pauseBtn.setVisibility(View.GONE);
                    playBtn.setVisibility(View.VISIBLE);
                    showNotification();
                }
            }

            @Override
            public void onAudioFocusLossCanDuck() {
                // Lower volume for notifications
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.3f, 0.3f);
                }
            }
        });

        // Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;

        findViewById(R.id.songListBtn).setOnClickListener(v -> showSongSelector());
//        findViewById(R.id.searchBtn).setOnClickListener(v -> showSearchDialog());

        // CRITICAL FIX: Load online songs first, then init player
        if (isOnline()) {
            loadOnlineSongsFromJson(); // This will call initPlayer() when done
        } else {
            fallbackToOffline();
//            songEnabled = new boolean[offlineAudioPaths.length];
//            for (int i = 0; i < songEnabled.length; i++) {
//                songEnabled[i] = true;
//            }
//            initPlayer();
        }

        startSeekBarUpdate();

        // Button listeners...
//        playBtn.setOnClickListener(v -> {
//            if (mediaPlayer == null) {
//                Toast.makeText(this, "Song not ready yet", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            mediaPlayer.start();
//            showNotification();
//            playBtn.setVisibility(View.GONE);
//            pauseBtn.setVisibility(View.VISIBLE);
//        });


        playBtn.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                Toast.makeText(this, "Song not ready yet", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Request audio focus before playing
            if (audioFocusManager.requestAudioFocus()) {
                mediaPlayer.start();
                showNotification();
                playBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Could not get audio focus", Toast.LENGTH_SHORT).show();
            }
        });
        pauseBtn.setOnClickListener(v -> {
            mediaPlayer.pause();
            pauseBtn.setVisibility(View.GONE);
            playBtn.setVisibility(View.VISIBLE);
        });

        loopBtn.setOnClickListener(v -> {
            isLooping = !isLooping;

            // ✅ Apply loop state to currently playing song
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(isLooping);
            }

            loopBtn.setIconTint(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(
                                    this,
                                    isLooping ? R.color.loop_on : R.color.loop_off
                            )
                    )
            );

            Toast.makeText(this, "Loop " + (isLooping ? "ON" : "OFF"),
                    Toast.LENGTH_SHORT).show();
        });

        prevBtn.setOnClickListener(v -> previousSong());
        nextBtn.setOnClickListener(v -> nextSong());
        downloadBtn.setOnClickListener(v -> downloadCurrentSong());

        createChannel();

        seekBar.setLabelFormatter(value -> formatTimeSeekBar((int) value));
        seekBar.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && mediaPlayer != null) {
                mediaPlayer.seekTo((int) value);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PREV);
        filter.addAction(ACTION_PAUSE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notifReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(notifReceiver, filter);
        }
    }

//    private void loadThumbnailByIndex(int index) {
//        if (isOnline()) {
//            if (index < onlineThumbs.length) {
//                Glide.with(this)
//                        .load(onlineThumbs[index])
//                        .placeholder(R.drawable.thumb_placeholder)
//                        .error(R.drawable.thumb_placeholder)
//                        .into(songThumbnail);
//            } else if (index < songs.length) {
//                songThumbnail.setImageResource(thumbnails[index]);
//            }
//
//        } else {
//            // ✅ Check DB for downloaded thumbnail
//            new Thread(() -> {
//                OfflineSong dbSong = db.offlineSongDao().getSongById("song_" + index);
//
//                runOnUiThread(() -> {
//                    if (dbSong != null && new File(dbSong.thumbPath).exists()) {
//                        Glide.with(this)
//                                .load(new File(dbSong.thumbPath))
//                                .placeholder(R.drawable.thumb_placeholder)
//                                .into(songThumbnail);
//                    } else if (index < thumbnails.length) {
//                        songThumbnail.setImageResource(thumbnails[index]);
//                    } else {
//                        songThumbnail.setImageResource(R.drawable.thumb_placeholder);
//                    }
//                });
//            }).start();
// //            if (index < songs.length) {
// //                // Raw resource
// //                songThumbnail.setImageResource(thumbnails[index]);
// //            } else {
// //                // Downloaded song
// //                File imgFile = new File(getFilesDir(),
// //                        "song_" + (index - songs.length) + ".png");
// //                if (imgFile.exists()) {
// //                    Glide.with(this)
// //                            .load(imgFile)
// //                            .placeholder(R.drawable.thumb_placeholder)
// //                            .into(songThumbnail);
// //                } else {
// //                    songThumbnail.setImageResource(R.drawable.thumb_placeholder);
// //                }
// //            }
//        }
//    }

    private void loadThumbnailByIndex(int index) {
        // ✅ Online mode - load from URL
        if (isOnline() && onlineThumbs != null && index < onlineThumbs.length
                && onlineThumbs[index] != null) {
            Glide.with(this)
                    .load(onlineThumbs[index])
                    .placeholder(R.drawable.thumb_placeholder)
                    .error(R.drawable.thumb_placeholder)
                    .into(songThumbnail);
            return;
        }

        // ✅ Offline mode - load from DB path
        if (offlineThumbPaths != null && index < offlineThumbPaths.length) {
            File thumbFile = new File(offlineThumbPaths[index]);
            if (thumbFile.exists()) {
                Glide.with(this)
                        .load(thumbFile)
                        .placeholder(R.drawable.thumb_placeholder)
                        .into(songThumbnail);
                return;
            }
        }

        // ✅ Final fallback
        songThumbnail.setImageResource(R.drawable.thumb_placeholder);
    }

    private void nextSong() {
        int queueSize = getQueueSize();

        if (queueSize == 0) {
            Toast.makeText(this, "No songs available", Toast.LENGTH_SHORT).show();
            return;
        }

        initSongEnabledArray();

        int startIndex = currentIndex;

        do {
            currentIndex = (currentIndex + 1) % queueSize;
        } while (!songEnabled[currentIndex] && currentIndex != startIndex);

        if (!songEnabled[currentIndex]) {
            Toast.makeText(this, "No songs selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = getTitleForIndex(currentIndex);
        Toast.makeText(this, "Now Playing: " + title, Toast.LENGTH_LONG).show();

        // ✅ Save loop state before reinitializing
        boolean wasLooping = isLooping;

        initPlayer();

        // ✅ Restore loop state
        isLooping = wasLooping;

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(isLooping); // ✅ Apply loop state
            mediaPlayer.start();
            showNotification();
            playBtn.setVisibility(View.GONE);
            pauseBtn.setVisibility(View.VISIBLE);
        }
    }

    // FIX: Update previousSong to use getTitleForIndex()
    private void previousSong() {
        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 3000) {
            mediaPlayer.seekTo(0);
            return;
        }

        int queueSize = getQueueSize();

        if (queueSize == 0) {
            Toast.makeText(this, "No songs available", Toast.LENGTH_SHORT).show();
            return;
        }

        initSongEnabledArray();

        int startIndex = currentIndex;

        do {
            currentIndex--;
            if (currentIndex < 0) currentIndex = queueSize - 1;
        } while (!songEnabled[currentIndex] && currentIndex != startIndex);

        if (!songEnabled[currentIndex]) {
            Toast.makeText(this, "No songs selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = getTitleForIndex(currentIndex);
        Toast.makeText(this, "Now Playing: " + title, Toast.LENGTH_LONG).show();

        // ✅ Save loop state before reinitializing
        boolean wasLooping = isLooping;

        initPlayer();

        // ✅ Restore loop state
        isLooping = wasLooping;

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(isLooping); // ✅ Apply loop state
            mediaPlayer.start();
            showNotification();
            playBtn.setVisibility(View.GONE);
            pauseBtn.setVisibility(View.VISIBLE);
        }
    }

    // OLD
//    private void showSongSelector() {
//        BottomSheetDialog dialog = new BottomSheetDialog(this);
//        View view = getLayoutInflater()
//                .inflate(R.layout.dialog_song_selector, null);
//
//        RecyclerView rv = view.findViewById(R.id.recycler);
//        rv.setLayoutManager(new LinearLayoutManager(this));
//
//        SongToggleAdapter toggleAdapter;
//
//        if (isOnline() && onlineTitles != null && onlineTitles.length > 0) {
//            // ✅ FIX: Use ONLINE arrays when online
//            int min = Math.min(onlineTitles.length, onlineThumbs.length);
//            min = Math.min(min, onlineUrls.length);
//            min = Math.min(min, songEnabled.length);
//
//            String[] tempTitles = Arrays.copyOf(onlineTitles, min);
//            String[] tempThumbs = Arrays.copyOf(onlineThumbs, min);
//            String[] tempUrls = Arrays.copyOf(onlineUrls, min);
//            boolean[] tempEnabled = Arrays.copyOf(songEnabled, min);
//
//            toggleAdapter = new SongToggleAdapter(
//                    this,           // context for Glide
//                    tempTitles,     // song titles
//                    tempThumbs,     // thumbnail URLs
//                    tempUrls,       // ✅ FIX: song URLs (not thumbnails!)
//                    tempEnabled     // enabled state
//            );
//        } else {
//            // ✅ FIX: Use OFFLINE arrays when offline
//            int min = Math.min(titles.length, thumbnails.length);
//            min = Math.min(min, songEnabled.length);
//
//            String[] tempTitles = Arrays.copyOf(titles, min);
//            int[] tempThumbs = Arrays.copyOf(thumbnails, min);
//            boolean[] tempEnabled = Arrays.copyOf(songEnabled, min);
//
//            toggleAdapter = new SongToggleAdapter(
//                    tempTitles,     // song titles
//                    tempThumbs,     // thumbnail resource IDs
//                    tempEnabled     // enabled state
//            );
//        }


    // NEW
//    private void showSongSelector() {
//        BottomSheetDialog dialog = new BottomSheetDialog(this);
//        View view = getLayoutInflater()
//                .inflate(R.layout.dialog_song_selector, null);
//
//        RecyclerView rv = view.findViewById(R.id.recycler);
//        rv.setLayoutManager(new LinearLayoutManager(this));
//
//        // Bottom Padding
//        rv.setClipToPadding(false);
//        rv.setPadding(0, 0, 0, 100);
//
//        // Select/Unselect All
//        MaterialButton toggleAllBtn = view.findViewById(R.id.toggleAllBtn);
//
//        SongToggleAdapter toggleAdapter;
//
//        if (isOnline() && onlineTitles != null && onlineTitles.length > 0) {
//            int min = Math.min(onlineTitles.length, onlineThumbs.length);
//            min = Math.min(min, onlineUrls.length);
//            min = Math.min(min, songEnabled.length);
//
//            String[] tempTitles = Arrays.copyOf(onlineTitles, min);
//            String[] tempThumbs = Arrays.copyOf(onlineThumbs, min);
//            String[] tempUrls = Arrays.copyOf(onlineUrls, min);
//            // ✅ DON'T copy - pass the actual array reference
//            boolean[] tempEnabled = songEnabled; // Pass reference, not copy!
//
//            toggleAdapter = new SongToggleAdapter(
//                    this,
//                    tempTitles,
//                    tempThumbs,
//                    tempUrls,
//                    tempEnabled
//            );
//        } else {
// //            int min = Math.min(titles.length, thumbnails.length);
//            int min = Math.min(titles.length, offlineThumbPaths.length);
//            min = Math.min(min, songEnabled.length);
//
//            String[] tempTitles = Arrays.copyOf(titles, min);
//            int[] tempThumbs = Arrays.copyOf(thumbnails, min);
//            // ✅ DON'T copy - pass the actual array reference
//            boolean[] tempEnabled = songEnabled; // Pass reference, not copy!
//
//            toggleAdapter = new SongToggleAdapter(
//                    tempTitles,
//                    tempThumbs,
//                    tempEnabled
//            );
//        }
//
//        rv.setAdapter(toggleAdapter);
//
//        updateToggleButtonText(toggleAllBtn, toggleAdapter);
//
//        // ✅ Handle button click
//        toggleAllBtn.setOnClickListener(v -> {
//            if (toggleAdapter.areAllUnselected()) {
//                // All are off, so select all
//                toggleAdapter.selectAll();
//                toggleAllBtn.setText("Unselect All");
//            } else {
//                // At least one is on, so unselect all
//                toggleAdapter.unselectAll();
//                toggleAllBtn.setText("Select All");
//            }
//        });
//
//        // ✅ Listen for individual toggle changes to update button text
//        toggleAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onChanged() {
//                updateToggleButtonText(toggleAllBtn, toggleAdapter);
//            }
//        });
//
//
//        toggleAdapter.setOrderChangedListener((newTitles, newThumbs, newEnabled, newUrls) -> {
//            updatePlayerPlaylist(newTitles, newThumbs, newEnabled, newUrls);
//        });
//
//        ItemTouchHelper.SimpleCallback callback =
//                new ItemTouchHelper.SimpleCallback(
//                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
//                        0
//                ) {
//                    @Override
//                    public boolean onMove(
//                            RecyclerView rv,
//                            RecyclerView.ViewHolder vh,
//                            RecyclerView.ViewHolder target
//                    ) {
//                        int from = vh.getBindingAdapterPosition();
//                        int to = target.getBindingAdapterPosition();
//
//                        ((SongToggleAdapter) rv.getAdapter())
//                                .moveItem(from, to);
//
//                        return true;
//                    }
//
//                    @Override
//                    public void onSwiped(RecyclerView.ViewHolder vh, int direction) {}
//
//                    @Override
//                    public boolean isLongPressDragEnabled() {
//                        return true;
//                    }
//                };
//
//        new ItemTouchHelper(callback).attachToRecyclerView(rv);
//
//        dialog.setContentView(view);
//        dialog.show();
//    }

    private void showSongSelector() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater()
                .inflate(R.layout.dialog_song_selector, null);

        RecyclerView rv = view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setClipToPadding(false);
        rv.setPadding(0, 0, 0, 100);

        MaterialButton toggleAllBtn = view.findViewById(R.id.toggleAllBtn);

        SongToggleAdapter toggleAdapter;

        if (isOnline() && onlineTitles != null && onlineTitles.length > 0) {
            // ✅ Online mode
            int min = Math.min(onlineTitles.length, onlineThumbs.length);
            min = Math.min(min, onlineUrls.length);
            min = Math.min(min, songEnabled.length);

            toggleAdapter = new SongToggleAdapter(
                    this,
                    Arrays.copyOf(onlineTitles, min),
                    Arrays.copyOf(onlineThumbs, min),
                    Arrays.copyOf(onlineUrls, min),
                    songEnabled
            );
        } else {
            // ✅ Offline mode - use onlineTitles + offlineThumbPaths (loaded from DB)
            if (onlineTitles == null || onlineTitles.length == 0
                    || offlineThumbPaths == null || offlineThumbPaths.length == 0) {
                Toast.makeText(this, "No songs available", Toast.LENGTH_SHORT).show();
                return;
            }

            int min = Math.min(onlineTitles.length, offlineThumbPaths.length);
            min = Math.min(min, songEnabled.length);

            // ✅ Convert file paths to URL-style strings for the adapter
            String[] tempThumbs = Arrays.copyOf(offlineThumbPaths, min);
            String[] tempUrls = Arrays.copyOf(offlineAudioPaths, min);

            toggleAdapter = new SongToggleAdapter(
                    this,              // context needed for Glide to load file paths
                    Arrays.copyOf(onlineTitles, min),
                    tempThumbs,        // file paths as strings
                    tempUrls,          // audio paths (not used for display but needed)
                    songEnabled
            );
        }

        rv.setAdapter(toggleAdapter);

        updateToggleButtonText(toggleAllBtn, toggleAdapter);

        toggleAllBtn.setOnClickListener(v -> {
            if (toggleAdapter.areAllUnselected()) {
                toggleAdapter.selectAll();
                toggleAllBtn.setText("Unselect All");
            } else {
                toggleAdapter.unselectAll();
                toggleAllBtn.setText("Select All");
            }
        });

        toggleAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                updateToggleButtonText(toggleAllBtn, toggleAdapter);
            }
        });

        toggleAdapter.setOrderChangedListener((newTitles, newThumbs, newEnabled, newUrls) -> {
            updatePlayerPlaylist(newTitles, newThumbs, newEnabled, newUrls);
        });

        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    public boolean onMove(RecyclerView rv,
                                          RecyclerView.ViewHolder vh,
                                          RecyclerView.ViewHolder target) {
                        ((SongToggleAdapter) rv.getAdapter())
                                .moveItem(vh.getBindingAdapterPosition(),
                                        target.getBindingAdapterPosition());
                        return true;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder vh, int dir) {}

                    @Override
                    public boolean isLongPressDragEnabled() { return true; }
                };

        new ItemTouchHelper(callback).attachToRecyclerView(rv);

        dialog.setContentView(view);
        dialog.show();
    }

    // Update toggle all text
    private void updateToggleButtonText(MaterialButton button, SongToggleAdapter adapter) {
        if (adapter.areAllUnselected()) {
            button.setText("Select All");
        } else {
            button.setText("Unselect All");
        }
    }

    // Add this method to MainActivity
//    public void updateSongEnabledState(int position, boolean enabled) {
//        if (position >= 0 && position < songEnabled.length) {
//            songEnabled[position] = enabled;
//
//            // If online mode, also update the online arrays
//            if (isOnline() && position < onlineTitles.length) {
//                // Arrays are already synced via songEnabled
//            }
//        }
//    }

    public void updateSongEnabledState(int position, boolean enabled) {
        if (position >= 0 && position < songEnabled.length) {
            songEnabled[position] = enabled;

            if (isOnline() && position < onlineTitles.length) {
                // Arrays are already synced via songEnabled
            }
        }
    }


//    public void updatePlayerPlaylist(List<String> newTitles,
//                                     List<Object> newThumbs,
//                                     List<Boolean> newEnabled,
//                                     List<String> newUrls)
//    {
//        // ✅ FIX: Remember which song is currently playing BEFORE reordering
//        String currentlyPlayingSong = null;
//        if (isOnline() && currentIndex < onlineTitles.length) {
//            currentlyPlayingSong = onlineTitles[currentIndex];
//        } else if (!isOnline() && currentIndex < titles.length) {
//            currentlyPlayingSong = titles[currentIndex];
//        }
//
//        // Clear old temp lists
//        playTitles.clear();
//        playThumbs.clear();
//        playEnabled.clear();
//        playUrls.clear();
//        playRawIds.clear();
//
//        for (int i = 0; i < newTitles.size(); i++) {
//            playTitles.add(newTitles.get(i));
//            playThumbs.add(newThumbs.get(i));
//            playEnabled.add(newEnabled.get(i));
//
//            if (isOnline()) {
//                playUrls.add(newUrls.get(i)); // song URL
//            } else {
//                // ✅ FIX: Need to get the actual raw song ID, not thumbnail
//                // This requires passing song IDs separately in offline mode
//                // For now, we'll rebuild from the original songs array
//            }
//        }
//
//        // ✅ FIX: Update the main arrays with reordered data
//        if (isOnline()) {
//            onlineTitles = playTitles.toArray(new String[0]);
//            onlineThumbs = new String[playThumbs.size()];
//            onlineUrls = playUrls.toArray(new String[0]);
//
//            for (int i = 0; i < playThumbs.size(); i++) {
//                onlineThumbs[i] = (String) playThumbs.get(i);
//            }
//
//            songEnabled = new boolean[playEnabled.size()];
//            for (int i = 0; i < playEnabled.size(); i++) {
//                songEnabled[i] = playEnabled.get(i);
//            }
//
//            // ✅ FIX: Find where the currently playing song moved to
//            int newIndex = 0;
//            if (currentlyPlayingSong != null) {
//                for (int i = 0; i < onlineTitles.length; i++) {
//                    if (onlineTitles[i].equals(currentlyPlayingSong)) {
//                        newIndex = i;
//                        break;
//                    }
//                }
//            }
//            currentIndex = newIndex;
//
//        } else {
//            titles = playTitles.toArray(new String[0]);
//
//            thumbnails = new int[playThumbs.size()];
//            for (int i = 0; i < playThumbs.size(); i++) {
//                thumbnails[i] = (Integer) playThumbs.get(i);
//            }
//
//            songEnabled = new boolean[playEnabled.size()];
//            for (int i = 0; i < playEnabled.size(); i++) {
//                songEnabled[i] = playEnabled.get(i);
//            }
//
//            // ✅ FIX: Find where the currently playing song moved to
//            int newIndex = 0;
//            if (currentlyPlayingSong != null) {
//                for (int i = 0; i < titles.length; i++) {
//                    if (titles[i].equals(currentlyPlayingSong)) {
//                        newIndex = i;
//                        break;
//                    }
//                }
//            }
//            currentIndex = newIndex;
//        }
//
//        // ✅ Don't call initPlayer() - just update the UI to reflect new position
//        loadTitleByIndex(currentIndex);
//        loadThumbnailByIndex(currentIndex);
//
//        Toast.makeText(this, "Playlist reordered", Toast.LENGTH_SHORT).show();
//    }


    public void updatePlayerPlaylist(List<String> newTitles,
                                     List<Object> newThumbs,
                                     List<Boolean> newEnabled,
                                     List<String> newUrls) {
        // Remember currently playing song
        String currentlyPlayingSong = (onlineTitles != null && currentIndex < onlineTitles.length)
                ? onlineTitles[currentIndex] : null;

        // ✅ Update onlineTitles
        onlineTitles = newTitles.toArray(new String[0]);

        // ✅ Update songEnabled
        songEnabled = new boolean[newEnabled.size()];
        for (int i = 0; i < newEnabled.size(); i++) {
            songEnabled[i] = newEnabled.get(i);
        }

        if (isOnline()) {
            // ✅ Update online arrays
            onlineThumbs = new String[newThumbs.size()];
            for (int i = 0; i < newThumbs.size(); i++) {
                onlineThumbs[i] = (String) newThumbs.get(i);
            }
            onlineUrls = newUrls.toArray(new String[0]);

        } else {
            // ✅ Update offline path arrays
            offlineThumbPaths = new String[newThumbs.size()];
            for (int i = 0; i < newThumbs.size(); i++) {
                offlineThumbPaths[i] = (String) newThumbs.get(i);
            }
            offlineAudioPaths = newUrls.toArray(new String[0]);
        }

        // ✅ Find where the currently playing song moved to
        int newIndex = 0;
        if (currentlyPlayingSong != null) {
            for (int i = 0; i < onlineTitles.length; i++) {
                if (onlineTitles[i].equals(currentlyPlayingSong)) {
                    newIndex = i;
                    break;
                }
            }
        }
        currentIndex = newIndex;

        loadTitleByIndex(currentIndex);
        loadThumbnailByIndex(currentIndex);

        Toast.makeText(this, "Playlist reordered", Toast.LENGTH_SHORT).show();
    }

    private void showNotification() {
        int playPauseIcon = mediaPlayer != null && mediaPlayer.isPlaying()
                ? R.drawable.ic_pause
                : R.drawable.ic_play;

        // FIX: Use getTitleForIndex() instead of titles[currentIndex]
        String currentTitle = getTitleForIndex(currentIndex);

        // Create intent to open app when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "music")
                        .setSmallIcon(R.drawable.ic_music)
                        .setContentTitle("Now Playing")
                        .setContentText(currentTitle)  // FIX: Use the safe method
                        .setContentIntent(contentIntent)
                        .addAction(R.drawable.ic_prev, "Prev", prevIntent())
                        .addAction(playPauseIcon, "Play/Pause", pauseIntent())
                        .addAction(R.drawable.ic_next, "Next", nextIntent())
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setMediaSession(mediaSession.getSessionToken())
                                .setShowActionsInCompactView(0, 1, 2))
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setOngoing(mediaPlayer != null && mediaPlayer.isPlaying())
                        .setOnlyAlertOnce(true)
                        .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManager nm =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(1, builder.build());
        }
    }

    // Notif Helper
    // method to cancel the notification:
    private void cancelNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(1); // Same ID used in showNotification()
        }
    }

    // Downloaded songs Helper
    private void checkIfDownloaded(int index, DownloadCheckCallback callback) {
        new Thread(() -> {
            boolean exists = db.offlineSongDao().exists("song_" + index) > 0
                    && storageManager.isSongFullyDownloaded(index);
            runOnUiThread(() -> callback.onResult(exists));
        }).start();
    }

    public interface DownloadCheckCallback {
        void onResult(boolean isDownloaded);
    }

    // Offline songs - Downloads
    private void updateDownloadButton() {
        checkIfDownloaded(currentIndex, isDownloaded -> {
            if (isDownloaded) {
                downloadBtn.setIconTint(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.loop_on)
                ));
                downloadBtn.setEnabled(false);
            } else {
                downloadBtn.setIconTint(ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.loop_off)
                ));
                downloadBtn.setEnabled(true);
            }
        });
    }
}