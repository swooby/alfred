package com.swooby.alfred.notification;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import com.smartfoo.android.core.R;
import com.smartfoo.android.core.texttospeech.FooTextToSpeech;

import java.util.LinkedList;
import java.util.List;

public class GoogleHangoutsNotificationParser
        extends AbstractNotificationParser
{
    private static class TextMessage
    {
        final String mFrom;
        final String mMessage;

        public TextMessage(String from, String message)
        {
            mFrom = from;
            mMessage = message;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof TextMessage)
            {
                return equals((TextMessage) o);
            }
            return super.equals(o);
        }

        public boolean equals(TextMessage o)
        {
            return mFrom.equals(o.mFrom) && mMessage.equals(o.mMessage);
        }

        @Override
        public int hashCode()
        {
            return mFrom.hashCode() + mMessage.hashCode();
        }
    }

    private final List<TextMessage> mTextMessages;

    public GoogleHangoutsNotificationParser(Context applicationContext, FooTextToSpeech textToSpeech)
    {
        super(applicationContext, textToSpeech, "com.google.android.talk", applicationContext.getString(R.string.hangouts_package_app_spoken_name));

        mTextMessages = new LinkedList<>();
    }

    private TextMessage addTextMessage(String from, String message)
    {
        TextMessage textMessage = new TextMessage(from, message);
        if (mTextMessages.contains(textMessage))
        {
            return null;
        }

        mTextMessages.add(textMessage);

        return textMessage;
    }

    @Override
    public boolean onNotificationPosted(StatusBarNotification sbn)
    {
        //super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();

        Bundle extras = notification.extras;
        if (extras == null)
        {
            return false;
        }

        List<TextMessage> textMessages = new LinkedList<>();

        CharSequence androidTitle = extras.getCharSequence("android.title", "Unknown User");
        CharSequence androidText = extras.getCharSequence("android.text", "Unknown Text");
        CharSequence[] androidTextLines = extras.getCharSequenceArray("android.textLines");

        if (androidTextLines != null)
        {
            for (CharSequence textLine : androidTextLines)
            {
                String[] parts = textLine.toString().split("  ");
                String from = parts[0];
                String message = parts[1];
                TextMessage textMessage = addTextMessage(from, message);
                if (textMessage != null)
                {
                    textMessages.add(textMessage);
                }
            }
        }
        else
        {
            String from = androidTitle.toString();
            String message = androidText.toString();
            TextMessage textMessage = addTextMessage(from, message);
            if (textMessage != null)
            {
                textMessages.add(textMessage);
            }
        }

        // TODO:(pv) I think there is perhaps a better even less verbatim way to speak the notification info...

        int count = textMessages.size();
        if (count == 0)
        {
            return false;
        }

        String title = mResources.getQuantityString(R.plurals.X_new_messages, count, count);
        mTextToSpeech.speak(title);
        for (TextMessage textMessage : textMessages)
        {
            mTextToSpeech.silence(750);
            mTextToSpeech.speak(mApplicationContext.getString(R.string.X_says, textMessage.mFrom));
            //mTextToSpeech.speak("to " + to);
            mTextToSpeech.silence(500);
            mTextToSpeech.speak(textMessage.mMessage);
        }

        return true;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        mTextMessages.clear();
    }
}
