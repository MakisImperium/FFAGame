package makisimperium.ffa.kit.service;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import makisimperium.ffa.kit.model.ArmorSet;
import makisimperium.ffa.kit.model.KitDefinition;
import makisimperium.ffa.kit.model.KitItem;
import makisimperium.ffa.kit.persistence.KitRepository;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class KitRegistry {

    private final PluginBase plugin;
    private final KitRepository kitRepository;
    private final Map<String, KitDefinition> kitsById = new ConcurrentHashMap<>();

    public KitRegistry(PluginBase plugin, KitRepository kitRepository) {
        this.plugin = plugin;
        this.kitRepository = kitRepository;
    }

    public void loadKits() throws Exception {
        kitsById.clear();
        for (KitDefinition kitDefinition : kitRepository.loadAll()) {
            if (kitDefinition.getKitId() == null || kitDefinition.getKitId().isBlank()) {
                continue;
            }
            kitsById.put(normalize(kitDefinition.getKitId()), kitDefinition);
        }
        if (kitsById.isEmpty()) {
            KitDefinition defaultKit = createDefaultKit();
            saveKit(defaultKit);
        }
    }

    public Collection<KitDefinition> listKits() {
        return kitsById.values();
    }

    public Optional<KitDefinition> findKit(String kitId) {
        if (kitId == null || kitId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(kitsById.get(normalize(kitId)));
    }

    public Optional<KitDefinition> getDefaultKit() {
        return kitsById.values().stream().findFirst();
    }

    public boolean createOrRenameKit(String kitId, String displayName) {
        if (kitId == null || kitId.isBlank()) {
            return false;
        }

        String normalizedKitId = normalize(kitId);
        KitDefinition existing = kitsById.get(normalizedKitId);
        if (existing != null) {
            existing.setDisplayName(displayName == null || displayName.isBlank() ? existing.getDisplayName() : displayName.trim());
            return saveKit(existing);
        }

        String resolvedDisplayName = displayName == null || displayName.isBlank() ? kitId.trim() : displayName.trim();
        KitDefinition created = new KitDefinition(normalizedKitId, resolvedDisplayName);
        return saveKit(created);
    }

    public boolean createOrUpdateKitFromInventory(String kitId, String displayName, Player sourcePlayer) {
        if (kitId == null || kitId.isBlank()) {
            return false;
        }

        String normalizedKitId = normalize(kitId);
        KitDefinition kitDefinition = new KitDefinition(normalizedKitId, displayName == null || displayName.isBlank() ? kitId : displayName);
        int inventorySize = sourcePlayer.getInventory().getSize();
        for (int slot = 0; slot < inventorySize; slot++) {
            Item item = sourcePlayer.getInventory().getItem(slot);
            if (item == null || item.isNull()) {
                continue;
            }
            kitDefinition.getInventoryItems().add(KitItem.fromItem(item, slot));
        }

        ArmorSet armorSet = new ArmorSet();
        if (!sourcePlayer.getInventory().getHelmet().isNull()) {
            armorSet.setHelmet(KitItem.fromItem(sourcePlayer.getInventory().getHelmet(), null));
        }
        if (!sourcePlayer.getInventory().getChestplate().isNull()) {
            armorSet.setChestplate(KitItem.fromItem(sourcePlayer.getInventory().getChestplate(), null));
        }
        if (!sourcePlayer.getInventory().getLeggings().isNull()) {
            armorSet.setLeggings(KitItem.fromItem(sourcePlayer.getInventory().getLeggings(), null));
        }
        if (!sourcePlayer.getInventory().getBoots().isNull()) {
            armorSet.setBoots(KitItem.fromItem(sourcePlayer.getInventory().getBoots(), null));
        }
        kitDefinition.getArmorSet().setHelmet(armorSet.getHelmet());
        kitDefinition.getArmorSet().setChestplate(armorSet.getChestplate());
        kitDefinition.getArmorSet().setLeggings(armorSet.getLeggings());
        kitDefinition.getArmorSet().setBoots(armorSet.getBoots());

        return saveKit(kitDefinition);
    }

    public boolean upsertInventoryItem(String kitId, int slot, Item item) {
        if (item == null || item.isNull() || slot < 0 || slot > 35) {
            return false;
        }
        Optional<KitDefinition> kitOptional = findKit(kitId);
        if (kitOptional.isEmpty()) {
            return false;
        }

        KitDefinition kit = kitOptional.get();
        kit.getInventoryItems().removeIf(existing -> existing.getSlot() != null && existing.getSlot() == slot);
        kit.getInventoryItems().add(KitItem.fromItem(item, slot));
        kit.getInventoryItems().sort(Comparator.comparing(kitItem -> kitItem.getSlot() == null ? Integer.MAX_VALUE : kitItem.getSlot()));
        return saveKit(kit);
    }

    public boolean removeInventoryItem(String kitId, int slot) {
        if (slot < 0 || slot > 35) {
            return false;
        }
        Optional<KitDefinition> kitOptional = findKit(kitId);
        if (kitOptional.isEmpty()) {
            return false;
        }

        KitDefinition kit = kitOptional.get();
        boolean removed = kit.getInventoryItems().removeIf(existing -> existing.getSlot() != null && existing.getSlot() == slot);
        if (!removed) {
            return false;
        }
        return saveKit(kit);
    }

    public boolean applyEquippedArmor(String kitId, Player sourcePlayer) {
        Optional<KitDefinition> kitOptional = findKit(kitId);
        if (kitOptional.isEmpty()) {
            return false;
        }
        KitDefinition kit = kitOptional.get();
        ArmorSet armorSet = kit.getArmorSet();

        armorSet.setHelmet(sourcePlayer.getInventory().getHelmet().isNull()
                ? null
                : KitItem.fromItem(sourcePlayer.getInventory().getHelmet(), null));
        armorSet.setChestplate(sourcePlayer.getInventory().getChestplate().isNull()
                ? null
                : KitItem.fromItem(sourcePlayer.getInventory().getChestplate(), null));
        armorSet.setLeggings(sourcePlayer.getInventory().getLeggings().isNull()
                ? null
                : KitItem.fromItem(sourcePlayer.getInventory().getLeggings(), null));
        armorSet.setBoots(sourcePlayer.getInventory().getBoots().isNull()
                ? null
                : KitItem.fromItem(sourcePlayer.getInventory().getBoots(), null));
        return saveKit(kit);
    }

    public boolean setArmorItemFromHand(String kitId, ArmorSlot armorSlot, Item handItem) {
        if (handItem == null || handItem.isNull()) {
            return false;
        }
        Optional<KitDefinition> kitOptional = findKit(kitId);
        if (kitOptional.isEmpty()) {
            return false;
        }
        KitDefinition kit = kitOptional.get();
        KitItem kitItem = KitItem.fromItem(handItem, null);
        applyArmorSet(kit.getArmorSet(), armorSlot, kitItem);
        return saveKit(kit);
    }

    public boolean removeArmorItem(String kitId, ArmorSlot armorSlot) {
        Optional<KitDefinition> kitOptional = findKit(kitId);
        if (kitOptional.isEmpty()) {
            return false;
        }
        KitDefinition kit = kitOptional.get();
        if (getArmorItem(kit.getArmorSet(), armorSlot) == null) {
            return false;
        }
        applyArmorSet(kit.getArmorSet(), armorSlot, null);
        return saveKit(kit);
    }

    public String describeArmor(KitDefinition kitDefinition) {
        List<ArmorSlot> order = List.of(ArmorSlot.HELMET, ArmorSlot.CHESTPLATE, ArmorSlot.LEGGINGS, ArmorSlot.BOOTS);
        StringBuilder builder = new StringBuilder();
        for (ArmorSlot armorSlot : order) {
            KitItem item = getArmorItem(kitDefinition.getArmorSet(), armorSlot);
            builder.append(armorSlot.name().toLowerCase(Locale.ROOT)).append(": ");
            if (item == null) {
                builder.append("-");
            } else {
                builder.append("id=").append(item.getItemId())
                        .append(":").append(item.getDamage())
                        .append(" x").append(item.getAmount());
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    private KitItem getArmorItem(ArmorSet armorSet, ArmorSlot armorSlot) {
        return switch (armorSlot) {
            case HELMET -> armorSet.getHelmet();
            case CHESTPLATE -> armorSet.getChestplate();
            case LEGGINGS -> armorSet.getLeggings();
            case BOOTS -> armorSet.getBoots();
        };
    }

    private void applyArmorSet(ArmorSet armorSet, ArmorSlot armorSlot, KitItem item) {
        switch (armorSlot) {
            case HELMET -> armorSet.setHelmet(item);
            case CHESTPLATE -> armorSet.setChestplate(item);
            case LEGGINGS -> armorSet.setLeggings(item);
            case BOOTS -> armorSet.setBoots(item);
        }
    }

    public enum ArmorSlot {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS
    }

    public boolean deleteKit(String kitId) {
        return removeKit(kitId).isPresent();
    }

    public Optional<KitDefinition> removeKit(String kitId) {
        Optional<KitDefinition> existing = findKit(kitId);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        try {
            kitRepository.delete(existing.get().getKitId());
            kitsById.remove(normalize(kitId));
            return Optional.of(existing.get());
        } catch (Exception exception) {
            plugin.getLogger().error("Failed to delete kit " + kitId, exception);
            return Optional.empty();
        }
    }

    public boolean restoreKit(KitDefinition kitDefinition) {
        if (kitDefinition == null || kitDefinition.getKitId() == null || kitDefinition.getKitId().isBlank()) {
            return false;
        }
        return saveKit(kitDefinition);
    }

    private boolean saveKit(KitDefinition kitDefinition) {
        try {
            kitRepository.save(kitDefinition);
            kitsById.put(normalize(kitDefinition.getKitId()), kitDefinition);
            return true;
        } catch (Exception exception) {
            plugin.getLogger().error("Failed to persist kit " + kitDefinition.getKitId(), exception);
            return false;
        }
    }

    private KitDefinition createDefaultKit() {
        KitDefinition kit = new KitDefinition("soldier", "Soldier");
        kit.getInventoryItems().add(new KitItem(267, 0, 1, 0));  // Iron Sword
        kit.getInventoryItems().add(new KitItem(261, 0, 1, 1));  // Bow
        kit.getInventoryItems().add(new KitItem(262, 0, 32, 8)); // Arrows
        kit.getInventoryItems().add(new KitItem(364, 0, 8, 7));  // Steak
        kit.getInventoryItems().add(new KitItem(322, 0, 2, 6));  // Golden Apple

        kit.getArmorSet().setHelmet(new KitItem(306, 0, 1, null));
        kit.getArmorSet().setChestplate(new KitItem(307, 0, 1, null));
        kit.getArmorSet().setLeggings(new KitItem(308, 0, 1, null));
        kit.getArmorSet().setBoots(new KitItem(309, 0, 1, null));
        return kit;
    }

    private String normalize(String kitId) {
        return kitId.trim().toLowerCase(Locale.ROOT);
    }
}


