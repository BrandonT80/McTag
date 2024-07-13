package bct.loadupstudios.MCTag;

import com.google.common.util.concurrent.ServiceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tag
{
	private String taggedPlayer;
	private FileConfiguration config;
	private Random rand = new Random();
	ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
	
	Logger logger;
	MyListener eventManager;
	
	Tag(FileConfiguration c, Logger l)
	{
		config = c;
		logger = l;
		if(!getConfig().getString("taggedPlayer").equals("noPlayer")) {
			taggedPlayer = getConfig().getString("taggedPlayer");
		}
		else {
			taggedPlayer = null;
		}
	}

	public void setEventManager(MyListener eventManager)
	{
		this.eventManager = eventManager;
	}
	
	public void reload(FileConfiguration conf) {
		config = conf;
		if(taggedPlayer != config.getString("taggedPlayer"))
		{
			if(!config.getString("taggedPlayer").equals("noPlayer")) {
				tagPlayer(config.getString("taggedPlayer"), taggedPlayer);
			}
		}
	}
	
	
	public void tagPlayer(String newtagplayer, String oldtagplayer) {
		notifyPlayer(newtagplayer, oldtagplayer);
		taggedPlayer = newtagplayer;
		eventManager.scoreboards.updateScoreboard();
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',"&c" + newtagplayer + " &bhas been tagged!"));
	}
	
	public void tagPlayer(String newtagplayer) {
		if(taggedPlayer == null) {
			taggedPlayer = newtagplayer;
			notifyPlayer(newtagplayer, null);
			eventManager.scoreboards.updateScoreboard();
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',"&c" + newtagplayer + " &bhas been tagged!"));
		}
		else if(!taggedPlayer.equals(newtagplayer)) {
			tagPlayer(newtagplayer, taggedPlayer);
			eventManager.scoreboards.updateScoreboard();
		}
		else if(taggedPlayer.equals(newtagplayer)) {
			logger.log(Level.INFO, "Random tagged player is already tagged");
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
	
	private void notifyPlayer(String tagPlayerName, String untagPlayerName) {
		Player tagPlayer = Bukkit.getPlayer(tagPlayerName);
		Player untagPlayer = null;
		if(untagPlayerName != null) {
			untagPlayer = Bukkit.getPlayer(untagPlayerName);
		}
		if(untagPlayer != null) {
			if(tagPlayer != null) {
				tagPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2You have been tagged!"));
			}
			untagPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&',"&bYou have tagged &c" + tagPlayerName));
		}	
		else {
			if(tagPlayer != null) {
				tagPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2You have been tagged!"));
			}
		}
	}
	
	public void it(CommandSender sender) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCurrent tagged player: &2" + getCurrentTag()));
	}
	
	public void start(CommandSender sender, String removedPlayer) {
		Object[] players = Bukkit.getOnlinePlayers().toArray();
		for(int i = 0; i > -1; i++) {
			Object name = players[rand.nextInt(Bukkit.getOnlinePlayers().size())];
			String playerName = name.toString().substring(17, name.toString().length()-1);
			if(!playerName.equals(removedPlayer) && eventManager.readPlayer(Bukkit.getPlayer(playerName).getName()) == 1) {
				tagPlayer(playerName);
				eventManager.scoreboards.updateScoreboard();
				break;
			}
			else {
				if(i != 20) {
					logger.log(Level.WARNING, "Player " + playerName + " is opted out of tag, finding another player...");
				} 
				else {
					logger.log(Level.WARNING, "Failed to find a suitable player in 20 iterations, this is likely due to many, or all players opting out. You may try again or specify a player using /mctag <playername>");
					if(sender != null) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cFailed to find a suitable player, please try again. Read console for more information."));
					}
					break;
				}
			}
		}
	}

	public void restartTagIfRemovedPlayerIsTagged(String checkPlayer) {
		if(getCurrentTag().equals(checkPlayer)) {                       //Restarts the tag if the player removed is the tagged player (Either logout or optout)
			start(null, checkPlayer);
		}
	}
}
