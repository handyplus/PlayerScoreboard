package cn.handyplus.scoreboard.api;

import cn.handyplus.scoreboard.constants.ScoreboardConstants;
import cn.handyplus.scoreboard.param.ScoreboardConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * API
 *
 * @author handy
 */
public class PlayerScoreboardApi {

    private PlayerScoreboardApi() {
    }

    /**
     * 获取对应 key 的计分板数据
     *
     * @param key 配置 key
     */
    public static ScoreboardConfig get(@NotNull String key) {
        ScoreboardConfig scoreboardConfig = ScoreboardConstants.SCOREBOARD_CONFIGS.get(key);
        scoreboardConfig.loadExternalConfig(ScoreboardConstants.SCOREBOARD_EXTERNAL.get(key));
        return scoreboardConfig;
    }

    /**
     * 自定义对应 key 的计分板数据
     *
     * @param key   配置 key
     * @param lines 计分板内容行
     */
    public static void set(@NotNull String key, @NotNull List<String> lines) {
        set(key, lines, null);
    }

    /**
     * 自定义对应 key 的计分板数据
     *
     * @param key   配置 key
     * @param lines 计分板内容行
     * @param title 计分板标题
     */
    public static void set(@NotNull String key, @NotNull List<String> lines, @Nullable String title) {
        set(ScoreboardConfig.of(key, lines, title));
    }

    /**
     * 自定义对应 key 的计分板数据
     *
     * @param scoreboard 自定义的计分板数据
     */
    public static void set(@NotNull ScoreboardConfig scoreboard) {
        ScoreboardConstants.SCOREBOARD_EXTERNAL.put(scoreboard.getKey(), scoreboard);
    }

    /**
     * 自定义对应 key 的计分板数据
     *
     * @param scoreboardList 自定义的计分板数据
     */
    public static void setBatch(@NotNull List<ScoreboardConfig> scoreboardList) {
        for (ScoreboardConfig scoreboard : scoreboardList) {
            ScoreboardConstants.SCOREBOARD_EXTERNAL.put(scoreboard.getKey(), scoreboard);
        }
    }

}
