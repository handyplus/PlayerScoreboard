package cn.handyplus.scoreboard.core;

import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.util.HandyConfigUtil;
import cn.handyplus.scoreboard.constants.ScoreboardConstants;
import cn.handyplus.scoreboard.param.ScoreboardConfig;
import cn.handyplus.scoreboard.util.ConfigUtil;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 计分板配置管理器
 *
 * @author handy
 */
public class ScoreboardConfigManager {

    private ScoreboardConfigManager() {
    }

    /**
     * 加载计分板配置
     */
    public static void loadConfigs() {
        ScoreboardConstants.SCOREBOARD_CONFIGS.clear();
        Set<String> scoreboardKeySet = HandyConfigUtil.getKey(ConfigUtil.SCOREBOARD_CONFIG, "scoreboards");
        for (String scoreboardKey : scoreboardKeySet) {
            List<String> lines = ConfigUtil.SCOREBOARD_CONFIG.getStringList("scoreboards." + scoreboardKey + ".lines");
            String title = ConfigUtil.SCOREBOARD_CONFIG.getString("scoreboards." + scoreboardKey + ".title");
            int priority = ConfigUtil.SCOREBOARD_CONFIG.getInt("scoreboards." + scoreboardKey + ".priority", 0);
            List<String> worlds = ConfigUtil.SCOREBOARD_CONFIG.getStringList("scoreboards." + scoreboardKey + ".worlds");
            ScoreboardConfig config = ScoreboardConfig.of(scoreboardKey, lines, title, priority, worlds);
            ScoreboardConstants.SCOREBOARD_CONFIGS.put(scoreboardKey, config);
        }
    }

    /**
     * 获取玩家应该显示的计分板配置
     *
     * @param player 玩家
     * @return 计分板配置
     */
    public static Optional<ScoreboardConfig> getPlayerScoreboardConfig(Player player) {
        // 按优先级从高到低排序遍历
        return ScoreboardConstants.SCOREBOARD_CONFIGS.values().stream()
                .sorted(Comparator.comparingInt(ScoreboardConfig::getPriority).reversed())
                .filter(config -> {
                    // 注入外部自定义
                    config.loadExternalConfig(ScoreboardConstants.SCOREBOARD_EXTERNAL.get(config.getKey()));
                    // 检查世界限制
                    List<String> worlds = config.getWorlds();
                    if (CollUtil.isNotEmpty(worlds)) {
                        // 如果配置了 [ALL] 则表示所有世界都允许
                        if (!CollUtil.contains(worlds, ScoreboardConstants.ALL) && !CollUtil.contains(worlds, player.getWorld().getName())) {
                            return false;
                        }
                    }
                    // 检查玩家是否有对应权限
                    return player.hasPermission(config.getPermission());
                }).findFirst();
    }

}
