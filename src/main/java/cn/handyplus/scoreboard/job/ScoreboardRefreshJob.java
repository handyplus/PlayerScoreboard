package cn.handyplus.scoreboard.job;

import cn.handyplus.lib.internal.HandyRunnable;
import cn.handyplus.lib.internal.HandySchedulerUtil;
import cn.handyplus.scoreboard.PlayerScoreboard;
import cn.handyplus.scoreboard.core.PlayerScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 计分板刷新定时任务
 *
 * @author handy
 */
public class ScoreboardRefreshJob {

    /**
     * 刷新任务
     */
    private static HandyRunnable REFRESH_TASK;

    private ScoreboardRefreshJob() {
    }

    /**
     * 启动定时任务
     */
    public static void start() {
        // 取消旧任务
        if (REFRESH_TASK != null) {
            REFRESH_TASK.cancel();
        }
        // 创建刷新任务
        REFRESH_TASK = new HandyRunnable() {
            @Override
            public void run() {
                // 遍历所有在线玩家,更新计分板
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // 在主线程更新计分板
                    HandySchedulerUtil.runTask(() -> PlayerScoreboardManager.updateScoreboard(player));
                }
            }
        };
        // 获取刷新间隔(秒)
        int interval = PlayerScoreboard.INSTANCE.getConfig().getInt("scoreboardRefreshInterval", 10);
        HandySchedulerUtil.runTaskTimerAsynchronously(REFRESH_TASK, 20L, interval * 20L);
    }

}
