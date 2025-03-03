package me.ncbpfluffybear.slimyrepair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.cscorelib2.collections.Pair;
import me.mrCookieSlime.Slimefun.cscorelib2.config.Config;
import me.mrCookieSlime.Slimefun.cscorelib2.updater.GitHubBuildsUpdater;

public class SlimyRepair extends JavaPlugin implements SlimefunAddon {

    public static SlimyRepair instance;
    public static HashMap<SlimefunItem, Pair<ItemStack, Integer>> repairMap = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        // Read something from your config.yml
        Config cfg = new Config(this);
        Config repairs = new Config(this, "repairs.yml");

        if (cfg.getBoolean("options.auto-update") && getDescription().getVersion().startsWith("DEV - ")) {
            new GitHubBuildsUpdater(this, getFile(), "NCBPFluffyBear/SlimyRepair/master/").start();
        }

        final File repairsFile = new File(getInstance().getDataFolder(), "repairs.yml");
        if (!repairsFile.exists()) {
            try {
                Files.copy(this.getClass().getResourceAsStream("/repairs.yml"), repairsFile.toPath());
            } catch (IOException e) {
                getInstance().getLogger().log(Level.SEVERE, "Failed to copy default repairs.yml file", e);
            }
        }

        for (String key : repairs.getKeys()) {
            SlimefunItem sfItem = SlimefunItem.getByID(key);
            String matType = repairs.getString(key + ".material-type");

            if (sfItem != null) {
                String material = repairs.getString(key + ".material");
                int repairAmt = repairs.getInt(key + ".repair-amount");

                if (matType.equalsIgnoreCase("vanilla")) {
                    Material vanillaMat = Material.getMaterial(material);

                    if (vanillaMat != null) {
                        repairMap.put(sfItem, new Pair<>(new ItemStack(vanillaMat), repairAmt));
                    }

                } else if (matType.equalsIgnoreCase("slimefun")) {
                    SlimefunItem sfMat = SlimefunItem.getByID(material);

                    if (sfMat != null) {
                        repairMap.put(sfItem, new Pair<>(sfMat.getItem(), repairAmt));
                    }
                }
            }
        }

        new SlimyAnvil().register(this);

    }

    @Override
    public void onDisable() {
        // Logic for disabling the plugin...
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/NCBPFluffyBear/SlimyRepair/issues";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    public static SlimyRepair getInstance() {
        return instance;
    }

}
