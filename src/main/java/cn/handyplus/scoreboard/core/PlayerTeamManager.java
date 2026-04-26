package cn.handyplus.scoreboard.core;

import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.constants.VersionCheckEnum;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.ComponentUtil;
import cn.handyplus.lib.util.LegacyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计分板团队管理器
 *
 * @author handy
 */
public class PlayerTeamManager {

    /**
     * 玩家 API 设置的 Tab 前缀
     */
    private static final Map<UUID, String> TAB_PREFIX_MAP = new ConcurrentHashMap<>();

    /**
     * 玩家 API 设置的 Tab 后缀
     */
    private static final Map<UUID, String> TAB_SUFFIX_MAP = new ConcurrentHashMap<>();

    /**
     * 已经注册 Tab Team 的玩家
     */
    private static final Set<UUID> TAB_TEAM_PLAYER_SET = ConcurrentHashMap.newKeySet();

    /**
     * 设置玩家在Tab列表中的前缀和后缀
     * 会在玩家的计分板上创建/更新Team
     *
     * @param player 玩家
     * @param prefix 前缀
     * @param suffix 后缀
     */
    public static void setTabPrefixAndSuffix(@NotNull Player player, @NotNull String prefix, @NotNull String suffix) {
        // 仅缓存 API 原始值，最终显示统一在 refreshTabTeam 合并头像配置，避免自动刷新覆盖 API 内容
        TAB_PREFIX_MAP.put(player.getUniqueId(), prefix);
        TAB_SUFFIX_MAP.put(player.getUniqueId(), suffix);
        refreshTabTeam(player);
    }

    /**
     * 刷新玩家在 Tab 列表中的 Team 显示
     *
     * @param player 玩家
     */
    public static void refreshTabTeam(@NotNull Player player) {
        if (!hasTabDisplay(player)) {
            // 未开启头像且没有 API 内容时不注册 Team；只清理曾经注册过的玩家，避免刷新时额外遍历
            if (TAB_TEAM_PLAYER_SET.remove(player.getUniqueId())) {
                unregisterTabTeam(player);
            }
            return;
        }
        Team team = getOrCreateTeam(player, player.getName());
        if (team != null) {
            TAB_TEAM_PLAYER_SET.add(player.getUniqueId());
            String prefix = getMergedPrefix(player);
            String suffix = TAB_SUFFIX_MAP.getOrDefault(player.getUniqueId(), "");
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
        TAB_PREFIX_MAP.remove(player.getUniqueId());
        TAB_SUFFIX_MAP.remove(player.getUniqueId());
        // 只有真实注册过 Team 的玩家才需要从所有计分板移除
        if (TAB_TEAM_PLAYER_SET.remove(player.getUniqueId())) {
            unregisterTabTeam(player);
        }
    }

    /**
     * 判断玩家是否需要 Tab Team 显示
     *
     * @param player 玩家
     * @return 是否需要注册或刷新 Team
     */
    private static boolean hasTabDisplay(Player player) {
        return BaseConstants.CONFIG.getBoolean("showTabHead")
                || TAB_PREFIX_MAP.containsKey(player.getUniqueId())
                || TAB_SUFFIX_MAP.containsKey(player.getUniqueId());
    }

    /**
     * 从所有在线玩家的计分板中移除目标玩家 Team
     *
     * @param player 目标玩家
     */
    private static void unregisterTabTeam(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = PlayerScoreboardManager.getScoreboard(onlinePlayer.getUniqueId());
            if (scoreboard == null) {
                continue;
            }
            Team team = scoreboard.getTeam(player.getName());
            if (team != null) {
                team.unregister();
            }
        }
    }

    /**
     * 合并配置头像和 API 前缀
     *
     * @param player 玩家
     * @return 合并后的 Tab 前缀
     */
    private static String getMergedPrefix(Player player) {
        String prefix = TAB_PREFIX_MAP.getOrDefault(player.getUniqueId(), "");
        if (BaseConstants.CONFIG.getBoolean("showTabHead")) {
            return BaseUtil.headComponent("${head}", player.getName()) + prefix;
        }
        return prefix;
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
            team.prefix(ComponentUtil.parseColor(prefix));
            team.suffix(ComponentUtil.parseColor(suffix));
        } else {
            team.setPrefix(LegacyUtil.parseColor(truncateTeamText(prefix)));
            team.setSuffix(LegacyUtil.parseColor(truncateTeamText(suffix)));
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
