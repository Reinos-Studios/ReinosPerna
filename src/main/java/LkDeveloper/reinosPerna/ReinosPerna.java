package LkDeveloper.reinosPerna;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Sound;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class ReinosPerna extends JavaPlugin implements Listener {

    private final Map<Player, Integer> playerInjuryTicks = new HashMap<>();
    private final Map<Player, Integer> playerSlownessLevel = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        if (config.getConfigurationSection("brokenLeg") == null) {
            config.createSection("brokenLeg");
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (config.getBoolean("brokenLeg." + player.getName(), false)) {
                applyInjuryEffects(player);
            }
        }

        getCommand("curarperna").setExecutor((sender, command, label, args) -> {
            if (sender.hasPermission("reinosperna.curar")) {
                if (args.length == 1) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        FileConfiguration config1 = getConfig();
                        if (config1.getBoolean("brokenLeg." + target.getName(), false)) {
                            curePlayer(target);
                            sender.sendMessage("Você curou a perna de " + target.getName());
                        } else {
                            sender.sendMessage(target.getName() + " não está com a perna quebrada.");
                        }
                    } else {
                        sender.sendMessage("Jogador não encontrado.");
                    }
                } else {
                    sender.sendMessage("Use: /curarperna <jogador>");
                }
            } else {
                sender.sendMessage("Você não tem permissão para usar este comando.");
            }
            return true;
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : playerInjuryTicks.keySet()) {
                    int ticks = playerInjuryTicks.get(player);
                    if (ticks < 600) {
                        playerInjuryTicks.put(player, ticks + 1);
                    } else if (ticks < 1200) {
                        applySlowness(player, 3);
                        playerInjuryTicks.put(player, ticks + 1);
                    } else if (ticks < 1800) {
                        applySlowness(player, 4);
                        playerInjuryTicks.put(player, ticks + 1);
                    } else {
                        applySlowness(player, 4);
                    }
                }
            }
        }.runTaskTimer(this, 20, 1);
    }

    @EventHandler
    public void onPlayerFall(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (event.getCause() == DamageCause.FALL) {
                double fallHeight = player.getFallDistance();

                if (fallHeight > 10) {
                    event.setDamage(0);  // Cancela o dano da queda
                    player.sendTitle("§cVocê quebrou a perna!", "", 10, 70, 20);
                    applyInjuryEffects(player);

                    getConfig().set("brokenLeg." + player.getName(), true);
                    saveConfig();

                    // Garantir que a saúde do jogador não seja menor que 0
                    double newHealth = Math.max(player.getHealth() - 12.0, 0.0);
                    player.setHealth(newHealth);

                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);

                    spawnBloodParticles(player);  // Partículas de sangue
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        FileConfiguration config = getConfig();

        if (config.getBoolean("brokenLeg." + player.getName(), false)) {
            config.set("brokenLeg." + player.getName(), null);
            saveConfig();
            playerInjuryTicks.remove(player);
            playerSlownessLevel.remove(player);
        }
    }

    private void applyInjuryEffects(Player player) {
        applySlowness(player, 3);
        playerInjuryTicks.put(player, 0);

        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));

        getConfig().set("brokenLeg." + player.getName(), true);
        saveConfig();
    }

    private void applySlowness(Player player, int slownessLevel) {
        if (!playerSlownessLevel.containsKey(player) || playerSlownessLevel.get(player) < slownessLevel) {
            player.removePotionEffect(PotionEffectType.SLOW);
            int duration = Integer.MAX_VALUE;
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, slownessLevel - 1));
            playerSlownessLevel.put(player, slownessLevel);
        }
    }

    private void spawnBloodParticles(Player player) {
        World world = player.getWorld();
        world.spawnParticle(Particle.REDSTONE, player.getLocation(), 50, 1.0, 1.0, 1.0, 0, new Particle.DustOptions(Color.RED, 1));
    }

    private void curePlayer(Player player) {
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.JUMP);
        player.setWalkSpeed(0.2f);
        player.sendMessage("Sua perna foi curada!");

        // Garantir que a saúde do jogador não ultrapasse o máximo
        player.setHealth(Math.min(player.getHealth() + 12.0, player.getMaxHealth()));

        playerInjuryTicks.remove(player);
        playerSlownessLevel.remove(player);

        getConfig().set("brokenLeg." + player.getName(), null);
        saveConfig();

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    @EventHandler
    public void onPlayerUseMilk(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType().name().contains("MILK_BUCKET")) {
            if (getConfig().getBoolean("brokenLeg." + player.getName(), false)) {
                event.setCancelled(true);
                player.sendMessage("Você não pode tomar leite enquanto está com a perna quebrada!");
            }
        }
    }
}
