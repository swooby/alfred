package com.swooby.alfred;

import android.content.Context;
import android.media.AudioManager;

import java.util.ArrayList;

public class AudioStreamType
{
    private static ArrayList<AudioStreamType> sTypes;

    public static final ArrayList<AudioStreamType> getTypes(Context context)
    {
        if (sTypes == null)
        {
            sTypes = new ArrayList<>();
            sTypes.add(new AudioStreamType(context.getString(R.string.audio_stream_notification), AudioManager.STREAM_NOTIFICATION));
            sTypes.add(new AudioStreamType(context.getString(R.string.audio_stream_media), AudioManager.STREAM_MUSIC));
            sTypes.add(new AudioStreamType(context.getString(R.string.audio_stream_alarm), AudioManager.STREAM_ALARM));
        }
        return sTypes;
    }

    private final String mName;
    private final int    mAudioStreamType;

    private AudioStreamType(String name, int audioStreamType)
    {
        mName = name;
        mAudioStreamType = audioStreamType;
    }

    @Override
    public String toString()
    {
        return mName;
    }

    public int getAudioStreamType()
    {
        return mAudioStreamType;
    }
}
