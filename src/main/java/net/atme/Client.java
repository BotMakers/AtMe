package net.atme;

import net.displayphoenix.Application;
import net.displayphoenix.util.StringHelper;

import java.io.*;

/**
 * @author TBroski
 */
public class Client {

    public static void createClientWrapper(DiscordBot bot, File directory) {
        try {
            StringBuilder packageJson = new StringBuilder();
            packageJson.append("{ \n")
                    .append("  \"name\": \"" + StringHelper.id(bot.getName()) + "\", \n")
                    .append("  \"version\": \"" + bot.getVersion().toLowerCase() + "\", \n")
                    .append("  \"description\": \"" + bot.getDescription() + "\", \n")
                    .append("  \"main\": \"index.js\", \n")
                    .append("  \"scripts\": { \n")
                    .append("    \"test\": \"echo \\\"Error: no test specified\\\" && exit 1\" \n")
                    .append("  }, \n")
                    .append("  \"author\": \"" + bot.getAuthor() + "\", \n")
                    .append("  \"license\": \"ISC\", \n")
                    .append("  \"dependencies\": { \n")
                    .append("    \"discord.js\": \"^12.2.0\", \n")
                    .append("    \"quick.db\": \"^7.1.1\" \n")
                    .append("  } \n")
                    .append("} \n");

            // Create package.json
            File packageJsonFile = new File(directory.getPath() + "/package.json");
            packageJsonFile.createNewFile();
            FileWriter packageJsonWriter = new FileWriter(packageJsonFile);
            packageJsonWriter.write(packageJson.toString());
            packageJsonWriter.flush();
            packageJsonWriter.close();

            // Create an wrapper
            File wrapper = new File(directory.getPath() + "/wrapper.bat");
            wrapper.setExecutable(true);
            if (!wrapper.exists()) {
                wrapper.createNewFile();
                StringBuilder installerBat = new StringBuilder();
                installerBat.append("@echo off \n")
                        .append("cd " + directory.getPath() + "\n")
                        .append("npm install discord.js --save \n");
                FileWriter installerWriter = new FileWriter(wrapper);
                installerWriter.write(installerBat.toString());
                installerWriter.flush();
                installerWriter.close();

                Application.systemExecute(directory.getPath() + "/wrapper.bat");
            }

            // Create quickdb installer
            File quickdb = new File(directory.getPath() + "/quickdb.bat");
            quickdb.setExecutable(true);
            if (!quickdb.exists()) {
                quickdb.createNewFile();
                StringBuilder installerBat = new StringBuilder();
                installerBat.append("@echo off \n")
                        .append("cd " + directory.getPath() + "\n")
                        .append("npm i quick.db \n");
                FileWriter installerWriter = new FileWriter(quickdb);
                installerWriter.write(installerBat.toString());
                installerWriter.flush();
                installerWriter.close();

                Application.systemExecute(directory.getPath() + "/quickdb.bat");
            }

            // Create a client run
            File run = new File(directory.getPath() + "/run.bat");
            run.setExecutable(true);
            if (!run.exists()) {
                run.createNewFile();
                StringBuilder runBat = new StringBuilder();
                runBat.append("cd " + directory.getPath() + "\n")
                        .append("node . \n");

                FileWriter runWriter = new FileWriter(run);
                runWriter.write(runBat.toString());
                runWriter.flush();
                runWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
