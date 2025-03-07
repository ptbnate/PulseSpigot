package me.nate.spigot.util;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

/**
 * @author SaithTime
 */
public class ClickableBuilder {
    private final TextComponent textComponent;

    public ClickableBuilder(String message) {
        this.textComponent = new TextComponent(message);
    }

    public ClickableBuilder setHover(String hover) {
        this.textComponent.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create())
        );
        return this;
    }

    public ClickableBuilder setClick(String command, ClickEvent.Action mode) {
        this.textComponent.setClickEvent(new ClickEvent(mode, command));
        return this;
    }

    public ClickableBuilder setClickRun(String command, ClickEvent.Action mode) {
        this.textComponent.setClickEvent(new ClickEvent(mode, command));
        return this;
    }

    public TextComponent build() {
        return this.textComponent;
    }
}