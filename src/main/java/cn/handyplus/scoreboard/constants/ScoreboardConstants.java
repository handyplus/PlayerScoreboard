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
     * 默认配置 key
     */
    public static final String DEFAULT_KEY = "default";

    /**
     * 缓存的计分板配置列表(按优先级排序)
     */
    public static List<ScoreboardConfig> SCOREBOARD_CONFIGS = new ArrayList<>();

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
