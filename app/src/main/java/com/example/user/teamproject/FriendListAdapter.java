package com.example.user.teamproject;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendListViewHolder> {
    private ArrayList<Friend_card> mFriendList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class FriendListViewHolder extends RecyclerView.ViewHolder {
        public TextView mUsername;
        public TextView mUUID;
        public TextView mTime;
        public int mConnection;
        public ImageView mStatus;

        public FriendListViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mUsername = itemView.findViewById(R.id.username);
            mUUID = itemView.findViewById(R.id.UUID);
            mTime = itemView.findViewById(R.id.time);
            mStatus = itemView.findViewById(R.id.status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public FriendListAdapter(ArrayList<Friend_card> friendList) {
        mFriendList = friendList;
    }

    @NonNull
    @Override
    public FriendListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_card, viewGroup, false);
        FriendListViewHolder flvh = new FriendListViewHolder(v, mListener);
        return flvh;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListViewHolder friendListViewHolder, int i) {
        Friend_card currentCard = mFriendList.get(i);

        friendListViewHolder.mUsername.setText(currentCard.getUsername());
        friendListViewHolder.mUUID.setText(currentCard.getUUID());
        friendListViewHolder.mTime.setText(currentCard.getTime());
        if(currentCard.getConnection() == 1){
            friendListViewHolder.mStatus.setImageResource(R.drawable.ic_online);
        }else{
            friendListViewHolder.mStatus.setImageResource(R.drawable.ic_offline);
        }

    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }
}
