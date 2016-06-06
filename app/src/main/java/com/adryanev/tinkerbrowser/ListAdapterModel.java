package com.adryanev.tinkerbrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by AdryanEV on 05/06/2016.
 */
public class ListAdapterModel extends ArrayAdapter<Link> {

    int groupID;
    ArrayList<Link> history;
    Context context;

    public ListAdapterModel(Context context, ArrayList<Link> history) {

        super(context,0,history);
        this.context=context;

        this.history = history;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Link link = getItem(position);
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout,parent,false);
        }
        TextView textTitle = (TextView)convertView.findViewById(R.id.siteTitle);
        textTitle.setText(link.getTitle());
        TextView textUrl = (TextView)convertView.findViewById(R.id.siteUrl);
        textUrl.setText(link.getUrl());

        //make url clickable
        Linkify.addLinks(textUrl,Linkify.ALL);
        return convertView;
    }
}
