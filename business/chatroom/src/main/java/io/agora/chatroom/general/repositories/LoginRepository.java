package io.agora.chatroom.general.repositories;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import http.VRHttpServer;
import io.agora.baseui.general.callback.ResultCallBack;
import io.agora.baseui.general.net.Resource;
import io.agora.chatroom.general.net.HttpManager;
import tools.ValueCallBack;
import tools.bean.VRUserBean;

public class LoginRepository extends BaseRepository {

    public LiveData<Resource<VRUserBean>> login(Context context,String deviceId) {
        return new NetworkOnlyResource<VRUserBean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<VRUserBean>> callBack) {
                HttpManager.getInstance(context).loginWithToken(deviceId,new ValueCallBack<VRUserBean>() {
                    @Override
                    public void onSuccess(VRUserBean value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {

                    }
                });
            }
        }.asLiveData();
    }


}
