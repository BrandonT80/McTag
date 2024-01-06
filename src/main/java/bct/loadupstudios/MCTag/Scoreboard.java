package bct.loadupstudios.MCTag;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Scoreboard {
    private final Map<UUID, FastBoard> scoreboards = new HashMap<>();    //Player Scoreboards
    private Map<String, Integer> playerScores = new HashMap<String, Integer>();
    private Logger logger;
    Scoreboard(Logger logger)
    {
        this.logger = logger;
    }

    public boolean addPlayerScoreboard(String playerName) {
        try {
            FastBoard board = new FastBoard(Bukkit.getPlayer(playerName));
            scoreboards.put(Bukkit.getPlayer(playerName).getUniqueId(), board);
        }
        catch(Exception e)
        {
            logger.log(Level.INFO, "Failed to add player scoreboard: " + e);
            return false;
        }
        return true;
    }

    public boolean addScore(String playerName)
    {
        //Add score locally to update the scoreboards, every 25 or so tags force update the database
        //And clear this local input
        return true;
    }

    public boolean updateDatabaseScores()
    {
        //Logic to take all current database additions and update the table
        return true;
    }

    public boolean pullScoreboards()
    {
        //Pull all players from database
        //Sort players from highest to lowest tag counts

        /*Fable Code
        for (FastBoard board : scoreboards.values()) {
            if (board.getLines().size() > (players.size() + 1)) {
                for (FastBoard boardz : scoreboards.values()) {
                    try {
                        if (!board.isDeleted()) {
                            boardz.updateLines("");
                            boardz.updateLine(0, "");
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error updating fastboard lines: " + e);
                    }
                }

            }
            for (int i = 0; i < players.size(); i++) {
                try {
                    board.updateLine(i + 1, "" + players.get(i).getPlayerName() + ": " + players.get(i).getPoints());
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error updating fastboard: " + e);
                }
            }
        }*/

        return true;
    }
}
