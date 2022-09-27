package io.agora.chatroom.ui;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import bean.ChatMessageData;
import custormgift.CustomMsgHelper;
import custormgift.OnMsgCallBack;
import io.agora.chatroom.general.repositories.ProfileManager;
import io.agora.secnceui.bean.GiftBean;
import io.agora.secnceui.widget.gift.ChatroomGiftView;
import io.agora.secnceui.widget.gift.GiftBottomDialog;
import io.agora.secnceui.widget.gift.OnSendClickListener;

public class RoomGiftViewDelegate {
   private static RoomGiftViewDelegate instance;
   private FragmentActivity activity;
   private GiftBottomDialog dialog;
   private int time = 2;
   private TextView send;
   private ChatroomGiftView giftView;

   RoomGiftViewDelegate(FragmentActivity activity, ChatroomGiftView giftView){
      this.activity = activity;
      this.giftView = giftView;
   }

   public static RoomGiftViewDelegate getInstance(FragmentActivity activity, ChatroomGiftView giftView){
      if(instance == null) {
         synchronized (RoomGiftViewDelegate.class) {
            if(instance == null) {
               instance = new RoomGiftViewDelegate(activity,giftView);
            }
         }
      }
      return instance;
   }


   public void showGiftDialog() {
      dialog = (GiftBottomDialog) activity.getSupportFragmentManager().findFragmentByTag("live_gift");
      if(dialog == null) {
         dialog = GiftBottomDialog.getNewInstance();
      }
      dialog.show(activity.getSupportFragmentManager(), "live_gift");
      dialog.setOnConfirmClickListener(new OnSendClickListener() {
         @Override
         public void SendGift(View view, Object bean) {
            GiftBean giftBean = (GiftBean) bean;
            CustomMsgHelper.getInstance().sendGiftMsg(
                    ProfileManager.getInstance().getProfile().getName(),
                    ProfileManager.getInstance().getProfile().getPortrait(),
                    giftBean.getId(), giftBean.getNum(),giftBean.getPrice(),giftBean.getName(),
                    new OnMsgCallBack() {
               @Override
               public void onSuccess(ChatMessageData message) {
                  Log.e("MenuItemClick",  "item_gift_onSuccess");
                  giftView.refresh();
                  if (view instanceof TextView){
                     send = ((TextView) view);
                     send.setText(time +"s");
                     send.setEnabled(false);
                     startTask();
                  }
               }

               @Override
               public void onError(String messageId, int code, String error) {
                  super.onError(messageId, code, error);
                  dialog.dismiss();
               }
            });
         }
      });
   }

   private Handler handler = new Handler();
   private Runnable task;

   // 开启倒计时任务
   private void startTask() {
      handler.postDelayed(task = new Runnable() {
         @Override
         public void run() {
            // 在这里执行具体的任务
            time--;
            send.setText(time+"s");
            // 任务执行完后再次调用postDelayed开启下一次任务
            if (time==0){
               stopTask();
               send.setEnabled(true);
               send.setText("Send");
            }else {
               handler.postDelayed(this, 1000);
            }
         }
      }, 1000);
   }

   // 停止计时任务
   private void stopTask() {
      if (task != null) {
         handler.removeCallbacks(task);
         task = null;
         time = 2;
      }
   }
}
