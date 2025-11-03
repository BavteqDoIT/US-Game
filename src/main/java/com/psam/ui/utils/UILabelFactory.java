package com.psam.ui.utils;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class UILabelFactory {

    public static VerticalLayout createColumnLabels(String[] ranges) {
        VerticalLayout columnLabels = new VerticalLayout();
        columnLabels.setSpacing(true);
        columnLabels.setPadding(false);
        columnLabels.setAlignItems(FlexComponent.Alignment.END);

        for (String range : ranges) {
            Div label = new Div();
            label.add(new Span("🎲 " + range));
            label.getStyle().set("font-size", "16px");
            label.getStyle().set("font-weight", "bold");
            label.getStyle().set("height", "50px");
            label.getStyle().set("display", "flex");
            label.getStyle().set("align-items", "center");
            label.getStyle().set("justify-content", "flex-end");
            label.getStyle().set("padding-right", "8px");
            columnLabels.add(label);
        }

        return columnLabels;
    }
}
