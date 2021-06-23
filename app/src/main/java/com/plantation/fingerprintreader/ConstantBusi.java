/**
 * ConstantBusi.
 * <p>
 * V1.0.0.
 */
package com.plantation.fingerprintreader;

import android.content.Context;

import com.plantation.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantBusi {

    // 图像后缀bmp
    public final static String IMAGE_SUFFIX_BMP = ".bmp";
    // 特征值文件后缀fea
    public final static String FEATURE_SUFFIX_FEA = ".fea";
    // 图像后缀 PNG.
    public final static String IMAGE_SUFFIX_PNG = ".png";
    // 图像后缀 RAW.
    public final static String IMAGE_SUFFIX_RAW = ".raw";
    // 指纹图像存放目录
    public final static String FINGER_IMAGE_FOLDER = "finger_image";
    // 采集的指纹特征值存放目录
    public final static String FINGER_FEATURE_FOLDER = "finger_feature";
    // 指纹采集信息存放目录
    public final static String FINGER_COLLECT_INFO_FOLDER = "finger_collect_info";
    public static Context context;
    /**
     * 手指下拉对象集合.
     */
    @SuppressWarnings("serial")
    public static final List<Dict> FPSELECTLIST = new ArrayList<Dict>() {
        {
            add(new Dict("0", context.getString(R.string.collect_finger)));
            add(new Dict("R1", context.getString(R.string.finger_R1)));
            add(new Dict("R2", context.getString(R.string.finger_R2)));
            add(new Dict("R3", context.getString(R.string.finger_R3)));
            add(new Dict("R4", context.getString(R.string.finger_R4)));
            add(new Dict("R5", context.getString(R.string.finger_R5)));
            add(new Dict("L1", context.getString(R.string.finger_L1)));
            add(new Dict("L2", context.getString(R.string.finger_L2)));
            add(new Dict("L3", context.getString(R.string.finger_L3)));
            add(new Dict("L4", context.getString(R.string.finger_L4)));
            add(new Dict("L5", context.getString(R.string.finger_L5)));

        }
    };
    /**
     * 指位信息.
     */
    @SuppressWarnings("serial")
    public static final Map<String, String> FPMAP = new HashMap<String, String>() {
        {
            put("R1", context.getString(R.string.finger_R1));
            put("R2", context.getString(R.string.finger_R2));
            put("R3", context.getString(R.string.finger_R3));
            put("R4", context.getString(R.string.finger_R4));
            put("R5", context.getString(R.string.finger_R5));
            put("L1", context.getString(R.string.finger_L1));
            put("L2", context.getString(R.string.finger_L2));
            put("L3", context.getString(R.string.finger_L3));
            put("L4", context.getString(R.string.finger_L4));
            put("L5", context.getString(R.string.finger_L5));

        }
    };

    static {

        context = PrjApp.appContext;

    }

    // 单态类.
    private ConstantBusi() {
    }

    /**
     * 手指信息.
     */
    public enum FP_INFO {
        R1("R1", context.getString(R.string.finger_R1), R.raw.r1), R2("R2",
                context.getString(R.string.finger_R2), R.raw.r2), R3("R3",
                context.getString(R.string.finger_R3), R.raw.r3), R4("R4",
                context.getString(R.string.finger_R4), R.raw.r4), R5("R5",
                context.getString(R.string.finger_R5), R.raw.r5), L1("L1",
                context.getString(R.string.finger_L1), R.raw.l1), L2("L2",
                context.getString(R.string.finger_L2), R.raw.l2), L3("L3",
                context.getString(R.string.finger_L3), R.raw.l3), L4("L4",
                context.getString(R.string.finger_L4), R.raw.l4), L5("L5",
                context.getString(R.string.finger_L5), R.raw.l5);

        private final String id;
        private final String name;
        private final int voiceFileName;

        FP_INFO(String id, String name, int voiceFileName) {
            this.id = id;
            this.name = name;
            this.voiceFileName = voiceFileName;
        }

        public static String getValueById(String id) {
            for (FP_INFO fpInfo : FP_INFO.values()) {
                if (fpInfo.getId().equals(id)) {
                    return fpInfo.getName();
                }
            }
            return null;
        }

        public static int getVoiceFileNameById(String id) {
            for (FP_INFO fpInfo : FP_INFO.values()) {
                if (fpInfo.getId().equals(id)) {
                    return fpInfo.getVoiceFileName();
                }
            }
            return 0;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getVoiceFileName() {
            return voiceFileName;
        }
    }
}
