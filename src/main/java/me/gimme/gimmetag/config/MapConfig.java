package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class MapConfig<T> extends AbstractConfig<Map<String, T>> {
    MapConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    MapConfig(@NotNull ConfigurationSection configurationSection, @NotNull String path) {
        super(configurationSection, path);
    }

    @NotNull
    @Override
    public Map<String, T> getValue() {
        return Objects.requireNonNull(getConfigurationSection().getConfigurationSection(getPath())).getValues(false).entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, e -> (T) e.getValue()));
    }
}
