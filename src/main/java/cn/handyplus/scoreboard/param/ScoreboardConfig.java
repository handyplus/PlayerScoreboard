package cn.handyplus.scoreboard.param;

import cn.handyplus.scoreboard.PlayerScoreboard;
import lombok.Data;

import java.util.List;

/**
 * 计分板配置对象
 *
 * @author handy
 */
@Data
public class ScoreboardConfig {

    /**
     * 配置 key
     */
    private String key;

    /**
     * 优先级
     */
    private int priority;

    /**
     * 权限节点
     */
    private String permission;

    /**
     * 计分板标题
     */
    private String title;

    /**
     * 计分板内容行
     */
    private List<String> lines;

    /**
     * 适用世界
     */
    private List<String> worlds;

    /**
     * 是否外部配置
     */
    private Boolean external;

    /**
     * 基本构造器
     *
     * @param key      配置 key
     * @param priority 优先级
     * @param title    计分板标题
     * @param lines    计分板内容行
     * @param worlds   适用世界
     * @return 计分板配置对象
     */
    public static ScoreboardConfig of(String key, int priority, String title, List<String> lines, List<String> worlds) {
        ScoreboardConfig scoreboardConfig = new ScoreboardConfig();
        scoreboardConfig.setKey(key);
        scoreboardConfig.setPriority(priority);
        scoreboardConfig.setPermission(PlayerScoreboard.INSTANCE.getName() + "." + key);
        scoreboardConfig.setTitle(title);
        scoreboardConfig.setLines(lines);
        scoreboardConfig.setExternal(false);
        return scoreboardConfig;
    }

    /**
     * 带外部配置参数的构造器
     *
     * @param key      配置 key
     * @param priority 优先级
     * @param title    计分板标题
     * @param lines    计分板内容行
     * @param external 是否外部配置
     * @return 计分板配置对象
     */
    public static ScoreboardConfig of(String key, int priority, String title, List<String> lines, List<String> worlds, Boolean external) {
        ScoreboardConfig scoreboardConfig = of(key, priority, title, lines, worlds);
        scoreboardConfig.setExternal(external);
        return scoreboardConfig;
    }

    /**
     * 全参数构造器
     *
     * @param key        配置 key
     * @param priority   优先级
     * @param title      计分板标题
     * @param lines      计分板内容行
     * @param external   是否外部配置
     * @param permission 权限节点
     * @return 计分板配置对象
     */
    public static ScoreboardConfig of(String key, int priority, String title, List<String> lines, List<String> worlds, Boolean external, String permission) {
        ScoreboardConfig scoreboardConfig = of(key, priority, title, lines, worlds, external);
        scoreboardConfig.setPermission(permission);
        return scoreboardConfig;
    }


}
