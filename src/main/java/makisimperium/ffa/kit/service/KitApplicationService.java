package makisimperium.ffa.kit.service;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import makisimperium.ffa.kit.model.ArmorSet;
import makisimperium.ffa.kit.model.KitDefinition;
import makisimperium.ffa.kit.model.KitItem;

import java.util.Optional;

public final class KitApplicationService {

    private final KitRegistry kitRegistry;

    public KitApplicationService(KitRegistry kitRegistry) {
        this.kitRegistry = kitRegistry;
    }

    public boolean applyKit(Player player, String kitId) {
        Optional<KitDefinition> kitDefinition = kitRegistry.findKit(kitId);
        if (kitDefinition.isEmpty()) {
            return false;
        }

        player.getInventory().clearAll();
        for (KitItem item : kitDefinition.get().getInventoryItems()) {
            Item nukkitItem = item.toItem();
            if (item.getSlot() == null) {
                player.getInventory().addItem(nukkitItem);
                continue;
            }
            int slot = item.getSlot();
            if (slot < 0 || slot >= player.getInventory().getSize()) {
                player.getInventory().addItem(nukkitItem);
                continue;
            }
            player.getInventory().setItem(slot, nukkitItem);
        }

        ArmorSet armorSet = kitDefinition.get().getArmorSet();
        if (armorSet.getHelmet() != null) {
            player.getInventory().setHelmet(armorSet.getHelmet().toItem());
        }
        if (armorSet.getChestplate() != null) {
            player.getInventory().setChestplate(armorSet.getChestplate().toItem());
        }
        if (armorSet.getLeggings() != null) {
            player.getInventory().setLeggings(armorSet.getLeggings().toItem());
        }
        if (armorSet.getBoots() != null) {
            player.getInventory().setBoots(armorSet.getBoots().toItem());
        }

        // Ensure the client receives the full inventory/armor snapshot immediately.
        player.getInventory().sendContents(player);
        player.getInventory().sendArmorContents(player);
        player.getInventory().setHeldItemSlot(0);
        return true;
    }
}


