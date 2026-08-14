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
    }
}
