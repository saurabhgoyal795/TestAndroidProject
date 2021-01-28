package com.averda.online.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.Display;

import androidx.core.app.TaskStackBuilder;

import com.averda.online.home.MainActivity;
import com.averda.online.mypackage.MyPackageActivity;
import com.averda.online.preferences.Preferences;
import com.averda.online.profile.NewProfileActivity;
import com.averda.online.server.ServerApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Utils {
    public interface DownloadListener{
        void startDownload(int max);
        void progress(int value, int max);
        void finishDownload();
        void failedDownload();
    }
    public static boolean isDebugModeOn = false;
    public static final String PHONE_PATTERN = "^[9876]\\d{9}$";
    public static List<String> titles;
    public static ArrayList<HashMap<String, Object>> filterOptions;
    static {
        titles = Arrays.asList("Home", "My Classes", "My OTS");
    }
    public static DisplayMetrics getMetrics(Activity context) {
        if(context == null){
            return null;
        }
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        return outMetrics;
    }
    public static void setUserProperties(Context context, JSONObject data){
        Preferences.put(context, Preferences.KEY_STUDENT_ID, data.optInt("StudentID"));
        Preferences.put(context, Preferences.KEY_COURSE_ID, data.optInt("CourseID"));
        Preferences.put(context, Preferences.KEY_SPEC_ID, data.optInt("SpecializationID"));
        Preferences.put(context, Preferences.KEY_STUDENT_CODE, data.optString("StudentCode"));
        Preferences.put(context, Preferences.KEY_STUDENT_NAME, data.optString("StudentName"));
        Preferences.put(context, Preferences.KEY_STUDENT_EMAIL, data.optString("EmailID"));
        Preferences.put(context, Preferences.KEY_STUDENT_PHONE, data.optString("MobileNo"));
        Preferences.put(context, Preferences.KEY_COURSE_NAME, data.optString("CourseName"));
        Preferences.put(context, Preferences.KEY_SPEC_NAME, data.optString("SpecName"));
        Preferences.put(context, Preferences.KEY_STUDENT_ROLLNO, data.optString("RollNo"));
        Preferences.put(context, Preferences.KEY_STUDENT_PROFILE_PIC, data.optString("ProfilePic"));
        Preferences.put(context, Preferences.KEY_STUDENT_SESSION_ID, data.optInt("StudentSessionID"));
    }

    public static boolean isValidEmailAddress(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidString(String str){
        return !TextUtils.isEmpty(str);
    }

    public static boolean isValidMobileNumber(String phone) {
        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    public static boolean isValidPassword(String password){
        return !TextUtils.isEmpty(password) && password.length() >= 5;
    }

    public static boolean isLoginCompleted(Context context){
        return Preferences.get(context, Preferences.KEY_IS_LOGIN_COMPLTED, false);
    }

    public static String getPhone(Context context){
        return Preferences.get(context, Preferences.KEY_STUDENT_PHONE, "");
    }
    public static String getEmail(Context context){
        return Preferences.get(context, Preferences.KEY_STUDENT_EMAIL, "");
    }
    public static String getName(Context context){
        return Preferences.get(context, Preferences.KEY_STUDENT_NAME, "");
    }
    public static int getStudentId(Context context){
        return Preferences.get(context, Preferences.KEY_STUDENT_ID, 0);
    }
    public static String getStudentCode(Context context){
        return Preferences.get(context, Preferences.KEY_STUDENT_CODE, "");
    }
    public static String getStudentSpec(Context context){
        return Preferences.get(context, Preferences.KEY_SPEC_NAME, "");
    }
    public static int getStudentSpecID(Context context){
        return Preferences.get(context, Preferences.KEY_SPEC_ID, 0);
    }
    public static boolean isConnectedToInternet(Context context) {
        if (context == null)
            return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
    public static boolean saveObject(Context context, Object obj, String fileName) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir(), fileName)));
            oos.writeObject(obj);
            oos.flush();
            oos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void deleteObject(Context context, String fileName) {
        try {
            new File(context.getFilesDir(), fileName).delete();
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    public static Object getObject(Context context, String fileName) {
        Object obj = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(context.getFilesDir(), fileName)));
            obj = ois.readObject();
            ois.close();
            return obj;
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
            return obj;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            return obj;
        } catch (IOException e3) {
            e3.printStackTrace();
            return obj;
        } catch (ClassNotFoundException e4) {
            e4.printStackTrace();
            return obj;
        } catch (Exception e5) {
            e5.printStackTrace();
            return obj;
        }
    }
    public static boolean isActivityDestroyed(Activity context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (context == null || context.isDestroyed())
                    return true;
            }
            if (context == null || context.isFinishing()) {
                return true;
            }
        } catch (Throwable e) {
            if (isDebugModeOn) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
    public static boolean downloadFile(String downloadPath, String savePath, DownloadListener listener) {
        BufferedInputStream ios = null;
        FileOutputStream fos = null;
        File file = new File(savePath);
        try {
            file.delete();
            URL url = new URL(downloadPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            ios = new BufferedInputStream(connection.getInputStream());
            file.getParentFile().mkdirs();
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int len = 0;
            int downloadLength = 0;
            int max = connection.getContentLength();
            listener.startDownload(max);
            while ((len = ios.read(buffer)) != -1) {
                downloadLength = downloadLength + len;
                fos.write(buffer, 0, len);
                listener.progress(downloadLength, max);
            }
            fos.flush();
            fos.close();
            ios.close();
            listener.finishDownload();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
            if (file != null && file.exists()) {
                file.delete();
            }
            listener.failedDownload();
        }
        return false;
    }
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static String getTime(long remainingTime){
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(remainingTime),
                TimeUnit.MILLISECONDS.toSeconds(remainingTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingTime))
        );
    }

    public static boolean isLollipop(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void openHome(Activity activity){
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.finish();
    }
    public static void openMyPackages(Activity activity, int screen){
        Intent intent = new Intent(activity, MyPackageActivity.class);
        intent.putExtra("position", screen);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void openMyPackagesNewTask(Activity activity, int screen){
        Intent intent = new Intent(activity, MyPackageActivity.class);
        intent.putExtra("position", screen);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity);
        stackBuilder.addNextIntentWithParentStack(intent);

        Intent[] intents = stackBuilder.getIntents();
        if(intents.length > 1){
            intents[0].setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intents[1].setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        stackBuilder.startActivities();
        activity.finish();
    }
    public static void openMyProfile(Activity activity){
        Intent intent = new Intent(activity, NewProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void openMyProfileNewTask(Activity activity){
        Intent intent = new Intent(activity, NewProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity);
        stackBuilder.addNextIntentWithParentStack(intent);
        Intent[] intents = stackBuilder.getIntents();
        if(intents.length > 1){
            intents[0].setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intents[1].setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        stackBuilder.startActivities();
        activity.finish();
    }

    public static String getFollowUsUrl(String type){
        switch (type){
            case "facebook":
                return "https://www.facebook.com/zonetechjpr";
            case "telegram":
                return "https://t.me/zonetechjaipur";
            case "youtube":
                return "https://www.youtube.com/channel/UCJrTJ_3Lnr0lzGesdv_ANUA";
            case "instagram":
                return "https://www.instagram.com/zonetechjaipur/";
            case "linkedin":
                return "https://www.linkedin.com/in/zonetech/";
            case "twitter":
                return "https://twitter.com/zonetechjaipur";
        }
        return "";
    }
    public static String getPhoneDetail() {
        String phoneDetail = " {" +
                "\"model\": \"" + Build.MODEL + "\", " +
                "\"manufacturer\": \"" + Build.MANUFACTURER + "\", " +
                "\"brand\": \"" + Build.BRAND + "\", " +
                "\"device\": \"" + Build.DEVICE + "\", " +
                "\"sdk\": \"" + Build.VERSION.SDK_INT + "\"" +
                "}";
        return phoneDetail;
    }

    public static void getCourseSpec(Context context, CompleteListener completeListener) {
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "GetCourseSpec", null, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                JSONObject data = response.optJSONObject("Body");
                JSONArray courseData = data.optJSONArray("Course");
                filterOptions = new ArrayList<>();
                for (int i = 0 ; i < courseData.length() ; i++){
                    JSONObject object = courseData.optJSONObject(i);
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("courseName", object.optString("CourseName"));
                    item.put("courseId", object.optInt("CourseID"));
                    filterOptions.add(item);
                }
                if(completeListener != null){
                    completeListener.success(response);
                }
            }

            @Override
            public void error(String error) {
            }
        });
    }
    public static Bitmap getBitmap(String filePath, int reqWidth,
                                   int reqHeight) throws FileNotFoundException {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];
            options.inSampleSize = inSampleSize;

            try {
                bitmap = BitmapFactory.decodeFile(filePath, options);
            } catch (Throwable e) {
                if (Utils.isDebugModeOn) {
                    e.printStackTrace();
                }
                options.inSampleSize = 4;
                try {
                    bitmap = BitmapFactory.decodeFile(filePath, options);
                } catch (Throwable e1) {
                    if (Utils.isDebugModeOn) {
                        e1.printStackTrace();
                    }
                    options.inSampleSize = 8;
                    try {
                        bitmap = BitmapFactory.decodeFile(filePath, options);
                    } catch (Throwable e2) {
                        if (Utils.isDebugModeOn) {
                            e2.printStackTrace();
                        }
                        options.inSampleSize = 16;
                        try {
                            bitmap = BitmapFactory.decodeFile(filePath, options);
                        } catch (Throwable e3) {
                            e3.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth
            , int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (reqHeight == 0 || reqWidth == 0)
                return inSampleSize;

            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    public static boolean copyFile(String srcPath, String desPath){
        try {
            FileInputStream fis = new FileInputStream(srcPath);
            File file = new File(desPath);
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            final int BUFFER_SIZE = 4096;
            byte buffer[] = new byte[BUFFER_SIZE];
            int length = 0;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            fos.flush();
            fos.close();
            return true;
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static int getStudentSessionId(Context context){
        return Preferences.get(context, Preferences.KEY_STUDENT_SESSION_ID,  getStudentId(context));
    }
}
