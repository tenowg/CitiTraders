/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.tehbeard.cititrader;

import java.util.Map;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author tenowg
 */
public class StockRoomTrait extends Trait implements InventoryHolder, StockRoomInterface {

    private Inventory stock;

    public StockRoomTrait() {
        this(54);
    }

    private StockRoomTrait(int size) {
        super("stockroom");
        if (size <= 0 || size > 54) {
            throw new IllegalArgumentException("Size must be between 1 and 54");
        }

        stock = Bukkit.createInventory(this, size, "Stockroom");
    }

    @Override
    public void load(DataKey data) throws NPCLoadException {
        //Load the inventory
        for (DataKey slotKey : data.getRelative("inv").getIntegerSubKeys()) {
            stock.setItem(Integer.parseInt(slotKey.name()), ItemStorage.loadItemStack(slotKey));
        }
    }

    @Override
    public void save(DataKey data) {
        //save the inventory
        int i = 0;
        for (ItemStack is : stock.getContents()) {
            if (is != null) {
                DataKey inv = data.getRelative("inv");
                ItemStorage.saveItem(inv.getRelative("" + i++), is);
            }
        }
    }

    public Inventory getInventory() {
        return stock;
    }

    public void openStockRoom(Player player) {
        TraderStatus state = Trader.getStatus(player.getName());
        state.setTrader(npc);
        state.setStatus(TraderStatus.Status.STOCKROOM);
        player.openInventory(getInventory());

    }

    public boolean hasStock(ItemStack locate, boolean checkAmount) {
        ItemStack is = locate.clone();
        Material material = locate.getType();
        int amount = locate.getAmount();

        int amountFound = 0;
        for (Map.Entry<Integer, ? extends ItemStack> e : npc.getTrait(StockRoomTrait.class).getInventory().all(material).entrySet()) {
            is.setAmount(e.getValue().getAmount());
            if (e.getValue().equals(is)) {
                amountFound += e.getValue().getAmount();
            }
        }

        return checkAmount ? amount <= amountFound : amountFound > 0;
    }

    public boolean isStockRoomEmpty() {
        for (ItemStack is : stock) {
            if (is != null) {
                return false;
            }
        }
        return true;
    }
}
