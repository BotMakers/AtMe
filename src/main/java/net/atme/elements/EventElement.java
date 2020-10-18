package net.atme.elements;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.displayphoenix.Application;
import net.displayphoenix.blockly.Blockly;
import net.displayphoenix.blockly.BlocklyXmlParser;
import net.displayphoenix.blockly.elements.workspace.ImplementedBlock;
import net.displayphoenix.blockly.ui.BlocklyDependencyPanel;
import net.displayphoenix.file.FileDialog;
import net.atme.DiscordBot;
import net.displayphoenix.ui.widget.RoundedButton;
import net.displayphoenix.util.ImageHelper;
import net.displayphoenix.util.PanelHelper;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author TBroski
 */
public class EventElement extends Element {

    private String xml = "";

    public EventElement(String name) {
        super(name);
    }

    @Override
    public String getRegistryName() {
        return "element.event";
    }

    @Override
    public ImageIcon getIcon() {
        return ImageHelper.getImage("atme/bot_screen/elements/event");
    }

    @Override
    public void serialize(JsonObject object) {
        object.add("xml", new JsonPrimitive(this.xml));
    }

    @Override
    public Element deserialize(JsonObject object) {
        EventElement eventElement = new EventElement(object.get("name").getAsString());
        eventElement.xml = object.get("xml").getAsString();
        return eventElement;
    }

    @Override
    public void getElement(DiscordBot bot, Element elementInput, String elementName) {
        EventElement element = elementInput != null ? (EventElement) elementInput : new EventElement(elementName);
        Application.openWindow("Event Editor", JFrame.DISPOSE_ON_CLOSE, parentFrame -> {
            BlocklyDependencyPanel blockly = new BlocklyDependencyPanel();
            if (!xml.isEmpty()) {
                blockly.getBlocklyPanel().setWorkspace(xml);
            }
            else {
                blockly.getBlocklyPanel().setWorkspace(new ImplementedBlock(Blockly.getBlockFromType("event_head"), 25, 25));
            }
            RoundedButton saveButton = new RoundedButton("Save");
            saveButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ImplementedBlock[] blocks = blockly.getBlocklyPanel().getWorkspace();
                    for (ImplementedBlock block : blocks) {
                        if (!Blockly.JAVASCRIPT.doesBlockContainFields(block)) {
                            Application.prompt("Error", "A block doesn't contain a field!", true);
                            return;
                        }
                    }
                    if (blockly.getUnsatisfiedDependencies().size() > 0) {
                        Application.prompt("Error", "Block(s) aren't satisfied with provisions!", true);
                        return;
                    }
                    element.xml = blockly.getBlocklyPanel().getRawWorkspace();
                    if (element != elementInput) {
                        bot.addElement(element);
                    }
                    bot.save(false);
                    parentFrame.dispose();
                }
            });
            RoundedButton importButton = new RoundedButton("Import");
            importButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    blockly.getBlocklyPanel().importXmlFileToWorkspace(parentFrame);
                }
            });
            RoundedButton exportButton = new RoundedButton("Export");
            exportButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    File savedFile = FileDialog.saveFile(parentFrame, ".atmap").getFile();
                    try {
                        FileWriter fileWriter = new FileWriter(savedFile);
                        fileWriter.write(blockly.getBlocklyPanel().getRawWorkspace());
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });
            JPanel buttonPanel = PanelHelper.join(saveButton, importButton, exportButton);
            parentFrame.add(PanelHelper.northAndCenterElements(buttonPanel, blockly));
        });
    }

    @Override
    public void parse(StringBuilder builder) {
        if (!this.xml.isEmpty()) {
            ImplementedBlock[] blocks = BlocklyXmlParser.fromWorkspaceXml(this.xml);
            for (ImplementedBlock block : blocks) {
                if (block.getBlock().getType().equalsIgnoreCase("event_head")) {
                    System.out.println(this.xml);
                    System.out.println(Blockly.JAVASCRIPT.getCode(block));
                    builder.append(Blockly.JAVASCRIPT.getCode(block));
                }
            }
        }
    }
}
