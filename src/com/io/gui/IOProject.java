package com.io.gui;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class IOProject implements ProjectComponent {

    private List<ProjectClosedEvent> projectClosedEvents = new ArrayList<>();
    public Hashtable<Document, IOPatcher> patchers = new Hashtable<>();

    public IOProject(Project project) {
    }

    public static IOProject getInstance(Project project) {
        return project.getComponent(IOProject.class);
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "IOProject";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        for (ProjectClosedEvent event : projectClosedEvents) {
            event.onClosed();
        }
    }

    public void addProjectClosedListener(ProjectClosedEvent event) {
        this.projectClosedEvents.add(event);
    }
}
