package com.per.pulldownrefresh;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PullDownRefreshActivity extends Activity implements OnScrollListener{
    public static final String TAG = "PullDownRefreshActivity";

    public static final int STATE_PULL_DOWN = 0;
    public static final int STATE_RELEASE_PULL = 1;
    public static final int STATE_REFRESHING = 2;
    public static final int STATE_REFRESH_DONE = 3;

    ListView mPullDownLv;
    View mHeaderRefresh;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_down_refresh);

        initView();
    }

    private void initView() {
        mPullDownLv = (ListView)findViewById(R.id.pull_down_refresh_listView);
        mPullDownLv.setOnScrollListener(this);
        mPullDownLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                Toast.makeText(mContext, "position " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pull_down_refresh, menu);
        return true;
    }

    public class MyAdapter extends BaseAdapter {
        int itemCount = 10;

        @Override
        public int getCount() {
            return itemCount;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if(convertView == null) {
                holder = new ViewHolder();

                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.pull_down_list_item_layout, null);
                holder.tv = (TextView)convertView;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.tv.setText("item " + (itemCount - position));

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tv;
    }

    @Override
    public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
        
    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {
        
    }
}
