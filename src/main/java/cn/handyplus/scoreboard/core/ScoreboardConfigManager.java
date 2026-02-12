package cn.handyplus.scoreboard.core;

import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.util.HandyConfigUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.scoreboard.constants.ScoreboardConstants;
import cn.handyplus.scoreboard.param.ScoreboardConfig;
import cn.handyplus.scoreboard.util.ConfigUtil;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        // 提前获取玩家信息，避免重复调用
        String worldName = player.getWorld().getName();

        // 按优先级从高到低排序遍历(使用合并后的优先级,取外部插件设置的最大值)
        Optional<ScoreboardConfig> first = ScoreboardConstants.SCOREBOARD_CONFIGS.values().stream()
                .filter(config -> {
                    // 先做轻量级检查：世界限制
                    List<String> worlds = config.getWorlds();
                    if (CollUtil.isNotEmpty(worlds)) {
                        // 如果配置了 [ALL] 则表示所有世界都允许
                        if (!CollUtil.contains(worlds, ScoreboardConstants.ALL) && !CollUtil.contains(worlds, worldName)) {
                            return false;
                        }
                    }
                    // 检查玩家是否有对应权限
                    return player.hasPermission(config.getPermission());
                })
                // 过滤后再计算优先级并排序，减少计算次数
                .map(config -> {
                    // 缓存优先级，避免重复计算
                    int priority = config.getMergedPriority(player.getUniqueId());
                    return new AbstractMap.SimpleEntry<>(config, priority);
                })
                // 过滤出优先级大于等于 0 的配置
                .filter(entry -> entry.getValue() >= 0)
                // 按优先级降序排序
                .sorted(Comparator.comparingInt((Map.Entry<ScoreboardConfig, Integer> entry) -> entry.getValue()).reversed())
                .map(Map.Entry::getKey)
                .findFirst();
        MessageUtil.sendConsoleDebugMessage("getPlayerScoreboardConfig: 玩家=" + player.getName() + ", 计分板=" + first.map(ScoreboardConfig::getKey).orElse("无"));
        return first;
    }

}
