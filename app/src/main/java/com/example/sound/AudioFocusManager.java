package com.example.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

public class AudioFocusManager {

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private AudioFocusListener listener;
    private boolean wasPlayingBeforeFocusLoss = false;

    public interface AudioFocusListener {
        void onAudioFocusGain();
        void onAudioFocusLoss();
        void onAudioFocusLossTransient();
        void onAudioFocusLossCanDuck();
    }

    public AudioFocusManager(Context context, AudioFocusListener listener) {
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.listener = listener;
    }

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            wasPlayingBeforeFocusLoss = false;
                            if (listener != null) {
                                listener.onAudioFocusLoss();
                            }
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            wasPlayingBeforeFocusLoss = true;
                            if (listener != null) {
                                listener.onAudioFocusLossTransient();
                            }
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            if (listener != null) {
                                listener.onAudioFocusLossCanDuck();
                            }
                            break;

                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (wasPlayingBeforeFocusLoss && listener != null) {
                                listener.onAudioFocusGain();
                                wasPlayingBeforeFocusLoss = false;
                            }
                            break;
                    }
                }
            };

    public boolean requestAudioFocus() {
        if (audioManager == null) return false;

        int result;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest == null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();

                audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(audioAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(audioFocusChangeListener)
                        .build();
            }

            result = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            result = audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
            );
        }

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public void abandonAudioFocus() {
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else {
                audioManager.abandonAudioFocus(audioFocusChangeListener);
            }
        }
    }

    public void release() {
        abandonAudioFocus();
        listener = null;
        audioManager = null;
        audioFocusRequest = null;
    }
}