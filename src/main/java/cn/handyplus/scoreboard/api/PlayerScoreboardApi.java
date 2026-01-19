package cn.handyplus.scoreboard.api;

import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.core.MapUtil;
import cn.handyplus.scoreboard.constants.ScoreboardConstants;
import cn.handyplus.scoreboard.core.PlayerScoreboardManager;
import cn.handyplus.scoreboard.param.ExternalLine;
import cn.handyplus.scoreboard.param.ScoreboardConfig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    // ==================== 计分板内容相关 API ====================

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
        PlayerScoreboardManager.updateScoreboard(player);
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
        PlayerScoreboardManager.updateScoreboard(player);
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
        PlayerScoreboardManager.updateScoreboard(player);
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
                PlayerScoreboardManager.updateScoreboard(player);
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
            PlayerScoreboardManager.updateScoreboard(player);
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

    // ==================== Team 相关 API ====================

    /**
     * 设置玩家在Tab列表中的前缀
     * 会在玩家的计分板上创建/更新Team
     *
     * @param player 玩家
     * @param prefix 前缀
     */
    public static void setTabPrefix(@NotNull Player player, @NotNull String prefix) {
        setTabPrefixAndSuffix(player, prefix, "");
    }

    /**
     * 设置玩家在Tab列表中的后缀
     * 会在玩家的计分板上创建/更新Team
     *
     * @param player 玩家
     * @param suffix 后缀
     */
    public static void setTabSuffix(@NotNull Player player, @NotNull String suffix) {
        setTabPrefixAndSuffix(player, "", suffix);
    }

    /**
     * 设置玩家在Tab列表中的前缀和后缀
     * 会在玩家的计分板上创建/更新Team
     *
     * @param player 玩家
     * @param prefix 前缀
     * @param suffix 后缀
     */
    public static void setTabPrefixAndSuffix(@NotNull Player player, @NotNull String prefix, @NotNull String suffix) {
        Team team = getOrCreateTeam(player, player.getName());
        if (team != null) {
            team.setPrefix(truncateToVersionLimit(prefix));
            team.setSuffix(truncateToVersionLimit(suffix));
            team.addEntry(player.getName());
        }
        // 同步到所有玩家
        PlayerScoreboardManager.syncScoreboardToAll(player);
    }

    /**
     * 移除玩家的Tab Team
     *
     * @param player 玩家
     */
    public static void removeTabTeam(@NotNull Player player) {
        Scoreboard scoreboard = getScoreboard(player);
        if (scoreboard != null) {
            Team team = scoreboard.getTeam(player.getName());
            if (team != null) {
                team.unregister();
            }
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 获取玩家的计分板
     * 其他插件可以通过此方法获取玩家的计分板,在上面注册Team等
     *
     * @param player 玩家
     * @return 玩家的计分板,如果不存在则返回null
     */
    @Nullable
    private static Scoreboard getScoreboard(@NotNull Player player) {
        return PlayerScoreboardManager.getScoreboard(player.getUniqueId());
    }

    /**
     * 获取或创建玩家计分板上的 Team
     *
     * @param player   玩家
     * @param teamName Team 名称
     * @return Team对象,如果玩家计分板不存在则返回null
     */
    @Nullable
    private static Team getOrCreateTeam(@NotNull Player player, @NotNull String teamName) {
        Scoreboard scoreboard = getScoreboard(player);
        if (scoreboard == null) {
            return null;
        }
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        return team;
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

    /**
     * 根据服务端版本截断字符串到计分板允许的最大长度。
     * 1.13 之前限制 16 字符，1.13 及之后限制 64 字符。
     */
    private static String truncateToVersionLimit(@NotNull String str) {
        int versionId = VersionCheckEnum.getEnum().getVersionId();
        if (versionId < VersionCheckEnum.V_1_13.getVersionId() && str.length() > 16) {
            return str.substring(0, 16);
        }
        if (versionId >= VersionCheckEnum.V_1_13.getVersionId() && str.length() > 64) {
            return str.substring(0, 64);
        }
        return str;
    }

}
