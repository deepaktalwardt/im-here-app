package com.example.user.teamproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.github.library.bubbleview.BubbleTextView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {

    private ArrayList<ChatModel> list_chats;
    private Context context;
    private static LayoutInflater layoutInflater;

    public CustomAdapter(Context context, ArrayList<ChatModel> list_chats) {
        this.list_chats = list_chats;
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return list_chats.size();
    }

    @Override
    public Object getItem(int position) {
        return list_chats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            Log.d("CustomAdapter", "position: " + position);
            Log.d("CustomAdapter", "position message: " + list_chats.get(position));
            if (list_chats.get(position).isSend) {
                view = layoutInflater.inflate(R.layout.bubble_item_sent, null);
            } else {
                view = layoutInflater.inflate(R.layout.bubble_item_received, null);
            }
            BubbleTextView text_message = (BubbleTextView) view.findViewById(R.id.text_message);
            text_message.setText(list_chats.get(position).getMessage());
        }
        return view;
    }
}
