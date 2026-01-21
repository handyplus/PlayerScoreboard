package cn.handyplus.scoreboard.constants;

import cn.handyplus.scoreboard.param.ScoreboardConfig;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 常量
 *
 * @author handy
 */
public final class ScoreboardConstants {
    private ScoreboardConstants() {
    }

    /**
     * 缓存的计分板配置列表
     */
    public static Map<String, ScoreboardConfig> SCOREBOARD_CONFIGS = new HashMap<>();

    /**
     * 缓存的计分板外部自定义配置列表(玩家级别)
     * key: 插件实例
     * value: 玩家 UUID -> 该插件注册的计分板配置(key为计分板配置key)
     */
    public static Map<Plugin, Map<UUID, Map<String, ScoreboardConfig>>> SCOREBOARD_EXTERNAL = new HashMap<>();

    /**
     * 所有世界标识
     */
    public static final String ALL = "[ALL]";

    /**
     * 计分板目标名称
     */
    public static final String OBJECTIVE_NAME = "psd_board";

    /**
     * 命令映射
     */
    public static Map<String, String> COMMAND_MAP = new HashMap<>();

}
