package cn.handyplus.scoreboard.listener;

import cn.handyplus.lib.annotation.HandyListener;
import cn.handyplus.lib.core.StrUtil;
import cn.handyplus.lib.internal.PlayerSchedulerUtil;
import cn.handyplus.scoreboard.constants.ScoreboardConstants;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * 当一个玩家执行一个命令的时候将会被触发
 *
 * @author handy
 * @since 1.2.1
 */
@HandyListener
public class PlayerCommandPreprocessEventListener implements Listener {

    /**
     * 命令别名处理
     *
     * @param event 事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEvent(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String[] param = event.getMessage().split(" ");
        String command = ScoreboardConstants.COMMAND_MAP.get(param[0].replace("/", ""));
        if (StrUtil.isEmpty(command)) {
            return;
        }
        Player player = event.getPlayer();
        event.setCancelled(true);
        PlayerSchedulerUtil.playerPerformCommand(player, command);
    }

}
