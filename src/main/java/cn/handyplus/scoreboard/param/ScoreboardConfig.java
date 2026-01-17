package cn.handyplus.scoreboard.param;

import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.core.StrUtil;
import cn.handyplus.scoreboard.PlayerScoreboard;
import cn.handyplus.scoreboard.constants.ScoreboardConstants;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 计分板配置对象
 *
 * @author handy
 */
@Data
public class ScoreboardConfig {

    private ScoreboardConfig() {
    }

    /**
     * 配置 key
     */
    private String key;

    /**
     * 计分板内容行
     */
    private List<String> lines;

    /**
     * 计分板标题
     */
    private String title;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 适用世界
     */
    private List<String> worlds;

    /**
     * 权限节点
     */
    private String permission;

    /**
     * 计分板对象
     *
     * @param key   配置 key
     * @param lines 计分板内容行
     * @return 计分板配置对象
     */
    public static ScoreboardConfig of(@NotNull String key, @NotNull List<String> lines) {
        return of(key, lines, null);
    }

    /**
     * 计分板对象
     *
     * @param key   配置 key
     * @param lines 计分板内容行
     * @param title 计分板标题
     * @return 计分板配置对象
     */
    public static ScoreboardConfig of(@NotNull String key, @NotNull List<String> lines, @Nullable String title) {
        return of(key, lines, title, null);
    }

    /**
     * 计分板对象
     *
     * @param key   配置 key
     * @param lines 计分板内容行
     * @param title 计分板标题
     * @return 计分板配置对象
     */
    public static ScoreboardConfig of(@NotNull String key, @NotNull List<String> lines, @Nullable String title, @Nullable Integer priority) {
        return of(key, lines, title, priority, null);
    }

    /**
     * 计分板对象
     *
     * @param key   配置 key
     * @param lines 计分板内容行
     * @param title 计分板标题
     * @return 计分板配置对象
     */
    public static ScoreboardConfig of(@NotNull String key, @NotNull List<String> lines, @Nullable String title, @Nullable Integer priority, @Nullable List<String> worlds) {
        ScoreboardConfig scoreboardConfig = new ScoreboardConfig();
        scoreboardConfig.setKey(key);
        scoreboardConfig.setLines(lines);
        scoreboardConfig.setTitle(title);
        scoreboardConfig.setPriority(priority);
        scoreboardConfig.setWorlds(CollUtil.isEmpty(worlds) ? Collections.singletonList(ScoreboardConstants.ALL) : worlds);
        scoreboardConfig.setPermission(PlayerScoreboard.INSTANCE.getName() + "." + key);
        return scoreboardConfig;
    }

    /**
     * 加载外部配置
     *
     * @param externalConfig 外部配置
     */
    public void loadExternalConfig(ScoreboardConfig externalConfig) {
        this.lines = CollUtil.isNotEmpty(externalConfig.getLines()) ? externalConfig.getLines() : this.lines;
        this.title = StrUtil.isNotEmpty(externalConfig.getTitle()) ? externalConfig.getTitle() : this.title;
        this.priority = externalConfig.getPriority() != null ? externalConfig.getPriority() : this.priority;
        this.worlds = CollUtil.isNotEmpty(externalConfig.getWorlds()) ? externalConfig.getWorlds() : this.worlds;
    }

}
