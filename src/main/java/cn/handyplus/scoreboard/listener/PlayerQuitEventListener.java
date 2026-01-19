package cn.handyplus.scoreboard.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.scoreboard.api.PlayerScoreboardApi;
import cn.handyplus.scoreboard.core.PlayerScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家退出监听器
 *
 * @author handy
 */
@HandyListener
public class PlayerQuitEventListener implements Listener {

    /**
     * 玩家被服务器踢出事件.
     *
     * @param event 事件
     */
    @EventHandler
    public void onKick(PlayerKickEvent event) {
        removeCache(event.getPlayer());
    }

    /**
     * 玩家离开服务器事件.
     *
     * @param event 事件
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeCache(event.getPlayer());
    }

    private void removeCache(Player player) {
        // 清理玩家计分板数据
        PlayerScoreboardManager.removeScoreboard(player);
        // 清理玩家的外部扩展数据
        PlayerScoreboardApi.clearPlayer(player.getUniqueId());
    }

}
