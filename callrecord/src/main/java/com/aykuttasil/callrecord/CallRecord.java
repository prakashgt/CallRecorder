package com.aykuttasil.callrecord;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.aykuttasil.callrecord.helper.PrefsHelper;
import com.aykuttasil.callrecord.receiver.CallRecordReceiver;
import com.aykuttasil.callrecord.service.CallRecordService;

/**
 * Created by aykutasil on 20.10.2016.
 */

public class CallRecord {

    private static final String TAG = CallRecord.class.getSimpleName();

    public static final String PREF_SAVE_FILE = "PrefSaveFile";
    public static final String PREF_CHANGE_FILE_NAME = "PrefChangeFileName";
    public static final String PREF_CHANGE_DIR_NAME = "PrefChangeDirName";
    public static final String PREF_CHANGE_DIR_PATH = "PrefChangeDirPath";

    public static String INTENT_FILE_NAME = "CallRecordFileName";
    public static String INTENT_DIR_NAME = "CallRecordDirName";
    public static String INTENT_DIR_PATH = "CallRecordDirPath";
    public static String INTENT_SHOW_SEED = "CallRecordShowSeed";
    public static String INTENT_AUDIO_SOURCE = "CallRecorAudioSource";
    public static String INTENT_AUDIO_ENCODER = "CallRecordAudioEncode";
    public static String INTENT_OUTPUT_FORMAT = "CallRecordOutputSource";


    private Context mContext;
    private CallRecordReceiver mCallRecordReceiver;
    private Builder mBuilder;
    private Intent intent;

    private CallRecord(Context context, Builder builder) {
        this.mContext = context;
        this.mCallRecordReceiver = new CallRecordReceiver();
        this.mBuilder = builder;
    }

    private CallRecord(Context context, Intent intent) {
        this.mContext = context;
        this.intent = intent;
        this.mCallRecordReceiver = new CallRecordReceiver();
    }

    public static CallRecord initReceiver(Context context) {
        CallRecord callRecord = new Builder(context).build();
        callRecord.startCallReceiver();
        return callRecord;
    }

    public static CallRecord initService(Context context) {
        CallRecord callRecord = new Builder(context).buildService();
        callRecord.startCallRecordService();
        return callRecord;
    }

    public void startCallReceiver() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CallRecordReceiver.ACTION_IN);
        intentFilter.addAction(CallRecordReceiver.ACTION_OUT);

        mCallRecordReceiver.setmBuilder(mBuilder);

        mContext.registerReceiver(mCallRecordReceiver, intentFilter);
    }

    public void stopCallReceiver() {
        try {
            mContext.unregisterReceiver(mCallRecordReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCallRecordService() {

        if (intent == null) {
            try {
                throw new Exception("Intent nesnesi boş. Lütfen buildService() i çalıştırdığınızdan emin olun.");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        intent.setClass(mContext, CallRecordService.class);

        Log.i(TAG, "startService()");

        mContext.startService(intent);
    }

    public void enableSaveFile() {
        PrefsHelper.writePrefBool(mContext, PREF_SAVE_FILE, true);
    }

    public void disableSaveFile() {
        PrefsHelper.writePrefBool(mContext, PREF_SAVE_FILE, false);
    }

    public boolean getStateSaveFile() {
        return PrefsHelper.readPrefBool(mContext, PREF_SAVE_FILE);
    }

    public void changeRecordFileName(String newFileName) {

        if (newFileName == null || newFileName.isEmpty()) {

            try {
                throw new Exception("newFileName can not be empty or null");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        PrefsHelper.writePrefString(mContext, PREF_CHANGE_FILE_NAME, newFileName);
    }

    public String getRecordFileName() {
        if (PrefsHelper.readPrefString(mContext, PREF_CHANGE_FILE_NAME) != null) {
            return PrefsHelper.readPrefString(mContext, PREF_CHANGE_FILE_NAME);
        } else {
            return mBuilder.getRecordFileName();
        }
    }

    public void changeRecordDirName(String newDirName) {

        if (newDirName == null || newDirName.isEmpty()) {

            try {
                throw new Exception("newDirName can not be empty or null");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        PrefsHelper.writePrefString(mContext, PREF_CHANGE_DIR_NAME, newDirName);
    }

    public String getRecordDirName() {
        if (PrefsHelper.readPrefString(mContext, PREF_CHANGE_DIR_NAME) != null) {
            return PrefsHelper.readPrefString(mContext, PREF_CHANGE_DIR_NAME);
        } else {
            return mBuilder.getRecordDirName();
        }
    }

    public void changeRecordDirPath(String newDirPath) {

        if (newDirPath == null || newDirPath.isEmpty()) {

            try {
                throw new Exception("newDirPath can not be empty or null");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        PrefsHelper.writePrefString(mContext, PREF_CHANGE_DIR_PATH, newDirPath);
    }

    public String getRecordDirPath() {
        if (PrefsHelper.readPrefString(mContext, PREF_CHANGE_DIR_PATH) != null) {
            return PrefsHelper.readPrefString(mContext, PREF_CHANGE_DIR_PATH);
        } else {
            return mBuilder.getRecordDirPath();
        }
    }

    public static class Builder {

        private Context context;
        private String recordFileName;
        private String recordDirName;
        private String recordDirPath;
        private int audioSource;
        private int audioEncoder;
        private int outputFormat;
        private boolean showSeed;

        public Builder(Context context) {
            this.context = context;
            this.recordFileName = "Record";
            this.recordDirName = "CallRecord";
            this.recordDirPath = Environment.getExternalStorageDirectory().getPath();
            this.audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
            this.audioEncoder = MediaRecorder.AudioEncoder.AMR_NB;
            this.outputFormat = MediaRecorder.OutputFormat.AMR_NB;
            this.showSeed = true;
        }

        public CallRecord build() {
            CallRecord callRecord = new CallRecord(context, Builder.this);
            callRecord.enableSaveFile();
            return callRecord;
        }

        public CallRecord buildService() {
            Intent intent = new Intent();
            intent.putExtra(INTENT_FILE_NAME, getRecordFileName());
            intent.putExtra(INTENT_DIR_NAME, getRecordDirName());
            intent.putExtra(INTENT_DIR_PATH, getRecordDirPath());
            intent.putExtra(INTENT_AUDIO_ENCODER, getAudioEncoder());
            intent.putExtra(INTENT_AUDIO_SOURCE, getAudioSource());
            intent.putExtra(INTENT_OUTPUT_FORMAT, getOutputFormat());
            intent.putExtra(INTENT_SHOW_SEED, isShowSeed());

            CallRecord callRecord = new CallRecord(context, intent);
            callRecord.enableSaveFile();
            return callRecord;
        }

        public Builder setRecordFileName(String recordFileName) {
            this.recordFileName = recordFileName;
            return this;
        }

        public String getRecordFileName() {
            return recordFileName;
        }

        public Builder setRecordDirName(String recordDirName) {
            this.recordDirName = recordDirName;
            return this;
        }

        public String getRecordDirName() {
            return recordDirName;
        }

        public int getAudioSource() {
            return audioSource;
        }

        public Builder setAudioSource(int audioSource) {
            this.audioSource = audioSource;
            return this;
        }

        public int getAudioEncoder() {
            return audioEncoder;
        }

        public Builder setAudioEncoder(int audioEncoder) {
            this.audioEncoder = audioEncoder;
            return this;
        }

        public int getOutputFormat() {
            return outputFormat;
        }

        public Builder setOutputFormat(int outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        public boolean isShowSeed() {
            return showSeed;
        }

        public Builder setShowSeed(boolean showSeed) {
            this.showSeed = showSeed;
            return this;
        }

        public String getRecordDirPath() {
            return recordDirPath;
        }

        public Builder setRecordDirPath(String recordDirPath) {
            this.recordDirPath = recordDirPath;
            return this;
        }

    }
}
