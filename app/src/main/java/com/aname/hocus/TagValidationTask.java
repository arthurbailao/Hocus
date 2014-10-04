package com.aname.hocus;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by arthurbailao on 9/29/14.
 */
public class TagValidationTask extends AsyncTask<String, Void, Map<String, String> > {

    private TagValidationTaskCompleted listener;
    private ProgressDialog dialog;

    public TagValidationTask(TagValidationTaskCompleted listener) {
        this.listener = listener;
        this.dialog = new ProgressDialog((MainActivity) listener);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Validating product, please wait...");
        dialog.show();
    }

    @Override
    protected Map<String, String> doInBackground(String... params) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            String uid = URLEncoder.encode(params[0], "utf-8");
            String rid = URLEncoder.encode(params[1], "utf-8");
            String uri = String.format("http://ec2-54-236-215-215.compute-1.amazonaws.com/api/v1/tags/%s/product?rid=%s", uid, rid);
            response = httpclient.execute(new HttpGet(uri));
            StatusLine statusLine = response.getStatusLine();
            final int statusCode = statusLine.getStatusCode();
            if( statusCode == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                return null;
//                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            //TODO Handle problems..
        } catch (IOException e) {
            e.printStackTrace();
            //TODO Handle problems..
        }

        if (responseString != null) {
            try {
                Log.i("Task", responseString);
                JSONObject object = new JSONObject(responseString);
                return jsonToMap(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Map<String, String> info) {
        if (info != null) {
            if (info.get("error") == null) {
                listener.onValidTag(info);
            } else {
                listener.onInvalidTag();
            }

        } else {
//                    Toast.makeText(this,
//                String.format("name: %s\nurl: %s", info.get("name"), info.get("url")),
//                Toast.LENGTH_LONG).show();
        }

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private Map<String, String> jsonToMap(JSONObject object) throws JSONException {
        Map<String, String> result = new HashMap<String, String>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext())
        {
            String key = keysItr.next();
            String value = object.get(key).toString();

//            if(value instanceof JSONArray)
//            {
//                value = toList((JSONArray) value);
//            }
//
//            else if(value instanceof JSONObject)
//            {
//                value = toMap((JSONObject) value);
//            }
            result.put(key, value);
        }
        return result;
    }
}
