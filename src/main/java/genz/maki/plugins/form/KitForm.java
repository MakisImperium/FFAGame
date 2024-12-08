package genz.maki.plugins.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.Main;
import genz.maki.plugins.config.FFAConfig;
import genz.maki.plugins.config.PlayerFile;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.kits.manager.KitsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitForm {

    private final FFAConfig ffaConfig;
    private final PlayerFile playerFile;
    private final RoomConfig roomConfig;
    private final Main main;
    private final KitsManager kitsManager;

    private final String FORMNAME;

    private final Map<Player, List<Integer>> kitItems;
    private final Map<Player, String> kitNames;


    public KitForm(Main main, FFAConfig ffaConfig, PlayerFile playerFile, RoomConfig roomConfig, KitsManager kitsManager) {
        this.main = main;
        this.ffaConfig = ffaConfig;
        this.playerFile = playerFile;
        this.roomConfig = roomConfig;
        this.kitsManager = kitsManager;

        this.FORMNAME = ffaConfig.getFormName();

        this.kitNames = new HashMap<>();
        this.kitItems = new HashMap<>();
    }

    /**
     * Displays the kit administration form to the specified player. If the player has administrative
     * permissions, a form with additional options for managing kits is shown. Otherwise, a simpler
     * form for selecting kits is presented.
     *
     * @param player the player to whom the form will be shown. This player will be added to the edit list
     *               and checked for administrative permissions to determine the form content.
     */
    // Funktion zum Erstellen der Kit-Form
    public void showKitAdminForm(Player player) {
        List<String> kits = this.kitsManager.listKitFiles();
        kitsManager.addPlayerToList(player);

        if (player.hasPermission("ffagame.admin")) {
            FormWindowSimple window = new FormWindowSimple("Kits Admin", "Choose a kit or create a new one:");
            for (String kit : kits) {
                window.addButton(new ElementButton(kit));
            }

            window.addButton(new ElementButton(TextFormat.GREEN + "Create New Kit"));
            window.addButton(new ElementButton(TextFormat.YELLOW + "Manage Kits"));
            window.addButton(new ElementButton(TextFormat.RED + "BACK TO MENU"));
            player.showFormWindow(window);
        } else {
            FormWindowSimple window = new FormWindowSimple("Select a Kit", "Choose a kit for the Battle");
            for (String kit : kits) {
                window.addButton(new ElementButton(kit));
            }

            player.showFormWindow(window);
        }
    }

    /**
     * Manages and displays a list of available kits for the specified player, allowing them to choose a kit to manage.
     * It lists available kit files and adds each as a selectable option in a form window.
     * Also, the player is added to a list of players currently managing kits.
     *
     * @param player the player who is managing the kits and will view the form window with available kits
     */
    public void manageKitsListForm(Player player) {
        List<String> kits = this.kitsManager.listKitFiles();
        kitsManager.addPlayerToList(player);
        FormWindowSimple window = new FormWindowSimple("Manage all Kits", "Choose a Kit to manage");
        for (String kit : kits) {
            window.addButton(new ElementButton(kit));
        }

        player.showFormWindow(window);
    }

    /**
     * Manages the display and interaction of a kit form for a player, allowing them
     * to modify, apply, or delete a specific kit based on its name.
     *
     * @param kitName the name of the kit to be managed, used to retrieve the relevant items
     *                and setup the form window for modification.
     * @param player  the player who is interacting with the kit form, for whom the form window
     *                will be displayed and actions will be applied.
     */
    public void manageKitForm(String kitName, Player player) throws IOException {
        List<Integer> kitItems = kitsManager.getKitItems(kitName);

        kitsManager.addPlayerToList(player);

        FormWindowSimple window = new FormWindowSimple(kitName, "\nModify or apply this kit");

        for (Integer itemId : kitItems) {
            if (itemId != 0) {
                Item item = Item.get(itemId);
                window.addButton(new ElementButton(item.getName()));
            }
        }

        window.addButton(new ElementButton(TextFormat.YELLOW + "Change"));
        window.addButton(new ElementButton(TextFormat.RED + "Delete"));
        window.addButton(new ElementButton(TextFormat.GREEN + "Apply"));

        player.showFormWindow(window);
    }

    /**
     * Creates a form for managing an individual item in the specified kit.
     *
     * @param player   the player to whom the item management form will be shown
     * @param itemName the name of the item to be managed
     * @param kitName  the name of the kit containing the item
     */
    public void manageItemForm(Player player, String itemName, String kitName) {
        FormWindowSimple window = new FormWindowSimple(kitName + " - " + itemName, "Manage the item in kit");

        window.addButton(new ElementButton(TextFormat.RED + "Delete Item"));
        window.addButton(new ElementButton(TextFormat.RED + "Back to Kit Manage"));

        player.showFormWindow(window);
    }

    /**
     * Displays a form allowing the player to change items in a specified kit.
     *
     * This form notifies the player that submitting the form will overwrite the kit's inventory
     * with the current inventory of the player.
     *
     * @param kitName the name of the kit to be changed
     * @param player the player who is attempting to change the kit items
     */
    public void showChangeKitItemsForm(String kitName, Player player) {
        kitsManager.addPlayerToList(player);
        FormWindowCustom customForm = new FormWindowCustom(kitName + " - Change");
        customForm.addElement(new ElementLabel("If you press \"Send\", the Kit Inventory will automatically be overwritten with your current inventory"));
        customForm.addElement(new ElementInput("Change Kit Name:", "", kitName));

        player.showFormWindow(customForm);
    }

    /**
     * Displays a confirmation form to the player to confirm if they want to delete a specific kit.
     * The form includes two options: "Yes" and "No".
     *
     * @param kitName The name of the kit that is being considered for deletion.
     * @param player The player to whom the confirmation form will be shown.
     */
    public void showSureToDeleteKitForm(String kitName, Player player) {
        kitsManager.addPlayerToList(player);
        FormWindowSimple window = new FormWindowSimple(kitName, "Are you sure you want to delete this kit?");
        window.addButton(new ElementButton(TextFormat.GREEN + "Yes"));
        window.addButton(new ElementButton(TextFormat.RED + "No"));
        player.showFormWindow(window);
    }

    /**
     * Displays a form to the specified player to select a kit for the battle.
     *
     * @param player the player to whom the kit selection form will be shown
     */
    public void showKitForm(Player player) {
        kitsManager.addPlayerToList(player);
        List<String> kits = this.kitsManager.listKitFiles();
        FormWindowSimple window = new FormWindowSimple("Select a Kit", "Choose a kit for the Battle");
        for (String kit : kits) {
            window.addButton(new ElementButton(kit));
        }

        player.showFormWindow(window);
    }

    /**
     * Creates a custom form for kit creation and displays it to the player.
     * This form allows the player to input a name for a new kit and displays
     * their current inventory items as labels.
     *
     * @param player the player to whom the kit creation form is presented
     */
    // Funktion zum Erstellen des Custom Forms für die Kit-Erstellung
    public void createKitForm(Player player) {
        kitsManager.addPlayerToList(player);
        FormWindowCustom window = new FormWindowCustom("Create New Kit");
        window.addElement(new ElementInput("Kit Name:", "Enter the name of the kit"));

        List<Item> inventoryItems = new ArrayList<>(player.getInventory().getContents().values());
        for (Item item : inventoryItems) {
            window.addElement(new ElementLabel(item.getName()));
        }

        player.showFormWindow(window);
    }

    public void showChangeKitForm(Player player) {
        kitsManager.addPlayerToList(player);
        List<String> kits = this.kitsManager.listKitFiles();
        FormWindowSimple window = new FormWindowSimple("Change a Kit", "Choose a kit for the Battle");
        window.setContent("Current Kit: " + kitsManager.getPlayerKit(player));

        for (String kit : kits) {
            window.addButton(new ElementButton(kit));
        }

        player.showFormWindow(window);
    }
   
}
