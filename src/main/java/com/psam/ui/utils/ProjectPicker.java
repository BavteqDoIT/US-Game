package com.psam.ui.utils;

import com.psam.game.Project;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

public class ProjectPicker extends RadioButtonGroup<Project> {

    public ProjectPicker() {
        setItems(Project.DOM, Project.LAS, Project.JEZIORO, Project.FABRYKA, Project.PLAC);
    }
}
