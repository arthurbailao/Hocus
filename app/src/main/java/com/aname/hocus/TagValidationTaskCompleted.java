package com.aname.hocus;

import java.util.Map;

/**
 * Created by arthurbailao on 9/29/14.
 */
public interface TagValidationTaskCompleted {
    void onValidTag(final Map<String, String> productInfo);
    void onInvalidTag();
}
