package com.example.demopulltorefresh;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.demopulltorefresh.PullToRefreshListView.OnRefreshListener;

public class TestActivity extends Activity {
    public final static String TAG = "TextActivity";

    private PullToRefreshListView mPullToRefreshLv;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mPullToRefreshLv = (PullToRefreshListView)findViewById(R.id.pull_to_refresh_lv);
        mPullToRefreshLv.setOnRefreshListener(new OnRefreshListener() {
            
            @Override
            public void onRefresh() {
                new RefreshTask().execute();
            }
        });
        mAdapter = new MyAdapter(this, 10);
        mPullToRefreshLv.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }

    public class MyAdapter extends BaseAdapter {
        public final static String LOCAL_TAG = "MyAdapter";

        private Context context;
        private int itemCount = 10;

        public MyAdapter(Context c, int count) {
            context = c;
            itemCount = count;
        }

        @Override
        public int getCount() {
            return itemCount;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mHolder;
            if(convertView == null) {
                mHolder = new ViewHolder();

                mHolder.itemTv = new TextView(context);
                mHolder.itemTv.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, 80));
                mHolder.itemTv.setGravity(Gravity.CENTER);
                mHolder.itemTv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                mHolder.itemTv.setTextColor(Color.BLACK);
                mHolder.itemTv.setTextSize(20);
                LinearLayout linearL = new LinearLayout(context);
                linearL.addView(mHolder.itemTv);

                convertView = linearL;
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder)convertView.getTag();
            }

            mHolder.itemTv.setText("item " + position);

            return convertView;
        }
    }

    public class RefreshTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPostExecute(Void result) {
            mPullToRefreshLv.onRefreshComplete();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public static class ViewHolder {
        TextView itemTv;
    }
}
