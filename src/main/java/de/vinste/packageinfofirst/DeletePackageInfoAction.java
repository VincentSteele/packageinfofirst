package de.vinste.packageinfofirst;

import com.intellij.ide.util.DeleteHandler;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

/** Deletes a hidden package-info.java through IntelliJ's standard delete flow. */
public final class DeletePackageInfoAction extends DumbAwareAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        boolean visible = PackageInfoSettings.getInstance().isPackageInfoHidden()
                && EditPackageInfoAction.findPackageInfo(event) != null;
        event.getPresentation().setEnabledAndVisible(visible);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null || !PackageInfoSettings.getInstance().isPackageInfoHidden()) {
            return;
        }

        PsiJavaFile packageInfo = ReadAction.computeBlocking(
                () -> EditPackageInfoAction.findPackageInfo(event)
        );
        if (packageInfo != null) {
            DeleteHandler.deletePsiElement(new PsiElement[]{packageInfo}, project);
        }
    }
}
