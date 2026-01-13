package cn.handyplus.scoreboard.constants;

import cn.handyplus.scoreboard.param.ScoreboardConfig;

import java.util.ArrayList;
import java.util.List;

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

}
