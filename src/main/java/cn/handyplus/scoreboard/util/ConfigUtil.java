package cn.handyplus.scoreboard.util;

import cn.handyplus.lib.command.HandyCommandWrapper;
import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.util.HandyConfigUtil;
import cn.handyplus.scoreboard.constants.ScoreboardConstants;
import cn.handyplus.scoreboard.core.ScoreboardConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

/**
 * 配置
 *
 * @author handy
 */
public class ConfigUtil {

    public static FileConfiguration SCOREBOARD_CONFIG;
    public static FileConfiguration TAB_LIST_CONFIG;

    /**
     * 初始化加载文件
     */
    public static void init() {
        // 加载 config
        HandyConfigUtil.loadConfig();
        HandyConfigUtil.loadLangConfig(false);
        SCOREBOARD_CONFIG = HandyConfigUtil.load("scoreboard.yml");
        TAB_LIST_CONFIG = HandyConfigUtil.load("tablist.yml");
        // 加载计分板配置
        ScoreboardConfigManager.loadConfigs();
        Set<String> commandAliasKey = HandyConfigUtil.getKey(BaseConstants.CONFIG, "commandAlias");
        for (String key : commandAliasKey) {
            ScoreboardConstants.COMMAND_MAP.put(key, BaseConstants.CONFIG.getString("commandAlias." + key));
            // 动态注入命令
            HandyCommandWrapper.injectCommand(key);
        }
    }

}
