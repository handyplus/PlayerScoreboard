package cn.handyplus.scoreboard.constants;

import cn.handyplus.scoreboard.param.ScoreboardConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 缓存的计分板外部自定义配置列表
     */
    public static Map<String, ScoreboardConfig> SCOREBOARD_EXTERNAL = new HashMap<>();

    /**
     * 所有世界标识
     */
    public static final String ALL = "[ALL]";

    /**
     * 命令映射
     *
     */
    public static Map<String, String> COMMAND_MAP = new HashMap<>();

}
