package cn.handyplus.scoreboard;

import cn.handyplus.lib.InitApi;
import cn.handyplus.lib.constants.HookPluginEnum;
import cn.handyplus.lib.util.HookPluginUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.scoreboard.job.ScoreboardRefreshJob;
import cn.handyplus.scoreboard.util.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 主类
 *
 * @author handy
 */
public class PlayerScoreboard extends JavaPlugin {
    public static PlayerScoreboard INSTANCE;
    public static boolean USE_PAPI;

    @Override
    public void onEnable() {
        INSTANCE = this;
        InitApi initApi = InitApi.getInstance(this);
        // 加载 配置文件
        ConfigUtil.init();
        // 加载 PlaceholderApi
        USE_PAPI = HookPluginUtil.hook(HookPluginEnum.PLACEHOLDER_API);

        // 初始化
        initApi.addMetrics(28917)
                .checkVersion()
                .initCommand("cn.handyplus.scoreboard.command")
                .initClickEvent("cn.handyplus.scoreboard.listener.gui")
                .initListener("cn.handyplus.scoreboard.listener")
                .enableSql("cn.handyplus.scoreboard.entity");

        // 启动计分板定时任务
        ScoreboardRefreshJob.start();

        MessageUtil.sendConsoleMessage(ChatColor.GREEN + "已成功载入服务器！");
        MessageUtil.sendConsoleMessage(ChatColor.GREEN + "Author:handy 使用文档: https://ricedoc.handyplus.cn/wiki/PlayerScoreboard/README/");
    }

    /**
     * 关闭处理
     */
    @Override
    public void onDisable() {
        InitApi.disable();
    }

}
