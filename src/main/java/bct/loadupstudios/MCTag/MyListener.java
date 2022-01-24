package bct.loadupstudios.MCTag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import com.earth2me.essentials.IEssentials;

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
	
	MyListener(Tag tagClassObject, FileConfiguration conf, Logger l)
	{
		tagClass = tagClassObject;
		config = conf;
		cooldown = config.getInt("cooldown") * 20;
		logger = l;

		checkForDatabase();
		try
		{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement(); 
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception found: " + e);
		}
	}
	
	MyListener()
	{
		
	}
	
	public void reload(FileConfiguration conf)
	{
		config = conf;
		cooldown = config.getInt("cooldown") * 20;
	}
	/*@EventHandler
    public void onPlayerClick(EntityDamageByEntityEvent event)
    {
		System.out.println("Interact!");
    }*/
	
	@EventHandler
    public void onInteract(PlayerInteractEntityEvent event)
    {
		if(!eventRunning && event.getRightClicked() instanceof Player)
		{
			
			eventRunning = true;
			//System.out.println("P: " + event.getPlayer().getPlayerTime() + "S:" + playerTime);
			if(tagClass.getCurrentTag().equals(event.getPlayer().getPlayerListName()) && (event.getPlayer().getPlayerTime() > (playerTime + cooldown) || Math.abs(event.getPlayer().getPlayerTime() - playerTime) > cooldown || event.getPlayer().getPlayerTime() == playerTime))
			{
				//System.out.println("Tag Interact!");
				//if(event.getRightClicked().isPermissionSet("mctag.opt") == false || event.getRightClicked().hasPermission("mctag.opt"))//event.getRightClicked().hasPermission("mctag.opt"))
				if(readPlayer(event.getRightClicked().getName()) == 1)
				{
					if(!ess.getUser(Bukkit.getPlayer(event.getRightClicked().getName())).isAfk())
					{
						event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5, 1);
						Bukkit.getPlayer(event.getRightClicked().getName()).playSound(Bukkit.getPlayer(event.getRightClicked().getName()).getLocation(), Sound.ENTITY_PLAYER_HURT, 5, 1);
						tagClass.tagPlayer(event.getRightClicked().getName(), event.getPlayer().getPlayerListName());
						playerTime = Bukkit.getPlayer(event.getRightClicked().getName()).getPlayerTime();
					}
					else
					{
						event.getPlayer().sendMessage("You cannot tag an afk player");
					}
				} 
				else
				{
					event.getPlayer().sendMessage(event.getPlayer().getUniqueId(), ChatColor.translateAlternateColorCodes('&', "&c" + event.getRightClicked().getName() + " &fis not playing tag."));
				}
			} 
			else if(tagClass.getCurrentTag().equals(event.getPlayer().getPlayerListName()))
			{
					event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou cant tag yet (Cooldown is " + (cooldown / 20) + "s)."));
			}
		}
		else
		{
			//System.out.println("Interact!");
			eventRunning = false;
		}
		//System.out.println("Interact!");
    }
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
		if(readPlayer(event.getPlayer().getDisplayName()) == 0)
		{
			insertPlayer(event.getPlayer().getDisplayName());
		}
		else
		{
			//logger.log(Level.INFO,"event.getPlayer().getDisplayName(): " + event.getPlayer().getDisplayName());
			//logger.log(Level.INFO,"readPlayer(event.getPlayer().getDisplayName()): " + readPlayer(event.getPlayer().getDisplayName()));
		}
    }
	
	public int readPlayer(String username)
	{
		sqlString = "SELECT USERNAME, PLAYING FROM MCTAG WHERE USERNAME = '" + username + "'";
		try
		{
			ResultSet result = stmt.executeQuery(sqlString);
			if(result.next())
			{
				return result.getInt("PLAYING");				
			}
			else
			{
				return 0;
			}
		}
		catch(Exception e)
		{
			//Return -1 to show this player is not in the database
			return -1;
		}
	}
	
	public int insertPlayer(String username)
	{
		sqlString = "INSERT INTO MCTAG VALUES('" + username + "', 1)";
		try
		{
			logger.log(Level.INFO, "Inserting " + username + " into the db");
			return stmt.executeUpdate(sqlString);
		}
		catch(Exception e)
		{
			return -1;
		}
	}
	
	public int playerOpt(String username, int opt)
	{
		sqlString = "UPDATE MCTAG SET PLAYING = " + opt + " WHERE USERNAME = '" + username + "'";
		try
		{
			//logger.log(Level.INFO, "Updating " + username + " in the db as: " + opt);
			return stmt.executeUpdate(sqlString);
		}
		catch(Exception e)
		{
			return -1;
		}
	}
	
	public int checkForDatabase()
	{
		try 
	    {
	    	//File tempFile = new File("~/plugins/MCTag/MCTag.h2.db");
	    	File tempFile = new File("./plugins/MCTag/MCTag.mv.db");
	    	if(tempFile.exists())
	    	{
	    		//System.out.println("File Exists");
	    		logger.log(Level.INFO, "Database Found!");
	    		return 1;
	    	}
	    	else
	    	{
	    		//System.out.println("Creating File");
	    		logger.log(Level.INFO, "Creating Database");
	    		// STEP 1: Register JDBC driver 
		    	Class.forName(JDBC_DRIVER);
		    	
		    	//STEP 2: Open a connection 
		        //System.out.println("Connecting to database..."); 
		        logger.log(Level.INFO, "Connecting to database...");
		        conn = DriverManager.getConnection(DB_URL,USER,PASS);
		        
		        //STEP 3: Execute a query 
		        //System.out.println("Creating table in given database..."); 
		        logger.log(Level.INFO, "Creating table in given database...");
		        
				stmt = conn.createStatement(); 

				sqlString = "CREATE TABLE MCTAG ("
						+ "  username VARCHAR(45) NOT NULL,"
						+ "  playing TINYINT NOT NULL DEFAULT 1,"
						+ "  PRIMARY KEY (username))";
				stmt.executeUpdate(sqlString);
				
				//System.out.println("Created table in given database..."); 
				logger.log(Level.INFO, "Created table in given database...");
				onExit();
				return 1;
	    	}
	    }
	    catch(Exception e)
	    {
	    	logger.log(Level.WARNING, "Exception found: " + e);
	    	return -1;
	    }
	}
	
	public void onExit()
	{
		try
		{
			// STEP 4: Clean-up environment 
			stmt.close();
			conn.close();
			logger.log(Level.INFO, "Database Closed");
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception found: " + e);
		}
	}
}
