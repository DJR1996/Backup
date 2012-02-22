package net.tgxn.bukkit.backup.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import net.tgxn.bukkit.backup.config.Strings;

/**
 *
 * @author Domenic Horner
 */
public class CheckInUtil implements Runnable {

    private String thisVersion;
    private Strings strings;

    public CheckInUtil(String thisVersion, Strings strings) {
        this.thisVersion = thisVersion;
        this.strings = strings;
    }

    public void run() {
        try {
            URL versionURL = new URL("1http://checkin.bukkitbackup.com/index.php?ver="+thisVersion+"&fromplugin");
            BufferedReader readURL = new BufferedReader(new InputStreamReader(versionURL.openStream()));
            String webVersion = readURL.readLine();
            if(!webVersion.equals(thisVersion))
                LogUtils.sendLog(strings.getString("pluginoutdate", "This: " + thisVersion + ", Latest: " +webVersion));
            else
                LogUtils.sendLog(strings.getString("pluginupdate", "At version: " + thisVersion));
            readURL.close();
        } catch (MalformedURLException ex) {
            LogUtils.sendLog("Failed to retrieve latest version.");
            LogUtils.exceptionLog(ex);
        } catch (IOException ex) {
            LogUtils.sendLog("Failed to retrieve latest version.");
        }
    }
}
