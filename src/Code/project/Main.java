package Code.project;

import net.minecraftforge.event.world.NoteBlockEvent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.Collection;

public class Main extends JavaPlugin implements Listener {
    World world;
    Team obs_team,T_team,CT_team;
    Location spawnlocation,T_spawn,CT_spawn;
    Scoreboard scoreboard;
    ScoreboardManager manager;
    static double x,y,z,t_x,t_y,t_z,ct_x,ct_y,ct_z;
    static boolean gameStatus=false,initialization_status=true,LoadStatus=false,Timer=false,startMode=false,sb=false;
    String JoinGameplayer[];
    private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp()) {
            if (args[0].equalsIgnoreCase("start")) {
                sender.sendMessage("CSGO插件已开始游戏.");
                start_game();
                return true;
            } else if ((args[0].equalsIgnoreCase("status"))) {
                sender.sendMessage("CSGO插件正常运行中.");
                return true;
            }
        }
        return false;
    }
    public String parse(String s) {
        return s.replace("&","§").replace("§§","&");
    }
    public void readfile(){
        String spawnpoint[] = {"spawnpoint.x","spawnpoint.y","spawnpoint.z"};
        String T_spawn[] = {"T_spawn.x","T_spawn.y","T_spawn.z"};
        String CT_spawn[] = {"CT_spawn.x","CT_spawn.y","CT_spawn.z"};
        for(int i=0;i<=2;i++){
            if(getConfig().getString(spawnpoint[i])==null) {
                System.out.println("ERROR:"+spawnpoint[i]+"不存在！");
                getConfig().set(spawnpoint[i],0);
                onDisable();
            }
        }
        x=getConfig().getInt("spawnpoint.x");
        y=getConfig().getInt("spawnpoint.y");
        z=getConfig().getInt("spawnpoint.z");
        System.out.println("你设置的Spawn坐标是："+x+" "+y+" "+z);
        for(int i=0;i<=2;i++){
            if(getConfig().getString(T_spawn[i])==null) {
                System.out.println("ERROR:"+T_spawn[i]+"不存在！");
                getConfig().set(T_spawn[i],0);
                onDisable();
            }
        }
        t_x=getConfig().getInt("T_spawn.x");
        t_y=getConfig().getInt("T_spawn.y");
        t_z=getConfig().getInt("T_spawn.z");
        System.out.println("你设置的T_spawn坐标是："+t_x+" "+t_y+" "+t_z);
        for(int i=0;i<=2;i++){
            if(getConfig().getString(CT_spawn[i])==null) {
                System.out.println("ERROR:"+CT_spawn[i]+"不存在！");
                getConfig().set(CT_spawn[i],0);
                onDisable();
            }
        }
        ct_x=getConfig().getInt("CT_spawn.x");
        ct_y=getConfig().getInt("CT_spawn.y");
        ct_z=getConfig().getInt("CT_spawn.z");
        System.out.println("你设置的CT_Spawn坐标是："+ct_x+" "+ct_y+" "+ct_z);
        saveConfig();
    }
    @Override
    public void onEnable() {
        System.out.println("CSGO已启用");
        System.out.println("onEnable has been invoked!");
        Bukkit.getPluginManager().registerEvents(this, this);
        //scoreboard = getServer().getScoreboardManager().getMainScoreboard();
        manager = Bukkit.getScoreboardManager();
        // 建立新Scoreboard
        scoreboard = manager.getNewScoreboard();
        // 注册新的记分项
        Objective objective = scoreboard.registerNewObjective("玩家数据", "dummy");
        // 设置记分项展示位置
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        // 给记分项增加 内容与对应的分数
        Score score = objective.getScore("金钱$");
        score.setScore(800);
        // 设置计分板

        CT_team = scoreboard.registerNewTeam("CT");
        CT_team.setPrefix(parse("&b[反恐精英]"));
        CT_team.setDisplayName("CT");
        CT_team.setAllowFriendlyFire(false);
        CT_team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);

        T_team = scoreboard.registerNewTeam("T");
        T_team.setPrefix(parse("&e[恐怖分子]"));
        T_team.setDisplayName("T");
        T_team.setAllowFriendlyFire(false);
        T_team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);

        obs_team = scoreboard.registerNewTeam("OBS");
        obs_team.setPrefix(parse("&f[观察者]"));
        obs_team.setDisplayName("OBS");
        obs_team.setAllowFriendlyFire(false);
        obs_team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
    }

    @Override
    public void onLoad() {
        System.out.println("CSGO插件加载中");
        readfile();
    }

    @Override
    public void onDisable() {
        System.out.println("CSGO插件已被卸载");
        obs_team.unregister();
        T_team.unregister();
        CT_team.unregister();
    }

    @EventHandler
    public void PlayerJoin(PlayerJoinEvent event) {
        if(initialization_status) {
            initialization_status = false;
            world = event.getPlayer().getWorld();
            spawnlocation = new Location(world, x, y, z);
            T_spawn = new Location(world, t_x, t_y, t_z);
            CT_spawn = new Location(world, ct_x, ct_y, ct_z);

        }

        Player player = event.getPlayer();
        player.setScoreboard(scoreboard);
        player.setBedSpawnLocation(spawnlocation);
        player.sendMessage("欢迎加入游戏");
        player.teleport(spawnlocation);
        player.sendMessage("已为你传送到游戏等待区域！");
        player.setGameMode(GameMode.ADVENTURE);
        ClearPlayerTeam(player);
        CheckPlayer();
    }

    public class Demo1_Thread extends Thread {
        public void run() {
                Bukkit.broadcastMessage("游戏即将开始,请玩家们做好准备！");
                for (int n = 10; n > 0; n--) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Bukkit.broadcastMessage("游戏开始剩余时间：" + n + "秒.");
                }
                Timer = true;
            }
        }
    public void CheckPlayer(){
        String winner = new String();
        winner = "NULL";
        int number = getServer().getOnlinePlayers().size();
        if(number > 1 && LoadStatus==false) {
            for (int i = 0; i < number; i++) {
                Player players = (Player) getServer().getOnlinePlayers().toArray()[i];
                if (players.getHealth() == 0)
                    ClearPlayerTeam(players);
            }

            if (T_team.getSize() == 0 || CT_team.getSize() == 0 && LoadStatus==false) {
                LoadStatus = true;
                startMode = true;
                gameStatus = false;
                if(T_team.getSize() == 0 && CT_team.getSize() == 0){
                    Bukkit.broadcastMessage("所有人都已经阵亡！");
                    winner="没有人获得胜利";
                }else if (T_team.getSize() == 0) {
                    Bukkit.broadcastMessage("恐怖分子全部被消灭，反恐精英阵营获胜！");
                    winner="T阵营获得胜利";
                }else if (CT_team.getSize() == 0) {
                    Bukkit.broadcastMessage("反恐精英全部被消灭，恐怖分子阵营获胜！");
                    winner="CT阵营获得胜利";
                }
                Bukkit.broadcastMessage("游戏结束！所有人返回大厅");
                for (int i = 0; i < number; i++) {
                    Player players = (Player) getServer().getOnlinePlayers().toArray()[i];
                    players.sendTitle("游戏结束",winner);
                    ClearPlayerTeam(players);
                    obs_team.addEntry(players.getName());
                    players.teleport(spawnlocation);
                    players.setGameMode(GameMode.ADVENTURE);
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "clear " + players.getName());
                }
                Demo1_Thread R1 = new Demo1_Thread();
                R1.start();
            }
        }
    }
    public void start_game() {
        int i = 0, number = Bukkit.getOnlinePlayers().size();
        if (gameStatus == false && Timer == true) {
            Timer = false;
            if (number > 1) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "kill @e[type=item]");
                while (i < number) {
                    Player players = (Player) getServer().getOnlinePlayers().toArray()[i];
                    for(int s = 0; s <= 100; ++s) {
                        players.sendMessage(" ");
                    }
                    if (i % 2 != 0)
                        set_team("T", T_spawn, players);
                    else
                        set_team("CT", CT_spawn, players);
                    i++;
                }
                Demo2_Thread R2 = new Demo2_Thread();
                R2.start();
            } else {
                Bukkit.broadcastMessage("因玩家人数不齐,本局游戏被取消！");
            }
        }
        LoadStatus = false;
        startMode = true;
        gameStatus = true;
    }
    public class Demo2_Thread extends Thread {
        public void run() {
            int n = 5, number = getServer().getOnlinePlayers().size();
            Bukkit.broadcastMessage("回合即将开始！");
            for (n = 5; n > 0; n--) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Bukkit.broadcastMessage("该回合开始剩余时间：" + n + "秒.");
            }
            for (int i = 0; i < number; i++) {
                Player players = (Player) getServer().getOnlinePlayers().toArray()[i];
                players.sendTitle("游戏开始","消灭所有敌人");
                players.setWalkSpeed(0.25f);
                players.setSneaking(false);
            }
        }
    }
    public void set_team(String Team,Location location,Player player) {//复活点,玩家
        if(player.getHealth()!=0) {
            ClearPlayerTeam(player);
            player.setHealth(20);
            player.setFoodLevel(25);
            player.setWalkSpeed(0);
            player.setSneaking(true);
            player.teleport(location);
            player.setGameMode(GameMode.ADVENTURE);

            ItemStack mp5ammo = new ItemStack(Material.FLINT, 60);
            ItemMeta meta = mp5ammo.getItemMeta();
            meta.setDisplayName("§c9mm弹药");
            ArrayList lore = new ArrayList();
            lore.add("§b一种适用于USP,MP5系列");
            lore.add("枪械的的武器弹药,弹药口径为9mm");
            meta.setLore(lore);
            mp5ammo.setItemMeta(meta);

            if (Team == "T") {
                player.sendMessage("您被选为恐怖分子阵营！");
                player.sendTitle("消灭你所看到的所有敌人！","恐怖分子");
                T_team.addEntry(player.getName());
                player.getInventory().clear();
                player.getInventory().setItem(6,mp5ammo);
                player.getInventory().setItem(5,mp5ammo);
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "crackshot give " + player.getName() + " 1");
            } else if (Team == "CT") {
                player.sendMessage("您被选为反恐精英阵营！");
                player.sendTitle("消灭你所看到的所有敌人！","反恐精英");
                CT_team.addEntry(player.getName());
                player.getInventory().clear();
                player.getInventory().setItem(6,mp5ammo);
                player.getInventory().setItem(5,mp5ammo);
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "crackshot give " + player.getName() + " 1");
            }
        }
    }
    @EventHandler
    public void PlayerMove(PlayerMoveEvent event){
        CheckPlayer();
        if(Timer==true){
            start_game();
        }
    }

    @EventHandler
    public void player(EntityDamageEvent event){
        CheckPlayer();
    }

    @EventHandler
    public void PlayerDeath(PlayerDeathEvent event){
        ClearPlayerTeam(event.getEntity());
        Player player = event.getEntity().getKiller();
    }

    @EventHandler
    public void PlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("对不起,您阵亡了！请耐心等待游戏结束！");
        CheckPlayer();
        ClearPlayerTeam(player);
        obs_team.addEntry(player.getName());
        player.teleport(CT_spawn);
        player.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void PlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        Bukkit.broadcastMessage("玩家"+player.getName()+"逃离了战场！");
        ClearPlayerTeam(player);
        CheckPlayer();
    }

    public void ClearPlayerTeam(Player player){
        player.getInventory().clear();
        try {
            if (scoreboard.getTeam("T")!=null)
                T_team.removeEntry(player.getName());
            if (scoreboard.getTeam("CT")!=null)
                CT_team.removeEntry(player.getName());
            if (scoreboard.getTeam("OBS")!=null)
                obs_team.removeEntry(player.getName());
        }catch (Exception e){
            System.out.println("ClearPlayerTeam()函数错误："+e);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // 暂未开发
    }
}