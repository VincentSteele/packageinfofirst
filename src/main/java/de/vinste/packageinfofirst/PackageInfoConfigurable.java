package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

public final class PackageInfoConfigurable implements Configurable {
    private JCheckBox customIconCheckBox;
    private JCheckBox displayNameCheckBox;
    private JCheckBox hidePackageInfoCheckBox;
    private JCheckBox packageBadgeCheckBox;
    private JCheckBox contextActionsCheckBox;
    private boolean packageBadgeWhenVisible;
    private boolean contextActionsWhenVisible;
    private boolean adjustingControls;

    @Override
    public @Nls String getDisplayName() {
        return "Package Info First";
    }

    @Override
    public @Nullable JComponent createComponent() {
        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        customIconCheckBox = new JCheckBox("Use the Package Info icon for package-info.java");
        customIconCheckBox.setSelected(settings.isCustomIconEnabled());
        displayNameCheckBox = new JCheckBox("Display package-info.java as Package Info");
        displayNameCheckBox.setSelected(settings.isDisplayNameEnabled());
        hidePackageInfoCheckBox = new JCheckBox("Hide Package Info from the Project view");
        hidePackageInfoCheckBox.setSelected(settings.isPackageInfoHidden());
        packageBadgeCheckBox = new JCheckBox("Show a badge on packages containing package-info.java");
        packageBadgeCheckBox.setToolTipText(
                "Always enabled when package-info.java is hidden from the Project view"
        );
        packageBadgeWhenVisible = settings.isPackageBadgeEnabled();
        packageBadgeCheckBox.addItemListener(event -> {
            if (!adjustingControls && packageBadgeCheckBox.isEnabled()) {
                packageBadgeWhenVisible = packageBadgeCheckBox.isSelected();
            }
        });
        contextActionsCheckBox = new JCheckBox("Show Package Info actions in the package context menu");
        contextActionsCheckBox.setToolTipText(
                "Always enabled when package-info.java is hidden from the Project view"
        );
        contextActionsWhenVisible = settings.isContextActionsEnabled();
        contextActionsCheckBox.addItemListener(event -> {
            if (!adjustingControls && contextActionsCheckBox.isEnabled()) {
                contextActionsWhenVisible = contextActionsCheckBox.isSelected();
            }
        });
        hidePackageInfoCheckBox.addItemListener(event -> updateDependentControls());
        updateDependentControls();

        return FormBuilder.createFormBuilder()
                .addComponent(customIconCheckBox)
                .addComponent(displayNameCheckBox)
                .addComponent(hidePackageInfoCheckBox)
                .addComponent(packageBadgeCheckBox)
                .addComponent(contextActionsCheckBox)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @Override
    public boolean isModified() {
        if (customIconCheckBox == null
                || displayNameCheckBox == null
                || hidePackageInfoCheckBox == null
                || packageBadgeCheckBox == null
                || contextActionsCheckBox == null) {
            return false;
        }

        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        return customIconCheckBox.isSelected() != settings.isCustomIconEnabled()
                || displayNameCheckBox.isSelected() != settings.isDisplayNameEnabled()
                || hidePackageInfoCheckBox.isSelected() != settings.isPackageInfoHidden()
                || packageBadgeWhenVisible != settings.isPackageBadgeEnabled()
                || contextActionsWhenVisible != settings.isContextActionsEnabled();
    }

    @Override
    public void apply() {
        if (customIconCheckBox == null
                || displayNameCheckBox == null
                || hidePackageInfoCheckBox == null
                || packageBadgeCheckBox == null
                || contextActionsCheckBox == null) {
            return;
        }

        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        boolean customIconEnabled = customIconCheckBox.isSelected();
        boolean displayNameEnabled = displayNameCheckBox.isSelected();
        boolean packageInfoHidden = hidePackageInfoCheckBox.isSelected();
        boolean iconChanged = settings.isCustomIconEnabled() != customIconEnabled;
        boolean displayNameChanged = settings.isDisplayNameEnabled() != displayNameEnabled;
        boolean visibilityChanged = settings.isPackageInfoHidden() != packageInfoHidden;
        boolean packageBadgeChanged = settings.isPackageBadgeEnabled() != packageBadgeWhenVisible;
        boolean contextActionsChanged = settings.isContextActionsEnabled() != contextActionsWhenVisible;
        if (!iconChanged
                && !displayNameChanged
                && !visibilityChanged
                && !packageBadgeChanged
                && !contextActionsChanged) {
            return;
        }

        settings.setCustomIconEnabled(customIconEnabled);
        settings.setDisplayNameEnabled(displayNameEnabled);
        settings.setPackageInfoHidden(packageInfoHidden);
        settings.setPackageBadgeEnabled(packageBadgeWhenVisible);
        settings.setContextActionsEnabled(contextActionsWhenVisible);
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
        if (customIconCheckBox == null
                || displayNameCheckBox == null
                || hidePackageInfoCheckBox == null
                || packageBadgeCheckBox == null
                || contextActionsCheckBox == null) {
            return;
        }

        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        adjustingControls = true;
        customIconCheckBox.setSelected(settings.isCustomIconEnabled());
        displayNameCheckBox.setSelected(settings.isDisplayNameEnabled());
        hidePackageInfoCheckBox.setSelected(settings.isPackageInfoHidden());
        packageBadgeWhenVisible = settings.isPackageBadgeEnabled();
        contextActionsWhenVisible = settings.isContextActionsEnabled();
        adjustingControls = false;
        updateDependentControls();
    }

    @Override
    public void disposeUIResources() {
        customIconCheckBox = null;
        displayNameCheckBox = null;
        hidePackageInfoCheckBox = null;
        packageBadgeCheckBox = null;
        contextActionsCheckBox = null;
        adjustingControls = false;
    }

    private void updateDependentControls() {
        if (hidePackageInfoCheckBox == null
                || packageBadgeCheckBox == null
                || contextActionsCheckBox == null) {
            return;
        }

        boolean packageInfoHidden = hidePackageInfoCheckBox.isSelected();
        adjustingControls = true;
        packageBadgeCheckBox.setSelected(packageInfoHidden || packageBadgeWhenVisible);
        packageBadgeCheckBox.setEnabled(!packageInfoHidden);
        contextActionsCheckBox.setSelected(packageInfoHidden || contextActionsWhenVisible);
        contextActionsCheckBox.setEnabled(!packageInfoHidden);
        adjustingControls = false;
    }
}
