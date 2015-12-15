package com.io.gui;

import com.intellij.ui.JBColor;

import java.util.ArrayList;

public class Colors {

    private static final ArrayList<JBColor> colorList = new ArrayList<JBColor>() {{
        add(JBColor.BLUE);
        add(JBColor.GREEN);
        add(JBColor.ORANGE);
        add(JBColor.PINK);
        add(JBColor.YELLOW);
        add(JBColor.RED);
    }};

    public static JBColor getColorById(int id) {
        return colorList.get(id);
    }
}
