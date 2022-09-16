package io.agora.chatroom.general.repositories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONException;

import io.agora.buddy.tool.GsonTools;
import io.agora.chat.ChatClient;
import manager.ChatroomConfigManager;
import tools.bean.VRUserBean;

public class ProfileManager {
   private static ProfileManager instance;
   private SharedPreferences.Editor editor;
   private SharedPreferences mSharedPreferences;
   private static String KEY_AT_PROFILE = "AT_PROFILE";

   public synchronized static ProfileManager getInstance(){
      if(instance == null){
         instance = new ProfileManager();
      }
      return instance;
   }

   @SuppressLint("CommitPrefEdits")
   private ProfileManager(){
      mSharedPreferences = ChatroomConfigManager.getInstance().getContext().getSharedPreferences("SP_AT_PROFILE", Context.MODE_PRIVATE);
      editor = mSharedPreferences.edit();
   }

   public void setProfile(VRUserBean value) {
      try {
         String device = ChatClient.getInstance().getDeviceInfo().getString("deviceid");
         if (TextUtils.isEmpty(device)){
            editor.putString(KEY_AT_PROFILE, GsonTools.beanToString(value));
         }else {
            editor.putString(device, GsonTools.beanToString(value));
         }
         editor.apply();
      } catch (JSONException e) {
         e.printStackTrace();
      }
   }

   public VRUserBean getProfile() {
      try {
         String device = ChatClient.getInstance().getDeviceInfo().getString("deviceid");
         String profile = mSharedPreferences.getString(device,"");
         if (!TextUtils.isEmpty(profile)){
           return GsonTools.toBean(profile,VRUserBean.class);
         }
      } catch (JSONException e) {
         e.printStackTrace();
      }
      return null;
   }
}
