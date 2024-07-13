package bct.loadupstudios.MCTag;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Scoreboard {
    private final Map<UUID, FastBoard> scoreboards = new HashMap<>();    //Player Scoreboards
    private Map<String, Integer> playerScores = new HashMap<String, Integer>();
    private Logger logger;
    private Tag tagClass;
    private FileConfiguration config;

    Scoreboard(Logger logger, Tag tagClass, FileConfiguration c) {
        this.logger = logger;
        this.tagClass = tagClass;
        this.config = c;
    }

    public boolean addPlayerScoreboard(String playerName) {
        try {
            FastBoard board = new FastBoard(Bukkit.getPlayer(playerName));
            scoreboards.put(Bukkit.getPlayer(playerName).getUniqueId(), board);
            updateScoreboard();
        }
        catch(Exception e) {
            logger.log(Level.INFO, "Failed to add player scoreboard: " + e);
            return false;
        }
        return true;
    }

    public boolean removePlayerScoreboard(String playerName) {
        try {
            scoreboards.get(Bukkit.getPlayer(playerName).getUniqueId()).delete();   //Deletes the scoreboard from the player
            scoreboards.remove(Bukkit.getPlayer(playerName).getUniqueId());         //Removes the now deleted scoreboard from the list
        }
        catch(Exception e) {
            logger.log(Level.INFO, "Failed to remove player scoreboard: " + e);
            return false;
        }
        return true;
    }


    public boolean addScore(String playerName) {
        //Add score locally to update the scoreboards, every 25 or so tags force update the database
        //And clear this local input
        return true;
    }

    public boolean updateDatabaseScores() {
        //Logic to take all current database additions and update the table
        return true;
    }

    public boolean pullScoreboards() {
        //Pull all players from database
        //Sort players from highest to lowest tag counts
        return true;
    }

    public void updateScoreboard() {
        //For each board
        for (FastBoard board : scoreboards.values()) {
            try {
                if (!board.isDeleted()) {
                    board.updateLines("");
                    board.updateTitle(ChatColor.translateAlternateColorCodes('&',"&cTagged Player:"));
                    board.updateLine(0, tagClass.getCurrentTag());
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error updating fastboard lines: " + e);
            }
        }
    }

    public boolean isScoreboardsActive() {
        return config.getBoolean("scoreboards");
    }
}
