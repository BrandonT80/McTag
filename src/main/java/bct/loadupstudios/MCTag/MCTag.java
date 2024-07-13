package bct.loadupstudios.MCTag;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCTag extends JavaPlugin
{
	Logger logger = this.getLogger();
	FileConfiguration config = this.getConfig();
	Tag tagClass = new Tag(config, logger);
	private boolean broadcastOpt = config.getBoolean("broadcastOpt");
	MyListener eventManager = new MyListener(tagClass, config, logger);
	ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
	String folder = this.getDataFolder().getPath();	//Gets the plugin path
	File configFile = new File(folder,"config.yml");	//Gets the config file for read/write purposes
	
    @Override
    public void onEnable() 
    {
    	saveDefaultConfig();
		checkConfig();
		getServer().getPluginManager().registerEvents(eventManager, this);
		tagClass.setEventManager(eventManager);
		if(config.getString("taggedPlayer").equals("noPlayer")) {
			logger.log(Level.INFO, "Tag loaded! No current player is tagged.");
		}
		else {
			logger.log(Level.INFO, "Tag loaded! Current Tagged Player: " + config.getString("taggedPlayer"));
		}
    }
    
    @Override
    public void onDisable() 
    {
    	config.set("taggedPlayer", tagClass.getCurrentTag());
		saveConfig();
		eventManager.onExit();
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
					if(sender.hasPermission("mctag.it")) {
						tagClass.it(sender);
					}
					else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have the required permission to use that command &b(mctag.it)&c."));
					}
					return true;
				case "start":
					if(sender.hasPermission("mctag.start")) {
						tagClass.start(sender, null);
					}
					else
					{
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have the required permission to use that command &b(mctag.start)&c."));
					}
					return true;
				case "restart":
					if(sender.hasPermission("mctag.restart")) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Restarting Tag."));
						tagClass.start(sender, null);
					}
					else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have the required permission to use that command &b(mctag.restart)&c."));
					}
					return true;
				case "optout":
					case "leave":
						if(eventManager.readPlayer(sender.getName()) != 2) {
							eventManager.playerOpt(sender.getName(), 2);
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have left the tag minigame."));
							if (broadcastOpt) {
								Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c" + sender.getName() + " &bhas left tag!"));
							}
						}
						else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUnable to opt out of tag, you are likely already out."));
						}
						return true;
				case "optin":
					case "join":
						if(eventManager.readPlayer(sender.getName()) != 1) {
							eventManager.playerOpt(sender.getName(), 1);
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2You have joined the tag minigame."));
							if (broadcastOpt) {
								Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&2" + sender.getName() + " &bhas opted into tag!"));
							}
						}
						else{
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUnable to join into tag, you are likely already in."));
						}
						return true;
				case "reload":
					if(sender.hasPermission("mctag.reload"))
					{
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Reloading tag..."));
						reload();
					}
					else
					{
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have the required permission to use that command &b(mctag.reload)&c."));
					}
					return true;
				case "score":
					//If a player turns on or off the score
						//if player has score on
							//then turn the scoreboard off
						//if player has score off
							//then turn the scoreboard on
					return true;
				default:
					if(Bukkit.getPlayer(args[0]) != null) {
						if(sender.hasPermission("mctag.name")) {
							if(eventManager.readPlayer(Bukkit.getPlayer(args[0]).getName()) == 1) {
								tagClass.tagPlayer(args[0]);
								sender.sendMessage("You have set the tagged player to " + args[0] + ".");
							}
							else {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUnable to tag, &b" + args[0] + " &cis not playing tag."));
							}
						}
						else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have the required permission to use that command &b(mctag.name)&c."));
						}
					}
					else {
						String commandOILP = "help tag";
						Bukkit.dispatchCommand(console, commandOILP);
					}
					return true;
				}
			}
			return true;
		}
		return false;
    }
    
    public void reload() {
		reloadConfig();
		config = getConfig();
		tagClass.reload(config);
		eventManager.reload(config);
		broadcastOpt = config.getBoolean("broadcastOpt");
	}

	public void checkConfig() {
		try {
			Scanner reader = new Scanner(configFile);	//Open the config file in a scanner for line-by-line reading
			if(reader.hasNext()) {	//If the file has a next line, load it in and begin
				String lineToCheck = reader.nextLine();
				if(lineToCheck.substring(9,12).equals("cha")) { //This happens if the comments still exist in the code, just check next line
					lineToCheck = reader.nextLine();
				}
				if(!(lineToCheck.substring(9,12)).equals("2.1")) {	//This checks the version, change this if the config has been ammended
					logger.log(Level.INFO, "New Config File Found, Updating...");	//Log that the file is being updated
					reader.close();		//Close the reader (No longer needed to read as we are going to overwrite this file)
					rewriteConfig();	//Call the re-write function
				}
				else {	//If the config is current, continue on
					reader.close();		//Close the reader as we are no longer needing to make changes
				}
			}
			else {	//If the config file is empty
				logger.log(Level.INFO, "Config File Empty, Updating...");	//Log that the file is empty
				reader.close();		//Close the reader (No longer needed to read as we are going to overwrite this file)
				rewriteConfig();	//Call the re-write function
			}
		}
		catch(Exception e) {	//Catch anything that can be caused by the config file being corrupted, suggest deleting
			logger.log(Level.INFO, "Failed reading config File, please delete and reload");	//Log that the config is problematic, and should be deleted manually
		}
	}

	/**
	 * Description: rewriteConfig - Checks the config to see if it is the latest, if not it will attempt to update it without losing data
	 * @param None
	 * @return None - Void
	 * @throws None
	 */
	public void rewriteConfig() {
		//Beginning of rewriteConfig - Rewrites the config to a new setup. Creates temp file to hold old data
		try {
			File tempFile2 = new File(folder,"tempFile2.yml");	//Setup a temporary file to distract the config the server is using
			tempFile2.createNewFile();		//Create the temporary file
			config.load(tempFile2);			//Load the config the server is using as the temporary file (allows modifications of the actual config file)
			FileWriter writer = new FileWriter(configFile);	//Open the config file in for writing
			//Below is the writing of the file for the newest version
			//Currently 2.1 rewrote for adding cooldown, broadcast opts
			writer.write("#Do not change the below config version\r\n"
					+ "version: 2.1\r\n"
					+ "taggedPlayer: noPlayer\r\n"
					+ "#Set below to true if you want to broadcast when a player opts in or out of tag\r\n"
					+ "broadcastOpt: true\r\n"
					+ "#Tag cooldown in seconds to re-tag the same player\r\n"
					+ "cooldown: 5\r\n"
					+ "#Set below to true if you want to use scoreboards, false if not\r\n"
					+ "scoreboards: true");
			writer.close();			//Close the configFile (writing done)
			config.load(configFile);	//Load the configFile back in to the server
			tempFile2.delete();			//Delete the temporary file
		}
		catch(Exception e) {	//If for any reason an error has occurred, catch it
			logger.log(Level.WARNING, "Failed to rewrite config file\n" + e);		//Print the fail and the exception thrown.
		}
	}
}
