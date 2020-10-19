package net.atme.blockly;

import net.displayphoenix.blockly.Blockly;
import net.displayphoenix.blockly.BlocklyPluginLoader;
import net.displayphoenix.blockly.elements.Block;
import net.displayphoenix.blockly.elements.Category;
import net.displayphoenix.blockly.elements.workspace.Field;

import java.io.File;

/**
 * @author TBroski
 */
public class Blocks {

    public static Category MESSAGE = new Category() {
        @Override
        public String getName() {
            return "Message";
        }

        @Override
        public String getColor() {
            return "%{BKY_TEXTS_HUE}";
        }
    };
    public static Category USER = new Category() {
        @Override
        public String getName() {
            return "User";
        }

        @Override
        public String getColor() {
            return "0";
        }
    };
    public static Category ROLE = new Category() {
        @Override
        public String getName() {
            return "Role";
        }

        @Override
        public String getColor() {
            return "270";
        }
    };
    public static Category CHANNEL = new Category() {
        @Override
        public String getName() {
            return "Channel";
        }

        @Override
        public String getColor() {
            return "60";
        }
    };
    public static Category GUILD = new Category() {
        @Override
        public String getName() {
            return "Guild";
        }

        @Override
        public String getColor() {
            return "315";
        }
    };

    public static void register() {
        Blockly.registerCategory(USER);
        Blockly.registerCategory(MESSAGE);
        Blockly.registerCategory(ROLE);
        Blockly.registerCategory(CHANNEL);
        Blockly.registerCategory(GUILD);
        File pluginDir = new File("src/main/resources/blockly/");
        pluginDir.mkdir();
        BlocklyPluginLoader.loadBlocksFromDirectory(pluginDir);

        Block eventBlock = Blockly.getBlockFromType("event_head");
        eventBlock.persist();
        eventBlock.hide();
        Blockly.JAVASCRIPT.registerBlockCode(eventBlock, "bot.on($[field%EVENT], $[marker%PARAMS] { \n$[statement%DO] \n})");
        Blockly.JAVASCRIPT.escapeSyntax(eventBlock);
        Blockly.JAVASCRIPT.addMarkerListener(eventBlock, (markerKey, block) -> {
            if (markerKey.equalsIgnoreCase("PARAMS") && block.getFields()[0].getKey().equalsIgnoreCase("EVENT")) {
                Field field = block.getFields()[0];
                if (field.getValue().equalsIgnoreCase("MESSAGE")) {
                    return "(message) =>";
                }
            }
            return null;
        });
        Blockly.JAVASCRIPT.manipulateField(eventBlock, field -> {
            if (field.getKey().equalsIgnoreCase("EVENT")) {
                return "'" + field.getValue().toLowerCase() + "'";
            }
            return null;
        });
    }
}
