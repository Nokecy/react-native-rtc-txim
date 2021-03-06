package com.reactnative.rtctxim.business.config;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.widget.EditText;
import android.widget.TextView;

import com.reactnative.rtctxim.R;
import com.reactnative.rtctxim.business.InitializeBusiness;
import com.reactnative.rtctxim.utils.UIUtils;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FaceManager {

    private static ArrayList<Emoji> emojiList = new ArrayList<>();
    private static LruCache<String, Bitmap> drawableCache = new LruCache(1024);
    private static Context context = InitializeBusiness.getAppContext();
    private static String[] emojiFilters = context.getResources().getStringArray(R.array.emoji_filter);
    private static final int drawableWidth = UIUtils.getPxByDp(32);
    private static ArrayList<FaceGroup> customFace = new ArrayList<>();

    public static ArrayList<Emoji> getEmojiList() {
        return emojiList;
    }

    public static ArrayList<FaceGroup> getCustomFaceList() {
        return customFace;
    }


    public static Bitmap getCustomBitmap(int groupId, String name) {
        for (int i = 0; i < customFace.size(); i++) {
            FaceGroup group = customFace.get(i);
            if (group.getGroupId() == groupId) {
                ArrayList<Emoji> faces = group.getFaces();
                for (int j = 0; j < faces.size(); j++) {
                    Emoji face = faces.get(j);
                    if (face.getFilter().equals(name)) {
                        return face.getIcon();
                    }
                }

            }
        }
        return null;
    }

    public static void loadFaceFiles() {
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < emojiFilters.length; i++) {
                    loadAssetBitmap(emojiFilters[i], "emoji/" + emojiFilters[i] + "@2x.png", true);
                }
                ArrayList<CustomFaceGroupConfigs> faceConfigs = InitializeBusiness.getBaseConfigs().getFaceConfigs();
                if (faceConfigs == null)
                    return;
                for (int i = 0; i < faceConfigs.size(); i++) {
                    CustomFaceGroupConfigs groupConfigs = faceConfigs.get(i);
                    FaceGroup groupInfo = new FaceGroup();
                    groupInfo.setGroupId(groupConfigs.getFaceGroupId());
                    groupInfo.setDesc(groupConfigs.getFaceIconName());
                    groupInfo.setPageColumnCount(groupConfigs.getPageColumnCount());
                    groupInfo.setPageRowCount(groupConfigs.getPageRowCount());
                    groupInfo.setGroupIcon(loadAssetBitmap(groupConfigs.getFaceIconName(), groupConfigs.getFaceIconPath(), false).getIcon());


                    ArrayList<FaceConfig> faceArray = groupConfigs.getFaceConfigs();
                    ArrayList<Emoji> faceList = new ArrayList<>();
                    for (int j = 0; j < faceArray.size(); j++) {
                        FaceConfig config = faceArray.get(j);
                        Emoji emoji = loadAssetBitmap(config.getFaceName(), config.getAssetPath(), false);
                        emoji.setWidth(config.getFaceWidth());
                        emoji.setHeight(config.getFaceHeight());
                        faceList.add(emoji);

                    }
                    groupInfo.setFaces(faceList);
                    customFace.add(groupInfo);
                }
            }
        }.start();


    }


    private static Emoji loadAssetBitmap(String filter, String assetPath, boolean isEmoji) {
        InputStream is = null;
        try {
            Emoji emoji = new Emoji();
            Resources resources = context.getResources();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDensity = DisplayMetrics.DENSITY_XXHIGH;
            options.inScreenDensity = resources.getDisplayMetrics().densityDpi;
            options.inTargetDensity = resources.getDisplayMetrics().densityDpi;
            context.getAssets().list("");
            is = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(0, 0, drawableWidth, drawableWidth), options);
            if (bitmap != null) {
                drawableCache.put(filter, bitmap);

                emoji.setIcon(bitmap);
                emoji.setFilter(filter);
                if (isEmoji)
                    emojiList.add(emoji);

            }
            return emoji;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // ???????????????????????????
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // ?????????????????????????????????????????????
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // ???????????????????????????????????????inSampleSize???????????????????????????????????????????????????
            // ?????????????????????????????????????????????
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {
        // ??????????????????inJustDecodeBounds?????????true????????????????????????
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // ?????????????????????????????????inSampleSize???
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // ??????????????????inSampleSize?????????????????????
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static boolean isFaceChar(String faceChar) {
        return drawableCache.get(faceChar) != null;
    }


    public static void handlerEmojiText(TextView comment, String content) {
        SpannableStringBuilder sb = new SpannableStringBuilder(content);
        String regex = "\\[(\\S+?)\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        Iterator<Emoji> iterator;
        Emoji emoji = null;
        while (m.find()) {
            iterator = emojiList.iterator();
            String tempText = m.group();
            while (iterator.hasNext()) {
                emoji = iterator.next();
                if (tempText.equals(emoji.getFilter())) {
                    //?????????Span?????????Span?????????
                    sb.setSpan(new ImageSpan(context, drawableCache.get(tempText)),
                            m.start(), m.end(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    break;
                }
            }
        }
        int selection = comment.getSelectionStart();
        comment.setText(sb);
        if (comment instanceof EditText) {
            ((EditText) comment).setSelection(selection);
        }
    }
}
