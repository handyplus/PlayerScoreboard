package cn.handyplus.template.util;

import cn.handyplus.lib.util.HandyConfigUtil;

/**
 * 配置
 *
 * @author handy
 */
public class ConfigUtil {

    /**
     * 初始化加载文件
     */
    public static void init() {
        // 加载 config
        HandyConfigUtil.loadConfig();
        HandyConfigUtil.loadLangConfig(false);
    }

}