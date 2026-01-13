package cn.handyplus.scoreboard.util;

import cn.handyplus.lib.util.HandyConfigUtil;
import cn.handyplus.scoreboard.core.ScoreboardConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 配置
 *
 * @author handy
 */
public class ConfigUtil {

    public static FileConfiguration SCOREBOARD_CONFIG;

    /**
     * 初始化加载文件
     */
    public static void init() {
        // 加载 config
        HandyConfigUtil.loadConfig();
        HandyConfigUtil.loadLangConfig(false);
        SCOREBOARD_CONFIG = HandyConfigUtil.load("scoreboard.yml");
        // 加载计分板配置
        ScoreboardConfigManager.loadConfigs();
    }

}