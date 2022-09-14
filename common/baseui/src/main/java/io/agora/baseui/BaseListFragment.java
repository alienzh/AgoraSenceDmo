package io.agora.baseui;

import android.os.Bundle;

import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.baseui.adapter.RoomBaseRecyclerViewAdapter;
import io.agora.baseui.interfaces.OnItemClickListener;


/**
 * The base list fragment
 * @param <T>
 */
public abstract class BaseListFragment<T> extends BaseInitFragment implements OnItemClickListener {
    public RecyclerView mRecyclerView;
    public RoomBaseRecyclerViewAdapter<T> mListAdapter;
    protected ConcatAdapter concatAdapter;

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mRecyclerView = initRecyclerView();
        mRecyclerView.setLayoutManager(getLayoutManager());
        concatAdapter = new ConcatAdapter();
        addHeader(concatAdapter);
        mListAdapter = initAdapter();
        concatAdapter.addAdapter(mListAdapter);
    }

    @Override
    protected void initListener() {
        super.initListener();
        mListAdapter.setOnItemClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        mRecyclerView.setAdapter(concatAdapter);
    }

    /**
     * Can add header adapters
     * @param adapter
     */
    public void addHeader(ConcatAdapter adapter) {
        // Add header adapter by adapter
    }

    /**
     * Can change the RecyclerView's orientation
     * @return
     */
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(mContext);
    }

    /**
     * Must initialize the RecyclerView
     * @return
     */
    protected abstract RecyclerView initRecyclerView();

    /**
     * Must provide the list adapter
     * @return
     */
    protected abstract RoomBaseRecyclerViewAdapter<T> initAdapter();

}
