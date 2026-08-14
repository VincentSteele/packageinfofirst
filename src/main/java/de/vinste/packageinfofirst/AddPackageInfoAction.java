package de.vinste.packageinfofirst;

import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreatePackageInfoAction;
import com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import org.jetbrains.annotations.NotNull;

/** Creates package-info.java using IntelliJ's built-in package-info template. */
public final class AddPackageInfoAction extends CreatePackageInfoAction {
    private static final String ACTION_TEXT = "Add Package Info";

    public AddPackageInfoAction() {
        getTemplatePresentation().setText(ACTION_TEXT);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setText(ACTION_TEXT);
        IdeView view = event.getData(LangDataKeys.IDE_VIEW);
        PsiDirectory[] directories = view != null ? view.getDirectories() : PsiDirectory.EMPTY_ARRAY;
        boolean canCreate = PackageInfoSettings.getInstance().areContextActionsAvailable()
                && directories.length == 1
                && directories[0].isWritable()
                && JavaDirectoryService.getInstance().getPackage(directories[0]) != null
                && ModuleUtilCore.findModuleForPsiElement(directories[0]) != null
                && directories[0].findFile(PsiPackage.PACKAGE_INFO_FILE) == null;
        event.getPresentation().setEnabledAndVisible(canCreate);
    }

    @Override
    protected void elementCreated(CreateFromTemplateDialog dialog, PsiElement createdElement) {
        PsiFile file = createdElement instanceof PsiFile psiFile
                ? psiFile
                : createdElement.getContainingFile();
        if (file != null && file.getVirtualFile() != null) {
            EditPackageInfoAction.previewDescriptor(file.getProject(), file.getVirtualFile()).navigate(true);
        }
    }
}
