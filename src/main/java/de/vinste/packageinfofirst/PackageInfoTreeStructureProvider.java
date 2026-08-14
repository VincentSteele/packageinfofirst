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
import com.intellij.psi.PsiPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

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
        boolean containsPackageInfo = false;
        for (AbstractTreeNode<?> child : children) {
            if (child instanceof PsiFileNode fileNode && isJavaPackageInfo(fileNode.getValue())) {
                containsPackageInfo = true;
                break;
            }
        }

        if (!containsPackageInfo) {
            return children;
        }

        boolean hidePackageInfo = PackageInfoSettings.getInstance().isPackageInfoHidden();
        List<AbstractTreeNode<?>> modifiedChildren = new ArrayList<>(children.size());
        for (AbstractTreeNode<?> child : children) {
            if (child instanceof PsiFileNode fileNode && isJavaPackageInfo(fileNode.getValue())) {
                if (!hidePackageInfo) {
                    modifiedChildren.add(new PackageInfoFileNode(fileNode, settings));
                }
            } else {
                modifiedChildren.add(child);
            }
        }
        return modifiedChildren;
    }

    static @Nullable PsiJavaFile findPackageInfo(@NotNull PsiDirectory directory) {
        PsiFile file = directory.findFile(PACKAGE_INFO_FILE_NAME);
        return isJavaPackageInfo(file) ? (PsiJavaFile)file : null;
    }

    static @Nullable PsiJavaFile findPackageInfo(@NotNull PsiPackage psiPackage) {
        for (PsiDirectory directory : psiPackage.getDirectories()) {
            PsiJavaFile packageInfo = findPackageInfo(directory);
            if (packageInfo != null) {
                return packageInfo;
            }
        }
        return null;
    }

    static boolean isJavaPackageInfo(@Nullable PsiFile file) {
        if (!(file instanceof PsiJavaFile) || !PACKAGE_INFO_FILE_NAME.equals(file.getName())) {
            return false;
        }

        PsiDirectory directory = file.getContainingDirectory();
        return directory != null && isJavaPackageDirectory(directory);
    }

    static boolean isJavaPackageDirectory(PsiDirectory directory) {
        VirtualFile virtualFile = directory.getVirtualFile();
        boolean belongsToJavaSources = JavaModuleSourceRootTypes.SOURCES.contains(
                ProjectRootManager.getInstance(directory.getProject())
                        .getFileIndex()
                        .getContainingSourceRootType(virtualFile)
        );

        return belongsToJavaSources
                && JavaDirectoryService.getInstance().getPackage(directory) != null;
    }
}
