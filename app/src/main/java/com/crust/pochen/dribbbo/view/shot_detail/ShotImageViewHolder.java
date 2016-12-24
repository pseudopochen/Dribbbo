package com.crust.pochen.dribbbo.view.shot_detail;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by pochen on 12/17/16.
 */

public class ShotImageViewHolder extends RecyclerView.ViewHolder {

    SimpleDraweeView image;

    public ShotImageViewHolder(View itemView) {
        super(itemView);
        image = (SimpleDraweeView) itemView;
    }
}
