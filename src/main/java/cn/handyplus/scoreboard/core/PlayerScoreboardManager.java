package cn.handyplus.scoreboard.core;

import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.internal.HandySchedulerUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.scoreboard.constants.ScoreboardConstants;
import cn.handyplus.scoreboard.hook.PlaceholderApiUtil;
import cn.handyplus.scoreboard.param.ScoreboardConfig;
import cn.handyplus.scoreboard.util.LegacyComponentUtil;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

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

    private PlayerScoreboardManager() {
    }

    /**
     * 为玩家创建计分板
     *
     * @param player 玩家
     */
    public static void createScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        Scoreboard scoreboard = PLAYER_SCOREBOARDS.computeIfAbsent(uuid, k -> Bukkit.getScoreboardManager().getNewScoreboard());
        PLAYER_SCOREBOARD_ENABLED.putIfAbsent(uuid, true);
        player.setScoreboard(scoreboard);
    }

    /**
     * 更新玩家计分板
     *
     * @param player 玩家
     */
    public static void updateScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        // 获取或创建计分板
        Scoreboard scoreboard = PLAYER_SCOREBOARDS.get(uuid);
        if (scoreboard == null) {
            createScoreboard(player);
        }
        // 玩家是否开启计分板
        Boolean isScoreboardEnabled = PLAYER_SCOREBOARD_ENABLED.getOrDefault(uuid, true);
        // 获取计分板配置
        Optional<ScoreboardConfig> configOpt = ScoreboardConfigManager.getPlayerScoreboardConfig(player);
        if (!configOpt.isPresent() || !isScoreboardEnabled) {
            // 如果没有配置则移除计分板内容
            hideScoreboard(uuid);
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
        if (scoreboard == null) {
            return;
        }
        // 标题和内容行的变量解析(使用合并后的内容,包含外部插件扩展)
        String title = PlaceholderApiUtil.set(player, scoreboardConfig.getMergedTitle(player.getUniqueId()));
        List<String> lines = PlaceholderApiUtil.set(player, scoreboardConfig.getMergedLines(player.getUniqueId()));

        // 根据版本选择不同的实现
        if (isPaperScoreCustomName()) {
            // Paper 1.20.4+ 支持 Score.customName 和 Component Objective（无闪烁更新）
            updateScoreboardContentPaper(scoreboard, title, lines);
        } else {
            // 其他版本使用传统字符串 API（需要重建 Objective）
            Objective oldObjective = scoreboard.getObjective(ScoreboardConstants.OBJECTIVE_NAME);
            if (oldObjective != null) {
                oldObjective.unregister();
            }
            updateScoreboardContentLegacy(scoreboard, title, lines);
        }
    }

    /**
     * Paper 1.20.4+ 更新计分板内容(使用 Score.customName API，无闪烁)
     */
    private static void updateScoreboardContentPaper(Scoreboard scoreboard, String title, List<String> lines) {
        // 获取或创建 Objective（不删除重建，避免闪烁）
        Objective objective = scoreboard.getObjective(ScoreboardConstants.OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.registerNewObjective(ScoreboardConstants.OBJECTIVE_NAME, Criteria.DUMMY, LegacyComponentUtil.toComponent(title));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            // 更新标题
            objective.displayName(LegacyComponentUtil.toComponent(title));
        }

        // 清理超出新行数的旧 entry
        for (int i = lines.size(); i < 20; i++) {
            scoreboard.resetScores(String.valueOf(i));
        }

        // 设置内容行（使用固定的 score 值，避免 score 变化导致重复显示）
        boolean showSerialNo = BaseConstants.CONFIG.getBoolean("showSerialNo");
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String entry = String.valueOf(i);
            Score lineScore = objective.getScore(entry);
            lineScore.customName(LegacyComponentUtil.toComponent(line));
            // score 用固定值：20-i，这样无论 lines 多少行，同一个 entry 的 score 始终不变
            lineScore.setScore(20 - i);
            if (!showSerialNo) {
                lineScore.numberFormat(NumberFormat.blank());
            }
        }
    }

    /**
     * 传统方式更新计分板内容(Paper 1.20.3- 和 Spigot)
     */
    @SuppressWarnings("deprecation")
    private static void updateScoreboardContentLegacy(Scoreboard scoreboard, String title, List<String> lines) {
        // 创建新目标(标题在1.13-最大32字符)
        Objective objective = scoreboard.registerNewObjective(ScoreboardConstants.OBJECTIVE_NAME, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(BaseUtil.replaceChatColor(title));
        // 设置内容行
        int score = lines.size();
        for (String line : lines) {
            // 内容行在1.13- 最大 30 字符
            Score lineScore = objective.getScore(BaseUtil.replaceChatColor(truncateLine(line)));
            lineScore.setScore(score);
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
     * 获取玩家的计分板
     *
     * @param playerUuid 玩家 UUID
     * @return 玩家的计分板,如果不存在则返回null
     */
    protected static Scoreboard getScoreboard(UUID playerUuid) {
        return PLAYER_SCOREBOARDS.get(playerUuid);
    }

    /**
     * 判断是否支持 Component API (Paper 1.16.5+)
     */
    protected static boolean isPaperComponent() {
        return (HandySchedulerUtil.isFolia() || HandySchedulerUtil.isPaper()) && BaseConstants.VERSION_ID >= VersionCheckEnum.V_1_16.getVersionId();
    }

    /**
     * 判断是否支持 Score.customName API (Paper 1.20.4+)
     */
    private static boolean isPaperScoreCustomName() {
        return isPaperComponent() && BaseConstants.VERSION_ID >= VersionCheckEnum.V_1_20_4.getVersionId();
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
        Objective objective = scoreboard.getObjective(ScoreboardConstants.OBJECTIVE_NAME);
        if (objective != null) {
            objective.unregister();
        }
    }

    /**
     * 截断内容行到版本允许的最大长度
     * 1.13- 最大 30 字符
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

}
