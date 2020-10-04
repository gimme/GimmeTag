package me.gimme.gimmetag.item;

import me.gimme.gimmecore.util.RomanNumerals;
import me.gimme.gimmetag.config.AbilityItemConfig;
import me.gimme.gimmetag.sfx.SoundEffect;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a custom item that can be used to create item stacks with abilities tied to them.
 * <p>
 * The ability of an item stack is used by right clicking with it.
 */
public abstract class AbilityItem extends CustomItem {

    private static final String USE_RESPONSE_MESSAGE_FORMAT = "" + ChatColor.RESET + ChatColor.YELLOW;

    private boolean consumable;
    private int cooldownTicks;
    private int durationTicks;
    private int level;

    @Nullable
    private String useResponseMessage;
    private boolean muted;
    private boolean showCooldown;
    private boolean showDuration;
    private boolean showLevel;

    /**
     * Creates a new ability item with the specified name, item type and configuration.
     *
     * @param name   a unique name and the display name of this item. The name can contain ChatColors and spaces, which
     *               will be stripped from the name and only be used for the display name.
     * @param type   the item type of the generated item stacks
     * @param config a config containing values to be applied to this ability item's variables
     */
    public AbilityItem(@NotNull String name, @NotNull Material type, @NotNull AbilityItemConfig config) {
        super(name, type);

        init(config);
    }

    /**
     * Creates a new ability item with the specified name, display name, item type and configuration.
     *
     * @param id          a unique name for this item
     * @param displayName the display name of this item
     * @param type        the item type of the generated item stacks
     * @param config      a config containing values to be applied to this ability item's variables
     */
    public AbilityItem(@NotNull String id, @NotNull String displayName, @NotNull Material type,
                       @NotNull AbilityItemConfig config) {
        super(id, displayName, type);

        init(config);
    }

    /**
     * Initializes this item's variables based on the given configuration.
     *
     * @param config the configuration to get the values from
     */
    private void init(@NotNull AbilityItemConfig config) {
        this.consumable = config.isConsumable();
        setCooldown(config.getCooldown());
        this.durationTicks = Ticks.secondsToTicks(config.getDuration());
        this.level = config.getLevel();

        showCooldown(cooldownTicks > 0);
        showDuration(durationTicks > 0);
        showLevel(level > 0);
    }

    @Override
    @NotNull
    public ItemStack createItemStack(int amount) {
        ItemStack itemStack = super.createItemStack(amount);
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

        if (showCooldown) {
            List<String> lore = itemMeta.getLore() != null ? itemMeta.getLore() : new ArrayList<>();
            lore.add(0, ChatColor.GRAY + formatSeconds(cooldownTicks) + " Cooldown");
            itemMeta.setLore(lore);
        }
        if (showLevel) setLevelInfo(itemMeta, level);
        if (showDuration) setDurationInfo(itemMeta, durationTicks);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Attempts to use this item's ability and returns if it was successful or not.
     * <p>
     * A use can be unsuccessful if the item is on cooldown or the implemented item's ability failed internally.
     *
     * @param itemStack the item stack that was used to activate its ability
     * @param user      the player that used the item stack
     * @return if the use was successful
     */
    boolean use(@NotNull ItemStack itemStack, @NotNull Player user) {
        if (user.hasCooldown(itemStack.getType())) return false;
        if (!onUse(itemStack, user)) return false;

        if (consumable) itemStack.setAmount(itemStack.getAmount() - 1);
        if (cooldownTicks > 0) user.setCooldown(itemStack.getType(), cooldownTicks);
        if (useResponseMessage != null && !useResponseMessage.isEmpty())
            user.sendMessage(USE_RESPONSE_MESSAGE_FORMAT + useResponseMessage);
        if (!muted) SoundEffect.USE_EFFECT.play(user);

        return true;
    }

    /**
     * Performs the ability of this item and returns if it was successful or not.
     * <p>
     * A use can be unsuccessful for any reason defined in the specific implementation.
     *
     * @param itemStack the item stack that was used to activate the ability
     * @param user      the player that used the item stack
     * @return if the use was successful
     */
    protected abstract boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user);

    void setCooldown(double seconds) {
        this.cooldownTicks = Ticks.secondsToTicks(seconds);
    }

    protected double getCooldown() {
        return Ticks.ticksToSeconds(cooldownTicks);
    }

    protected int getCooldownTicks() {
        return cooldownTicks;
    }

    protected double getDuration() {
        return Ticks.ticksToSeconds(durationTicks);
    }

    protected int getDurationTicks() {
        return durationTicks;
    }

    /**
     * Returns an amplifier based on this item's level, which is useful when applying potion effects.
     * <p>
     * The amplifier is always 1 less than the level. For example, the potion effect "Speed I" has amplifier 0, "Speed
     * II" has amplifier 1, and so on.
     *
     * @return the amplifier, used for potion effects
     */
    protected int getAmplifier() {
        return level - 1;
    }

    /**
     * Sets the message that is sent to the player after using this item's ability. The message can be null to not send
     * any message.
     *
     * @param message the message to send to the player after each use, or null for no message
     */
    protected void setUseResponseMessage(@Nullable String message) {
        this.useResponseMessage = message;
    }

    /**
     * Mutes the default sound effect that is played after using this item's ability.
     */
    protected void mute() {
        muted = true;
    }

    /**
     * Sets if the cooldown of this item should be displayed in the lore.
     *
     * @param showCooldown if the cooldown should be displayed in the lore
     */
    void showCooldown(boolean showCooldown) {
        this.showCooldown = showCooldown;
    }

    /**
     * Sets if the duration of this item should be displayed next to the display name.
     *
     * @param showDuration if the duration should be displayed next to the display name
     */
    void showDuration(boolean showDuration) {
        this.showDuration = showDuration;
    }

    /**
     * Sets if the level of this item should be displayed next to the display name.
     *
     * @param showLevel if the level should be displayed next to the display name
     */
    private void showLevel(boolean showLevel) {
        this.showLevel = showLevel;
    }


    /**
     * Appends the specified duration information to the display name of the given item meta.
     *
     * @param itemMeta      the item meta whose display name to edit
     * @param durationTicks the duration in ticks
     */
    private static void setDurationInfo(@NotNull ItemMeta itemMeta, int durationTicks) {
        itemMeta.setDisplayName(itemMeta.getDisplayName() + getFormattedDuration(durationTicks));
    }

    /**
     * Appends the specified level to the display name of the given item meta.
     *
     * @param itemMeta the item meta whose display name to edit
     * @param level    the level to append to the display name
     */
    private static void setLevelInfo(@NotNull ItemMeta itemMeta, int level) {
        itemMeta.setDisplayName(itemMeta.getDisplayName() + " " + RomanNumerals.toRoman(level));
    }

    /**
     * Converts the specified ticks into seconds and returns it in a string formatted according to the standard way to
     * display durations.
     *
     * @param durationTicks the duration in ticks to be converted into seconds
     * @return a formatted string showing the specified duration
     */
    public static String getFormattedDuration(int durationTicks) {
        return "" + ChatColor.RESET + ChatColor.GRAY + " (" + formatSeconds(durationTicks) + ")" + ChatColor.RESET;
    }
}
