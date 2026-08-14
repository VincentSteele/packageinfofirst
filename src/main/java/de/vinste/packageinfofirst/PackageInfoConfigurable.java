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
import java.awt.GridLayout;

public final class PackageInfoConfigurable implements Configurable {
    private JCheckBox customIconCheckBox;
    private JCheckBox hidePackageInfoCheckBox;

    @Override
    public @Nls String getDisplayName() {
        return "Package Info First";
    }

    @Override
    public @Nullable JComponent createComponent() {
        customIconCheckBox = new JCheckBox("Show the custom icon for package-info.java");
        customIconCheckBox.setSelected(PackageInfoSettings.getInstance().isCustomIconEnabled());
        hidePackageInfoCheckBox = new JCheckBox("Hide package-info.java from the Project view");
        hidePackageInfoCheckBox.setSelected(PackageInfoSettings.getInstance().isPackageInfoHidden());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(customIconCheckBox);
        panel.add(hidePackageInfoCheckBox);
        return panel;
    }

    @Override
    public boolean isModified() {
        if (customIconCheckBox == null || hidePackageInfoCheckBox == null) {
            return false;
        }

        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        return customIconCheckBox.isSelected() != settings.isCustomIconEnabled()
                || hidePackageInfoCheckBox.isSelected() != settings.isPackageInfoHidden();
    }

    @Override
    public void apply() {
        if (customIconCheckBox == null || hidePackageInfoCheckBox == null) {
            return;
        }

        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        boolean customIconEnabled = customIconCheckBox.isSelected();
        boolean packageInfoHidden = hidePackageInfoCheckBox.isSelected();
        boolean iconChanged = settings.isCustomIconEnabled() != customIconEnabled;
        boolean visibilityChanged = settings.isPackageInfoHidden() != packageInfoHidden;
        if (!iconChanged && !visibilityChanged) {
            return;
        }

        settings.setCustomIconEnabled(customIconEnabled);
        settings.setPackageInfoHidden(packageInfoHidden);
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            ProjectView.getInstance(project).refresh();
            if (iconChanged) {
                FileEditorManagerEx fileEditorManager = FileEditorManagerEx.getInstanceEx(project);
                for (VirtualFile file : fileEditorManager.getOpenFiles()) {
                    fileEditorManager.updateFilePresentation(file);
                }
            }
        }
    }

    @Override
    public void reset() {
        if (customIconCheckBox != null) {
            customIconCheckBox.setSelected(PackageInfoSettings.getInstance().isCustomIconEnabled());
        }
        if (hidePackageInfoCheckBox != null) {
            hidePackageInfoCheckBox.setSelected(PackageInfoSettings.getInstance().isPackageInfoHidden());
        }
    }

    @Override
    public void disposeUIResources() {
        customIconCheckBox = null;
        hidePackageInfoCheckBox = null;
    }
}
