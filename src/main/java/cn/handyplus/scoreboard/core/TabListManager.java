package cn.handyplus.scoreboard.core;

import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.internal.HandySchedulerUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.ComponentUtil;
import cn.handyplus.scoreboard.hook.PlaceholderApiUtil;
import cn.handyplus.scoreboard.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * TabList 管理器
 *
 * @author handy
 */
public class TabListManager {

    private TabListManager() {
    }

    /**
     * 刷新玩家 TabList 的表头和表尾
     *
     * @param player 玩家
     */
    public static void updateTabList(Player player) {
        if (!Bukkit.isPrimaryThread()) {
            HandySchedulerUtil.runTask(() -> updateTabList(player));
            return;
        }
        if (!ConfigUtil.TAB_LIST_CONFIG.getBoolean("tabList.enabled", false)) {
            clearTabList(player);
            return;
        }
        String header = buildContent(player, ConfigUtil.TAB_LIST_CONFIG.getStringList("tabList.header"));
        String footer = buildContent(player, ConfigUtil.TAB_LIST_CONFIG.getStringList("tabList.footer"));
        applyHeaderFooter(player, header, footer);
    }

    /**
     * 清空玩家 TabList 的表头和表尾
     *
     * @param player 玩家
     */
    public static void clearTabList(Player player) {
        if (!Bukkit.isPrimaryThread()) {
            HandySchedulerUtil.runTask(() -> clearTabList(player));
            return;
        }
        applyHeaderFooter(player, "", "");
    }

    /**
     * 拼接配置内容并处理变量、颜色和 MiniMessage
     *
     * @param player 玩家
     * @param lines  配置行
     * @return 拼接后的内容
     */
    private static String buildContent(Player player, List<String> lines) {
        if (CollUtil.isEmpty(lines)) {
            return "";
        }
        List<String> parsedLines = PlaceholderApiUtil.set(player, lines);
        parsedLines.replaceAll(line -> BaseUtil.headComponent(line, player.getName()));
        return String.join("\n", parsedLines);
    }

    /**
     * 设置
     *
     * @param player 玩家
     * @param header 表头
     * @param footer 表尾
     */
    private static void applyHeaderFooter(Player player, String header, String footer) {
        player.sendPlayerListHeaderAndFooter(ComponentUtil.parseColor(header), ComponentUtil.parseColor(footer));
    }

}
