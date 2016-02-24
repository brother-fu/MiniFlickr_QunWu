package com.miniflickr.qunwu.miniflickr;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.miniflickr.qunwu.miniflickr.entity.GalleryItem;

import java.util.List;

/**
 * Created by Qun Wu on 2016/2/22.
 */

/*
RecyclerView.Adapter: public static abstract class Adapter<VH extends ViewHolder>
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.VHWithImg> {
    private Context mContext;
    //all GalleryItem(s) to describe VHWithImg(s) in RecyclerView
    private List<GalleryItem> mList;

    public GalleryAdapter(Context context, List<GalleryItem> list){
        mContext = context;
        mList = list;
    }

    @Override
    public VHWithImg onCreateViewHolder(ViewGroup parent, int viewType) {
        /**
         * .from(Context):Obtains the LayoutInflater from the given context.
         */
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder,parent,false);
        VHWithImg vh = new VHWithImg(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(VHWithImg holder, int position) {
        final GalleryItem item = mList.get(position);
        holder.imgView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //goes into PhotoActivity
                Intent intent = new Intent(mContext, PhotoActivity.class);
                intent.putExtra("item",item);
                mContext.startActivity(intent);
            }
        });
        //Begin a load with Glide by passing in a context.
        Glide.with(mContext)
                .load(item.getUrl())
                .thumbnail(0.5f)
                        //init a real content of a imgView in VHWithImg here:
                .into(holder.imgView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
    /*
    A ViewHolder: describes an item view and metadata about its place within the RecyclerView
    Param: (View itemView) is the a item view described by ViewHolder, (itemView) returned by 'Inflater.inflater(R.layout.view_holder)'
     */
    public static class VHWithImg extends RecyclerView.ViewHolder{
        public ImageView imgView ;
        public VHWithImg(View itemView){
            super(itemView);
            imgView = (ImageView)itemView.findViewById(R.id.gallery_item);
        }
    }

    public void addAll(List<GalleryItem> newList) {
        //.addAll(): Adds the objects in the specified collection to the end of this List(mList)
        mList.addAll(newList);
    }
    //clear all GalleryItem(s) in RecyclerView
    public void clear() {
        mList.clear();
    }
}
