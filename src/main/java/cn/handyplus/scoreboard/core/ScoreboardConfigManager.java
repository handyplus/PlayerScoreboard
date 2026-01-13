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
            int priority = ConfigUtil.SCOREBOARD_CONFIG.getInt("scoreboards." + scoreboardKey + ".priority", 0);
            String title = ConfigUtil.SCOREBOARD_CONFIG.getString("scoreboards." + scoreboardKey + ".title", "&a计分板");
            List<String> lines = ConfigUtil.SCOREBOARD_CONFIG.getStringList("scoreboards." + scoreboardKey + ".lines");
            List<String> worlds = ConfigUtil.SCOREBOARD_CONFIG.getStringList("scoreboards." + scoreboardKey + ".worlds");
            ScoreboardConfig config = ScoreboardConfig.of(scoreboardKey, priority, title, lines, worlds);
            ScoreboardConstants.SCOREBOARD_CONFIGS.add(config);
        }
        // 按优先级从高到低排序
        ScoreboardConstants.SCOREBOARD_CONFIGS.sort(Comparator.comparingInt(ScoreboardConfig::getPriority).reversed());
    }

    /**
     * 获取玩家应该显示的计分板配置
     *
     * @param player 玩家
     * @return 计分板配置
     */
    public static Optional<ScoreboardConfig> getPlayerScoreboardConfig(Player player) {
        for (ScoreboardConfig config : ScoreboardConstants.SCOREBOARD_CONFIGS) {
            // 检查世界限制
            List<String> worlds = config.getWorlds();
            if (CollUtil.isNotEmpty(worlds)) {
                // 如果配置了 [ALL] 则表示所有世界都允许
                if (!CollUtil.contains(worlds, ScoreboardConstants.ALL) && !CollUtil.contains(worlds, player.getWorld().getName())) {
                    continue;
                }
            }
            // default 配置所有玩家都有权限
            if (ScoreboardConstants.DEFAULT_KEY.equals(config.getKey())) {
                return Optional.of(config);
            }
            // 检查玩家是否有对应权限
            if (player.hasPermission(config.getPermission())) {
                return Optional.of(config);
            }
        }
        return Optional.empty();
    }

}
