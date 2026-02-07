package cn.handyplus.scoreboard.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.util.AssertUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.scoreboard.core.PlayerScoreboardManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 切换计分板显示状态
 *
 * @author handy
 */
public class ToggleCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "toggle";
    }

    @Override
    public String permission() {
        return "playerScoreboard.toggle";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = AssertUtil.notPlayer(sender, BaseUtil.getLangMsg("noPlayerFailureMsg"));
        boolean enabled = PlayerScoreboardManager.toggleScoreboard(player);
        MessageUtil.sendMessage(player, BaseUtil.getLangMsg(enabled ? "scoreboardEnabled" : "scoreboardDisabled"));
    }

}
