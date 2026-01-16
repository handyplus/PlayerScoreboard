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
        PLAYER_SCOREBOARD_ENABLED.put(player.getUniqueId(), true);
        player.setScoreboard(scoreboard);
    }

    /**
     * 更新玩家计分板
     *
     * @param player 玩家
     */
    public static void updateScoreboard(Player player) {
        if (!isScoreboardEnabled(player)) {
            return;
        }
        // 获取或创建计分板
        UUID uuid = player.getUniqueId();
        if (PLAYER_SCOREBOARDS.get(uuid) == null) {
            createScoreboard(player);
        }
        // 获取计分板配置
        Optional<ScoreboardConfig> configOpt = ScoreboardConfigManager.getPlayerScoreboardConfig(player);
        if (!configOpt.isPresent()) {
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
        // 标题和内容行的变量解析
        String title = PlaceholderApiUtil.set(player, scoreboardConfig.getTitle());
        List<String> lines = PlaceholderApiUtil.set(player, scoreboardConfig.getLines());
        // 创建新目标
        Objective objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy", BaseUtil.replaceChatColor(title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        // 设置内容行
        boolean showSerialNo = BaseConstants.CONFIG.getBoolean("showSerialNo");
        int score = lines.size();
        for (String line : lines) {
            org.bukkit.scoreboard.Score lineScore = objective.getScore(BaseUtil.replaceChatColor(line));
            lineScore.setScore(score);
            // 不显示数字时，使用空白格式隐藏（需要  Paper 1.20.3+）
            if (!showSerialNo && ServerTypeEnum.PAPER.equals(ServerTypeEnum.getServerType())
                    && BaseConstants.VERSION_ID >= VersionCheckEnum.V_1_20_3.getVersionId()) {
                lineScore.numberFormat(NumberFormat.blank());
            }
            score--;
        }
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
     * 检查玩家计分板是否启用
     *
     * @param player 玩家
     * @return 是否启用
     */
    public static boolean isScoreboardEnabled(Player player) {
        return PLAYER_SCOREBOARD_ENABLED.getOrDefault(player.getUniqueId(), true);
    }

}
