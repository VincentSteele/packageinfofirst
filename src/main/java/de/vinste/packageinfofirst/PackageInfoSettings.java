package de.vinste.packageinfofirst;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.APP)
@State(name = "PackageInfoFirstSettings", storages = @Storage("packageInfoFirst.xml"))
public final class PackageInfoSettings implements PersistentStateComponent<PackageInfoSettings.SettingsState> {
    private SettingsState state = new SettingsState();

    public static PackageInfoSettings getInstance() {
        return ApplicationManager.getApplication().getService(PackageInfoSettings.class);
    }

    public boolean isCustomIconEnabled() {
        return state.customIconEnabled;
    }

    public void setCustomIconEnabled(boolean customIconEnabled) {
        state.customIconEnabled = customIconEnabled;
    }

    public boolean isPackageInfoHidden() {
        return state.packageInfoHidden;
    }

    public void setPackageInfoHidden(boolean packageInfoHidden) {
        state.packageInfoHidden = packageInfoHidden;
    }

    public boolean isPackageBadgeEnabled() {
        return state.packageBadgeEnabled;
    }

    public void setPackageBadgeEnabled(boolean packageBadgeEnabled) {
        state.packageBadgeEnabled = packageBadgeEnabled;
    }

    public boolean isPackageBadgeVisible() {
        return state.packageInfoHidden || state.packageBadgeEnabled;
    }

    public boolean isDisplayNameEnabled() {
        return state.displayNameEnabled;
    }

    public void setDisplayNameEnabled(boolean displayNameEnabled) {
        state.displayNameEnabled = displayNameEnabled;
    }

    public boolean isContextActionsEnabled() {
        return state.contextActionsEnabled;
    }

    public void setContextActionsEnabled(boolean contextActionsEnabled) {
        state.contextActionsEnabled = contextActionsEnabled;
    }

    public boolean areContextActionsAvailable() {
        return state.packageInfoHidden || state.contextActionsEnabled;
    }

    @Override
    public @NotNull SettingsState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull SettingsState state) {
        this.state = state;
    }

    public static final class SettingsState {
        public boolean customIconEnabled = true;
        public boolean packageInfoHidden = false;
        public boolean packageBadgeEnabled = true;
        public boolean displayNameEnabled = true;
        public boolean contextActionsEnabled = false;
    }
}
