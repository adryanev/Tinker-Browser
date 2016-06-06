package com.adryanev.tinkerbrowser;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Browser;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Created by AdryanEV on 05/06/2016.
 */
public class History extends Activity implements Serializable {

    ArrayList<Link> history;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        getBrowserList();
    }

    private void getBrowserList() {
        Bundle extra = getIntent().getBundleExtra("extra");
        history = (ArrayList<Link>) extra.getSerializable("history");
        ListAdapterModel lam = new ListAdapterModel(this, history);
        ListView lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(lam);


    }
}

