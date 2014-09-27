package com.aname.hocus;

import java.util.Map;

/**
 * Created by arthurbailao on 9/26/14.
 */
public interface FacebookLoginCompleted {
    void onFacebookLoginCompleted(final Map<String, String> userInfo);
}
