package me.ncbpfluffybear.slimyrepair;

import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.handlers.ItemHandler;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The {@link SlimyAnvil} is a block that repairs
 * a config specified {@link SlimefunItem}.
 *
 * @author NCBPFluffyBear
 */

public class SlimyAnvil extends SlimefunItem implements RecipeDisplayItem {

    public SlimyAnvil() {
        super(SRItems.slimy_repair, SRItems.SLIMY_ANVIL, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
            new ItemStack(Material.SLIME_BLOCK), new ItemStack(Material.ANVIL), new ItemStack(Material.SLIME_BLOCK),
            new ItemStack(Material.ANVIL), new ItemStack(Material.IRON_BLOCK), new ItemStack(Material.ANVIL),
            new ItemStack(Material.SLIME_BLOCK), new ItemStack(Material.ANVIL), new ItemStack(Material.SLIME_BLOCK),
        });

        addItemHandler(onInteract());
    }

    private ItemHandler onInteract() {
        return (BlockUseHandler) e -> {
            Optional<Block> opt = e.getClickedBlock();
            if (!opt.isPresent()) {
                return;
            }

            Player p = e.getPlayer();
            Block b = opt.get();

            SlimefunItem sfBlock = BlockStorage.check(b);

            if (sfBlock != null && sfBlock == SRItems.SLIMY_ANVIL.getItem()) {
                ItemStack item = p.getInventory().getItemInMainHand();
                SlimefunItem sfItem = SlimefunItem.getByItem(item);
                ItemMeta meta = item.getItemMeta();

                if (meta instanceof Damageable) {
                    Damageable damageable = (Damageable) meta;

                    if (!SlimyRepair.repairMap.containsKey(sfItem)) {
                        send(p, "&c这个物品不能被修复!");
                        return;
                    }

                    // Show the material needed to repair the item
                    if (!p.isSneaking()) {

                        ItemStack repairItem = SlimyRepair.repairMap.get(sfItem).getFirstValue();
                        String itemName;
                        if (repairItem instanceof SlimefunItemStack) {
                            itemName = repairItem.getItemMeta().getDisplayName();
                        } else {
                            itemName = toTitleCase(repairItem.getType().name().replace('_', ' '));
                        }
                        send(p, "&a这个物品可以被修复!" +
                            "\n  &b需求: &e" + itemName +
                            "\n  &b修复: &e" + SlimyRepair.repairMap.get(sfItem).getSecondValue() + " 耐久度" +
                            "\n  &6下蹲右键粘液科技铁砧来修复此物品!");
                        return;
                    }

                    // Block interaction if the item is repaired AFTER checking for sneaking in case
                    // the player only wants to see item info.
                    if (!damageable.hasDamage()) {
                        send(p, "&c物品已被修复!");
                        return;
                    }

                    // Check for materials then fix
                    if (p.getInventory().containsAtLeast(SlimyRepair.repairMap.get(sfItem).getFirstValue(), 1)) {

                        int damage = damageable.getDamage();

                        p.getInventory().removeItem(SlimyRepair.repairMap.get(sfItem).getFirstValue());
                        damageable.setDamage(damage - SlimyRepair.repairMap.get(sfItem).getSecondValue());
                        item.setItemMeta(meta);

                        send(p, "&a你的物品已经被修复!");
                    } else {
                        send(p, "&c修复材料不足!");
                    }
                }

            }
        };
    }

    @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> displayRecipes = new ArrayList<>();

        SlimyRepair.repairMap.forEach((key, mat) -> {
            ItemStack item = key.getItem().clone();
            ItemMeta meta = item.getItemMeta();
            ((Damageable) meta).setDamage(item.getType().getMaxDurability() - mat.getSecondValue());
            item.setItemMeta(meta);
            displayRecipes.add(item);
            displayRecipes.add(mat.getFirstValue());
        });


        return displayRecipes;
    }

    private void send(Player p, String message) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&a&l粘液科技&7&l铁砧&8] " + message));
    }

    public static String toTitleCase(String givenString) {
        String[] arr = givenString.toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();

        for (String s : arr) {
            sb.append(Character.toUpperCase(s.charAt(0)))
                .append(s.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
