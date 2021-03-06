package com.example.kata.onlab.ui.list;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kata.onlab.R;
import com.example.kata.onlab.network.FriendDetail;
import com.example.kata.onlab.network.Friends;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kata on 2017. 04. 16..
 */

public class FriendsRecAdapter extends RecyclerView.Adapter<FriendsRecAdapter.ItemViewHolder> {
    List<Friends> friends = new ArrayList<>();
    Context mContext;
    private MyInterface listener;

    public FriendsRecAdapter(MyInterface listen) {
        listener = listen;

    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.friend_item, parent, false);
        mContext = parent.getContext();
        ItemViewHolder viewHolder = new ItemViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        if (position == 0) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_white_36dp));
            holder.imageView.setBorderColor(R.color.blue_normal);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.unsort();
                }
            });
        } else {
            final Friends friend = friends.get(position - 1);
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.ic_launcher));
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.sort(friend.id);
                }
            });

        }

    }

    public interface MyInterface {
        public void sort(int id);
        public void unsort();
    }


    @Override
    public int getItemCount() {
        return friends.size() + 1;
    }


    public void update(List<Friends> itemsrec) {
        friends.clear();
        friends.addAll(itemsrec);
        notifyDataSetChanged();

    }

    public void add(FriendDetail friendd) {
        Friends friend = new Friends(friendd.id, friendd.name);
        friends.add(friend);
        notifyItemInserted(friends.size() - 1 + 1);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        CircularImageView imageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            imageView = (CircularImageView) itemView.findViewById(R.id.item);

        }
    }
}
