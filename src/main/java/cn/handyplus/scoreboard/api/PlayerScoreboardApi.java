package cn.handyplus.scoreboard.api;

import cn.handyplus.lib.core.MapUtil;
import cn.handyplus.scoreboard.constants.ScoreboardConstants;
import cn.handyplus.scoreboard.param.ExternalLine;
import cn.handyplus.scoreboard.param.ScoreboardConfig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * 计分板API
 * 供其他插件调用,对计分板进行扩展操作
 * 支持玩家级别的个性化内容(如任务进度等)
 *
 * @author handy
 */
public class PlayerScoreboardApi {

    private PlayerScoreboardApi() {
    }

    /**
     * 设置指定玩家计分板的标题
     *
     * @param plugin        插件实例
     * @param player        玩家
     * @param scoreboardKey 计分板配置 key
     * @param title         标题
     */
    public static void setTitle(@NotNull Plugin plugin, @NotNull Player player, @NotNull String scoreboardKey, @NotNull String title) {
        ScoreboardConfig config = getOrCreateConfig(plugin, player.getUniqueId(), scoreboardKey);
        config.setTitle(title);
    }

    /**
     * 设置指定玩家计分板的内容
     *
     * @param plugin        插件实例
     * @param player        玩家
     * @param scoreboardKey 计分板配置 key
     * @param lines         要追加的行内容
     */
    public static void addLines(@NotNull Plugin plugin, @NotNull Player player, @NotNull String scoreboardKey, @NotNull ExternalLine lines) {
        ScoreboardConfig config = getOrCreateConfig(plugin, player.getUniqueId(), scoreboardKey);
        config.setExternalLine(lines);
    }

    /**
     * 设置指定玩家计分板的优先级
     *
     * @param plugin        插件实例
     * @param player        玩家
     * @param scoreboardKey 计分板配置 key
     * @param priority      优先级(数字越大优先级越高)
     */
    public static void setPriority(@NotNull Plugin plugin, @NotNull Player player, @NotNull String scoreboardKey, int priority) {
        ScoreboardConfig config = getOrCreateConfig(plugin, player.getUniqueId(), scoreboardKey);
        config.setPriority(priority);
    }

    /**
     * 清除该插件注册的指定玩家的指定计分板配置
     *
     * @param plugin        插件实例
     * @param player        玩家
     * @param scoreboardKey 计分板配置 key
     */
    public static void clear(@NotNull Plugin plugin, @NotNull Player player, @NotNull String scoreboardKey) {
        Map<UUID, Map<String, ScoreboardConfig>> pluginConfigs = ScoreboardConstants.SCOREBOARD_EXTERNAL.get(plugin);
        if (pluginConfigs != null) {
            Map<String, ScoreboardConfig> playerConfigs = pluginConfigs.get(player.getUniqueId());
            if (playerConfigs != null) {
                playerConfigs.remove(scoreboardKey);
            }
        }
    }

    /**
     * 清除该插件注册的指定玩家的所有计分板配置
     *
     * @param plugin 插件实例
     * @param player 玩家
     */
    public static void clear(@NotNull Plugin plugin, @NotNull Player player) {
        Map<UUID, Map<String, ScoreboardConfig>> pluginConfigs = ScoreboardConstants.SCOREBOARD_EXTERNAL.get(plugin);
        if (pluginConfigs != null) {
            pluginConfigs.remove(player.getUniqueId());
        }
    }

    /**
     * 清除该插件注册的所有计分板配置
     *
     * @param plugin 插件实例
     */
    public static void clear(@NotNull Plugin plugin) {
        ScoreboardConstants.SCOREBOARD_EXTERNAL.remove(plugin);
    }

    /**
     * 清除指定玩家的所有外部计分板配置(玩家退出时调用)
     *
     * @param playerUuid 玩家 UUID
     */
    public static void clearPlayer(@NotNull UUID playerUuid) {
        for (Map<UUID, Map<String, ScoreboardConfig>> pluginConfigs : ScoreboardConstants.SCOREBOARD_EXTERNAL.values()) {
            pluginConfigs.remove(playerUuid);
        }
    }

    /**
     * 获取或创建插件的玩家计分板配置
     *
     * @param plugin        插件实例
     * @param playerUuid    玩家 UUID
     * @param scoreboardKey 计分板配置 key
     * @return 计分板配置
     */
    private static ScoreboardConfig getOrCreateConfig(Plugin plugin, UUID playerUuid, String scoreboardKey) {
        Map<UUID, Map<String, ScoreboardConfig>> pluginConfigs = ScoreboardConstants.SCOREBOARD_EXTERNAL.computeIfAbsent(plugin, k -> MapUtil.of());
        Map<String, ScoreboardConfig> playerConfigs = pluginConfigs.computeIfAbsent(playerUuid, k -> MapUtil.of());
        ScoreboardConfig config = playerConfigs.get(scoreboardKey);
        if (config == null) {
            config = ScoreboardConfig.of(scoreboardKey, new ArrayList<>());
            playerConfigs.put(scoreboardKey, config);
        }
        return config;
    }

}
