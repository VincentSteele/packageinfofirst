package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.impl.nodes.PackageElement;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Opens the selected package's package-info.java in a preview editor tab. */
public final class EditPackageInfoAction extends DumbAwareAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(findPackageInfo(event) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        PsiJavaFile packageInfo = ReadAction.computeBlocking(() -> findPackageInfo(event));
        VirtualFile file = packageInfo != null ? packageInfo.getVirtualFile() : null;
        if (file != null) {
            previewDescriptor(project, file).navigate(true);
        }
    }

    static @NotNull OpenFileDescriptor previewDescriptor(
            @NotNull Project project,
            @NotNull VirtualFile file
    ) {
        return new OpenFileDescriptor(project, file).setUsePreviewTab(true);
    }

    static @Nullable PsiJavaFile findPackageInfo(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return null;
        }

        PsiElement element = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (element instanceof PsiDirectory directory) {
            return PackageInfoTreeStructureProvider.findPackageInfo(directory);
        }
        if (element instanceof PsiPackage psiPackage) {
            return PackageInfoTreeStructureProvider.findPackageInfo(psiPackage);
        }

        PackageElement packageElement = event.getData(PackageElement.DATA_KEY);
        if (packageElement != null) {
            return PackageInfoTreeStructureProvider.findPackageInfo(packageElement.getPackage());
        }

        return null;
    }
}
