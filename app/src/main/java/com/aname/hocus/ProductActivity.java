package com.aname.hocus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;


public class ProductActivity extends Activity {

    private TextView textViewName;
    private ImageView imageViewImg;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        textViewName = (TextView) findViewById(R.id.textView_name);
        imageViewImg = (ImageView) findViewById(R.id.imageView_img);

        Intent intent = getIntent();
        String img = intent.getStringExtra("img");
        new LoadImageTask().execute(img);
        textViewName.setText(intent.getStringExtra("name"));
    }

    private class LoadImageTask extends AsyncTask<String, Integer, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ProductActivity.this);
            dialog.setMessage("Loading...");
            dialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            dialog.setProgress(values[0]);
        }

        @Override
        protected Bitmap doInBackground(String... args) {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                imageViewImg.setImageBitmap(bitmap);
            } else {
                // Deu pau pra dar load
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.product, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
