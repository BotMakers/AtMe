package net.atme.work;

import net.displayphoenix.Application;
import net.displayphoenix.file.FileDialog;
import net.atme.DiscordBot;
import net.displayphoenix.init.ColorInit;
import net.displayphoenix.lang.Localizer;
import net.displayphoenix.screen.impl.CircleLoadingSplashScreen;
import net.displayphoenix.ui.widget.TextField;
import net.displayphoenix.util.ComponentHelper;
import net.displayphoenix.util.ImageHelper;
import net.displayphoenix.util.PanelHelper;
import net.displayphoenix.util.StringHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * @author TBroski
 */
public class WorkCreator {

    public static void createNewWork(JFrame opener) {
        Application.openWindow(JFrame.DISPOSE_ON_CLOSE, parentFrame -> {
            JLabel botNameDisplay = new JLabel("Name");
            ComponentHelper.themeComponent(botNameDisplay);
            ComponentHelper.deriveFont(botNameDisplay, 40);
            botNameDisplay.setHorizontalAlignment(SwingConstants.CENTER);


            TextField botNameField = new TextField("Name");
            botNameField.setPreferredSize(new Dimension(150, 45));
            botNameField.setHorizontalAlignment(SwingConstants.CENTER);
            botNameField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (botNameField.getText() != null && !botNameField.getText().isEmpty()) {
                        botNameDisplay.setText(botNameField.getText());
                    }
                    else {
                        botNameDisplay.setText("No Name");
                    }
                }
            });

            TextField botPrefixField = new TextField("prefix");
            botPrefixField.setPreferredSize(new Dimension(150, 45));
            botPrefixField.setHorizontalAlignment(SwingConstants.CENTER);

            TextField botTokenField = new TextField("Token");
            botTokenField.setPreferredSize(new Dimension(150, 45));
            botTokenField.setHorizontalAlignment(SwingConstants.CENTER);

            TextField botDescField = new TextField("Description");
            botDescField.setPreferredSize(new Dimension(150, 45));
            botDescField.setHorizontalAlignment(SwingConstants.CENTER);

            TextField botAuthorField = new TextField("Author");
            botAuthorField.setPreferredSize(new Dimension(150, 45));
            botAuthorField.setHorizontalAlignment(SwingConstants.CENTER);


            JLabel botDisplay = new JLabel();
            botDisplay.setIcon(ImageHelper.resize(Application.getIcon(), 300));
            botDisplay.setHorizontalAlignment(SwingConstants.CENTER);
            botDisplay.setCursor(new Cursor(Cursor.HAND_CURSOR));
            botDisplay.setToolTipText(Localizer.translate("creation.create_bot.text"));
            botDisplay.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (StringHelper.massContains(botNameDisplay.getText(), "/", "?", "!", ".")) {
                        Application.prompt("Invalid name", "Name can't contain [/,?,!,.]", true);
                        return;
                    }
                    File file = FileDialog.getFileDirectory(parentFrame);
                    DiscordBot discordBot = new DiscordBot(file, botNameDisplay.getText(), "1.0.0", botDescField.getText(), botAuthorField.getText(), botPrefixField.getText(), botTokenField.getText());
                    parentFrame.dispose();

                    CircleLoadingSplashScreen splashScreen = new CircleLoadingSplashScreen(ImageHelper.getImage("atme/alt_discord_buddy").getImage(), 300, 300, 140, 10, Color.BLUE, ColorInit.TRANSPARENT);
                    splashScreen.setLoadingProgress(50);
                    discordBot.save(true);
                    splashScreen.setLoadingProgress(100);

                    discordBot.open();
                    opener.dispose();
                }
            });

            JPanel botPanel = PanelHelper.northAndCenterElements(botNameDisplay, botDisplay);
            parentFrame.add("North", botPanel);


            JLabel botNameFieldDesc = new JLabel(Localizer.translate("creation.bot_name.text"));
            ComponentHelper.themeComponent(botNameFieldDesc);
            ComponentHelper.deriveFont(botNameFieldDesc, 15);
            botNameFieldDesc.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel botPrefixFieldDesc = new JLabel(Localizer.translate("creation.bot_prefix.text"));
            ComponentHelper.themeComponent(botPrefixFieldDesc);
            ComponentHelper.deriveFont(botPrefixFieldDesc, 15);
            botPrefixFieldDesc.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel botTokenFieldDesc = new JLabel(Localizer.translate("creation.bot_token.text"));
            ComponentHelper.themeComponent(botTokenFieldDesc);
            ComponentHelper.deriveFont(botTokenFieldDesc, 15);
            botTokenFieldDesc.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel botDescFieldDesc = new JLabel(Localizer.translate("creation.bot_desc.text"));
            ComponentHelper.themeComponent(botDescFieldDesc);
            ComponentHelper.deriveFont(botDescFieldDesc, 15);
            botDescFieldDesc.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel botAuthorFieldDesc = new JLabel(Localizer.translate("creation.bot_author.text"));
            ComponentHelper.themeComponent(botAuthorFieldDesc);
            ComponentHelper.deriveFont(botAuthorFieldDesc, 15);
            botAuthorFieldDesc.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel botTopPanel = PanelHelper.grid(50, 0, botNameFieldDesc, botNameField);
            botTopPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
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
            JLabel creationHelpLabel = new JLabel(Localizer.translate("creation.bot_creation_help.text"));
            ComponentHelper.themeComponent(creationHelpLabel);
            ComponentHelper.deriveFont(creationHelpLabel, 8);
            JPanel creationHelpPanel = PanelHelper.join(creationHelpLabel);
            
            JPanel bottomPanel = PanelHelper.northAndCenterElements(botTopPanel, PanelHelper.northAndCenterElements(sep1, PanelHelper.northAndCenterElements(botMidTopPanel, PanelHelper.northAndCenterElements(sep2, PanelHelper.northAndCenterElements(botMidPanel, PanelHelper.northAndCenterElements(sep3, PanelHelper.northAndCenterElements(botMidBotPanel, PanelHelper.northAndCenterElements(sep4, PanelHelper.northAndCenterElements(botBotPanel, creationHelpPanel)))))))));
            bottomPanel.setOpaque(true);
            bottomPanel.setBackground(Application.getTheme().getColorTheme().getSecondaryColor());
            bottomPanel.setBorder(BorderFactory.createMatteBorder(10, 0, 10, 0, Application.getTheme().getColorTheme().getSecondaryColor()));

            parentFrame.add("South", bottomPanel);
        }, Math.round(Application.getTheme().getWidth() / 1.3F), Application.getTheme().getHeight());
    }
}
