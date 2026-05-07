package cn.handyplus.scoreboard.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.scoreboard.core.TabListManager;
import cn.handyplus.scoreboard.job.ScoreboardRefreshJob;
import cn.handyplus.scoreboard.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 重载配置
 *
 * @author handy
 */
public class ReloadCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "reload";
    }

    @Override
    public String permission() {
        return "playerScoreboard.reload";
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ConfigUtil.init();
        // 重新加载定时任务
        ScoreboardRefreshJob.start();
        // 重载后立即刷新在线玩家显示
        for (Player player : Bukkit.getOnlinePlayers()) {
            TabListManager.updateTabList(player);
        }
        MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("reloadMsg"));
    }

}
