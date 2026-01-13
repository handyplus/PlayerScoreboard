package cn.handyplus.scoreboard.hook;

import cn.handyplus.scoreboard.PlayerScoreboard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 变量工具类
 *
 * @author handy
 */
public class PlaceholderApiUtil {

    /**
     * 替换变量
     *
     * @param player 玩家
     * @param str    字符串
     * @return 新字符串
     */
    public static String set(Player player, String str) {
        if (!PlayerScoreboard.USE_PAPI || player == null) {
            return str;
        }
        // 是否包含变量
        if (PlaceholderAPI.containsPlaceholders(str)) {
            return PlaceholderAPI.setPlaceholders(player, str);
        }
        return str;
    }

    /**
     * 替换变量
     *
     * @param player  玩家
     * @param strList 字符串集合
     * @return 新字符串集合
     */
    public static List<String> set(Player player, List<String> strList) {
        if (!PlayerScoreboard.USE_PAPI || player == null) {
            return strList;
        }
        return PlaceholderAPI.setPlaceholders(player, strList);
    }

}