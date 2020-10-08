package me.gimme.gimmetag.item;

import me.gimme.gimmetag.config.BouncyProjectileConfig;
import me.gimme.gimmetag.item.entities.BouncyProjectile;
import me.gimme.gimmetag.sfx.PlayableSound;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BouncyProjectileItem extends AbilityItem {

    private final Plugin plugin;
    private final double speed;
    private final double gravity;
    private final int maxExplosionTimerTicks;
    private final int groundExplosionTimerTicks;
    private final double restitutionFactor;
    private final double frictionFactor;
    private final boolean trail;
    private final boolean bounceMarks;
    private final boolean glowing;
    private final double radius;
    private final double power;

    @Nullable
    private ItemStack displayItem;
    @Nullable
    private PlayableSound explosionSound;

    public BouncyProjectileItem(@NotNull String name, @NotNull Material type, @NotNull BouncyProjectileConfig config,
                                @NotNull Plugin plugin) {
        super(name, type, config);

        this.plugin = plugin;
        this.speed = config.getSpeed();
        this.gravity = config.getGravity();
        this.maxExplosionTimerTicks = Ticks.secondsToTicks(config.getMaxExplosionTimer());
        this.groundExplosionTimerTicks = Ticks.secondsToTicks(config.getGroundExplosionTimer());
        this.restitutionFactor = config.getRestitutionFactor();
        this.frictionFactor = config.getFrictionFactor();
        this.trail = config.getTrail();
        this.bounceMarks = config.getBounceMarks();
        this.glowing = config.getGlowing();
        this.radius = config.getRadius();
        this.power = config.getPower();

        setUseSound(SoundEffects.THROW);
    }

    protected abstract void onExplode(@NotNull Projectile projectile);

    protected abstract void onHitEntity(@NotNull Projectile projectile, @NotNull Entity entity);

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        BouncyProjectile bouncyProjectile = BouncyProjectile.launch(plugin, user, speed, maxExplosionTimerTicks, displayItem);

        bouncyProjectile.setOnExplode((projectile) -> {
            if (explosionSound != null) explosionSound.play(projectile.getLocation());
            onExplode(projectile);
        });
        bouncyProjectile.setOnHitEntity(this::onHitEntity);
        bouncyProjectile.setGroundExplosionTimerTicks(groundExplosionTimerTicks);
        bouncyProjectile.setGravity(gravity);
        bouncyProjectile.setRestitutionFactor(restitutionFactor);
        bouncyProjectile.setFrictionFactor(frictionFactor);
        bouncyProjectile.setTrail(trail);
        bouncyProjectile.setBounceMarks(bounceMarks);
        bouncyProjectile.setGlowing(glowing);

        return true;
    }

    protected void setDisplayItem(@NotNull Material material, boolean enchanted) {
        ItemStack itemStack = new ItemStack(material);

        if (enchanted) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            CustomItem.setGlowing(Objects.requireNonNull(itemMeta), true);
            itemStack.setItemMeta(itemMeta);
        }

        this.displayItem = itemStack;

    }

    protected void setExplosionSound(@NotNull PlayableSound explosionSound) {
        this.explosionSound = explosionSound;
    }

    protected double getRadius() {
        return radius;
    }

    protected double getPower() {
        return power;
    }
}
