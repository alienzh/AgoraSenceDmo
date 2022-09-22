package io.agora.secnceui.widget.primary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.agora.secnceui.R;
import io.agora.secnceui.widget.expression.ExpressionIcon;
import io.agora.secnceui.widget.expression.ExpressionView;
import io.agora.secnceui.widget.expression.SmileUtils;

public class ChatPrimaryMenuView extends RelativeLayout implements ExpressionView.ExpressionClickListener {

    protected Activity activity;
    protected InputMethodManager inputManager;
    private LinearLayoutCompat inputLayout;
    private LinearLayoutCompat menuLayout;
    private ArrayList<MenuItemModel> itemModels = new ArrayList<MenuItemModel>();
    private Map<Integer, MenuItemModel> itemMap = new HashMap();
    private MenuItemClickListener clickListener;
    private ConstraintLayout inputView;
    private EditText edContent;
    private ImageView icon;
    private TextView mSend;
    private boolean isShowEmoji;
    private RelativeLayout normalLayout;
    private ExpressionView expressionView;
    private ViewGroup rootView;


    public ChatPrimaryMenuView(Context context) {
        this(context, null);
    }

    public ChatPrimaryMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatPrimaryMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.widget_primary_menu_layout, this);
        activity = (Activity) context;
        inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        initViews();
    }

    private void initViews() {
        menuLayout = findViewById(R.id.menu_layout);
        inputLayout = findViewById(R.id.input_layout);
        inputView = findViewById(R.id.input_view);
        edContent = findViewById(R.id.input_edit_view);
        icon = findViewById(R.id.icon_emoji);
        mSend = findViewById(R.id.input_send);
        normalLayout = findViewById(R.id.normal_layout);
        expressionView = findViewById(R.id.expression_view);
        rootView =activity.getWindow().getDecorView().findViewById(android.R.id.content);

        expressionView.setExpressionListener(this);
        expressionView.init(7);

        edContent.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d("focus", "focused");
                    if (null != clickListener)
                        clickListener.onInputViewFocusChange(true);
                } else {
                    Log.d("focus", "focus lost");
                    if (null != clickListener)
                        clickListener.onInputViewFocusChange(false);
                    inputView.setVisibility(View.GONE);
                }
            }
        });
        inputLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inputView.setVisibility(View.VISIBLE);
                edContent.requestFocus();
            }
        });
        icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowEmoji = !isShowEmoji;
                checkShowExpression(isShowEmoji);
                if (null != clickListener)
                    clickListener.onEmojiClick(isShowEmoji);
            }
        });
        mSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != clickListener)
                    clickListener.onSendMessage(edContent.getText().toString().trim());
                edContent.setText("");
            }
        });

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int displayHeight = r.bottom - r.top;
                int rootHeight = rootView.getHeight();
                int softKeyHeight = rootHeight - displayHeight;
                Log.e("onGlobalLayout","softKeyHeight" + softKeyHeight +" rootHeight:" + rootHeight +" displayHeight:" + displayHeight);
            }
        });
    }

    public void addMenu( int drawableRes, int itemId){
        registerMenuItem(drawableRes,itemId);
        if(!itemMap.containsKey(itemId)) {
            ImageView imageView = new ImageView(activity);
            imageView.setLayoutParams(new LayoutParams(dp2px(activity,38), dp2px(activity,38)));
            imageView.setPadding(dp2px(activity,7),dp2px(activity,7)
                    ,dp2px(activity,7),dp2px(activity,7));
            imageView.setImageResource(drawableRes);
            imageView.setBackgroundResource(R.drawable.bg_primary_menu_item_icon);
            imageView.setId(itemId);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != clickListener)
                    clickListener.onChatExtendMenuItemClick(v.getId(),v);
                }
            });
            menuLayout.addView(imageView);
        }
    }

    public void initMenu(int roomType) {
        if (roomType == 0){
            normalLayout.setVisibility(VISIBLE);
            registerMenuItem(R.drawable.icon_close_mic,R.id.extend_item_mic);
            registerMenuItem(R.drawable.icon_handuphard,R.id.extend_item_hand_up);
            registerMenuItem(R.drawable.icon_eq,R.id.extend_item_eq);
            registerMenuItem(R.drawable.icon_gift,R.id.extend_item_gift);
            addView();
        }else if (roomType == 1){
            normalLayout.setVisibility(VISIBLE);
            inputLayout.setVisibility(GONE);
            registerMenuItem(R.drawable.icon_close_mic,R.id.extend_item_mic);
            registerMenuItem(R.drawable.icon_handuphard,R.id.extend_item_hand_up);
            registerMenuItem(R.drawable.icon_eq,R.id.extend_item_eq);
            addView();
        }
    }

    private void addView(){
        for (MenuItemModel itemModel : itemModels) {
            ImageView imageView = new ImageView(activity);
            LinearLayoutCompat.LayoutParams marginLayoutParams = new LinearLayoutCompat.LayoutParams(dp2px(activity,38), dp2px(activity,38));
            marginLayoutParams.leftMargin = dp2px(activity,5);
            marginLayoutParams.setMarginStart(dp2px(activity,5));
            imageView.setPadding(dp2px(activity,5),dp2px(activity,7)
                    ,dp2px(activity,5),dp2px(activity,7));
            imageView.setImageResource(itemModel.image);
            imageView.setBackgroundResource(R.drawable.bg_primary_menu_item_icon);
            imageView.setId(itemModel.id);

            if (itemModel.id == R.id.extend_item_gift){
                marginLayoutParams.setMarginEnd(dp2px(activity,15));
            }
            imageView.setLayoutParams(marginLayoutParams);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != clickListener)
                        clickListener.onChatExtendMenuItemClick(v.getId(),v);
                }
            });
            if (itemModel.id == R.id.extend_item_hand_up){
                RelativeLayout relativeLayout = new RelativeLayout(activity);
                relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(dp2px(activity,40), dp2px(activity,38)));

                ImageView status = new ImageView(activity);
                status.setId(R.id.extend_item_hand_up_status);
                status.setImageResource(R.drawable.bg_primary_hand_status);
                status.setVisibility(GONE);

                RelativeLayout.LayoutParams imgLayout = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                imgLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP | ALIGN_PARENT_RIGHT);
                imgLayout.setMargins(0,13,13,0);
                relativeLayout.addView(imageView, marginLayoutParams);
                relativeLayout.addView(status, imgLayout);
                menuLayout.addView(relativeLayout);
                continue;
            }
            menuLayout.addView(imageView);
        }
    }

    public void setShowHandStatus(boolean isShowHandStatus){
        ImageView handStatus = menuLayout.findViewById(R.id.extend_item_hand_up_status);
        if (isShowHandStatus){
            handStatus.setVisibility(VISIBLE);
        }else {
            handStatus.setVisibility(GONE);
        }
    }

    public void setHandStatus(boolean isShow,int role){
        ImageView hand = menuLayout.findViewById(R.id.extend_item_hand_up);
        ImageView dot = menuLayout.findViewById(R.id.extend_item_hand_up_status);
        if (role == 0){ // 0为房主
            hand.setImageResource(R.drawable.icon_handuphard);
            if (isShow){
                dot.setVisibility(VISIBLE);
            }else {
                dot.setVisibility(GONE);
            }
        }else {
            if (isShow){
                hand.setImageResource(R.drawable.icon_handup_dot);
            }else {
                hand.setImageResource(R.drawable.icon_handuphard);
            }
        }
    }

    public void setEnableHand(boolean isEnable){
        ImageView hand = menuLayout.findViewById(R.id.extend_item_hand_up);
        if (isEnable){
            hand.setImageResource(R.drawable.icon_vector);
        }else {
            hand.setImageResource(R.drawable.icon_handuphard);
        }
    }

    public void setEnableMic(boolean isEnable){
        ImageView mic = menuLayout.findViewById(R.id.extend_item_mic);
        if (isEnable){
            mic.setImageResource(R.drawable.icon_mic);
        }else {
            mic.setImageResource(R.drawable.icon_close_mic);
        }
    }

    /**
         * register menu item
         *
         * @param drawableRes
         *            background of item
         * @param itemId
         *             id
         */
    public void registerMenuItem( int drawableRes, int itemId) {
        if(!itemMap.containsKey(itemId)) {
            MenuItemModel item = new MenuItemModel();
            item.image = drawableRes;
            item.id = itemId;
            itemModels.add(item);
        }
    }

    public void checkShowExpression(boolean isShow){
        isShowEmoji = isShow;
        if (isShowEmoji){
            icon.setImageResource(R.drawable.icon_key);
            expressionView.setVisibility(VISIBLE);
        }else {
            icon.setImageResource(R.drawable.icon_face);
            expressionView.setVisibility(GONE);
        }
    }

    @Override
    public void onDeleteImageClicked() {
        if (!TextUtils.isEmpty(edContent.getText())) {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            edContent.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onExpressionClicked(ExpressionIcon expressionIcon) {
        if(expressionIcon != null) {
            edContent.append(SmileUtils.getSmiledText(getContext(),expressionIcon.getLabelString()));
        }
    }

    public void hideExpressionView(){
        expressionView.setVisibility(GONE);
    }

    public void showInput(){
        inputView.setVisibility(View.GONE);
    }

    public static class MenuItemModel{
        public int image;
        public int id;
    }

    public void setMenuItemOnClickListener(MenuItemClickListener listener){
        this.clickListener = listener;
    }


    public EditText getEdContent(){
        return edContent;
    }

    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @SuppressWarnings("unused")
    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
