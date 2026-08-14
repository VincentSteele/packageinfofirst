package de.vinste.packageinfofirst;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public final class PackageInfoIconProvider implements FileIconProvider, DumbAware {
    private static final Icon ICON = IconLoader.getIcon("/icons/packageInfo.svg", PackageInfoIconProvider.class);

    @Override
    public @Nullable Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        if (project == null
                || !PackageInfoSettings.getInstance().isCustomIconEnabled()
                || !"package-info.java".equals(file.getName())) {
            return null;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        return psiFile != null && PackageInfoTreeStructureProvider.isJavaPackageInfo(psiFile) ? ICON : null;
    }
}
