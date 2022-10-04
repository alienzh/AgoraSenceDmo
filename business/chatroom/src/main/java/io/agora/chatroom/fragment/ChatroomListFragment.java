package io.agora.chatroom.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.android.arouter.launcher.ARouter;

import io.agora.CallBack;
import io.agora.baseui.adapter.RoomBaseRecyclerViewAdapter;
import io.agora.baseui.general.callback.OnResourceParseCallback;
import io.agora.buddy.tool.ThreadManager;
import io.agora.buddy.tool.ToastTools;
import io.agora.chat.ChatClient;
import io.agora.chatroom.R;
import io.agora.chatroom.adapter.ChatroomListAdapter;
import io.agora.chatroom.general.repositories.ProfileManager;
import io.agora.chatroom.model.ChatroomViewModel;
import io.agora.config.RouterParams;
import io.agora.config.RouterPath;
import io.agora.secnceui.widget.encryption.ChatroomEncryptionInputDialog;
import manager.ChatroomConfigManager;
import tools.bean.VRUserBean;
import tools.bean.VRoomBean;
import tools.bean.VRoomInfoBean;

public class ChatroomListFragment extends BaseChatroomListFragment<VRoomBean.RoomsBean> {
    private ChatroomListAdapter listAdapter;
    private ChatroomViewModel chatroomViewModel;
    private static final int pageSize = 10;
    private VRoomBean.RoomsBean roomBean;
    private itemCountListener listener;
    private String Cursor = "";
    private String mPassWord;
    private int position;
    private boolean isRefreshing = false;
    private boolean isLoadingNextPage = false;
    private boolean isEnd;

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        Log.e("chatroomViewModel"," current fragment: " + this);
        listAdapter = (ChatroomListAdapter) mListAdapter;
        listAdapter.setEmptyView(R.layout.chatroom_no_data_layout);
    }

    @Override
    protected RoomBaseRecyclerViewAdapter<VRoomBean.RoomsBean> initAdapter() {
        RoomBaseRecyclerViewAdapter adapter = new ChatroomListAdapter();
        return adapter;
    }

    @Override
    protected void initArgument() {
        super.initArgument();
        if (null != getArguments())
            position = getArguments().getInt("position",0);
    }

    @Override
    public void onResume() {
        super.onResume();
        reset();
        if (null != getArguments())
            position = getArguments().getInt("position",0);
        chatroomViewModel.getDataList(getActivity(),pageSize,position-1,"");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        chatroomViewModel = new ViewModelProvider(this).get(ChatroomViewModel.class);
        chatroomViewModel.getRoomObservable().observe(this, response ->{
            parseResource(response, new OnResourceParseCallback<>() {
                @Override
                public void onSuccess(@Nullable VRoomBean data) {
                    if (data != null){
                        Cursor = data.getCursor();
                        if (TextUtils.isEmpty(Cursor)){
                            isEnd = true;
                        }
                        Log.e("ListFragment1","getCursor: " + data.getCursor());
                        if (isRefreshing){
                            listAdapter.setData(data.getRooms());
                        }else {
                            listAdapter.addData(data.getRooms());
                        }
                        Log.e("ListFragment1","getData: " + listAdapter.getData().size());
                        if (null != listener)
                            listener.getItemCount(data.getTotal());
                        finishRefresh();
                        isRefreshing = false;
                    }
                }
            });
        });

        chatroomViewModel.getCheckPasswordObservable().observe(this,response -> {
            parseResource(response, new OnResourceParseCallback<VRoomInfoBean>() {
                @Override
                public void onSuccess(@Nullable VRoomInfoBean data) {
                    goChatroomPage(roomBean,mPassWord);
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    ToastTools.show(getActivity(),"check password failed", Toast.LENGTH_SHORT);
                }
            });
        });

    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                int totalCount = lm.getItemCount();
                if (lastVisibleItemPosition == totalCount - 1 && !isLoadingNextPage && !isRefreshing) {
                        // 在前面addLoadItem后，itemCount已经变化
                        // 增加一层判断，确保用户是滑到了正在加载的地方，才加载更多
                        int findLastVisibleItemPosition = lm.findLastVisibleItemPosition();
                        if (findLastVisibleItemPosition == lm.getItemCount() - 1) {
                            ThreadManager.getInstance().runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    isLoadingNextPage = true;
                                    if (!isEnd){
                                        pullData();
                                    }
                                }
                            });
                        }
                    }
                }
        });
    }

    private void pullData() {
        ThreadManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Log.e("ListFragment1","Cursor: " + Cursor);
                chatroomViewModel.getDataList(getActivity(),pageSize,position-1,Cursor);
                isLoadingNextPage = false;
            }
        });
    }


    @Override
    public void onItemClick(View view, int position) {
        roomBean = listAdapter.getItem(position);
        Log.e("onItemClick","getOwnerUid: " + roomBean.getOwnerUid());
        if (!ChatClient.getInstance().isLoggedIn()){
            VRUserBean data = ProfileManager.getInstance().getProfile();
            Log.d("ChatroomListFragment","chat_uid: " + data.getChat_uid());
            Log.d("ChatroomListFragment","im_token: " + data.getIm_token());
            ChatroomConfigManager.getInstance().login(data.getChat_uid(), data.getIm_token(), new CallBack() {
                @Override
                public void onSuccess() {
                    Log.d("ChatroomListFragment","Login success");
                    checkPrivate();
                }

                @Override
                public void onError(int code, String msg) {
                    Log.e("ChatroomListFragment", "Login onError code:" + code + " desc: " + msg);
                    // TODO: 2022/9/16  202
                    checkPrivate();
                }
            });
        }else {
            checkPrivate();
        }
    }

    private void goChatroomPage(VRoomBean.RoomsBean roomBean) {
        ARouter.getInstance()
                .build(RouterPath.ChatroomPath)
                .withSerializable(RouterParams.KEY_CHATROOM_INFO, roomBean)
                .navigation();
    }

    private void goChatroomPage(VRoomBean.RoomsBean roomBean,String password) {
        ARouter.getInstance()
                .build(RouterPath.ChatroomPath)
                .withSerializable(RouterParams.KEY_CHATROOM_INFO, roomBean)
                .withString(RouterParams.KEY_CHATROOM_JOIN_PASSWORD,password)
                .navigation();
    }

    private void checkPrivate(){
        ThreadManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (roomBean.isIs_private()){
                    showDialog();
                }else {
                    goChatroomPage(roomBean);
                }
            }
        });

    }

    public void showDialog(){
        new ChatroomEncryptionInputDialog()
                .leftText(getActivity().getString(R.string.chatroom_cancel))
                .rightText(getActivity().getString(R.string.chatroom_confirm))
                .setOnClickListener(new ChatroomEncryptionInputDialog.OnClickBottomListener() {
                    @Override
                    public void onCancelClick() {}

                    @Override
                    public void onConfirmClick(@NonNull String password) {
                        mPassWord = password;
                        chatroomViewModel.checkPassword(getActivity(), roomBean.getRoom_id(), password);
                    }
                })
                .show(getActivity().getSupportFragmentManager(), "encryptionInputDialog");
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        reset();
        Log.e("ChatroomListFragment","onRefresh" + listAdapter.getItemCount());
        chatroomViewModel.getDataList(getActivity(),pageSize,position-1,Cursor);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface itemCountListener{
        void getItemCount(int count);
    }

    public void SetItemCountChangeListener(itemCountListener listener){
        this.listener = listener;
    }

    private void reset(){
        isRefreshing = true;
        Cursor = "";
    }

}
