package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
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
 * Replaces real Java {@code package-info.java} files with a Project View node
 * whose sort position is directly below child packages.
 */
public final class PackageInfoTreeStructureProvider implements TreeStructureProvider, DumbAware {
    private static final String PACKAGE_INFO_FILE_NAME = "package-info.java";

    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(
            @NotNull AbstractTreeNode<?> parent,
            @NotNull Collection<AbstractTreeNode<?>> children,
            ViewSettings settings
    ) {
        // PsiDirectoryNode is the representation used by the standard Project pane.
        // Restricting the provider here avoids changing alternate project views.
        if (!(parent instanceof PsiDirectoryNode)) {
            return children;
        }

        List<AbstractTreeNode<?>> updatedChildren = null;
        int index = 0;

        for (AbstractTreeNode<?> child : children) {
            if (child instanceof PsiFileNode fileNode && isJavaPackageInfo(fileNode.getValue())) {
                if (updatedChildren == null) {
                    updatedChildren = new ArrayList<>(children);
                }
                updatedChildren.set(index, new PackageInfoFileNode(fileNode, settings));
            }
            index++;
        }

        return updatedChildren != null ? updatedChildren : children;
    }

    static boolean isJavaPackageInfo(PsiFile file) {
        if (!(file instanceof PsiJavaFile) || !PACKAGE_INFO_FILE_NAME.equals(file.getName())) {
            return false;
        }

        VirtualFile virtualFile = file.getVirtualFile();
        PsiDirectory directory = file.getContainingDirectory();
        if (virtualFile == null || directory == null) {
            return false;
        }

        boolean belongsToProjectSources = ProjectRootManager.getInstance(file.getProject())
                .getFileIndex()
                .getSourceRootForFile(virtualFile) != null;

        return belongsToProjectSources
                && JavaDirectoryService.getInstance().getPackage(directory) != null;
    }
}

