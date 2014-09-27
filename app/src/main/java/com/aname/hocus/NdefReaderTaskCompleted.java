package com.aname.hocus;

import java.util.Map;

/**
 * Created by arthurbailao on 9/26/14.
 */
public interface NdefReaderTaskCompleted {
    void onNdefReaderTaskCompleted(final Map<String, String> info);
}
