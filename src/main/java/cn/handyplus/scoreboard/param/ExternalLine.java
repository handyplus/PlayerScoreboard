package cn.handyplus.scoreboard.param;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 外部插件扩展行内容
 *
 * @author handy
 */
@Data
public class ExternalLine {

    /**
     * 模式: 追加/替换
     */
    private ExternalLineMode mode;

    /**
     * 行内容
     */
    private List<String> lines;

    /**
     * 排序优先级(数字越大越靠前显示)
     */
    private int priority;

    private ExternalLine() {
    }

    /**
     * 创建追加模式的外部行
     *
     * @param lines 行内容
     * @return ExternalLine
     */
    public static ExternalLine append(@NotNull List<String> lines) {
        return append(lines, 0);
    }

    /**
     * 创建追加模式的外部行
     *
     * @param lines    行内容
     * @param priority 优先级
     * @return ExternalLine
     */
    public static ExternalLine append(@NotNull List<String> lines, int priority) {
        ExternalLine externalLine = new ExternalLine();
        externalLine.setMode(ExternalLineMode.APPEND);
        externalLine.setLines(lines);
        externalLine.setPriority(priority);
        return externalLine;
    }

    /**
     * 创建替换模式的外部行
     *
     * @param lines 行内容
     * @return ExternalLine
     */
    public static ExternalLine replace(@NotNull List<String> lines) {
        return replace(lines, 0);
    }

    /**
     * 创建替换模式的外部行
     *
     * @param lines    行内容
     * @param priority 优先级
     * @return ExternalLine
     */
    public static ExternalLine replace(@NotNull List<String> lines, int priority) {
        ExternalLine externalLine = new ExternalLine();
        externalLine.setMode(ExternalLineMode.REPLACE);
        externalLine.setLines(lines);
        externalLine.setPriority(priority);
        return externalLine;
    }

    /**
     * 外部行模式
     */
    public enum ExternalLineMode {
        /**
         * 追加模式: 在原有内容后追加
         */
        APPEND,
        /**
         * 替换模式: 替换原有内容
         */
        REPLACE
    }

}
