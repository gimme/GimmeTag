package me.gimme.gimmetag.item;

import me.gimme.gimmetag.sfx.SoundEffect;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbilityItem extends CustomItem {

    private static final String USE_RESPONSE_MESSAGE_FORMAT = "" + ChatColor.RESET + ChatColor.YELLOW;

    private int cooldownTicks;
    private boolean consumable;
    @Nullable
    private String useResponseMessage;
    private boolean muted = false;

    public AbilityItem(@NotNull String name, @NotNull Material type, boolean glowing, double cooldown, boolean consumable,
                       @Nullable String useResponseMessage) {
        super(name, type, glowing);

        this.cooldownTicks = (int) Math.round(cooldown * 20);
        this.consumable = consumable;
        this.useResponseMessage = useResponseMessage;
    }

    public AbilityItem(@NotNull String id, @NotNull String displayName, @NotNull Material type, boolean glowing,
                       double cooldown, boolean consumable, @Nullable String useResponseMessage) {
        super(id, displayName, type, glowing);

        this.cooldownTicks = (int) Math.round(cooldown * 20);
        this.consumable = consumable;
        this.useResponseMessage = useResponseMessage;
    }

    @Override
    @NotNull
    public ItemStack createItemStack(int amount) {
        ItemStack itemStack = super.createItemStack(amount);
        if (cooldownTicks <= 0) return itemStack;

        ItemMeta itemMeta = itemStack.getItemMeta();
        Objects.requireNonNull(itemMeta).setDisplayName(itemMeta.getDisplayName()
                + ChatColor.RESET + ChatColor.GRAY + ChatColor.ITALIC + " [" + getCooldown() + " Cooldown]");

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void use(@NotNull ItemStack itemStack, @NotNull Player user) {
        if (!onUse(itemStack, user)) return;

        if (consumable) itemStack.setAmount(itemStack.getAmount() - 1);
        if (cooldownTicks > 0) user.setCooldown(itemStack.getType(), cooldownTicks);
        if (useResponseMessage != null && !useResponseMessage.isEmpty())
            user.sendMessage(USE_RESPONSE_MESSAGE_FORMAT + useResponseMessage);
        if (!muted) SoundEffect.USE_EFFECT.play(user);
    }

    protected abstract boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user);

    protected void mute() {
        muted = true;
    }

    protected String getCooldown() {
        return formatSeconds(cooldownTicks);
    }
}