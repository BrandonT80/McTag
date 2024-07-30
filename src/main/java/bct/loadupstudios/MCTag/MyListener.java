package bct.loadupstudios.MCTag;

import jdk.jfr.internal.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import com.earth2me.essentials.IEssentials;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MyListener implements Listener
{
	Tag tagClass;
	boolean eventRunning = false; //This is to cancel the event running multiple times
	long playerTime = 0;
	private FileConfiguration config;
	private int cooldown;
	IEssentials ess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
	Connection conn = null; 
    Statement stmt = null; 
    String sqlString;
    Logger logger;
    // JDBC driver name and database URL 
 	static final String JDBC_DRIVER = "org.h2.Driver";   
 	static final String DB_URL = "jdbc:h2:./plugins/MCTag/MCTag";  
 	//  Database credentials 
 	static final String USER = "sa"; 
 	static final String PASS = "";
	Scoreboard scoreboards;
	
	MyListener(Tag tagClassObject, FileConfiguration conf, Logger l) {
		tagClass = tagClassObject;
		config = conf;
		cooldown = config.getInt("cooldown") * 20;
		logger = l;

		checkForDatabase();
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement(); 
		}
		catch(Exception e) {
			logger.log(Level.WARNING, "Exception found: " + e);
		}
		scoreboards = new Scoreboard(logger, tagClass, config);
	}
	
	MyListener()
	{
		
	}
	
	public void reload(FileConfiguration conf) {
		config = conf;
		cooldown = config.getInt("cooldown") * 20;
	}
	
	@EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
		if(!eventRunning && event.getRightClicked() instanceof Player) {
			eventRunning = true;
			if(tagClass.getCurrentTag().equals(event.getPlayer().getName())
					&& (event.getPlayer().getPlayerTime() > (playerTime + cooldown)
					|| Math.abs(event.getPlayer().getPlayerTime() - playerTime) > cooldown
					|| event.getPlayer().getPlayerTime() == playerTime))
			{
				if(readPlayer(event.getRightClicked().getName()) == 1) {
					if(!ess.getUser(Bukkit.getPlayer(event.getRightClicked().getName())).isAfk()) {
						event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5, 1);
						Bukkit.getPlayer(event.getRightClicked().getName()).playSound(Bukkit.getPlayer(event.getRightClicked().getName()).getLocation(), Sound.ENTITY_PLAYER_HURT, 5, 1);
						tagClass.tagPlayer(event.getRightClicked().getName(), event.getPlayer().getName());
						playerTime = Bukkit.getPlayer(event.getRightClicked().getName()).getPlayerTime();
					}
					else {
						event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou cannot tag an afk player"));
					}
				} 
				else {
					event.getPlayer().sendMessage(event.getPlayer().getUniqueId(), ChatColor.translateAlternateColorCodes('&', "&c" + event.getRightClicked().getName() + " &cis not playing tag."));
				}
			} 
			else if(tagClass.getCurrentTag().equals(event.getPlayer().getPlayerListName())) {
				event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou cant tag yet (Cooldown is " + (cooldown / 20) + "s)."));
			}
		}
		else {
			eventRunning = false;
		}
    }
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		switch(readPlayer(event.getPlayer().getName())) {
			case 0:
				insertPlayer(event.getPlayer().getName());
			case 1:
				if(scoreboards.isScoreboardsActive()) {
					scoreboards.addPlayerScoreboard(event.getPlayer().getName());
				}
				break;
		}
    }

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if(config.getBoolean("autoTagOnLeave")) {
			if (readPlayer(event.getPlayer().getName()) == 1) {
				scoreboards.removePlayerScoreboard(event.getPlayer().getName());
			}
			tagClass.restartTagIfRemovedPlayerIsTagged(event.getPlayer().getName());
		}
	}
	
	public int readPlayer(String username) {
		sqlString = "SELECT USERNAME, PLAYING FROM MCTAG WHERE USERNAME = '" + username + "'";
		try {
			ResultSet result = stmt.executeQuery(sqlString);
			if(result.next()) {
				//Return Values: 1 - Playing, 2 - Not Playing
				return result.getInt("PLAYING");				
			}
			else {
				return 0;
			}
		}
		catch(Exception e) {
			//Return -1 to show this player is not in the database
			return -1;
		}
	}
	
	public int insertPlayer(String username) {
		sqlString = "INSERT INTO MCTAG VALUES('" + username + "', 1)";
		try {
			logger.log(Level.INFO, "Inserting " + username + " into the db");
			return stmt.executeUpdate(sqlString);
		}
		catch(Exception e) {
			logger.log(Level.INFO, "Error Found Inserting " + username + " into the db - " + e);
			return -1;
		}
	}
	
	public int playerOpt(String username, int opt) {
		sqlString = "UPDATE MCTAG SET PLAYING = " + opt + " WHERE USERNAME = '" + username + "'";
		try {
			int statementReturn = stmt.executeUpdate(sqlString);
			if (opt == 2) {
				tagClass.restartTagIfRemovedPlayerIsTagged(username);
			}
			if(scoreboards.isScoreboardsActive()) {
				if (opt == 2) {
					scoreboards.removePlayerScoreboard(username);
				} else if (opt == 1) {
					scoreboards.addPlayerScoreboard(username);
				}
			}
			return statementReturn;
		}
		catch(Exception e) {
			return -1;
		}
	}
	
	public int checkForDatabase() {
		try {
	    	//File tempFile = new File("~/plugins/MCTag/MCTag.h2.db");
	    	File tempFile = new File("./plugins/MCTag/MCTag.mv.db");
	    	if(tempFile.exists()) {
	    		logger.log(Level.INFO, "Database Found!");
	    		return 1;
	    	}
	    	else {
	    		logger.log(Level.INFO, "Creating Database");
	    		// STEP 1: Register JDBC driver 
		    	Class.forName(JDBC_DRIVER);
		    	
		    	//STEP 2: Open a connection
		        logger.log(Level.INFO, "Connecting to database...");
		        conn = DriverManager.getConnection(DB_URL,USER,PASS);
		        
		        //STEP 3: Execute a query
		        logger.log(Level.INFO, "Creating table in given database...");
		        
				stmt = conn.createStatement(); 

				sqlString = "CREATE TABLE MCTAG ("
						+ "  username VARCHAR(45) NOT NULL,"
						+ "  playing TINYINT NOT NULL DEFAULT 1,"
						+ "  PRIMARY KEY (username))";
				stmt.executeUpdate(sqlString);

				logger.log(Level.INFO, "Created table in given database...");
				onExit();
				return 1;
	    	}
	    }
	    catch(Exception e) {
	    	logger.log(Level.WARNING, "Exception found: " + e);
	    	return -1;
	    }
	}
	
	public void onExit() {
		try {
			// STEP 4: Clean-up environment 
			stmt.close();
			conn.close();
			logger.log(Level.INFO, "Database Closed");
		}
		catch(Exception e) {
			logger.log(Level.WARNING, "Exception found: " + e);
		}
	}
}
