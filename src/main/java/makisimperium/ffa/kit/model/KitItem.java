package makisimperium.ffa.kit.model;

import cn.nukkit.item.Item;

public final class KitItem {

    private int itemId;
    private int damage;
    private int amount;
    private Integer slot;

    public KitItem() {
    }

    public KitItem(int itemId, int damage, int amount, Integer slot) {
        this.itemId = itemId;
        this.damage = damage;
        this.amount = amount;
        this.slot = slot;
    }

    public static KitItem fromItem(Item item, Integer slot) {
        return new KitItem(item.getId(), item.getDamage(), item.getCount(), slot);
    }

    public Item toItem() {
        return Item.get(itemId, damage, Math.max(1, amount));
    }

    public int getItemId() {
        return itemId;
    }

    public int getDamage() {
        return damage;
    }

    public int getAmount() {
        return amount;
    }

    public Integer getSlot() {
        return slot;
    }
}


