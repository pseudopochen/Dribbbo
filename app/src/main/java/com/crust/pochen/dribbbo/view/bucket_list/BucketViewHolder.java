package com.crust.pochen.dribbbo.view.bucket_list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crust.pochen.dribbbo.R;
import com.crust.pochen.dribbbo.view.base.BaseViewHolder;

import org.w3c.dom.Text;

import butterknife.BindView;

/**
 * Created by pochen on 12/15/16.
 */

public class BucketViewHolder extends BaseViewHolder {

    @BindView(R.id.bucket_layout) View bucketLayout;
    @BindView(R.id.bucket_name) TextView bucketName;
    @BindView(R.id.bucket_shot_count) TextView bucketCount;
    @BindView(R.id.bucket_shot_chosen) ImageView bucketChosen;

    public BucketViewHolder(View itemView) {
        super(itemView);
    }
}

