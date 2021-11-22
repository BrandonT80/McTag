package bct.loadupstudios.MCTag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;


import com.earth2me.essentials.IEssentials;
//import de.kinglol12345.AntiAFKPlus.api.AntiAFKPlusAPI;


public class MyListener implements Listener
{
	Tag tagClass;
	boolean eventRunning = false; //This is to cancel the event running multiple times
	long playerTime = 0;
	private FileConfiguration config;
	private int cooldown;
	IEssentials ess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
	
	MyListener(Tag tagClassObject, FileConfiguration conf)
	{
		tagClass = tagClassObject;
		config = conf;
		cooldown = config.getInt("cooldown") * 20;
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
				if(event.getRightClicked().isPermissionSet("mctag.opt") == false || event.getRightClicked().hasPermission("mctag.opt"))//event.getRightClicked().hasPermission("mctag.opt"))
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
}
