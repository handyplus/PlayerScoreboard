package cn.handyplus.scoreboard.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.scoreboard.core.PlayerScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * 玩家切换世界监听器
 *
 * @author handy
 */
@HandyListener
public class PlayerChangedWorldListener implements Listener {

    /**
     * 玩家切换世界后重新设置计分板
     *
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        // 重新创建计分板，防止跨世界传送后计分板丢失
        PlayerScoreboardManager.updateScoreboard(event.getPlayer());
    }

}
