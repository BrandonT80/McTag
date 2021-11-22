package bct.loadupstudios.MCTag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import java.util.Random;

public class Tag
{
	private String taggedPlayer;
	private FileConfiguration config;
	private Random rand = new Random();
	ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
	
	Tag(FileConfiguration c)
	{
		config = c;
		if(!getConfig().getString("taggedPlayer").equals("noPlayer"))
		{
			taggedPlayer = getConfig().getString("taggedPlayer");
		}
		else
		{
			taggedPlayer = null;
		}
	}
	
	public void reload(FileConfiguration conf)
	{
		config = conf;
		System.out.println("T: " + config.getString("taggedPlayer"));
		if(taggedPlayer != config.getString("taggedPlayer"))
		{
			if(config.getString("taggedPlayer") == "noPlayer")
			{
			
			}
			else
			{
				tagPlayer(config.getString("taggedPlayer"), taggedPlayer);
			}
		}
	}
	
	
	public void tagPlayer(String newtagplayer, String oldtagplayer)
	{
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		taggedPlayer = newtagplayer;
		String command = "nick " + oldtagplayer + " off";
		Bukkit.dispatchCommand(console, command);
		command = "nick " + newtagplayer + " &c" + newtagplayer;
		Bukkit.dispatchCommand(console, command);
		//command = "tag";
		//Bukkit.dispatchCommand(console, command);
		notifyPlayer(newtagplayer, oldtagplayer);
		
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',"&c" + newtagplayer + " &bhas been tagged!"));
	}
	
	public void tagPlayer(String newtagplayer)
	{
		if(taggedPlayer == null)
		{
			taggedPlayer = newtagplayer;
			String command = "nick " + newtagplayer + " &c" + newtagplayer;
			//Bukkit.dispatchCommand(console, command);
			//command = "tag";
			Bukkit.dispatchCommand(console, command);
			notifyPlayer(newtagplayer, null);
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',"&c" + newtagplayer + " &bhas been tagged!"));
		}
		else if(!taggedPlayer.equals(newtagplayer))
		{
			tagPlayer(newtagplayer, taggedPlayer);
		}
		else if(taggedPlayer.equals(newtagplayer))
		{
			System.out.println("Random tagged player is already tagged");
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',"&c" + newtagplayer + " &bhas been tagged by the GODS, again!"));
		}
	}
	
	public String getCurrentTag()
	{
		return taggedPlayer;
	}
	
	private FileConfiguration getConfig()
	{
		return config;
	}
	
	private void notifyPlayer(String tagPlayerName, String untagPlayerName)
	{
		//System.out.println("Notify: " + playerName);
		Player tagPlayer = Bukkit.getPlayer(tagPlayerName);
		Player untagPlayer = null;
		if(untagPlayerName != null)
		{
			untagPlayer = Bukkit.getPlayer(untagPlayerName);
		}
		//System.out.println("Notify2: " + player);
		if(untagPlayer != null)
		{
			if(tagPlayer != null)
			{
				tagPlayer.sendMessage("You have been tagged!");
			}
			untagPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&',"You have tagged &c" + tagPlayerName));
		}	
		else
		{
			if(tagPlayer != null)
			{
					tagPlayer.sendMessage("You have been tagged!");
			
			}
		}
	}
	
	public void it(CommandSender sender)
	{
		sender.sendMessage("Current tagged player: " + getCurrentTag());
	}
	
	public void start(CommandSender sender)
	{
		//System.out.println("Start Called");
		Object[] players = Bukkit.getOnlinePlayers().toArray();
		for(int i = 0; i > -1; i++)
		{
			Object name = players[rand.nextInt(Bukkit.getOnlinePlayers().size())];
			String playerName = name.toString().substring(17, name.toString().length()-1);
			//System.out.println("Name: " + playerName);
			//System.out.println("Start End");
			if(Bukkit.getPlayer(playerName).hasPermission("mctag.opt"))
			{
				tagPlayer(playerName);
				break;
			}
			else
			{ 
				if(i != 20)
				{
					System.out.println("Player " + playerName + " is opted out of tag, finding another player...");
				} 
				else 
				{
					System.out.println("Failed to find a suitable player in 20 iterations, this is likely due to many, or all players opting out. You may try again or specify a player using /tag <playername>");
					if(sender != null)
					{
						sender.sendMessage("Failed to find a suitable player, please try again. Read console for more information.");
					}
					break;
				}
			}
		}
	}
	
}
