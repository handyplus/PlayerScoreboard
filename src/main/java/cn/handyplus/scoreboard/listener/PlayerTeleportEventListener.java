package cn.handyplus.scoreboard.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.scoreboard.core.PlayerScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * 传送立即刷新
 *
 * @author handy
 */
@HandyListener
public class PlayerTeleportEventListener implements Listener {

    /**
     * 传送立即刷新
     *
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(PlayerTeleportEvent event) {
        // 重新创建计分板，防止跨世界传送后计分板丢失
        PlayerScoreboardManager.updateScoreboard(event.getPlayer());
    }

}
