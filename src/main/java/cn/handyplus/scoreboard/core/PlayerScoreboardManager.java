package cn.handyplus.scoreboard.core;

import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.internal.ServerTypeEnum;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.scoreboard.hook.PlaceholderApiUtil;
import cn.handyplus.scoreboard.param.ScoreboardConfig;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计分板管理器
 *
 * @author handy
 */
public class PlayerScoreboardManager {

    /**
     * 玩家计分板缓存
     */
    private static final Map<UUID, Scoreboard> PLAYER_SCOREBOARDS = new ConcurrentHashMap<>();

    /**
     * 玩家计分板启用状态
     */
    private static final Map<UUID, Boolean> PLAYER_SCOREBOARD_ENABLED = new ConcurrentHashMap<>();

    /**
     * 计分板目标名称
     */
    private static final String OBJECTIVE_NAME = "psd_board";

    private PlayerScoreboardManager() {
    }

    /**
     * 为玩家创建计分板
     *
     * @param player 玩家
     */
    public static void createScoreboard(Player player) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        PLAYER_SCOREBOARDS.put(player.getUniqueId(), scoreboard);
        PLAYER_SCOREBOARD_ENABLED.putIfAbsent(player.getUniqueId(), true);
        player.setScoreboard(scoreboard);
    }

    /**
     * 更新玩家计分板
     *
     * @param player 玩家
     */
    public static void updateScoreboard(Player player) {
        // 获取或创建计分板
        UUID uuid = player.getUniqueId();
        if (PLAYER_SCOREBOARDS.get(uuid) == null) {
            createScoreboard(player);
        }
        // 玩家是否开启计分板
        Boolean isScoreboardEnabled = PLAYER_SCOREBOARD_ENABLED.getOrDefault(player.getUniqueId(), true);
        // 获取计分板配置
        Optional<ScoreboardConfig> configOpt = ScoreboardConfigManager.getPlayerScoreboardConfig(player);
        if (!configOpt.isPresent() || !isScoreboardEnabled) {
            // 如果没有配置则移除计分板内容
            hideScoreboard(player.getUniqueId());
        } else {
            // 更新计分板内容
            updateScoreboardContent(player, configOpt.get());
        }
    }

    /**
     * 更新计分板内容
     *
     * @param player           玩家
     * @param scoreboardConfig 计分板配置
     */
    private static void updateScoreboardContent(Player player, ScoreboardConfig scoreboardConfig) {
        Scoreboard scoreboard = PLAYER_SCOREBOARDS.get(player.getUniqueId());
        // 移除旧的目标
        Objective oldObjective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (oldObjective != null) {
            oldObjective.unregister();
        }
        // 标题和内容行的变量解析(使用合并后的内容,包含外部插件扩展)
        String title = PlaceholderApiUtil.set(player, scoreboardConfig.getMergedTitle(player.getUniqueId()));
        List<String> lines = PlaceholderApiUtil.set(player, scoreboardConfig.getMergedLines(player.getUniqueId()));
        // 创建新目标(标题在1.13-最大32字符)
        Objective objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy", BaseUtil.replaceChatColor(truncateTitle(title)));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        // 设置内容行
        boolean showSerialNo = BaseConstants.CONFIG.getBoolean("showSerialNo") && ServerTypeEnum.PAPER.equals(ServerTypeEnum.getServerType()) && BaseConstants.VERSION_ID >= VersionCheckEnum.V_1_20_3.getVersionId();
        int score = lines.size();
        for (String line : lines) {
            // 内容行在1.13-最大30字符
            Score lineScore = objective.getScore(BaseUtil.replaceChatColor(truncateLine(line)));
            lineScore.setScore(score);
            // 不显示数字时，使用空白格式隐藏（需要 Paper 1.20.3+）
            if (!showSerialNo) {
                lineScore.numberFormat(NumberFormat.blank());
            }
            score--;
        }
    }

    /**
     * 截断标题到版本允许的最大长度
     * 1.13- 最大32字符
     *
     * @param title 标题
     * @return 截断后的标题
     */
    private static String truncateTitle(String title) {
        if (BaseConstants.VERSION_ID < VersionCheckEnum.V_1_13.getVersionId() && title.length() > 32) {
            return title.substring(0, 32);
        }
        return title;
    }

    /**
     * 截断内容行到版本允许的最大长度
     * 1.13- 最大30字符
     *
     * @param line 内容行
     * @return 截断后的内容行
     */
    private static String truncateLine(String line) {
        if (BaseConstants.VERSION_ID < VersionCheckEnum.V_1_13.getVersionId() && line.length() > 30) {
            return line.substring(0, 30);
        }
        return line;
    }

    /**
     * 移除玩家计分板
     *
     * @param player 玩家
     */
    public static void removeScoreboard(Player player) {
        PLAYER_SCOREBOARDS.remove(player.getUniqueId());
        PLAYER_SCOREBOARD_ENABLED.remove(player.getUniqueId());
    }

    /**
     * 切换玩家计分板显示状态
     *
     * @param player 玩家
     * @return 切换后的状态
     */
    public static boolean toggleScoreboard(Player player) {
        UUID playerUuid = player.getUniqueId();
        boolean enabled = !PLAYER_SCOREBOARD_ENABLED.getOrDefault(playerUuid, true);
        PLAYER_SCOREBOARD_ENABLED.put(playerUuid, enabled);
        if (!enabled) {
            // 隐藏计分板
            hideScoreboard(playerUuid);
        } else {
            // 显示计分板
            updateScoreboard(player);
        }
        return enabled;
    }

    /**
     * 隐藏玩家计分板
     *
     * @param playerUuid 玩家 UUID
     */
    private static void hideScoreboard(UUID playerUuid) {
        Scoreboard scoreboard = PLAYER_SCOREBOARDS.get(playerUuid);
        if (scoreboard == null) {
            return;
        }
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective != null) {
            objective.unregister();
        }
    }

    /**
     * 获取玩家的计分板
     *
     * @param playerUuid 玩家 UUID
     * @return 玩家的计分板,如果不存在则返回null
     */
    public static Scoreboard getScoreboard(UUID playerUuid) {
        return PLAYER_SCOREBOARDS.get(playerUuid);
    }

    /**
     * 同步玩家的计分板Team到所有在线玩家
     * 用于让其他玩家看到该玩家的Tab前缀/后缀
     *
     * @param player 目标玩家
     */
    public static void syncScoreboardToAll(Player player) {
        Scoreboard scoreboard = PLAYER_SCOREBOARDS.get(player.getUniqueId());
        if (scoreboard == null) {
            return;
        }
        // 将该玩家的 Team 信息同步到所有在线玩家的计分板上
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) {
                continue;
            }
            Scoreboard targetBoard = PLAYER_SCOREBOARDS.get(onlinePlayer.getUniqueId());
            if (targetBoard == null) {
                continue;
            }
            // 复制 Team 信息
            Team sourceTeam = scoreboard.getTeam(player.getName());
            if (sourceTeam != null) {
                Team targetTeam = targetBoard.getTeam(player.getName());
                if (targetTeam == null) {
                    targetTeam = targetBoard.registerNewTeam(player.getName());
                }
                targetTeam.setPrefix(sourceTeam.getPrefix());
                targetTeam.setSuffix(sourceTeam.getSuffix());
                targetTeam.addEntry(player.getName());
            }
        }
    }

    /**
     * 同步所有在线玩家的Team信息到指定玩家
     * 用于玩家刚登录时，获取其他玩家的Tab前缀/后缀
     *
     * @param player 目标玩家
     */
    public static void syncAllToPlayer(Player player) {
        Scoreboard targetBoard = PLAYER_SCOREBOARDS.get(player.getUniqueId());
        if (targetBoard == null) {
            return;
        }
        // 将所有在线玩家的 Team 信息同步到该玩家的计分板上
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) {
                continue;
            }
            Scoreboard sourceBoard = PLAYER_SCOREBOARDS.get(onlinePlayer.getUniqueId());
            if (sourceBoard == null) {
                continue;
            }
            // 复制 Team 信息
            Team sourceTeam = sourceBoard.getTeam(onlinePlayer.getName());
            if (sourceTeam != null) {
                Team targetTeam = targetBoard.getTeam(onlinePlayer.getName());
                if (targetTeam == null) {
                    targetTeam = targetBoard.registerNewTeam(onlinePlayer.getName());
                }
                targetTeam.setPrefix(sourceTeam.getPrefix());
                targetTeam.setSuffix(sourceTeam.getSuffix());
                targetTeam.addEntry(onlinePlayer.getName());
            }
        }
    }

}
