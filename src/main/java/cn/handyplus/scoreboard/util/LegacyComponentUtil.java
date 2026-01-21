package cn.handyplus.scoreboard.util;

import cn.handyplus.lib.util.BaseUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Legacy 组件工具类
 *
 * @author handy
 */
public class LegacyComponentUtil {

    /**
     * 用于 Component 解析的序列化器(支持 § 颜色代码以及 HEX 颜色)
     */
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .build();

    private LegacyComponentUtil() {
    }

    /**
     * 将字符串转为 Component
     * 支持 & 和 § 颜色代码以及 HEX 颜色
     *
     * @param text 字符串
     * @return Component
     */
    public static Component toComponent(String text) {
        return SERIALIZER.deserialize(BaseUtil.replaceChatColor(text));
    }

}
