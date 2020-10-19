package net.atme.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.displayphoenix.Application;
import net.displayphoenix.file.DetailedFile;
import net.displayphoenix.file.FileDialog;
import net.atme.DiscordBot;
import net.atme.App;
import net.atme.work.WorkCreator;
import net.displayphoenix.lang.Localizer;
import net.displayphoenix.ui.widget.FadeOnHoverWidget;
import net.displayphoenix.ui.widget.OverlayOnHoverWidget;
import net.displayphoenix.util.ComponentHelper;
import net.displayphoenix.util.ImageHelper;
import net.displayphoenix.util.PanelHelper;
import net.displayphoenix.util.StringHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TBroski
 */
public class LobbyScreen {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void open() {
        Application.openWindow(parentFrame -> {
            JLabel icon = new JLabel();
            icon.setIcon(ImageHelper.resize(Application.getIcon(), 300));
            icon.setCursor(new Cursor(Cursor.HAND_CURSOR));
            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    App.DOWNLOAD_PAGE.open();
                }
            });
            icon.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel version = new JLabel(Application.getVersion());
            ComponentHelper.themeComponent(version);
            ComponentHelper.deriveFont(version, 37);
            version.setCursor(new Cursor(Cursor.HAND_CURSOR));
            version.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    App.DOWNLOAD_PAGE.open();
                }
            });
            version.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel iconPanel = PanelHelper.northAndCenterElements(icon, version);
            iconPanel.setBorder(BorderFactory.createEmptyBorder(60, 0, 80, 0));

            FadeOnHoverWidget addWork = new FadeOnHoverWidget(ImageHelper.getImage("add_work"), ImageHelper.getImage("add_work_hovered"), 0.005F);
            addWork.setPreferredSize(new Dimension(140,140));
            addWork.setCursor(new Cursor(Cursor.HAND_CURSOR));
            addWork.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (Application.systemExecute("node -v") == 0) {
                        WorkCreator.createNewWork(parentFrame);
                    }
                    else {
                        Application.promptWithButtons("Node.js", "Node.js not found, is required (8.0.0+).", true, new Application.PromptedButton("Download", new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                App.NODEJS_DOWNLOAD_PAGE.open();
                            }
                        }).closeWindow(), new Application.PromptedButton("Disregard", new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                super.mouseClicked(e);
                            }
                        }).closeWindow());
                    }
                }
            });
            FadeOnHoverWidget openWork = new FadeOnHoverWidget(ImageHelper.getImage("open_file"), ImageHelper.getImage("open_file_hovered"), 0.005F);
            openWork.setPreferredSize(new Dimension(140,140));
            openWork.setCursor(new Cursor(Cursor.HAND_CURSOR));
            openWork.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    DetailedFile work = FileDialog.openFile(parentFrame, ".atme");
                    if (work != null && work.getFile() != null) {
                        DiscordBot bot = DiscordBot.read(work.getFile());
                        bot.open();
                        parentFrame.dispose();
                    }
                }
            });
            JPanel actions = PanelHelper.join(FlowLayout.CENTER, 80, 0, addWork, openWork);

            JPanel leftPanel = PanelHelper.northAndCenterElements(iconPanel, actions);
            leftPanel.setPreferredSize(new Dimension(Math.round(parentFrame.getWidth() * 0.7F), parentFrame.getHeight()));


            parentFrame.add("West", leftPanel);


            JPanel preferences = PanelHelper.westAndEastElements(PanelHelper.createVerticalSeperator(Application.getTheme().getColorTheme().getAccentColor(), 1, 0), PanelHelper.join());
            preferences.setPreferredSize(new Dimension(Math.round(parentFrame.getWidth() * 0.3F), parentFrame.getHeight()));
            preferences.setOpaque(true);
            preferences.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            preferences.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Application.getTheme().getColorTheme().getSecondaryColor()));
            preferences.setPreferredSize(new Dimension(Math.round(parentFrame.getWidth() * 0.3F), Math.round(parentFrame.getHeight() * 0.1F)));

            OverlayOnHoverWidget changeLocal = new OverlayOnHoverWidget(ImageHelper.getImage("lang/" + Application.getSelectedLocal().getTag()), Application.getTheme().getColorTheme().getAccentColor(), 0.5F, 0.005F);
            changeLocal.setPreferredSize(new Dimension(75, 35));
            changeLocal.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JFrame localWindow = Application.promptLocalChange("Language");
                    localWindow.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            parentFrame.dispose();
                            open();
                        }
                    });
                }
            });

            preferences.add(PanelHelper.join(changeLocal));

            JPanel recentsPanel = PanelHelper.grid(0,0);
            recentsPanel.setBackground(Application.getTheme().getColorTheme().getSecondaryColor().darker().darker().darker());
            recentsPanel.setOpaque(true);
            recentsPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Application.getTheme().getColorTheme().getAccentColor())); //(Color) UIManager.get("MCreatorLAF.LIGHT_ACCENT")
            recentsPanel.setPreferredSize(new Dimension(Math.round(parentFrame.getWidth() * 0.3F), parentFrame.getHeight()));

            List<RecentBot> recentBots = getRecentBots();
            if (recentBots != null) {
                DefaultListModel<RecentBot> botsModel = new DefaultListModel<>();
                for (RecentBot bot : recentBots) {
                    botsModel.addElement(bot);
                }
                JList<RecentBot> recentBotsList = new JList<>(botsModel);
                recentBotsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                recentBotsList.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            Application.promptYesOrNo("Remove", "Remove recent bot " + recentBotsList.getSelectedValue().getName() + "?", true, new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    removeRecentBot(recentBotsList.getSelectedValue());
                                    parentFrame.dispose();
                                    open();
                                }
                            });
                        }
                        else if (e.getClickCount() == 2) {
                            File botFile = new File(recentBotsList.getSelectedValue().getPath());
                            if (botFile != null) {
                                DiscordBot openedBot = DiscordBot.read(botFile);
                                openedBot.open();
                                parentFrame.dispose();
                            }
                        }
                    }
                });
                recentBotsList.setCellRenderer(new RecentsRenderer());
                recentBotsList.setBackground(Application.getTheme().getColorTheme().getSecondaryColor().darker().darker().darker());
                JScrollPane scrollBar = new JScrollPane(recentBotsList);
                scrollBar.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                recentsPanel.add(recentBotsList);
            }
            else {
                JLabel noRecentBots = new JLabel(Localizer.translate("lobby.no_recent_bots.text"));
                ComponentHelper.themeComponent(noRecentBots);
                ComponentHelper.deriveFont(noRecentBots, 22);
                recentsPanel.add(PanelHelper.join(noRecentBots));
            }

            parentFrame.add("East", PanelHelper.centerAndSouthElements(recentsPanel, preferences)); //PanelHelper.centerAndSouthElements(recentsPanel, preferences)
        });
    }

    private static List<RecentBot> getRecentBots() {
        try {
            File recentsFile = new File("recents.json");
            if (!recentsFile.createNewFile()) {
                FileReader reader = new FileReader(recentsFile);
                Map<String, String> recents = gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
                if (recents == null)
                    recents = new HashMap<>();
                reader.close();
                List<RecentBot> bots = new ArrayList<>();
                if (recents.isEmpty())
                    return null;
                List<String> namesToRemove = new ArrayList<>();
                for (String name : recents.keySet()) {
                    if (new File(recents.get(name)).exists()) {
                        bots.add(new RecentBot(name, recents.get(name)));
                    }
                    else {
                        namesToRemove.add(name);
                    }
                }
                for (String name : namesToRemove) {
                    recents.remove(name);
                }
                return bots;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void removeRecentBot(RecentBot recentBot) {
        try {
            File recentsFile = new File("recents.json");
            if (!recentsFile.createNewFile()) {
                FileReader reader = new FileReader(recentsFile);
                Map<String, String> recents = gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
                reader.close();
                if (recents != null) {
                    recents.remove(recentBot.getName());
                }
                String json = gson.toJson(recents);
                FileWriter writer = new FileWriter(recentsFile);
                writer.write(json);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class RecentsRenderer extends JLabel implements ListCellRenderer<RecentBot> {

        @Override
        public Component getListCellRendererComponent(JList<? extends RecentBot> list, RecentBot value, int index, boolean isSelected, boolean cellHasFocus) {
            ComponentHelper.themeComponent(this);

            setOpaque(isSelected);
            setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            setForeground(isSelected ? Color.GRAY : Application.getTheme().getColorTheme().getTextColor());

            //setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 0));

            String path = value.getPath();
            setText("<html><font style=\"font-size: 25px;\">" + StringHelper.abbreviateString(value.name, 120) + "</font><small><br>" + StringHelper.abbreviateStringInverse(path, 120));
            //setText("hi");
            return this;
        }
    }

    private static class RecentBot {

        private String name;
        private String path;

        public RecentBot(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
    }
}
