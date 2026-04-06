package makisimperium.ffa.listener;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import makisimperium.ffa.arena.service.ArenaSessionService;

public final class ArenaProtectionListener implements Listener {

    private final ArenaSessionService arenaSessionService;

    public ArenaProtectionListener(ArenaSessionService arenaSessionService) {
        this.arenaSessionService = arenaSessionService;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!arenaSessionService.isPlayerInArena(player.getUniqueId())) {
            return;
        }
        if (arenaSessionService.isInsideSafeZone(player) || arenaSessionService.hasRespawnProtection(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = resolveAttacker(event.getDamager());
        boolean victimInArena = arenaSessionService.isPlayerInArena(victim.getUniqueId());
        boolean attackerInArena = attacker != null && arenaSessionService.isPlayerInArena(attacker.getUniqueId());

        if (!victimInArena && !attackerInArena) {
            return;
        }
        if (!victimInArena || !attackerInArena) {
            event.setCancelled(true);
            return;
        }
        if (!arenaSessionService.areInSameArena(victim.getUniqueId(), attacker.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (arenaSessionService.hasRespawnProtection(victim.getUniqueId())
                || arenaSessionService.hasRespawnProtection(attacker.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (arenaSessionService.isInsideSafeZone(victim) || arenaSessionService.isInsideSafeZone(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!arenaSessionService.isPlayerInArena(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!arenaSessionService.isPlayerInArena(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!arenaSessionService.isPlayerInArena(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!arenaSessionService.isPlayerInArena(event.getPlayer().getUniqueId())) {
            return;
        }
        if (arenaSessionService.isInsideSafeZone(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof EntityProjectile projectile && projectile.shootingEntity instanceof Player player) {
            return player;
        }
        return null;
    }
}


