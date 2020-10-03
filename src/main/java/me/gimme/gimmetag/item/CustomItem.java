package me.gimme.gimmetag.item;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a custom item that can be used to create item stacks to be used in game.
 */
public abstract class CustomItem {

    private static final NamespacedKey ID_KEY = new NamespacedKey(GimmeTag.getPlugin(), "CustomItemId");
    private static final NamespacedKey UNIQUE_ID_KEY = new NamespacedKey(GimmeTag.getPlugin(), "UniqueId");
    private static final PersistentDataType<String, String> ID_DATA_TYPE = PersistentDataType.STRING;
    private static final PersistentDataType<String, String> UNIQUE_ID_DATA_TYPE = PersistentDataType.STRING;

    private final String id;
    private final String displayName;
    private final Material type;
    private boolean glowing = true;

    /**
     * Creates a new custom item with the specified name and item type.
     *
     * @param name a unique name and the display name of this item. The name can contain ChatColors and spaces, which
     *             will be stripped from the name and only be used for the display name.
     * @param type the item type of the generated item stacks
     */
    public CustomItem(@NotNull String name, @NotNull Material type) {
        this(name, name, type);
    }

    /**
     * Creates a new custom item with the specified name, display name and item type.
     *
     * @param id          a unique name for this item
     * @param displayName the display name of this item
     * @param type        the item type of the generated item stacks
     */
    public CustomItem(@NotNull String id, @NotNull String displayName, @NotNull Material type) {
        this.id = ChatColor.stripColor(id).toLowerCase().replaceAll(" ", "_");
        this.displayName = displayName;
        this.type = type;
    }

    /**
     * Creates an item stack from this custom item to be used in game.
     */
    @NotNull
    public ItemStack createItemStack() {
        return createItemStack(1);
    }

    /**
     * Creates an ItemStack from this custom item, with the specified stack size, to be used in game.
     *
     * @param amount the stack size
     * @return the created item stack
     */
    @NotNull
    public ItemStack createItemStack(int amount) {
        ItemStack itemStack = new ItemStack(type, amount);

        ItemMeta itemMeta = itemStack.getItemMeta();
        Objects.requireNonNull(itemMeta);

        itemMeta.setDisplayName(displayName);

        setGlowing(itemStack, glowing);

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        dataContainer.set(ID_KEY, ID_DATA_TYPE, id);
        dataContainer.set(UNIQUE_ID_KEY, UNIQUE_ID_DATA_TYPE, UUID.randomUUID().toString());

        onCreate(itemStack, itemMeta);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    /**
     * Does something with the newly created item stack.
     * <p>
     * This can be implemented to do extra changes on the item stack before finishing the creation of it.
     * <p>
     * {@link ItemStack#setItemMeta(ItemMeta)} will be called automatically after this method returns and does not need
     * to be explicitly called.
     *
     * @param itemStack the newly created item stack
     * @param itemMeta  the item meta of the newly created item stack
     */
    protected abstract void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta);

    /**
     * @return the unique name of this custom item
     */
    @NotNull
    public String getId() {
        return id;
    }

    /**
     * Stops the created item stacks from glowing as if they are enchanted.
     * <p>
     * The glow effect is enabled by default.
     */
    protected void disableGlow() {
        this.glowing = false;
    }


    /**
     * Returns the given item stack's unique identifier, or null if it was not created from a custom item.
     * <p>
     * This identifier is unique for each item stack even if created from the same custom item.
     *
     * @param itemStack the item stack to get the unique identifier of
     * @return the given item stack's unique identifier, or null if it was not created from a custom item
     */
    @Nullable
    public static UUID getUniqueId(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;

        String uuidString = itemMeta.getPersistentDataContainer().get(UNIQUE_ID_KEY, UNIQUE_ID_DATA_TYPE);
        if (uuidString == null) return null;

        return UUID.fromString(uuidString);
    }

    /**
     * Returns the unique identifier of the custom item that the given item stack was created from, or null if not from
     * a custom item.
     * <p>
     * Note that this results in the same identifier for every item stack that was created from the same custom item.
     *
     * @param itemStack the item stack to get the custom item identifier of
     * @return the unique identifier of the custom item that the given item stack was created from, else null
     */
    @Nullable
    public static String getCustomItemId(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;

        return itemMeta.getPersistentDataContainer().get(ID_KEY, ID_DATA_TYPE);
    }

    /**
     * Returns if the given item stack was created from a custom item.
     *
     * @param itemStack the item stack to check if created from a custom item
     * @return if the given item stack was created from a custom item
     */
    public static boolean isCustomItem(@NotNull ItemStack itemStack) {
        return getCustomItemId(itemStack) != null;
    }

    /**
     * Sets the given item stack to be glowing, as if it was enchanted, or not.
     *
     * @param itemStack the item stack to modify
     * @param glowing   if the item stack should be glowing
     */
    protected static void setGlowing(@NotNull ItemStack itemStack, boolean glowing) {
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

        if (glowing) {
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            itemMeta.removeEnchant(Enchantment.LUCK);
            itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemStack.setItemMeta(itemMeta);
    }

    /**
     * Converts the specified ticks into seconds and returns it in a string with a trailing "s".
     *
     * @param ticks the time in ticks to be converted into seconds
     * @return the time in seconds with a trailing "s"
     */
    protected static String formatSeconds(int ticks) {
        return Ticks.ticksToSecondsString(ticks) + "s";
    }
}
