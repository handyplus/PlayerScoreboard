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
import org.bukkit.scoreboard.ScoreboardManager;

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
        Objective oldObjective = scoreboard.getObjective(ScoreboardConstants.OBJECTIVE_NAME);
        if (oldObjective != null) {
            oldObjective.unregister();
        }
        // 标题和内容行的变量解析(使用合并后的内容,包含外部插件扩展)
        String title = PlaceholderApiUtil.set(player, scoreboardConfig.getMergedTitle(player.getUniqueId()));
        List<String> lines = PlaceholderApiUtil.set(player, scoreboardConfig.getMergedLines(player.getUniqueId()));

        // 根据是否为 Paper 1.16+ 选择不同的实现(Component API 需要 Paper 1.16.5+)
        if (isPaperComponent()) {
            updateScoreboardContentPaper(scoreboard, title, lines);
        } else {
            updateScoreboardContentSpigot(scoreboard, title, lines);
        }
    }

    /**
     * Paper 服务端更新计分板内容(使用 Component API)
     */
    private static void updateScoreboardContentPaper(Scoreboard scoreboard, String title, List<String> lines) {
        // 创建新目标(使用 Component API)
        Objective objective = scoreboard.registerNewObjective(ScoreboardConstants.OBJECTIVE_NAME, Criteria.DUMMY, LegacyComponentUtil.toComponent(title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        // 设置内容行
        boolean showSerialNo = BaseConstants.CONFIG.getBoolean("showSerialNo") && BaseConstants.VERSION_ID >= VersionCheckEnum.V_1_20_3.getVersionId();
        int score = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // 用索引生成唯一 entry，避免相同内容被合并
            String entry = String.valueOf(i);
            Score lineScore = objective.getScore(entry);
            lineScore.customName(LegacyComponentUtil.toComponent(line));
            lineScore.setScore(score);
            // 不显示数字时，使用空白格式隐藏（需要 Paper 1.20.3+）
            if (!showSerialNo) {
                lineScore.numberFormat(NumberFormat.blank());
            }
            score--;
        }
    }

    /**
     * Spigot 服务端更新计分板内容(使用传统字符串 API)
     */
    @SuppressWarnings("deprecation")
    private static void updateScoreboardContentSpigot(Scoreboard scoreboard, String title, List<String> lines) {
        // 创建新目标(标题在1.13-最大32字符)
        Objective objective = scoreboard.registerNewObjective(ScoreboardConstants.OBJECTIVE_NAME, "dummy", BaseUtil.replaceChatColor(truncateTitle(title)));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
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
