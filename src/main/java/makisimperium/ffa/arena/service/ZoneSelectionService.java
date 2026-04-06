package makisimperium.ffa.arena.service;

import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import makisimperium.ffa.arena.model.CuboidZone;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ZoneSelectionService {

    private final Map<UUID, Selection> selectionsByPlayer = new ConcurrentHashMap<>();

    public void setFirstPosition(Player player) {
        Selection selection = selectionsByPlayer.computeIfAbsent(player.getUniqueId(), ignored -> new Selection());
        selection.worldName = player.getLevel().getName();
        selection.first = new Vector3(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    public void setSecondPosition(Player player) {
        Selection selection = selectionsByPlayer.computeIfAbsent(player.getUniqueId(), ignored -> new Selection());
        selection.worldName = player.getLevel().getName();
        selection.second = new Vector3(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    public Optional<CuboidZone> buildZone(Player player) {
        Selection selection = selectionsByPlayer.get(player.getUniqueId());
        if (selection == null || selection.first == null || selection.second == null || selection.worldName == null) {
            return Optional.empty();
        }
        return Optional.of(CuboidZone.fromCorners(selection.worldName, selection.first, selection.second));
    }

    private static final class Selection {
        private String worldName;
        private Vector3 first;
        private Vector3 second;
    }
}


