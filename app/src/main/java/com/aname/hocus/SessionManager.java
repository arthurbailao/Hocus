package com.aname.hocus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.facebook.Session;

import org.apache.http.io.SessionInputBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arthurbailao on 9/25/14.
 */
public class SessionManager {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private static final int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "AndroidHivePref";
    private static final String IS_LOGIN = "IsLoggedIn";

    public SessionManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        this.editor = preferences.edit();
    }

    public void create(final Map<String, String> info) {
        this.editor.putBoolean(IS_LOGIN, true);

        for(Map.Entry<String, String> entry : info.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }

        this.editor.commit();
    }

    public void destroy() {
        Session session = Session.getActiveSession();
        if (session != null)
            session.closeAndClearTokenInformation();

        this.editor.clear();
        this.editor.commit();
    }

    public Map<String, String> getSessionInfo() {
        Map<String, String> result = new HashMap<String, String>();
        Map<String, ?> entries = preferences.getAll();
        for(Map.Entry<String, ?> entry : entries.entrySet()) {
            String key = entry.getKey();
            if(key != IS_LOGIN)
                result.put(key, entry.getValue().toString());
        }
        return result;
    }

    public boolean isOpen() {
        return this.preferences.getBoolean(IS_LOGIN, false);
    }
}
