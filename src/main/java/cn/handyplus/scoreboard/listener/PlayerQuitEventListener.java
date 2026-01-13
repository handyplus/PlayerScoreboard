package cn.handyplus.scoreboard.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.scoreboard.core.PlayerScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家退出监听器
 *
 * @author handy
 */
@HandyListener
public class PlayerQuitEventListener implements Listener {

    /**
     * 玩家退出服务器
     *
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 清理玩家计分板数据
        PlayerScoreboardManager.removeScoreboard(event.getPlayer());
    }

}
