package io.agora.secnceui.widget.gift;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import io.agora.baseui.BaseInitFragment;
import io.agora.baseui.interfaces.OnItemClickListener;
import io.agora.secnceui.R;
import io.agora.secnceui.bean.GiftBean;
import io.agora.secnceui.widget.recyclerview.HorizontalPageLayoutManager;
import io.agora.secnceui.widget.recyclerview.PagingScrollHelper;

public class LiveGiftListFragment extends BaseInitFragment implements OnItemClickListener {
    private RecyclerView rvList;
    private GiftListAdapter adapter;
    private GiftBean giftBean;
    private OnConfirmClickListener listener;
    private int position;

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle data = getArguments();
        if (null != data) position = data.getInt("position");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.chatroom_gift_fragment_list_layout;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        rvList = findViewById(R.id.rv_list);

        PagingScrollHelper snapHelper = new PagingScrollHelper();
        HorizontalPageLayoutManager manager = new HorizontalPageLayoutManager(1, 4);
        rvList.setHasFixedSize(true);
        rvList.setLayoutManager(manager);
        adapter = new GiftListAdapter();
        rvList.setAdapter(adapter);

        snapHelper.setUpRecycleView(rvList);
        snapHelper.updateLayoutManger();
        snapHelper.scrollToPosition(0);
        rvList.setHorizontalScrollBarEnabled(true);
    }

    @Override
    protected void initListener() {
        super.initListener();
        adapter.setOnItemClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        adapter.setData(GiftRepository.getGiftsByPage(getContext(),position));
    }

    @Override
    public void onItemClick(View view, int position) {
        giftBean = adapter.getItem(position);
        boolean checked = giftBean.isChecked();
        giftBean.setChecked(!checked);
        if(giftBean.isChecked()) {
            adapter.setSelectedPosition(position);
        }else {
            adapter.setSelectedPosition(-1);
        }
        if (listener != null)
        listener.onConfirmClick(view,giftBean);
    }

    public void setOnItemSelectClickListener(OnConfirmClickListener listener) {
        this.listener = listener;
    }

}
