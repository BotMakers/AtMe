package net.atme.elements;

import com.google.gson.JsonObject;
import net.atme.DiscordBot;
import net.displayphoenix.util.StringHelper;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Element {

    private static List<Element> REGISTERED_ELEMENTS = new ArrayList<>();

    private String name;

    public Element(String name) {
        this.name = name;
        for (Element element : REGISTERED_ELEMENTS) {
            if (element.getClass() == this.getClass()) {
                return;
            }
        }
        REGISTERED_ELEMENTS.add(this);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return StringHelper.id(this.name);
    }

    public abstract String getRegistryName();
    public abstract ImageIcon getIcon();
    public abstract void serialize(JsonObject object);
    public abstract Element deserialize(JsonObject object);
    public abstract void getElement(DiscordBot bot, Element element, String elementName);
    public abstract void parse(StringBuilder builder);

    public static List<Element> getRegisteredElements() {
        return REGISTERED_ELEMENTS;
    }
}
