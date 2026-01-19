package cn.handyplus.scoreboard.param;

import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.core.StrUtil;
import cn.handyplus.scoreboard.PlayerScoreboard;
import cn.handyplus.scoreboard.constants.ScoreboardConstants;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
     * 外部扩展行
     */
    private ExternalLine externalLine;

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
     * 获取合并外部扩展行后的完整内容(玩家级别)
     *
     * @param playerUuid 玩家 UUID
     * @return 完整的行内容
     */
    public List<String> getMergedLines(@NotNull UUID playerUuid) {
        List<String> result = new ArrayList<>(this.lines);
        // 收集所有插件对该玩家该 key 的扩展配置
        List<ExternalLine> allExternalLines = new ArrayList<>();
        for (Map<UUID, Map<String, ScoreboardConfig>> pluginConfigs : ScoreboardConstants.SCOREBOARD_EXTERNAL.values()) {
            Map<String, ScoreboardConfig> playerConfigs = pluginConfigs.get(playerUuid);
            if (playerConfigs != null) {
                ScoreboardConfig externalConfig = playerConfigs.get(this.key);
                if (externalConfig != null && externalConfig.getExternalLine() != null) {
                    allExternalLines.add(externalConfig.getExternalLine());
                }
            }
        }
        if (CollUtil.isEmpty(allExternalLines)) {
            return result;
        }
        // 按优先级从高到低排序
        allExternalLines.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        // 检查是否有替换模式(优先级最高的替换模式生效)
        for (ExternalLine ext : allExternalLines) {
            if (ExternalLine.ExternalLineMode.REPLACE.equals(ext.getMode())) {
                return new ArrayList<>(ext.getLines());
            }
        }
        // 追加模式: 按优先级追加所有内容
        for (ExternalLine ext : allExternalLines) {
            if (ExternalLine.ExternalLineMode.APPEND.equals(ext.getMode())) {
                result.addAll(ext.getLines());
            }
        }
        return result;
    }

    /**
     * 获取合并外部扩展后的标题(玩家级别)
     *
     * @param playerUuid 玩家 UUID
     * @return 标题
     */
    public String getMergedTitle(@NotNull UUID playerUuid) {
        // 查找外部配置的标题
        for (Map<UUID, Map<String, ScoreboardConfig>> pluginConfigs : ScoreboardConstants.SCOREBOARD_EXTERNAL.values()) {
            Map<String, ScoreboardConfig> playerConfigs = pluginConfigs.get(playerUuid);
            if (playerConfigs != null) {
                ScoreboardConfig externalConfig = playerConfigs.get(this.key);
                if (externalConfig != null && StrUtil.isNotEmpty(externalConfig.getTitle())) {
                    return externalConfig.getTitle();
                }
            }
        }
        return this.title;
    }

    /**
     * 获取合并外部扩展后的优先级(玩家级别)
     * 取所有外部插件设置的最大值,如果没有外部插件设置则返回默认值
     *
     * @param playerUuid 玩家 UUID
     * @return 优先级
     */
    public Integer getMergedPriority(@NotNull UUID playerUuid) {
        Integer maxPriority = null;
        for (Map<UUID, Map<String, ScoreboardConfig>> pluginConfigs : ScoreboardConstants.SCOREBOARD_EXTERNAL.values()) {
            Map<String, ScoreboardConfig> playerConfigs = pluginConfigs.get(playerUuid);
            if (playerConfigs != null) {
                ScoreboardConfig externalConfig = playerConfigs.get(this.key);
                if (externalConfig != null && externalConfig.getPriority() != null) {
                    if (maxPriority == null || externalConfig.getPriority() > maxPriority) {
                        maxPriority = externalConfig.getPriority();
                    }
                }
            }
        }
        return maxPriority != null ? maxPriority : this.priority;
    }

}
