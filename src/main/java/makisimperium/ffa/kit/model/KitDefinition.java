package makisimperium.ffa.kit.model;

import java.util.ArrayList;
import java.util.List;

public final class KitDefinition {

    private String kitId;
    private String displayName;
    private List<KitItem> inventoryItems = new ArrayList<>();
    private ArmorSet armorSet = new ArmorSet();

    public KitDefinition() {
    }

    public KitDefinition(String kitId, String displayName) {
        this.kitId = kitId;
        this.displayName = displayName;
    }

    public String getKitId() {
        return kitId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<KitItem> getInventoryItems() {
        if (inventoryItems == null) {
            inventoryItems = new ArrayList<>();
        }
        return inventoryItems;
    }

    public ArmorSet getArmorSet() {
        if (armorSet == null) {
            armorSet = new ArmorSet();
        }
        return armorSet;
    }
}


