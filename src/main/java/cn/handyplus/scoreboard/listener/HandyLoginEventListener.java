package cn.handyplus.scoreboard.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.internal.HandyLoginEvent;
import cn.handyplus.lib.util.HandyHttpUtil;
import cn.handyplus.scoreboard.core.PlayerScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 登录事件
 *
 * @author handy
 */
@HandyListener
public class HandyLoginEventListener implements Listener {

    /**
     * op 进入服务器发送更新提醒
     *
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEvent(HandyLoginEvent event) {
        // 为玩家创建计分板
        PlayerScoreboardManager.createScoreboard(event.getPlayer());
        // 立即刷新
        PlayerScoreboardManager.updateScoreboard(event.getPlayer());
        // 检查版本更新
        HandyHttpUtil.checkVersion(event.getPlayer());
    }

}