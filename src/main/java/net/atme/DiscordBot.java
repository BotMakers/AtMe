package net.atme;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import net.displayphoenix.Application;
import net.displayphoenix.file.DetailedFile;
import net.atme.elements.Element;
import net.atme.ui.LobbyScreen;
import net.atme.work.WorkCreator;
import net.displayphoenix.init.ColorInit;
import net.displayphoenix.lang.Localizer;
import net.displayphoenix.screen.impl.CircleLoadingSplashScreen;
import net.displayphoenix.ui.widget.FadeOnHoverWidget;
import net.displayphoenix.ui.widget.OverlayOnHoverWidget;
import net.displayphoenix.ui.widget.RoundedButton;
import net.displayphoenix.ui.widget.TextField;
import net.displayphoenix.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TBroski
 */
public class DiscordBot extends DetailedFile {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private transient List<Element> elements = new ArrayList<>();
    private List<String> tempPathsToSave;
    private String name;
    private String version;
    private String description;
    private String author;
    private String prefix;
    private String token;

    public DiscordBot(File file, String name, String version, String description, String author, String prefix, String token) {
        super(file);
        this.name = name;
        this.version = version;
        this.description = description;
        this.author = author;
        this.prefix = prefix.toLowerCase();
        this.token = token;
    }

    public static DiscordBot read(File file) {
        try {
            FileReader reader = new FileReader(file);
            DiscordBot bot = GSON.fromJson(reader, DiscordBot.class);
            if (bot.elements == null)
                bot.elements = new ArrayList<>();

            for (String elementPath : bot.tempPathsToSave) {
                File elementFile = new File(elementPath);
                if (!elementFile.createNewFile()) {
                    FileReader elementReader = new FileReader(elementFile);
                    JsonObject object = GSON.fromJson(elementReader, JsonObject.class);
                    for (Element element : Element.getRegisteredElements()) {
                        if (object.get("type") != null && object.get("type").getAsString().equalsIgnoreCase(element.getRegistryName())) {
                            bot.elements.add(element.deserialize(object.getAsJsonObject("element")));
                            break;
                        }
                    }
                    elementReader.close();
                }
                else {
                    Application.prompt("Base file contents changed!", "Base file not found! Errors may occur.", true);
                    new FileNotFoundException().printStackTrace();
                }
            }

            reader.close();
            return bot;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void open() {
        if (this.elements == null)
            this.elements = new ArrayList<>();
        addRecentFile(this);
        DiscordBot bot = this;
        Application.openWindow(this.getName() + " - " + Application.getTitle(), JFrame.EXIT_ON_CLOSE, parentFrame -> {
            parentFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    save(false);
                }
            });

            FadeOnHoverWidget addElement = new FadeOnHoverWidget(ImageHelper.getImage("bot_screen/add_element"), ImageHelper.getImage("bot_screen/add_element_hovered"), 0.005F);
            addElement.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (App.STATE == App.State.STABLE) {
                        Application.openWindow("Add element", JFrame.DISPOSE_ON_CLOSE, parentFrame1 -> {
                            parentFrame1.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosing(WindowEvent e) {
                                    super.windowClosing(e);
                                    open();
                                }
                            });
                            parentFrame.dispose();
                            Component[] array = new Component[Element.getRegisteredElements().size()];
                            int i = 0;
                            for (Element element : Element.getRegisteredElements()) {
                                OverlayOnHoverWidget elementWidget = new OverlayOnHoverWidget(element.getIcon(), Application.getTheme().getColorTheme().getSecondaryColor(), 0.5F, 0.005F);
                                elementWidget.setPreferredSize(new Dimension(50, 50));
                                elementWidget.addMouseListener(new MouseAdapter() {
                                    @Override
                                    public void mouseClicked(MouseEvent e) {
                                        openBasicElementCreator(element);
                                    }
                                });
                                array[i] = PanelHelper.join(elementWidget);
                                i++;
                            }
                            JPanel elements = PanelHelper.grid(10, 10, array);
                            parentFrame1.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
                            parentFrame1.add(elements);
                        }, Math.round(Application.getTheme().getWidth() * 0.6F), Math.round(Application.getTheme().getHeight() * 0.6F));
                    }
                    else {
                        Application.prompt("Can't add element.", "Bot state is " + App.STATE + ".", true);
                    }
                }
            });
            addElement.setToolTipText(Localizer.translate("bot.add_element.text"));

            FadeOnHoverWidget runClient = new FadeOnHoverWidget(ImageHelper.getImage("bot_screen/run_client"), ImageHelper.getImage("bot_screen/run_client_hovered"), 0.005F);
            runClient.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    parentFrame.dispose();
                    run();
                }
            });
            runClient.setToolTipText(Localizer.translate("bot.run_client.text"));

            FadeOnHoverWidget settings = new FadeOnHoverWidget(ImageHelper.getImage("bot_screen/settings"), ImageHelper.getImage("bot_screen/settings_hovered"), 0.005F);
            settings.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (App.STATE == App.State.STABLE) {
                        refactor(parentFrame);
                    }
                    else {
                        Application.prompt("Can't refactor.", "Bot state is " + App.STATE + ".", true);
                    }
                }
            });
            settings.setToolTipText(Localizer.translate("bot.settings.text"));

            parentFrame.add("South", PanelHelper.join(FlowLayout.CENTER, settings, addElement, runClient));

            //Menu Bar
            JMenuBar menu = new JMenuBar();
            menu.setBorderPainted(false);
            menu.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            parentFrame.setJMenuBar(menu);

            //File
            JMenu fileMenu = new JMenu("File");
            fileMenu.setForeground(Application.getTheme().getColorTheme().getTextColor());
            fileMenu.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            fileMenu.setFont(Application.getTheme().getFont());
            menu.add(fileMenu);

            //File items
            JMenuItem iSaveBot = new JMenuItem(Localizer.translate("bot.file.save.text"));
            iSaveBot.setForeground(Application.getTheme().getColorTheme().getTextColor());
            iSaveBot.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            iSaveBot.setFont(Application.getTheme().getFont());
            iSaveBot.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    save(false);
                }
            });
            fileMenu.add(iSaveBot);

            JMenuItem iRebuildBot = new JMenuItem(Localizer.translate("bot.file.rebuild.text"));
            iRebuildBot.setForeground(Application.getTheme().getColorTheme().getTextColor());
            iRebuildBot.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            iRebuildBot.setFont(Application.getTheme().getFont());
            iRebuildBot.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    save(true);
                }
            });
            fileMenu.add(iRebuildBot);

            JMenuItem iNewBot = new JMenuItem(Localizer.translate("bot.file.new.text"));
            iNewBot.setForeground(Application.getTheme().getColorTheme().getTextColor());
            iNewBot.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            iNewBot.setFont(Application.getTheme().getFont());
            iNewBot.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    WorkCreator.createNewWork(parentFrame);
                }
            });
            fileMenu.add(iNewBot);

            JMenuItem iOpenBot = new JMenuItem(Localizer.translate("bot.file.open.text"));
            iOpenBot.setForeground(Application.getTheme().getColorTheme().getTextColor());
            iOpenBot.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            iOpenBot.setFont(Application.getTheme().getFont());
            iOpenBot.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LobbyScreen.open();
                    parentFrame.dispose();
                }
            });
            fileMenu.add(iOpenBot);

            //Element Panel
            DefaultListModel<Element> elementModel = new DefaultListModel<>();
            for (Element element : this.elements) {
                elementModel.addElement(element);
            }
            JList<Element> elementJList = new JList<>(elementModel);
            elementJList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            elementJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            elementJList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        Application.promptYesOrNo("Remove", "Remove element " + elementJList.getSelectedValue().getName() + "?", true, new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                elements.remove(elementJList.getSelectedValue());
                                parentFrame.dispose();
                                open();
                            }
                        });
                    }
                    else if (e.getClickCount() == 2) {
                        Element element = elementJList.getSelectedValue();
                        element.getElement(bot, element, element.getName());
                    }
                }
            });
            elementJList.setCellRenderer(new ElementsRenderer());
            JScrollPane scrollBar = new JScrollPane(elementJList);
            scrollBar.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            elementJList.setPreferredSize(new Dimension(800, 400));
            elementJList.setOpaque(false);
            parentFrame.add("Center", PanelHelper.join(elementJList));
        }, 1000, 600);
    }

    public void save(boolean rebuild) {
        try {
            // Creating the folders
            File workspaceFolder = new File(this.getFile().getPath() + "/" + StringHelper.condense(this.name) + "Workspace");
            workspaceFolder.mkdir();
            workspaceFolder.createNewFile();

            File elementFolder = new File(workspaceFolder.getPath() + "/elements");
            elementFolder.mkdir();
            elementFolder.createNewFile();


            File implFolder = new File(workspaceFolder.getPath() + "/impl");
            implFolder.mkdir();
            implFolder.createNewFile();

            // Saving elements
            this.tempPathsToSave = new ArrayList<>();
            for (Element element : this.elements) {
                File elementFile = new File(elementFolder.getPath() + "/" +  element.getId() + this.elements.indexOf(element));
                if (elementFile.createNewFile());
                    this.tempPathsToSave.add(elementFile.getPath());
                FileWriter writer = new FileWriter(elementFile);
                JsonObject object = new JsonObject();
                object.add("type", new JsonPrimitive(element.getRegistryName()));
                JsonObject elementObject = new JsonObject();
                elementObject.add("name", new JsonPrimitive(element.getName()));
                element.serialize(elementObject);
                object.add("element", elementObject);
                writer.write(object.toString());
                writer.flush();
                writer.close();
            }

            // Saving main root file
            String atmeJson = GSON.toJson(this);
            File atmeFile = new File(workspaceFolder.getPath() + "/" + StringHelper.id(this.name) + ".atme");
            atmeFile.createNewFile();
            FileWriter writer = new FileWriter(atmeFile);
            writer.write(atmeJson);
            writer.flush();
            writer.close();

            // Build index.js
            if (rebuild) {
                rebuild();
            }
            else {
                build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void build() {
        App.STATE = App.State.BUILDING;

        // Regenerate index.js
        try {
            File index = new File(this.getFile().getPath() + "/" + StringHelper.condense(this.name) + "Workspace/impl/index.js");
            index.createNewFile();

            StringBuilder mainWrapper = new StringBuilder();
            mainWrapper.append("const Discord = require('discord.js'); \n")
                    .append("const bot = new Discord.Client(); \n")
                    .append("const prefix = `" + this.getPrefix().toLowerCase() + "`; \n")
                    .append("const token = '" + this.getToken() + "'; \n")
                    .append("const db = require('quick.db') \n")
                    .append("bot.on('raw', (packet) => { \n")
                    .append("    if (!['MESSAGE_REACTION_ADD', 'MESSAGE_REACTION_REMOVE'].includes(packet.t)) return; \n")
                    .append("    bot.channels.fetch(packet.d.channel_id).then(channel => { \n")
                    .append("        if (channel.messages.cache.has(packet.d.message_id)) return; \n")
                    .append("        channel.messages.fetch(packet.d.message_id).then(message => { \n")
                    .append("            const emoji = packet.d.emoji.id ? `${packet.d.emoji.name}:${packet.d.emoji.id}` : packet.d.emoji.name; \n")
                    .append("            const reaction = message.reactions.cache.get(emoji); \n")
                    .append("            if (reaction) reaction.users.cache.set(packet.d.user_id, bot.users.cache.get(packet.d.user_id)); \n")
                    .append("            if (packet.t === 'MESSAGE_REACTION_ADD') { \n")
                    .append("                bot.emit('messageReactionAdd', reaction, bot.users.cache.get(packet.d.user_id)); \n")
                    .append("            } \n")
                    .append("            if (packet.t === 'MESSAGE_REACTION_REMOVE') { \n")
                    .append("                bot.emit('messageReactionRemove', reaction, bot.users.cache.get(packet.d.user_id)); \n")
                    .append("            } \n")
                    .append("        }); \n")
                    .append("    }) \n")
                    .append("}) \n");

            for (Element element : this.elements) {
                element.parse(mainWrapper);
                mainWrapper.append("\n");
            }

            mainWrapper.append("\n")
                    .append("bot.login(token);");

            FileWriter parser = new FileWriter(index);
            parser.write(mainWrapper.toString());
            parser.flush();
            parser.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        App.STATE = App.State.STABLE;
    }

    public void run() {
        build();
        App.STATE = App.State.RUNNING;
        File run = new File(this.getFile().getPath() + "/" + StringHelper.condense(this.name) + "Workspace/impl/run.bat");
        if (run.exists()) {
            try {
                //System.out.println(run.getPath());
                //Process process = Runtime.getRuntime().exec("cmd.exe /c start " + run.getPath());
                Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "start", "cmd", "/k", run.getPath()});
                System.out.println("[RUNNING] " + this.getName());

                Application.openWindow("Stop Discord Bot", JFrame.DISPOSE_ON_CLOSE, parentFrame -> {
                    OverlayOnHoverWidget stopButton = new OverlayOnHoverWidget(Color.WHITE, Color.RED, 1F, 0.005F);
                    stopButton.setPreferredSize(new Dimension(100, 100));
                    stopButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            System.out.println("[DESTROYED] " + getName());
                            Application.systemExecute("taskkill /f /im node.exe");
                            process.destroy();
                            parentFrame.dispose();
                            App.STATE = App.State.STABLE;
                            open();
                        }
                    });
                    JPanel buttonPanel = PanelHelper.join(stopButton);
                    JLabel note = new JLabel(Localizer.translate("bot.kill.note"));
                    ComponentHelper.themeComponent(note);
                    JPanel notePanel = PanelHelper.join(note);
                    notePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
                    buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
                    parentFrame.add(PanelHelper.centerAndSouthElements(buttonPanel, notePanel));
                }, 300, 300);

/*                StringBuilder output = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line + "\n");
                }*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Application.promptYesOrNo("No wrapper", "No client wrapper found. Rebuild?", true, new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    rebuild(() -> {
                        run();
                    });
                }
            });
        }
    }

    public void rebuild() {
        DiscordBot bot = this;
        Application.prompt("Rebuild", "Rebuilding (May take a moment)", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Deleting entire impl folder
                disposeClientWrapper();

                // Recreating directory
                File implDirectory = new File(getFile().getPath() + "/" + StringHelper.condense(name) + "Workspace/impl");
                implDirectory.mkdir();
                try {
                    implDirectory.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Rebuilding wrapper
                Client.createClientWrapper(bot, implDirectory);

                // Building code
                build();
            }
        }, true);
    }
    public void rebuild(Runnable runAfterRebuild) {
        DiscordBot bot = this;
        Application.prompt("Rebuild", "Rebuilding (May take a moment)", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Deleting entire impl folder
                disposeClientWrapper();

                // Recreating directory
                File implDirectory = new File(getFile().getPath() + "/" + StringHelper.condense(name) + "Workspace/impl");
                implDirectory.mkdir();
                try {
                    implDirectory.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Rebuilding wrapper
                Client.createClientWrapper(bot, implDirectory);

                // Building code
                build();
                runAfterRebuild.run();
            }
        }, true);
    }

    private void disposeClientWrapper() {
        File implFile = new File(this.getFile().getPath() + "/" + StringHelper.condense(this.name) + "Workspace/impl");
        implFile.mkdir();
        if (implFile.exists()) {
            FileHelper.deleteFolder(implFile);
        }
    }

    public void addElement(Element element) {
        this.elements.add(element);
        save(false);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getToken() {
        return token;
    }

    public String getPrefix() {
        return prefix;
    }

    private static void addRecentFile(DiscordBot bot) {
        try {
            File file = new File("recents.json");
            file.createNewFile();

            FileReader reader = new FileReader(file);
            Map<String, String> recentBots = GSON.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
            if (recentBots == null)
                recentBots = new HashMap<>();
            reader.close();

            String prettyPath = StringHelper.getPrettyPath(bot.getFile().getPath()) + "/" + StringHelper.condense(bot.name) + "Workspace/" + StringHelper.id(bot.getName()) + ".atme";

            for (String path : recentBots.values()) {
                if (path.equalsIgnoreCase(bot.getFile().getPath())) {
                    return;
                }
            }
            recentBots.put(bot.getName(), prettyPath);

            String recentBotsBack = GSON.toJson(recentBots);
            FileWriter writer = new FileWriter(file);
            writer.write(recentBotsBack);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openBasicElementCreator(Element element) {
        Application.openWindow(JFrame.DISPOSE_ON_CLOSE, parentFrame -> {
            DiscordBot bot = this;

            TextField nameField = new TextField("Element Name");

            RoundedButton createButton = new RoundedButton("Create");
            createButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (Element prevElement : elements) {
                        if (StringHelper.id(prevElement.getName()).equalsIgnoreCase(StringHelper.id(nameField.getText()))) {
                            Application.prompt("Not applicable", "Name already used.", true);
                            return;
                        }
                    }
                    element.getElement(bot, null, nameField.getText());
                    parentFrame.dispose();
                }
            });
            JPanel namePanel = PanelHelper.join(nameField);
            namePanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
            JPanel createPanel = PanelHelper.join(createButton);
            createPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));

            parentFrame.add(PanelHelper.northAndSouthElements(namePanel, createPanel));
        }, 750, 400);
    }

    private void refactor(JFrame opener) {
        Application.openWindow("Refactor Bot", JFrame.DISPOSE_ON_CLOSE, parentFrame -> {
            JLabel botNameDisplay = new JLabel(this.getName());
            ComponentHelper.themeComponent(botNameDisplay);
            ComponentHelper.deriveFont(botNameDisplay, 45);
            botNameDisplay.setHorizontalAlignment(SwingConstants.CENTER);


            TextField botPrefixField = new TextField(this.getPrefix());
            botPrefixField.setPreferredSize(new Dimension(150, 45));
            botPrefixField.setHorizontalAlignment(SwingConstants.CENTER);

            TextField botTokenField = new TextField(this.getToken());
            botTokenField.setPreferredSize(new Dimension(150, 45));
            botTokenField.setHorizontalAlignment(SwingConstants.CENTER);

            TextField botVersField = new TextField(this.getVersion());
            botVersField.setPreferredSize(new Dimension(150, 45));
            botVersField.setHorizontalAlignment(SwingConstants.CENTER);

            TextField botDescField = new TextField(this.getDescription());
            botDescField.setPreferredSize(new Dimension(150, 45));
            botDescField.setHorizontalAlignment(SwingConstants.CENTER);

            TextField botAuthorField = new TextField(this.getAuthor());
            botAuthorField.setPreferredSize(new Dimension(150, 45));
            botAuthorField.setHorizontalAlignment(SwingConstants.CENTER);


            JLabel botDisplay = new JLabel();
            botDisplay.setIcon(ImageHelper.resize(Application.getIcon(), 300));
            botDisplay.setHorizontalAlignment(SwingConstants.CENTER);
            botDisplay.setCursor(new Cursor(Cursor.HAND_CURSOR));
            botDisplay.setToolTipText(Localizer.translate("refactor.refactor_bot.text"));
            botDisplay.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (StringHelper.massContains(botNameDisplay.getText(), "/", "?", "!", ".")) {
                        Application.prompt("Invalid name", "Name can't contain [/,?,!,.]", true);
                        return;
                    }
                    Application.promptYesOrNo("Refactor?", "Are you sure you want to refactor bot?", true, new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            name = botNameDisplay.getText();
                            prefix = botPrefixField.getText();
                            version = botVersField.getText();
                            description = botDescField.getText();
                            token = botTokenField.getText();
                            author = botAuthorField.getText();

                            opener.dispose();
                            parentFrame.dispose();

                            CircleLoadingSplashScreen splashScreen = new CircleLoadingSplashScreen(ImageHelper.getImage("atme/alt_discord_buddy").getImage(), 300, 300, 140, 10, Color.CYAN, ColorInit.TRANSPARENT);
                            splashScreen.setLoadingProgress(50);
                            save(true);
                            splashScreen.setLoadingProgress(100);
                            open();
                        }
                    });
                }
            });

            JPanel botPanel = PanelHelper.northAndCenterElements(botNameDisplay, botDisplay);
            parentFrame.add("North", botPanel);


            JLabel botPrefixFieldDesc = new JLabel(Localizer.translate("creation.bot_prefix.text"));
            ComponentHelper.themeComponent(botPrefixFieldDesc);
            ComponentHelper.deriveFont(botPrefixFieldDesc, 15);
            botPrefixFieldDesc.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel botTokenFieldDesc = new JLabel(Localizer.translate("creation.bot_token.text"));
            ComponentHelper.themeComponent(botTokenFieldDesc);
            ComponentHelper.deriveFont(botTokenFieldDesc, 15);
            botTokenFieldDesc.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel botVersFieldDesc = new JLabel(Localizer.translate("refactor.bot_vers.text"));
            ComponentHelper.themeComponent(botVersFieldDesc);
            ComponentHelper.deriveFont(botVersFieldDesc, 15);
            botVersFieldDesc.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel botDescFieldDesc = new JLabel(Localizer.translate("creation.bot_desc.text"));
            ComponentHelper.themeComponent(botDescFieldDesc);
            ComponentHelper.deriveFont(botDescFieldDesc, 15);
            botDescFieldDesc.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel botAuthorFieldDesc = new JLabel(Localizer.translate("creation.bot_author.text"));
            ComponentHelper.themeComponent(botAuthorFieldDesc);
            ComponentHelper.deriveFont(botAuthorFieldDesc, 15);
            botAuthorFieldDesc.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel botTopPanel = PanelHelper.grid(50, 0, botVersFieldDesc, botVersField);
            botTopPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            JPanel sep1 = PanelHelper.createSeperator(Application.getTheme().getColorTheme().getAccentColor(), 3, 10);
            JPanel botMidTopPanel = PanelHelper.grid(50, 0, botDescFieldDesc, botDescField);
            botMidTopPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            JPanel sep2 = PanelHelper.createSeperator(Application.getTheme().getColorTheme().getAccentColor(), 3, 10);
            JPanel botMidPanel = PanelHelper.grid(50, 0, botTokenFieldDesc, botTokenField);
            botMidPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            JPanel sep3 = PanelHelper.createSeperator(Application.getTheme().getColorTheme().getAccentColor(), 3, 10);
            JPanel botMidBotPanel = PanelHelper.grid(50, 0, botPrefixFieldDesc, botPrefixField);
            botMidBotPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            JPanel sep4 = PanelHelper.createSeperator(Application.getTheme().getColorTheme().getAccentColor(), 3, 10);
            JPanel botBotPanel = PanelHelper.grid(50, 0, botAuthorFieldDesc, botAuthorField);
            botBotPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            JLabel creationHelpLabel = new JLabel(Localizer.translate("refactor.bot_refactor_help.text"));
            ComponentHelper.themeComponent(creationHelpLabel);
            ComponentHelper.deriveFont(creationHelpLabel, 8);
            JPanel creationHelpPanel = PanelHelper.join(creationHelpLabel);

            JPanel bottomPanel = PanelHelper.northAndCenterElements(botTopPanel, PanelHelper.northAndCenterElements(sep1, PanelHelper.northAndCenterElements(botMidTopPanel, PanelHelper.northAndCenterElements(sep2, PanelHelper.northAndCenterElements(botMidPanel, PanelHelper.northAndCenterElements(sep3, PanelHelper.northAndCenterElements(botMidBotPanel, PanelHelper.northAndCenterElements(sep4, PanelHelper.northAndCenterElements(botBotPanel, creationHelpPanel)))))))));
            bottomPanel.setOpaque(true);
            bottomPanel.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            bottomPanel.setBorder(BorderFactory.createMatteBorder(10, 0, 10, 0, Application.getTheme().getColorTheme().getSecondaryColor()));

            parentFrame.add("South", bottomPanel);
        }, Math.round(Application.getTheme().getWidth() / 1.3F), Math.round(Application.getTheme().getHeight()));
    }

    private static class ElementsRenderer extends JLabel implements ListCellRenderer<Element> {

        @Override
        public Component getListCellRendererComponent(JList<? extends Element> list, Element value, int index, boolean isSelected, boolean cellHasFocus) {
            setOpaque(isSelected);
            setBackground(Application.getTheme().getColorTheme().getPrimaryColor().darker().darker().darker());
            setPreferredSize(new Dimension(100, 50));
            setText(value.getName());
            setIcon(ImageHelper.resize(value.getIcon(), 20));
            setFont(Application.getTheme().getFont());
            setForeground(Application.getTheme().getColorTheme().getTextColor());
            setHorizontalAlignment(SwingConstants.CENTER);

            JPanel thisPanel = PanelHelper.join(this);
            thisPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            return thisPanel;
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Application.getTheme().getColorTheme().getSecondaryColor());
            g.fillRoundRect(0,0,this.getWidth(), this.getHeight(), 15, 15);
            super.paintComponent(g);
        }
    }
}
