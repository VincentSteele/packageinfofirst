package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Pins real Java {@code package-info.java} files to the top of their package in
 * the Project view.
 */
public final class PackageInfoTreeStructureProvider implements TreeStructureProvider, DumbAware {
    private static final String PACKAGE_INFO_FILE_NAME = "package-info.java";

    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(
            @NotNull AbstractTreeNode<?> parent,
            @NotNull Collection<AbstractTreeNode<?>> children,
            ViewSettings settings
    ) {
        List<AbstractTreeNode<?>> modifiedChildren = null;
        int index = 0;

        for (AbstractTreeNode<?> child : children) {
            if (child instanceof PsiFileNode fileNode && isJavaPackageInfo(fileNode.getValue())) {
                if (modifiedChildren == null) {
                    modifiedChildren = new ArrayList<>(children);
                }
                modifiedChildren.set(index, new PackageInfoFileNode(fileNode, settings));
            }
            index++;
        }

        return modifiedChildren != null ? modifiedChildren : children;
    }

    static boolean isJavaPackageInfo(PsiFile file) {
        if (!(file instanceof PsiJavaFile) || !PACKAGE_INFO_FILE_NAME.equals(file.getName())) {
            return false;
        }

        PsiDirectory directory = file.getContainingDirectory();
        return directory != null && isJavaPackageDirectory(directory);
    }

    static boolean isJavaPackageDirectory(PsiDirectory directory) {
        VirtualFile virtualFile = directory.getVirtualFile();
        boolean belongsToProjectSources = ProjectRootManager.getInstance(directory.getProject())
                .getFileIndex()
                .getSourceRootForFile(virtualFile) != null;

        return belongsToProjectSources
                && JavaDirectoryService.getInstance().getPackage(directory) != null;
    }
}
