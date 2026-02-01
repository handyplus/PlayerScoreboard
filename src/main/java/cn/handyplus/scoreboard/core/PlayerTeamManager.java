package cn.handyplus.scoreboard.core;

import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.ComponentUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 计分板团队管理器
 *
 * @author handy
 */
public class PlayerTeamManager {

    /**
     * 设置玩家在Tab列表中的前缀和后缀
     * 会在玩家的计分板上创建/更新Team
     *
     * @param player 玩家
     * @param prefix 前缀
     * @param suffix 后缀
     */
    public static void setTabPrefixAndSuffix(@NotNull Player player, @NotNull String prefix, @NotNull String suffix) {
        Team team = getOrCreateTeam(player, player.getName());
        if (team != null) {
            setTeamPrefixAndSuffix(team, prefix, suffix);
            team.addEntry(player.getName());
        }
        // 同步到所有玩家
        syncScoreboardToAll(player);
        // 同步其他玩家的信息到自己
        syncAllToPlayer(player);
    }

    /**
     * 移除玩家的Tab Team
     *
     * @param player 玩家
     */
    public static void removeTabTeam(@NotNull Player player) {
        Scoreboard scoreboard = PlayerScoreboardManager.getScoreboard(player.getUniqueId());
        if (scoreboard != null) {
            Team team = scoreboard.getTeam(player.getName());
            if (team != null) {
                team.unregister();
            }
        }
    }

    /**
     * 同步玩家的计分板Team到所有在线玩家
     * 用于让其他玩家看到该玩家的Tab前缀/后缀
     *
     * @param player 目标玩家
     */
    private static void syncScoreboardToAll(Player player) {
        Scoreboard scoreboard = PlayerScoreboardManager.getScoreboard(player.getUniqueId());
        if (scoreboard == null) {
            return;
        }
        // 将该玩家的 Team 信息同步到所有在线玩家的计分板上
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) {
                continue;
            }
            Scoreboard targetBoard = PlayerScoreboardManager.getScoreboard(onlinePlayer.getUniqueId());
            if (targetBoard == null) {
                continue;
            }
            // 复制 Team 信息
            Team sourceTeam = scoreboard.getTeam(player.getName());
            if (sourceTeam != null) {
                Team targetTeam = targetBoard.getTeam(player.getName());
                if (targetTeam == null) {
                    targetTeam = targetBoard.registerNewTeam(player.getName());
                }
                copyTeam(sourceTeam, targetTeam);
                targetTeam.addEntry(player.getName());
            }
        }
    }

    /**
     * 同步所有在线玩家的Team信息到指定玩家
     * 用于玩家刚登录时，获取其他玩家的Tab前缀/后缀
     *
     * @param player 目标玩家
     */
    private static void syncAllToPlayer(Player player) {
        Scoreboard targetBoard = PlayerScoreboardManager.getScoreboard(player.getUniqueId());
        if (targetBoard == null) {
            return;
        }
        // 将所有在线玩家的 Team 信息同步到该玩家的计分板上
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) {
                continue;
            }
            Scoreboard sourceBoard = PlayerScoreboardManager.getScoreboard(onlinePlayer.getUniqueId());
            if (sourceBoard == null) {
                continue;
            }
            // 复制 Team 信息
            Team sourceTeam = sourceBoard.getTeam(onlinePlayer.getName());
            if (sourceTeam != null) {
                Team targetTeam = targetBoard.getTeam(onlinePlayer.getName());
                if (targetTeam == null) {
                    targetTeam = targetBoard.registerNewTeam(onlinePlayer.getName());
                }
                copyTeam(sourceTeam, targetTeam);
                targetTeam.addEntry(onlinePlayer.getName());
            }
        }
    }

    /**
     * 复制 Team 的 prefix 和 suffix
     * Paper 1.16+ 使用 Component API，其他使用传统字符串 API
     */
    @SuppressWarnings("deprecation")
    private static void copyTeam(Team source, Team target) {
        if (BaseUtil.supportsComponentApi()) {
            target.prefix(source.prefix());
            target.suffix(source.suffix());
        } else {
            target.setPrefix(source.getPrefix());
            target.setSuffix(source.getSuffix());
        }
    }

    /**
     * 设置 Team 的 prefix 和 suffix
     * Paper 1.16+ 使用 Component API，其他使用传统字符串 API
     *
     * @param team   Team 对象
     * @param prefix 前缀
     * @param suffix 后缀
     */
    @SuppressWarnings("deprecation")
    private static void setTeamPrefixAndSuffix(Team team, String prefix, String suffix) {
        if (BaseUtil.supportsComponentApi()) {
            team.prefix(ComponentUtil.parseMessage(prefix));
            team.suffix(ComponentUtil.parseMessage(suffix));
        } else {
            team.setPrefix(BaseUtil.replaceChatColor(truncateTeamText(prefix)));
            team.setSuffix(BaseUtil.replaceChatColor(truncateTeamText(suffix)));
        }
    }

    /**
     * 获取或创建玩家计分板上的 Team
     *
     * @param player   玩家
     * @param teamName Team 名称
     * @return Team对象,如果玩家计分板不存在则返回null
     */
    @Nullable
    private static Team getOrCreateTeam(@NotNull Player player, @NotNull String teamName) {
        Scoreboard scoreboard = PlayerScoreboardManager.getScoreboard(player.getUniqueId());
        if (scoreboard == null) {
            return null;
        }
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        return team;
    }

    /**
     * 截断 Team prefix/suffix 到版本允许的最大长度
     * 1.13- 最大 16 字符，1.13+ 最大 64 字符
     *
     * @param text 文本
     * @return 截断后的文本
     */
    private static String truncateTeamText(String text) {
        if (BaseConstants.VERSION_ID < VersionCheckEnum.V_1_13.getVersionId() && text.length() > 16) {
            return text.substring(0, 16);
        }
        if (BaseConstants.VERSION_ID >= VersionCheckEnum.V_1_13.getVersionId() && text.length() > 64) {
            return text.substring(0, 64);
        }
        return text;
    }

}
