package com.aname.hocus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.facebook.UiLifecycleHelper;
import java.util.Map;

public class MainActivity extends Activity implements NdefReaderTaskCompleted, TagValidationTaskCompleted {

    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String TAG = "MainActiviy";

    private UiLifecycleHelper uiHelper;
    private SessionManager sessionManager;
    private NfcAdapter nfcAdapter;

    private LinearLayout nfcEnabledLayout;
    private LinearLayout nfcDisabledLayout;
    private ImageView nfcEnabledImage;
    private ImageView nfcDisabledImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(uiHelper == null) {
            uiHelper = new UiLifecycleHelper(this, null);
        }
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        nfcEnabledLayout = (LinearLayout) findViewById(R.id.layout_nfc_enabled);
        nfcDisabledLayout= (LinearLayout) findViewById(R.id.layout_nfc_disabled);
        nfcEnabledImage = (ImageView) findViewById(R.id.imageView_nfc_enabled);
        nfcDisabledImage = (ImageView) findViewById(R.id.imageView_nfc_disabled);

        nfcDisabledLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS), 0);
            }
        });

        sessionManager = new SessionManager(this);
        if(!sessionManager.isOpen()) {
            startLoginActivity();
            return;
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        updateNfcStatus();
        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        updateNfcStatus();
        if(sessionManager.isOpen()) {
            setupForegroundDispatch(this, nfcAdapter);
        } else {
            startLoginActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, nfcAdapter);
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNdefReaderTaskCompleted(Map<String, String> info) {
        new TagValidationTask(this).execute(info.get("uid"), info.get("rid"));
    }

    @Override
    public void onValidTag(Map<String, String> info) {
//        Toast.makeText(this,
//                String.format("name: %s\nurl: %s", info.get("name"), info.get("url")),
//                Toast.LENGTH_LONG).show();
        Intent i = new Intent(this, ProductActivity.class);
        i.putExtra("name", info.get("name"));
        i.putExtra("img", info.get("img"));
        startActivity(i);
    }

    @Override
    public void onInvalidTag() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title)
                .setMessage(R.string.alert_text)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
//                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // do nothing
//                    }
//                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    private void logout() {
        sessionManager.destroy();
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)){
            String type = intent.getType();
            if(MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask(this).execute(tag);
            }
        } else if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask(this).execute(tag);
                    break;
                }
            }
        }
    }

    private void updateNfcStatus() {
        if(nfcAdapter.isEnabled()) {
            nfcEnabledLayout.setVisibility(View.VISIBLE);
            nfcEnabledImage.setVisibility(View.VISIBLE);
            nfcDisabledLayout.setVisibility(View.INVISIBLE);
            nfcDisabledImage.setVisibility(View.INVISIBLE);
        } else {
            nfcEnabledLayout.setVisibility(View.INVISIBLE);
            nfcEnabledImage.setVisibility(View.INVISIBLE);
            nfcDisabledLayout.setVisibility(View.VISIBLE);
            nfcDisabledImage.setVisibility(View.VISIBLE);
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our mani//fest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
}
