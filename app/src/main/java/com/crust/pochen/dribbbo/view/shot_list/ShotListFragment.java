package com.crust.pochen.dribbbo.view.shot_list;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crust.pochen.dribbbo.R;
import com.crust.pochen.dribbbo.dribbble.Dribbble;
import com.crust.pochen.dribbbo.dribbble.DribbbleException;
import com.crust.pochen.dribbbo.model.Shot;
import com.crust.pochen.dribbbo.utils.ModelUtils;
import com.crust.pochen.dribbbo.view.base.DribbbleTask;
import com.crust.pochen.dribbbo.view.base.InfiniteAdapter;
import com.crust.pochen.dribbbo.view.base.SpaceItemDecoration;
import com.crust.pochen.dribbbo.view.shot_detail.ShotFragment;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pochen on 12/15/16.
 */

public class ShotListFragment extends Fragment {

    public static final int REQ_CODE_SHOT = 100;
    public static final String KEY_List_TYPE = "listType";
    public static final String KEY_BUCKET_ID = "bucketId";

    public static final int LIST_TYPE_POPULAR = 1;
    public static final int LIST_TYPE_LIKED = 2;
    public static final int LIST_TYPE_BUCKET = 3;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_container) SwipeRefreshLayout swipeRefreshLayout;

    private ShotListAdapter adapter;

    private int listType;

    private InfiniteAdapter.LoadMoreListener onLoadMore = new InfiniteAdapter.LoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (Dribbble.isLoggedIn()) {
                AsyncTaskCompat.executeParallel(new LoadShotsTask(false));
            }
        }
    };

    public static ShotListFragment newInstance(int listType) {
        Bundle args = new Bundle();
        args.putInt(KEY_List_TYPE, listType);

        ShotListFragment fragment = new ShotListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ShotListFragment newBucketListInstance(@NonNull String bucketId) {
        Bundle args = new Bundle();
        args.putInt(KEY_List_TYPE, LIST_TYPE_BUCKET);
        args.putString(KEY_BUCKET_ID, bucketId);

        ShotListFragment fragment = new ShotListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SHOT && resultCode == Activity.RESULT_OK) {
            Shot updatedShot = ModelUtils.toObject(data.getStringExtra(ShotFragment.KEY_SHOT),
                    new TypeToken<Shot>(){});
            for (Shot shot : adapter.getData()) {
                if (TextUtils.equals(shot.id, updatedShot.id)) {
                    shot.likes_count = updatedShot.likes_count;
                    shot.buckets_count = updatedShot.buckets_count;
                    adapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipe_recycler_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listType = getArguments().getInt(KEY_List_TYPE);

        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AsyncTaskCompat.executeParallel(new LoadShotsTask(true));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.spacing_medium)));

        //final Handler handler = new Handler();
        //adapter = new ShotListAdapter(fakeData(0), new ShotListAdapter.LoadMoreListener() {

        //adapter = new ShotListAdapter(new ArrayList<Shot>(), new ShotListAdapter.LoadMoreListener() {
          //  @Override
            //public void onLoadMore() {
                //Toast.makeText(getContext(), "load more called", Toast.LENGTH_LONG).show();
            /*    new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    List<Shot> moreData = fakeData(adapter.getDataCount() / COUNT_PER_LOAD);
                                    adapter.append(moreData);
                                    adapter.setShowLoading(moreData.size() >= COUNT_PER_LOAD);
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();*/

              //  AsyncTaskCompat.executeParallel(new LoadShotTask(adapter.getDataCount() / COUNT_PER_LOAD + 1));
           // }
        //});
        adapter = new ShotListAdapter(this, new ArrayList<Shot>(), onLoadMore);
        recyclerView.setAdapter(adapter);
    }

    private class LoadShotsTask extends DribbbleTask<Void, Void, List<Shot>> {

        boolean refresh;

        private LoadShotsTask(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected List<Shot> doJob(Void... params) throws DribbbleException {
            int page = refresh ? 1 : adapter.getData().size() / Dribbble.COUNT_PER_LOAD + 1;
            switch(listType) {
                case LIST_TYPE_POPULAR:
                    return Dribbble.getShots(page);
                case LIST_TYPE_LIKED:
                    return Dribbble.getLikedShots(page);
                case LIST_TYPE_BUCKET:
                    String bucketId = getArguments().getString(KEY_BUCKET_ID);
                    return Dribbble.getBucketShots(bucketId, page);
                default:
                    return Dribbble.getShots(page);
            }
        }

        @Override
        protected void onSuccess(List<Shot> shots) {
            adapter.setShowLoading(shots.size() >= Dribbble.COUNT_PER_LOAD);
            if (refresh) {
                swipeRefreshLayout.setRefreshing(false);
                adapter.setData(shots);
            } else {
                swipeRefreshLayout.setEnabled(true);
                adapter.append(shots);
            }
        }

        @Override
        protected void onFailed(DribbbleException e) {
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }


/*
    private class LoadShotTask extends AsyncTask<Void, Void, List<Shot>> {

        int page;

        public LoadShotTask(int page) {
            this.page = page;
        }

        @Override
        protected List<Shot> doInBackground(Void... params) {
            try {
                return Dribbble.getShots(page);
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Shot> shots) {
            if (shots != null) {
                adapter.append(shots);
            } else {
                Snackbar.make(getView(), "Error!", Snackbar.LENGTH_LONG).show();
            }
        }
    }



    private List<Shot> fakeData(int page ) {
        List<Shot> shotList = new ArrayList<>();
        Random random = new Random();

        int count = page < 2 ? COUNT_PER_LOAD : 10;

        for (int i = 0; i < count; i++) {
            Shot shot = new Shot();
            shot.title = "shot" + i;
            shot.views_count = random.nextInt(10000);
            shot.likes_count = random.nextInt(200);
            shot.buckets_count = random.nextInt(50);
            shot.description = makeDescription();

            shot.user = new User();
            shot.user.name = shot.title + " author";

            shotList.add(shot);
        }
        return shotList;
    }

    private static final String[] words = {
            "bottle", "bowl", "brick", "building", "bunny", "cake", "car", "cat", "cup",
            "desk", "dog", "duck", "elephant", "engineer", "fork", "glass", "griffon", "hat", "key",
            "knife", "lawyer", "llama", "manual", "meat", "monitor", "mouse", "tangerine", "paper",
            "pear", "pen", "pencil", "phone", "physicist", "planet", "potato", "road", "salad",
            "shoe", "slipper", "soup", "spoon", "star", "steak", "table", "terminal", "treehouse",
            "truck", "watermelon", "window"
    };

    private static String makeDescription() {
        return TextUtils.join(" ", words);
    }

*/
}
