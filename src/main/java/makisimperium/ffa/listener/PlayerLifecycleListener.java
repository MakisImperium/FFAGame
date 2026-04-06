package makisimperium.ffa.listener;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import makisimperium.ffa.arena.service.ArenaSessionService;
import makisimperium.ffa.messaging.FfaMessageFormatter;
import makisimperium.ffa.player.model.PlayerLanguage;
import makisimperium.ffa.player.service.PlayerProfileService;
import makisimperium.ffa.scoreboard.FfaScoreboardService;
import makisimperium.ffa.ui.form.FfaFormController;

public final class PlayerLifecycleListener implements Listener {

    private final PluginBase plugin;
    private final PlayerProfileService playerProfileService;
    private final ArenaSessionService arenaSessionService;
    private final FfaScoreboardService scoreboardService;
    private final FfaFormController formController;

    public PlayerLifecycleListener(
            PluginBase plugin,
            PlayerProfileService playerProfileService,
            ArenaSessionService arenaSessionService,
            FfaScoreboardService scoreboardService,
            FfaFormController formController
    ) {
        this.plugin = plugin;
        this.playerProfileService = playerProfileService;
        this.arenaSessionService = arenaSessionService;
        this.scoreboardService = scoreboardService;
        this.formController = formController;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerProfileService.loadProfile(player);
        scoreboardService.handleJoin(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        arenaSessionService.removePlayerSession(player.getUniqueId());
        formController.clearPlayerState(player.getUniqueId());
        scoreboardService.handleQuit(player);
        playerProfileService.unloadProfile(player, true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!arenaSessionService.isPlayerInArena(victim.getUniqueId())) {
            return;
        }

        playerProfileService.registerDeath(victim.getUniqueId());
        event.setKeepInventory(true);
        event.setKeepExperience(true);
        event.setDrops(new Item[0]);

        Player killer = resolveKiller(victim.getLastDamageCause());
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())
                && arenaSessionService.areInSameArena(killer.getUniqueId(), victim.getUniqueId())) {
            playerProfileService.registerKill(killer.getUniqueId());
            arenaSessionService.replenishPlayerAfterKill(killer);
            int streak = playerProfileService.getStatisticsOrDefault(killer.getUniqueId()).getCurrentKillstreak();
            killer.sendMessage(FfaMessageFormatter.success(t(killer,
                    "Elimination confirmed. Loadout refilled. Streak: " + streak + ".",
                    "Eliminierung bestaetigt. Loadout aufgefuellt. Streak: " + streak + ".")));
            scoreboardService.refreshPlayer(killer);
        }

        victim.sendMessage(FfaMessageFormatter.error(t(victim,
                "You were eliminated. Redeploying...",
                "Du wurdest eliminiert. Neuer Einsatz laeuft...")));
        scoreboardService.refreshPlayer(victim);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        arenaSessionService.handleRespawn(event);
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> scoreboardService.refreshPlayer(event.getPlayer()), 10);
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            if (event.getPlayer().isOnline() && arenaSessionService.isPlayerInArena(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(FfaMessageFormatter.info(t(event.getPlayer(),
                        "Back in combat. Respawn shield active for 5 seconds.",
                        "Zurueck im Kampf. Respawn-Schutz fuer 5 Sekunden aktiv.")));
            }
        }, 12);
    }

    private Player resolveKiller(EntityDamageEvent lastDamageCause) {
        if (!(lastDamageCause instanceof EntityDamageByEntityEvent damageByEntityEvent)) {
            return null;
        }

        Entity damager = damageByEntityEvent.getDamager();
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof EntityProjectile projectile && projectile.shootingEntity instanceof Player player) {
            return player;
        }
        return null;
    }

    private String t(Player player, String english, String german) {
        PlayerLanguage language = playerProfileService.getLanguage(player.getUniqueId());
        return language == PlayerLanguage.GERMAN ? german : english;
    }
}


