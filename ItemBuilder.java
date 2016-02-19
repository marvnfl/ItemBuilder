package com.minepire.plugins.core.utils;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.*;

/**
 * Implementation of a builder pattern used to create an {@link ItemStack}.
 * <p>
 * Example usage:
 * {@code ItemStack example = new ItemBuilder(Material.ARROW, 32).name("Custom Arrow").build();}
 *
 * @author marvnfl (3ptO)
 */
public class ItemBuilder {

    private ItemStack item;
    private ItemMeta itemMeta;

    /**
     * Constructs a new ItemBuilder.
     *
     * @param itemStack The {@link ItemStack} to build.
     */
    public ItemBuilder(ItemStack itemStack) {
        item = itemStack;
        itemMeta = item.getItemMeta();
    }

    /**
     * Constructs a new ItemBuilder.
     *
     * @param material The type of the item.
     */
    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    /**
     * Constructs a new ItemBuilder.
     *
     * @param material The type of item.
     * @param quantity The item quantity.
     */
    public ItemBuilder(Material material, int quantity) {
        this(new ItemStack(material, quantity));
    }

    /**
     * Constructs a new ItemBuilder.
     *
     * @param material   The type of item.
     * @param quantity   The item quantity.
     * @param durability The item's damage value
     */
    public ItemBuilder(Material material, int quantity, int durability) {
        this(new ItemStack(material, quantity, (short) durability));
    }

    /**
     * Builds an {@link ItemStack} from the current ItemBuilder instance.
     *
     * @return The resulting {@link ItemStack}.
     */
    public ItemStack build() {
        // Applies ItemMeta to the ItemStack.
        item.setItemMeta(itemMeta);

        return item;
    }

    /**
     * Changes the type of the item.
     *
     * @param material The new type.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder type(Material material) {
        item.setType(material);

        return this;
    }

    /**
     * Changes the quantity of the item.
     *
     * @param quantity The new quantity.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder quantity(int quantity) {
        item.setAmount(quantity);

        return this;
    }

    /**
     * Changes the damage value of the item.
     *
     * @param durability The new damage value.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder durability(int durability) {
        item.setDurability((short) durability);

        return this;
    }

    /**
     * Changes the display name of the item.
     *
     * @param name The new display name.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder name(String name) {
        itemMeta.setDisplayName(name);

        return this;
    }

    /**
     * Adds lines to the lore of the item.
     *
     * @param lore The lines to add.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder addLore(String... lore) {
        List<String> loreList = itemMeta.hasLore()
                ? itemMeta.getLore() : new ArrayList<>();
        Collections.addAll(loreList, lore);

        return setLore(loreList);
    }

    /**
     * Changes the lore of the item.
     *
     * @param lore The new lore.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder setLore(List<String> lore) {
        itemMeta.setLore(lore);

        return this;
    }

    /**
     * Adds {@link ItemFlag}s to the item.
     *
     * @param flags The flags to add.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder addFlags(ItemFlag... flags) {
        itemMeta.addItemFlags(flags);

        return this;
    }

    /**
     * Adds an {@link Enchantment} to the item.
     *
     * @param enchant      The {@link Enchantment} to add.
     * @param level        The level of the {@link Enchantment}.
     * @param forceEnchant Forces the enchantment on an item.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder enchant(Enchantment enchant, int level, boolean forceEnchant) {
        itemMeta.addEnchant(enchant, level, forceEnchant);

        return this;
    }

    /**
     * Toggles whether the item should be glowing.
     * <p>
     * Uses the LURE {@link Enchantment} to apply an enchantment to the item.
     * Removes the enchantment identifier by applying the HIDE_ENCHANTS {@link ItemFlag}.
     *
     * @param glow If the item should be glowing.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder glow(boolean glow) {
        boolean isGlowing = (itemMeta.hasEnchant(Enchantment.LURE) && itemMeta.getEnchants().size() == 1
                && itemMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS) && itemMeta.getItemFlags().size() == 1);

        /*
         * Checks if the item is already enchanted (glowing). If true, throws an exception.
         */
        try {
            if (glow && !isGlowing && itemMeta.hasEnchants()) {
                throw new RuntimeException("\n Cannot apply glow to an already enchanted item.");
            }
        } catch (RuntimeException exception) {
            exception.printStackTrace();

            return this;
        }

        if (glow && !isGlowing) {
            addFlags(ItemFlag.HIDE_ENCHANTS);
            enchant(Enchantment.LURE, 1, true);
        } else if (!glow && isGlowing) {
            clearFlags();
            clearEnchants();
        }

        return this;
    }

    /**
     * Clears any lore associated with the item.
     *
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder clearLore() {
        return setLore(null);
    }

    /**
     * Removes specified lines from the lore of the item.
     *
     * @param indices The lines to remove.
     * @return The current ItemBuilder instance.
     */

    public ItemBuilder removeLore(int... indices) {
        try {
            if (containsDuplicates(indices)) {
                throw new RuntimeException("\n Param list cannot contain duplicate values.");
            }
        } catch (RuntimeException exception) {
            exception.printStackTrace();

            return this;
        }

        /*
         * Creates a copied list of the item's current lore.
         */
        List<String> loreCopy = new ArrayList<>(itemMeta.getLore());

        /*
         * Removes specified indices (lines) from the copied list.
         *
         * Sorts and then reverses the array to remove elements without a
         */
        for (int index : sortDescending(indices)) {
            loreCopy.remove(index);
        }

        /*
         * Overwrites the item's lore to the copied list that does not include the removed lines.
         */
        return setLore(loreCopy);
    }

    /**
     * Clears any {@link ItemFlag}s associated with the item.
     *
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder clearFlags() {
        return removeFlags(itemMeta.getItemFlags().toArray(new ItemFlag[itemMeta.getItemFlags().size()]));
    }

    /**
     * Removes specified {@link ItemFlag}s from the item.
     *
     * @param flags The flags to remove.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder removeFlags(ItemFlag... flags) {
        itemMeta.removeItemFlags(flags);

        return this;
    }

    /**
     * Clears any {@link Enchantment}s associated with the item.
     *
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder clearEnchants() {
        if (itemMeta.hasEnchants()) {
            itemMeta.getEnchants().clear();
        }

        return this;
    }

    /**
     * Removes specified {@link Enchantment} from the item.
     *
     * @param enchants The {@link Enchantment}s to remove.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder removeEnchants(Enchantment... enchants) {
        if (itemMeta.hasEnchants()) {
            for (Enchantment enchant : enchants) {
                itemMeta.getEnchants().remove(enchant);
            }
        }

        return this;
    }

    /**
     * Toggles whether the item should be unbreakable.
     *
     * @param unbreakable If the item should be unbreakable.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        itemMeta.spigot().setUnbreakable(unbreakable);

        return this;
    }

    /* ---------------------- PLAYER SKULLS ---------------------- */

    /**
     * Changes the owner of the skull (player head).
     *
     * @param owner The new owner.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder skullOwner(String owner) {
        if (item.getType().equals(Material.SKULL_ITEM) && item.getDurability() == 3) {
            ((SkullMeta) itemMeta).setOwner(owner);
        }

        return this;
    }

    /**
     * Removes the owner from the skull (player head).
     *
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder removeSkullOwner() {
        return skullOwner(null);
    }

    /* ------------------------- POTIONS ------------------------- */

    /**
     * Changes the type of the potion.
     *
     * @param effect   The new potion type.
     * @param level    The new potion level.
     * @param splash   If the potion should be throwable.
     * @param extended If the potion should have an extended duration.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder potion(PotionType effect, int level, boolean splash, boolean extended) {
        if (item.getType().equals(Material.POTION)) {
            Potion potion = new Potion(effect, level);

            potion.setSplash(splash);
            potion.setHasExtendedDuration(extended);

            potion.apply(item);
        }

        return this;
    }

    /* ---------------------- LEATHER ARMOR ---------------------- */

    /**
     * Changes the color of the leather armor piece.
     *
     * @param color The new {@link Color} value.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder armorColor(Color color) {
        if (item.getType().equals(Material.LEATHER_BOOTS)
                || item.getType().equals(Material.LEATHER_CHESTPLATE)
                || item.getType().equals(Material.LEATHER_HELMET)
                || item.getType().equals(Material.LEATHER_LEGGINGS)) {
            ((LeatherArmorMeta) itemMeta).setColor(color);
        }

        return this;
    }

    /**
     * Removes the color from the leather armor piece.
     *
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder removeArmorColor() {
        return armorColor(null);
    }

    /* ------------------------ FIREWORKS ------------------------ */

    /**
     * Changes the power (height) of the firework.
     * <p>
     * Each level of power is half a second of flight time, i.e., 1 power = 0.5 seconds.
     *
     * @param power The new power value.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder fireworkPower(int power) {
        if (item.getType().equals(Material.FIREWORK)) {
            ((FireworkMeta) itemMeta).setPower(power);
        }

        return this;
    }

    /**
     * Adds {@link FireworkEffect}s to the firework.
     *
     * @param effects The {@link FireworkEffect}s to add.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder addEffects(FireworkEffect... effects) {
        if (item.getType().equals(Material.FIREWORK)) {
            ((FireworkMeta) itemMeta).addEffects(effects);
        }

        return this;
    }

    /**
     * Clears any {@link FireworkEffect}s associated with the firework.
     *
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder clearEffects() {
        if (item.getType().equals(Material.FIREWORK)) {
            ((FireworkMeta) itemMeta).clearEffects();
        }

        return this;
    }

    /**
     * Removes specified {@link FireworkEffect}s from the firework.
     *
     * @param effects The {@link FireworkEffect}s to remove.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder removeEffects(FireworkEffect... effects) {
        if (item.getType().equals(Material.FIREWORK)) {
            List<FireworkEffect> effectsList = new ArrayList<>();
            Collections.addAll(effectsList, effects);

            for (int i = ((FireworkMeta) itemMeta).getEffectsSize() - 1; i >= 0; i--) {
                if (effectsList.contains(((FireworkMeta) itemMeta).getEffects().get(i))) {
                    ((FireworkMeta) itemMeta).removeEffect(i);
                }
            }
        }

        return this;
    }

    /* ------------------------- BANNERS ------------------------- */

    /**
     * Changes the color of the banner.
     *
     * @param color The new color.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder bannerColor(DyeColor color) {
        if (item.getType().equals(Material.BANNER)) {
            ((BannerMeta) itemMeta).setBaseColor(color);
        }

        return this;
    }

    /**
     * Adds a {@link Pattern} to the banner.
     *
     * @param pattern The {@link Pattern} to add.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder addPattern(Pattern pattern) {
        if (item.getType().equals(Material.BANNER)) {
            ((BannerMeta) itemMeta).addPattern(pattern);
        }

        return this;
    }

    /**
     * Changes a specified {@link Pattern} of the banner.
     *
     * @param index   The {@link Pattern} to change.
     * @param pattern The new {@link Pattern}.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder setPattern(int index, Pattern pattern) {
        if (item.getType().equals(Material.BANNER)) {
            ((BannerMeta) itemMeta).setPattern(index, pattern);
        }

        return this;
    }

    /**
     * Changes the current {@link Pattern}s of the banner.
     *
     * @param patterns The new {@link Pattern}s.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder setPatterns(Pattern... patterns) {
        if (item.getType().equals(Material.BANNER)) {
            List<Pattern> patternsList = new ArrayList<>();
            Collections.addAll(patternsList, patterns);

            ((BannerMeta) itemMeta).setPatterns(patternsList);
        }

        return this;
    }

    /**
     * Clears any {@link Pattern}s associated with the banner.
     *
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder clearPatterns() {
        return setPatterns((Pattern[]) null);
    }

    /**
     * Removes specified {@link Pattern}s from the banner.
     *
     * @param patterns The {@link Pattern}s to remove.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder removePatterns(Pattern... patterns) {
        if (item.getType().equals(Material.BANNER)) {
            List<Pattern> patternsList = new ArrayList<>();
            Collections.addAll(patternsList, patterns);

            for (int i = ((BannerMeta) itemMeta).numberOfPatterns() - 1; i >= 0; i--) {
                if (patternsList.contains(((BannerMeta) itemMeta).getPatterns().get(i))) {
                    ((BannerMeta) itemMeta).removePattern(i);
                }
            }
        }

        return this;
    }

    /* -------------------------- BOOKS -------------------------- */

    /**
     * Changes the title of the book.
     *
     * @param title The new title.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder title(String title) {
        if (item.getType().equals(Material.WRITTEN_BOOK)) {
            ((BookMeta) itemMeta).setTitle(title);
        }

        return this;
    }

    /**
     * Changes the author of the book.
     *
     * @param author The new author.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder author(String author) {
        if (item.getType().equals(Material.WRITTEN_BOOK)) {
            ((BookMeta) itemMeta).setAuthor(author);
        }

        return this;
    }

    /**
     * Adds pages to the end of the book.
     *
     * @param pages The new pages to add.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder addPages(String... pages) {
        if (item.getType().equals(Material.WRITTEN_BOOK)) {
            ((BookMeta) itemMeta).addPage(pages);
        }

        return this;
    }

    /**
     * Changes a specified page of the book.
     *
     * @param index   The page to change.
     * @param content The new page content.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder setPage(int index, String content) {
        if (item.getType().equals(Material.WRITTEN_BOOK)) {
            ((BookMeta) itemMeta).setPage(index, content);
        }

        return this;
    }

    /**
     * Changes the current pages of the book.
     *
     * @param pages The new pages.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder setPages(String... pages) {
        if (item.getType().equals(Material.WRITTEN_BOOK)) {
            ((BookMeta) itemMeta).setPages();
        }

        return this;
    }

    /**
     * Clears any pages associated with the book.
     *
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder clearPages() {
        return setPages((String[]) null);
    }

    /**
     * Removes specified pages from the book.
     *
     * @param indices The pages to remove.
     * @return The current ItemBuilder instance.
     */
    public ItemBuilder removePages(int... indices) {
        try {
            if (containsDuplicates(indices)) {
                throw new RuntimeException("\n Param list cannot contain duplicates.");
            }
        } catch (RuntimeException exception) {
            exception.printStackTrace();

            return this;
        }

        if (item.getType().equals(Material.WRITTEN_BOOK)) {
            /*
             * Creates a copied list of the book's current pages.
             */
            List<String> pagesCopy = new ArrayList<>(((BookMeta) itemMeta).getPages());

            /*
             * Removes the specified indices (pages) from the copied list.
             */
            for (int index : sortDescending(indices)) {
                pagesCopy.remove(index);
            }

            /*
             * Overwrites the book's pages to the copied list that does not include the removed pages.
             */
            ((BookMeta) itemMeta).setPages(pagesCopy);
        }

        return this;
    }

    /* ------------------------ UTILITIES ------------------------ */

    /**
     * Sorts the param list in descending order by first sorting in ascending order and then swapping elements.
     * <p>
     * This ensures the correct lines will be removed considering the list is modified as elements are removed.
     *
     * @param array The array to sort.
     * @return The array sorted in descending order.
     */
    public int[] sortDescending(int[] array) {
        Arrays.sort(array);

        for (int left = 0, right = array.length - 1; left < right; left++, right--) {
            int temp = array[left];
            array[left] = array[right];
            array[right] = temp;
        }

        return array;
    }

    /**
     * Tests to see if the array contains duplicate values.
     *
     * @param array The array to test.
     * @return True, if the array contains duplicate values.
     */
    public boolean containsDuplicates(int[] array) {
        Set<Integer> set = new HashSet<>();
        for (int i = array.length - 1; i >= 0; i--) {
            set.add(array[i]);
        }

        return array.length != set.size();
    }
}
