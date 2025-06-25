package com.example.chuansong;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CSword extends JavaPlugin implements PluginMessageListener {

    private Economy economy;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, Boolean> cooldownMap = new HashMap<>();
    private Map<String, Integer> serverCosts = new HashMap<>();
    private boolean freeMode = false;
    private String currentServer;

    @Override
    public void onEnable() {
        loadConfigurations();
        registerChannels();
        setupEconomy();
        getCommand("cs").setExecutor(this);
    }

    private void loadConfigurations() {
        saveDefaultConfig();
        reloadConfig();
        setupDataFile();

        currentServer = getConfig().getString("current-server", "lobby").toLowerCase();
        freeMode = getConfig().getBoolean("free-mode", false);
        loadServerCosts();

        if (freeMode) {
            getLogger().info("配置文件中启用了免费模式，所有传送将免费");
        }
    }

    private void registerChannels() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
    }

    private void setupEconomy() {
        if (freeMode) return;

        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("没有检测到经济插件，启动免费模式");
            freeMode = true;
            return;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        } else {
            getLogger().info("没有找到经济系统，启动免费模式");
            freeMode = true;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return showServerList(sender);
        }

        // 处理重载命令
        if (args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        // 必须是玩家才能执行传送
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用传送功能！");
            return true;
        }

        return handleTeleport((Player) sender, args[0].toLowerCase());
    }

    private boolean showServerList(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "§6§lCSword §f- 跨服传送系统");
        sender.sendMessage(freeMode ? ChatColor.GREEN + "当前为免费模式" : ChatColor.GOLD + "首次传送需要付费");

        sender.sendMessage(ChatColor.GOLD + "可用服务器列表:");
        serverCosts.forEach((server, cost) -> {
            String costInfo = freeMode ? ChatColor.GREEN + "免费" : ChatColor.GOLD + "(价格: " + cost + "金币)";
            sender.sendMessage(ChatColor.YELLOW + "- " + ChatColor.GREEN + server + " " + costInfo);
        });

        sender.sendMessage(ChatColor.GRAY + "当前服务器: " + ChatColor.GREEN + currentServer);
        sender.sendMessage(ChatColor.GRAY + "使用 §a/cs <服务器名> §7进行传送");
        sender.sendMessage(ChatColor.GRAY + "管理员使用 §a/cs reload §7重载配置");
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("csword.reload")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }

        try {
            loadConfigurations();
            setupEconomy();
            sender.sendMessage(ChatColor.GREEN + "配置已重载！");
            getLogger().info("配置已通过命令重载");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "重载配置时出错: " + e.getMessage());
            getLogger().severe("重载配置失败: " + e.getMessage());
        }
        return true;
    }

    private boolean handleTeleport(Player player, String targetServer) {
        UUID uuid = player.getUniqueId();

        if (cooldownMap.containsKey(uuid)) {
            player.sendMessage(ChatColor.YELLOW + "正在传送中，请稍候...");
            return true;
        }

        if (!serverCosts.containsKey(targetServer)) {
            player.sendMessage(ChatColor.RED + "未知的服务器名称！使用 /cs 查看可用服务器");
            return true;
        }

        if (targetServer.equalsIgnoreCase(currentServer)) {
            player.sendMessage(ChatColor.RED + "您已经在 " + currentServer + " 服务器！");
            return true;
        }

        int costAmount = serverCosts.get(targetServer);
        cooldownMap.put(uuid, true);

        new BukkitRunnable() {
            int countdown = 3;

            @Override
            public void run() {
                if (countdown > 0) {
                    player.sendMessage(ChatColor.GREEN + "倒计时 " + countdown + " 秒...");
                    countdown--;
                } else {
                    processPaymentAndTeleport(player, targetServer, costAmount);
                    cooldownMap.remove(uuid);
                    cancel();
                }
            }
        }.runTaskTimer(this, 0L, 20L);

        player.sendMessage(ChatColor.GREEN + "正在传送至 " + targetServer + "...");
        return true;
    }

    private void processPaymentAndTeleport(Player player, String targetServer, int costAmount) {
        if (freeMode) {
            sendToServer(player, targetServer);
            return;
        }

        List<String> paidPlayers = dataConfig.getStringList("servers." + targetServer);
        boolean isFirstPayment = !paidPlayers.contains(player.getUniqueId().toString());

        if (isFirstPayment) {
            if (economy.has(player, costAmount)) {
                economy.withdrawPlayer(player, costAmount);
                paidPlayers.add(player.getUniqueId().toString());
                dataConfig.set("servers." + targetServer, paidPlayers);
                saveDataFile();
                player.sendMessage(ChatColor.RED + "已扣除 " + costAmount + " 金币,仅收费一次");
            } else {
                player.sendMessage(ChatColor.RED + "扣款失败，余额不足！");
                return;
            }
        }

        sendToServer(player, targetServer);
    }

    private void loadServerCosts() {
        serverCosts.clear();
        ConfigurationSection servers = getConfig().getConfigurationSection("servers");
        if (servers != null) {
            servers.getKeys(false).forEach(server -> {
                serverCosts.put(server.toLowerCase(), servers.getInt(server + ".cost", 0));
            });
        }
    }

    private void setupDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
                dataConfig = YamlConfiguration.loadConfiguration(dataFile);
                dataConfig.set("servers", new HashMap<>());
                dataConfig.save(dataFile);
            } catch (IOException e) {
                getLogger().severe("初始化 data.yml 失败: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("保存 data.yml 失败: " + e.getMessage());
        }
    }

    private void sendToServer(Player player, String serverName) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "跨服传送失败，请联系管理员！");
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // 可处理返回消息（此处无需操作）
    }
}