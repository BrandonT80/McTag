package bct.loadupstudios.MCTag;

//import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

//import bct.loadupstudios.MCTag.MyListener;
//import bct.loadupstudios.MCTag.Tag;

public final class MCTag extends JavaPlugin
{
	FileConfiguration config = this.getConfig();
	Tag tagClass = new Tag(config);
	private boolean broadcastOpt = config.getBoolean("broadcastOpt");
	MyListener eventManager = new MyListener(tagClass, config);
	ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
	
    @Override
    public void onEnable() 
    {
        // TODO Insert logic to be performed when the plugin is enabled
    	saveDefaultConfig();
		
		getServer().getPluginManager().registerEvents(eventManager, this);
		
		//config.addDefault("youAreAwesome", true);
        //config.options().copyDefaults(true);
        //saveConfig();
        
		//System.out.println("String: " + getConfig().getString("taggedPlayer"));
		//System.out.println("String2: " + config.getString("taggedPlayer"));
		
		if(config.getString("taggedPlayer").equals("noPlayer"))
		{
			System.out.println("Tag loaded! No current player is tagged.");
		}
		else
		{
			System.out.println("Tag loaded! Current Tagged Player: " + config.getString("taggedPlayer"));
		}
    }
    
    @Override
    public void onDisable() 
    {
        // TODO Insert logic to be performed when the plugin is disabled
    	config.set("taggedPlayer", tagClass.getCurrentTag());
		saveConfig();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
    {
    	if(command.getName().equalsIgnoreCase("mctag"))
		{ 
			if(args.length > 0)
			{
				//Tag it command
				switch(args[0])
				{
				case "it":
					//Make sure user has the permission to use the command
					if(sender.hasPermission("mctag.it"))
					{
						tagClass.it(sender);
					}
					else
					{
						sender.sendMessage("You do not have the required permission to use that command (tag.it).");
					}
					return true;
				case "start":
					//Make sure user has the permission to use the command
					if(sender.hasPermission("mctag.start"))
					{
						tagClass.start(sender);
					}
					else
					{
						sender.sendMessage("You do not have the required permission to use that command (tag.start).");
					}
					return true;
				case "restart":
					//Make sure user has the permission to use the command
					if(sender.hasPermission("mctag.restart"))
					{
						sender.sendMessage("Restarting Tag.");
						tagClass.start(sender);	
					}
					else
					{
						sender.sendMessage("You do not have the required permission to use that command (mctag.restart).");
					}
					return true;
				case "optout":
					//Make sure user has the permission to use the command
					String commandOOLP = "lp user " + sender.getName() + " permission set mctag.opt false";
					Bukkit.dispatchCommand(console, commandOOLP);
					sender.sendMessage("You have opted out of tag.");
					if(broadcastOpt)
					{
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',"&c" + sender.getName() + " &bhas opted out of tag!"));
					}
					return true;
				case "optin":
					//Make sure user has the permission to use the command
					String commandOILP = "lp user " + sender.getName() + " permission set mctag.opt true";
					Bukkit.dispatchCommand(console, commandOILP);
					sender.sendMessage("You have opted into tag.");
					if(broadcastOpt)
					{
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',"&c" + sender.getName() + " &bhas opted into tag!"));
					}
					return true;
				case "reload":
					if(sender.hasPermission("mctag.reload"))
					{
						sender.sendMessage("Reloading tag...");
						System.out.println("Reloading tag...");
						reload();
					}
					else
					{
						sender.sendMessage("You do not have the required permission to use that command (tag.reload).");
					}
				default:
					System.out.println("Default found: " + args[0]);
					if(Bukkit.getPlayer(args[0]) != null)
					{
						System.out.println(Bukkit.getPlayer(args[0]).getDisplayName());
						if(sender.hasPermission("mctag.name"))
						{
							if(Bukkit.getPlayer(args[0]).hasPermission("mctag.opt"))
							{
								tagClass.tagPlayer(args[0]);
								sender.sendMessage("You have set the tagged player to " + args[0] + ".");
							}
							else
							{
								sender.sendMessage("Unable to tag, " + args[0] + " is not playing tag.");
							}
						}
						else
						{
							sender.sendMessage("You do not have the required permission to use that command (tag.name).");
						}
					}
					
					else
					{
						commandOILP = "help tag";
						Bukkit.dispatchCommand(console, commandOILP);
					}
					return true;
				}
			}
			return true;
		}
		return false;
    }
    
    public void reload()
	{
		reloadConfig();
		config = getConfig();
		tagClass.reload(config);
		eventManager.reload(config);
		broadcastOpt = config.getBoolean("broadcastOpt");
	}
}
