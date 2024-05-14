package coder.kam1kadzdev.ferma;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.*;

public final class Ferma extends JavaPlugin implements Listener {
    private final Map<UUID, PlayerTimer> playerTimers = new HashMap<>();

    private boolean isTimerRunning = false;
    private int timerSeconds;
    private boolean isTimerRunning2 = false;
    private int timerSeconds2;
    private boolean reward2 = false;
    private boolean reward = false;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        getLogger().info("**************************************************");
        getLogger().info("* Плагин на гуи выращивания ");
        getLogger().info("* Название: Ferma");
        getLogger().info("* Статус: Загружен");
        getLogger().info("* Оригинальная версия: 1.16.5 PAPER");
        getLogger().info("**************************************************");
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("ferma").setExecutor(this);
        saveDefaultConfig();
        config = getConfig();
        loadConfigValues();

        timerSeconds = config.getInt("timer.scute");
        timerSeconds2 = config.getInt("timer.nautilus_shell");
    }

    @Override
    public void onDisable() {
        getLogger().info("**************************************************");
        getLogger().info("* Плагин на гуи выращивания ");
        getLogger().info("* Название: Ferma");
        getLogger().info("* Статус: Отключение");
        getLogger().info("* Оригинальная версия: 1.16.5 PAPER");
        getLogger().info("**************************************************");
    }

    private void loadConfigValues() {
        saveDefaultConfig();

    }
    private String getConfigMessage(String path) {
        return getConfig().getString(path, "Текст не найден в конфиге.");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("ferma.reload")) {
                reloadConfig();
                config = getConfig();
                sender.sendMessage("§a[§2Ferma§a] §fКонфигурация плагина перезагружена.");
            } else {
                sender.sendMessage("§c[§4!§c] §fУ вас нет прав на перезагрузку конфигурации.");
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(getConfigMessage("messages.check_on_player"));
            return true;
        }

        Player player = (Player) sender;
        openFermaGUI(player);
        return true;
    }


    private void openFermaGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 5 * 9, config.getString("gui.ferma_title", "Ферма"));

        ItemStack flowerPotItem = createFlowerPotItem();
        gui.setItem(29, flowerPotItem);
        ItemStack flowerPotItem2 = createFlowerPotItem2();
        gui.setItem(33, flowerPotItem2);

        ItemStack bookItem = createItem(Material.BOOK, config.getString("book.title", "Информация"),
                config.getStringList("book.lines").toArray(new String[0]));
        gui.setItem(4, bookItem);

        gui.setItem(20, createItem(Material.SCUTE, config.getString("scute.title", "Панцирь 123"),
                config.getStringList("scute.lines").toArray(new String[0])));

        gui.setItem(24, createItem(Material.NAUTILUS_SHELL, config.getString("nautilus_shell.title", "Раковина 123"),
                config.getStringList("nautilus_shell.lines").toArray(new String[0])));

        player.openInventory(gui);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);

        List<String> loreList = new ArrayList<>();
        loreList.addAll(Arrays.asList(lore));
        meta.setLore(loreList);

        item.setItemMeta(meta);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_DYE);

        return item;
    }
    private ItemStack createFlowerPotItem2() {
        ItemStack item = new ItemStack(Material.FLOWER_POT);
        ItemMeta meta = item.getItemMeta();

        String displayName = config.getString("flower_pot_2.title", "Горшокцветочный 123");
        List<String> loreList = config.getStringList("flower_pot_2.lines");

        if (isTimerRunning2) {
            loreList.add(config.getString("flower_pot_2.timer_line", "Осталось время: %time%"));
        }
        if (reward2) {
            loreList.add(config.getString("flower_pot_2.reward_line", "Нажмите, чтобы забрать награду"));
        }

        meta.setDisplayName(displayName);
        meta.setLore(loreList);

        item.setItemMeta(meta);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_DYE);

        return item;
    }


    private ItemStack createFlowerPotItem() {
        ItemStack item = new ItemStack(Material.FLOWER_POT);
        ItemMeta meta = item.getItemMeta();

        String displayName = config.getString("flower_pot.title", "Горшокцветочный 123");
        List<String> loreList = config.getStringList("flower_pot.lines");

        if (isTimerRunning) {
            loreList.add(config.getString("flower_pot.timer_line", "Осталось время: %time%"));
        }
        if (reward) {
            loreList.add(config.getString("flower_pot.reward_line", "Нажмите, чтобы забрать награду"));
        }

        meta.setDisplayName(displayName);
        meta.setLore(loreList);

        item.setItemMeta(meta);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_DYE);

        return item;
    }


    private void startTimer(UUID playerId, ItemStack flowerPotItem, Player player) {
        PlayerTimer playerTimer = new PlayerTimer(playerId, flowerPotItem, player);

        new BukkitRunnable() {
            @Override
            public void run() {
                playerTimer.run();
            }
        }.runTaskTimer(this, 0L, 20L);
    }


    private void cancelTimer(ItemStack flowerPotItem, Player player) {
        isTimerRunning = false;
        timerSeconds = config.getInt("timer.scute");

        reward = true;
        flowerPotItem = createFlowerPotItem();
        updateFlowerPotItem(flowerPotItem, player);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }


    private void updateFlowerPotItem(ItemStack flowerPotItem, Player player) {
        ItemMeta meta = flowerPotItem.getItemMeta();

        if (isTimerRunning) {
            List<String> loreList = meta.getLore();

            if (loreList != null && !loreList.isEmpty()) {
                String timerLinePlaceholder = config.getString("flower_pot.timer_line", "Осталось время312: %time% 312");
                String formattedTimerLine = timerLinePlaceholder.replace("%time%", formatTime(timerSeconds));
                loreList.set(loreList.size() - 1, formattedTimerLine);
                meta.setLore(loreList);
            }
        }

        flowerPotItem.setItemMeta(meta);

        String guiTitle = config.getString("gui.ferma_title", "Ферма");
        if (player.getOpenInventory().getTitle().equals(guiTitle)) {
            player.getOpenInventory().setItem(29, flowerPotItem);
        }
    }


    private void startTimer2(ItemStack flowerPotItem2, Player player) {
        isTimerRunning2 = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (timerSeconds2 > 0) {
                    updateFlowerPotItem2(flowerPotItem2, player);
                    timerSeconds2--;
                } else {
                    cancelTimer2(flowerPotItem2, player);
                    cancel();
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void cancelTimer2(ItemStack flowerPotItem2, Player player) {
        isTimerRunning2 = false;
        timerSeconds2 = config.getInt("timer.nautilus_shell");

        reward2 = true;
        flowerPotItem2 = createFlowerPotItem2();
        updateFlowerPotItem2(flowerPotItem2, player);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }


    private void updateFlowerPotItem2(ItemStack flowerPotItem2, Player player) {
        ItemMeta meta = flowerPotItem2.getItemMeta();

        if (isTimerRunning2) {
            List<String> loreList = meta.getLore();

            if (loreList != null && !loreList.isEmpty()) {
                String timerLinePlaceholder = config.getString("flower_pot_2.timer_line", "Осталось время: %time%");
                String formattedTimerLine = timerLinePlaceholder.replace("%time%", formatTime(timerSeconds2));
                loreList.set(loreList.size() - 1, formattedTimerLine);
                meta.setLore(loreList);
            }
        }

        flowerPotItem2.setItemMeta(meta);

        String guiTitle = config.getString("gui.ferma_title", "Ферма");
        if (player.getOpenInventory().getTitle().equals(guiTitle)) {
            player.getOpenInventory().setItem(33, flowerPotItem2);
        }
    }


    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        UUID playerId = player.getUniqueId();

        // Retrieve the player timer from the map
        PlayerTimer playerTimer = playerTimers.get(playerId);

        if (event.getView().getTitle().equals(config.getString("gui.ferma_title", "Ферма"))) {
            event.setCancelled(true);
            Inventory playerInventory = player.getInventory();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                if (clickedItem != null && clickedItem.getType() == Material.FLOWER_POT) {
                    int slot = event.getRawSlot();
                    if (slot == 29) {
                        if (reward) {
                            player.getInventory().close();
                            giveReward(player, Double.parseDouble(config.getString("prices.scute")));
                            reward = false;

                            player.sendMessage(config.getString("messages.payment_successful"));
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                        } else if (playerInventory.containsAtLeast(new ItemStack(Material.SCUTE), 5)) {
                            if (!playerTimer.isTimerRunning()) {
                                playerInventory.removeItem(new ItemStack(Material.SCUTE, 5));
                                player.sendMessage(config.getString("messages.timer_start"));
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                                ItemStack flowerPotItem = createFlowerPotItem();
                                updateFlowerPotItem(flowerPotItem, player);
                                playerTimer.startTimer();
                            } else {
                                player.getInventory().close();

                                player.sendMessage(config.getString("messages.error_timer_enabled"));
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

                            }


                        } else {
                            player.closeInventory();
                            player.sendMessage(config.getString("messages.error_timer_enabled_2"));
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        }
                    } else if (slot == 33) {
                        if (reward2) {
                            player.getInventory().close();
                            giveReward(player, Double.parseDouble(config.getString("prices.nautilus_shell")));
                            reward2 = false;

                            player.sendMessage(config.getString("messages.payment_successful"));
                        } else if (playerInventory.containsAtLeast(new ItemStack(Material.NAUTILUS_SHELL), 5)) {
                            if (!playerTimer.isTimerRunning2()) {
                                playerInventory.removeItem(new ItemStack(Material.NAUTILUS_SHELL, 5));
                                player.sendMessage(config.getString("messages.timer_start_2"));
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                                ItemStack flowerPotItem2 = createFlowerPotItem2();
                                updateFlowerPotItem2(flowerPotItem2, player);
                                playerTimer.startTimer2();
                            } else {
                                player.getInventory().close();

                                player.sendMessage(config.getString("messages.error_timer_enabled_3"));
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

                            }


                        } else {
                            player.closeInventory();
                            player.sendMessage(config.getString("messages.error_timer_enabled_4"));
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        }
                    }
                }
            }
        }
    }

    public void giveReward(Player player, double amount) {
        String command = "eco give " + player.getName() + " " + amount;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private class PlayerTimer implements Runnable {
        private final UUID playerId;
        private boolean isTimerRunning = false;
        private boolean isTimerRunning2 = false;
        private boolean reward = false;
        private boolean reward2 = false;
        private int timerSeconds;
        private ItemStack flowerPotItem;
        private Player player;

        public PlayerTimer(UUID playerId, ItemStack flowerPotItem, Player player) {
            this.playerId = playerId;
            this.flowerPotItem = flowerPotItem;
            this.player = player;
            this.timerSeconds = config.getInt("timer.scute");
        }
        @Override
        public void run() {
        }

        public boolean isTimerRunning() {
            return isTimerRunning;
        }

        public boolean isTimerRunning2() {
            return isTimerRunning2;
        }

        public void startTimer() {
            isTimerRunning = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (timerSeconds > 0) {
                        updateFlowerPotItem();
                        timerSeconds--;
                    } else {
                        cancelTimer();
                        cancel();
                    }
                }
            }.runTaskTimer(Ferma.this, 0L, 20L);
        }

        public void startTimer2() {
            isTimerRunning2 = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (timerSeconds2 > 0) {
                        updateFlowerPotItem2();
                        timerSeconds2--;
                    } else {
                        cancelTimer2();
                        cancel();
                    }
                }
            }.runTaskTimer(Ferma.this, 0L, 20L);
        }

        private void updateFlowerPotItem() {
            if (flowerPotItem != null) {
                ItemMeta meta = flowerPotItem.getItemMeta();

                if (isTimerRunning) {
                    List<String> loreList = meta.getLore();

                    if (loreList != null && !loreList.isEmpty()) {
                        String timerLinePlaceholder = config.getString("flower_pot.timer_line", "Remaining time: %time%");
                        String formattedTimerLine = timerLinePlaceholder.replace("%time%", formatTime(timerSeconds));
                        loreList.set(loreList.size() - 1, formattedTimerLine);
                        meta.setLore(loreList);
                    }
                }

                flowerPotItem.setItemMeta(meta);

                String guiTitle = config.getString("gui.ferma_title", "Farm");
                if (player != null && player.getOpenInventory() != null && player.getOpenInventory().getTitle().equals(guiTitle)) {
                    player.getOpenInventory().setItem(29, flowerPotItem);
                }
            }
        }


        private void updateFlowerPotItem2() {
            ItemMeta meta = flowerPotItem.getItemMeta();

            if (isTimerRunning2) {
                List<String> loreList = meta.getLore();

                if (loreList != null && !loreList.isEmpty()) {
                    String timerLinePlaceholder = config.getString("flower_pot_2.timer_line", "Remaining time: %time%");
                    String formattedTimerLine = timerLinePlaceholder.replace("%time%", formatTime(timerSeconds2));
                    loreList.set(loreList.size() - 1, formattedTimerLine);
                    meta.setLore(loreList);
                }
            }

            flowerPotItem.setItemMeta(meta);

            String guiTitle = config.getString("gui.ferma_title", "Farm");
            if (player.getOpenInventory().getTitle().equals(guiTitle)) {
                player.getOpenInventory().setItem(33, flowerPotItem);
            }
        }

        private void cancelTimer() {
            isTimerRunning = false;
            timerSeconds = config.getInt("timer.scute");

            reward = true;
            flowerPotItem = createFlowerPotItem();
            updateFlowerPotItem();

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }

        private void cancelTimer2() {
            isTimerRunning2 = false;
            timerSeconds2 = config.getInt("timer.nautilus_shell");

            reward2 = true;
            flowerPotItem = createFlowerPotItem2();
            updateFlowerPotItem2();

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }

        private String formatTime(int seconds) {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            seconds = seconds % 60;

            if (hours > 0) {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } else {
                return String.format("%02d:%02d", minutes, seconds);
            }
        }
    }


}
