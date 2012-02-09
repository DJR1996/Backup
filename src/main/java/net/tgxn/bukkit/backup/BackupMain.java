package net.tgxn.bukkit.backup;

import java.io.File;
import net.tgxn.bukkit.backup.config.Settings;
import net.tgxn.bukkit.backup.config.Strings;
import net.tgxn.bukkit.backup.listeners.CommandListener;
import net.tgxn.bukkit.backup.listeners.EventListener;
import net.tgxn.bukkit.backup.threading.PrepareBackup;
import static net.tgxn.bukkit.backup.utils.FileUtils.FILE_SEPARATOR;
import net.tgxn.bukkit.backup.utils.LogUtils;
import net.tgxn.bukkit.backup.utils.SharedUtils;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class file for Backup plugin.
 *
 * @author Domenic Horner (gamerx)
 */
public class BackupMain extends JavaPlugin {
    
    // Task ID for main recurring BackupTask. and Main plugin data folder.
    public int mainBackupTaskID = -2;
    public File mainDataFolder;
    
    // Strings, Settings, and the PrepareBackup task.
    private static Strings strings;
    private static Settings settings;
    private PrepareBackup prepareBackup;
    
    @Override
    public void onLoad() {
        
        // Initalize main data folder variable.
        mainDataFolder = this.getDataFolder();
        
        // Initalize logging utilities.
        LogUtils.initLogUtils(this);
        
        // check and create main datafile.
        SharedUtils.checkFolderAndCreate(mainDataFolder);
        
        // Load configuration files.
        strings = new Strings(new File(mainDataFolder, "strings.yml"));
        settings = new Settings(this, new File(mainDataFolder, "config.yml"), strings);
        
        // Run version checking on strings file.
        strings.checkStringsVersion(settings.getStringProperty("requiredstrings"));
        
        // Complee initalization of LogUtils.
        LogUtils.finishInitLogUtils(settings.getBooleanProperty("displaylog"), settings.getBooleanProperty("logtofile"), settings.getStringProperty("backuplogname"));
        
        // Check backup path and the temp folder.
        if(SharedUtils.checkFolderAndCreate(new File(settings.getStringProperty("backuppath")))) {
            
            // Create the tempoary folder.
            if(settings.getBooleanProperty("usetemp"))
                SharedUtils.checkFolderAndCreate(new File(settings.getStringProperty("backuppath").concat(FILE_SEPARATOR).concat(settings.getStringProperty("tempfoldername"))));
            
            // Notify users of this.
            LogUtils.sendLog(strings.getString("createbudir"));
        }
    }
    
    @Override
    public void onEnable() {

        // Get server and plugin manager instances.
        Server pluginServer = getServer();
        PluginManager pluginManager = pluginServer.getPluginManager();

        // Create new "PrepareBackup" instance.
        prepareBackup = new PrepareBackup(pluginServer, settings, strings);

        // Initalize Command Listener.
        getCommand("backup").setExecutor(new CommandListener(prepareBackup, this, settings, strings));
        
        // Initalize Event Listener.
        EventListener eventListener = new EventListener(prepareBackup, this, settings, strings);
        pluginManager.registerEvents(eventListener, this);
        
        // COnfigure main backup task schedule.
        int backupInterval = settings.getIntervalInMinutes();
        if (backupInterval != 0) {
            backupInterval *= 1200;
            mainBackupTaskID = pluginServer.getScheduler().scheduleAsyncRepeatingTask(this, prepareBackup, backupInterval, backupInterval);
        } else {
            LogUtils.sendLog(strings.getString("disbaledauto"));
        }

        // Startup configuration output.
        if (settings.getBooleanProperty("showconfigonstartup")) {
            String interval = settings.getStringProperty("backupinterval");
            String max = Integer.toString(settings.getIntProperty("maxbackups"));
            String empty = (settings.getBooleanProperty("backupemptyserver")) ? "Yes" : "No";
            String everything = (settings.getBooleanProperty("backupeverything")) ? "Yes" : "No";
            String split = (settings.getBooleanProperty("splitbackup")) ? "Yes" : "No";
            String zip = (settings.getBooleanProperty("zipbackup")) ? "Yes" : "No";
            String path = settings.getStringProperty("backuppath");
            LogUtils.sendLog("Configuration:");
            LogUtils.sendLog("Interval: " + interval + ", Max: " + max + ", On Empty: " + empty + ", Everything: " + everything + ".", false);
            LogUtils.sendLog("Split: " + split + ", ZIP: " + zip + ", Path: " + path + ".", false);
        }
        
        // Notify loading complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed loading!", false);
    }
    
    @Override
    public void onDisable () {
        
        // Stop and scheduled tasks.
        this.getServer().getScheduler().cancelTasks(this);
        
        // Shutdown complete.
        LogUtils.sendLog(this.getDescription().getFullName() + " has completed un-loading!", false);
    }
}
