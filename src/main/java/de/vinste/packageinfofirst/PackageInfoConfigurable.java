package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public final class PackageInfoConfigurable implements Configurable {
    private JCheckBox customIconCheckBox;

    @Override
    public @Nls String getDisplayName() {
        return "Package Info First";
    }

    @Override
    public @Nullable JComponent createComponent() {
        customIconCheckBox = new JCheckBox("Show the custom icon for package-info.java");
        customIconCheckBox.setSelected(PackageInfoSettings.getInstance().isCustomIconEnabled());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(customIconCheckBox, BorderLayout.NORTH);
        return panel;
    }

    @Override
    public boolean isModified() {
        return customIconCheckBox != null
                && customIconCheckBox.isSelected() != PackageInfoSettings.getInstance().isCustomIconEnabled();
    }

    @Override
    public void apply() {
        if (customIconCheckBox == null) {
            return;
        }

        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        boolean customIconEnabled = customIconCheckBox.isSelected();
        if (settings.isCustomIconEnabled() == customIconEnabled) {
            return;
        }

        settings.setCustomIconEnabled(customIconEnabled);
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            ProjectView.getInstance(project).refresh();
            FileEditorManagerEx fileEditorManager = FileEditorManagerEx.getInstanceEx(project);
            for (VirtualFile file : fileEditorManager.getOpenFiles()) {
                fileEditorManager.updateFilePresentation(file);
            }
        }
    }

    @Override
    public void reset() {
        if (customIconCheckBox != null) {
            customIconCheckBox.setSelected(PackageInfoSettings.getInstance().isCustomIconEnabled());
        }
    }

    @Override
    public void disposeUIResources() {
        customIconCheckBox = null;
    }
}
