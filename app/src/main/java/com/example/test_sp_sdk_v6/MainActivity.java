package com.example.test_sp_sdk_v6;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private boolean mIsFirstResume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RNCMP cmp = RNCMP.getInstance(getApplicationContext());

                // FIXME: Use this button to test loadPrivacyManager()
                boolean isGdprApplies = ((TestApplication)getApplication()).mSubjectToGDPR;
                if (isGdprApplies) {
                    cmp.loadGdprPrivacyManager(null, true, new Promise());
                } else {
                    cmp.loadCcpaPrivacyManager(null, true, new Promise());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsFirstResume) {
            // Call CMP.loadMessage() during app launch
            mIsFirstResume = false;
            RNCMP cmp = RNCMP.getInstance(getApplicationContext());
            cmp.loadMessage(null, true, new Promise());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
