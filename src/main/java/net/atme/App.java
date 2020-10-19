package net.atme;

import net.displayphoenix.Application;
import net.displayphoenix.blockly.Blockly;
import net.displayphoenix.blockly.ui.BlocklyDependencyPanel;
import net.displayphoenix.blockly.ui.BlocklyPanel;
import net.displayphoenix.enums.WidgetStyle;
import net.atme.blockly.Blocks;
import net.atme.elements.EventElement;
import net.atme.ui.LobbyScreen;
import net.displayphoenix.init.ColorInit;
import net.displayphoenix.screen.impl.CircleLoadingSplashScreen;
import net.displayphoenix.ui.ColorTheme;
import net.displayphoenix.ui.Theme;
import net.displayphoenix.util.ImageHelper;
import net.displayphoenix.web.Website;

import java.awt.*;

/**
 * @author TBroski
 */
public class App {

    public static Website DOWNLOAD_PAGE = new Website("https://google.com");
    public static Website NODEJS_DOWNLOAD_PAGE = new Website("https://nodejs.org/en/download/");

    public static State STATE = State.STABLE;

    //        Theme theme = new Theme(new ColorTheme(new Color(92, 219, 149), new Color(5, 56, 107), new Color(237, 245, 225)), WidgetStyle.POPPING, new Font(Font.MONOSPACED, Font.PLAIN, 14), 1200, 900);
    public static void main(String args[]) {
        CircleLoadingSplashScreen splashScreen = new CircleLoadingSplashScreen(ImageHelper.getImage("alt_discord_buddy").getImage(), 300, 300, 140, 10, Color.BLUE, ColorInit.TRANSPARENT);
        splashScreen.setLoadingProgress(25);
        Theme theme = new Theme(new ColorTheme(new Color(38, 38, 38), new Color(192, 226, 113), new Color(255, 255, 255), Color.GRAY), WidgetStyle.POPPING, new Font(Font.MONOSPACED, Font.PLAIN, 14), 1200, 800);
        System.out.println("Theme created");
        splashScreen.setLoadingProgress(50);
        new EventElement("event");
        System.out.println("Elements created");
        splashScreen.setLoadingProgress(75);
        Blockly.queueFlowControl();
        Blockly.queueLogic();
        Blockly.queueMath();
        Blockly.queueText();
        Blocks.register();
        System.out.println("Blockly created");
        splashScreen.setLoadingProgress(100);
        Application.create("AtMe", ImageHelper.getImage("alt_discord_buddy"), theme, "Alpha 1.0");

        //LobbyScreen.open();
    }

    public enum State {
        STABLE, BUILDING, RUNNING
    }
}
