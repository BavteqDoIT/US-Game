package com.psam.ui.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class MessageService {

    private final Div messagePanel = new Div();

    public MessageService() {
        setupMessagePanel();
    }

    private void setupMessagePanel() {
        messagePanel.getStyle()
                .set("border", "1px solid #ccc")
                .set("padding", "10px")
                .set("width", "410px")
                .set("height", "80px")
                .set("background", "#fafafa")
                .set("overflow", "hidden")
                .set("position", "relative")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-family", "Arial, sans-serif")
                .set("font-size", "14px")
                .set("flex-shrink", "0")
                .set("flex-grow", "0");
    }

    public void log(String msg) {
        UI ui = UI.getCurrent();

        ui.access(() -> {
            messagePanel.removeAll();

            Span message = new Span(msg);
            message.getStyle()
                    .set("opacity", "0")
                    .set("transition", "opacity 0.5s ease-in-out");

            messagePanel.add(message);

            ui.getPage().executeJs("setTimeout(() => {$0.style.opacity = '1';}, 50);", message.getElement());
        });
    }

    public Div getMessagePanel() {
        return messagePanel;
    }
}
