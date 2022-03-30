package ru.xfenilafs.lobby.utils.hologram;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.redline.core.bukkit.ApiManager;
import ru.redline.core.bukkit.holographic.ProtocolHolographic;
import ru.redline.core.bukkit.protocollib.entity.impl.FakeArmorStand;
import ru.xfenilafs.lobby.utils.AbstractCacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class HologramManager extends AbstractCacheManager<Hologram> {

    public void cacheHologram(String hologramName, Hologram hologram) {
        cacheData(hologramName.toLowerCase(), hologram);
    }

    public Hologram getCachedHologram(String hologramName) {
        return getCache(hologramName.toLowerCase());
    }

    public void createHologram(String hologramName, Location location, Applicable<Hologram> hologramApplicable) {
        Hologram hologram = createHologram(location);

        cacheHologram(hologramName, hologram);

        hologramApplicable.apply(hologram);
    }

    public Hologram createHologram(Location location) {
        return new HologramImpl(location);
    }


    @Getter
    public static class HologramImpl implements Hologram {

        private Location location;
        private final ProtocolHolographic hologram;

        public HologramImpl(Location location) {
            this.location = location;
            this.hologram = ApiManager.createHologram(location);
        }

        @Override
        public void addLine(String line) {
            hologram.addTextLine(line);
        }

        @Override
        public void addItem(Material material) {
            hologram.addDropLine(new ItemStack(material));
        }

        @Override
        public void modifyLine(int index, String line) {
            hologram.setTextLine(index, line);
        }

        @Override
        public void spawn() {
            hologram.spawn();
        }

        @Override
        public void addReceiver(Player player) {
            hologram.addReceivers(player);
        }

        @Override
        public void remove() {
            hologram.remove();
        }

        @Override
        public void removeReceiver(Player player) {
            hologram.removeReceivers(player);
        }

        @Override
        public void setLocation(Location location) {
            this.location = location;

            hologram.teleport(location);
        }

        @Override
        public void refreshHologram() {
            hologram.update();
        }
    }

}