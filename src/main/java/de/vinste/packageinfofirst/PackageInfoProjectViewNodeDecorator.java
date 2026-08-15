package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.nodes.PackageElement;
import com.intellij.openapi.util.IconLoader;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Adds a small marker to Java packages with package-info.java. */
public final class PackageInfoProjectViewNodeDecorator implements ProjectViewNodeDecorator {
    private static final Icon PACKAGE_INFO_ICON = IconLoader.getIcon(
            "/icons/packageInfo.svg",
            PackageInfoProjectViewNodeDecorator.class
    );
    private static final Icon PACKAGE_INFO_ERROR_ICON = IconLoader.getIcon(
            "/icons/packageInfoError.svg",
            PackageInfoProjectViewNodeDecorator.class
    );
    static final Icon BADGE = IconUtil.scale(PACKAGE_INFO_ICON, null, 0.5f);
    static final Icon ERROR_BADGE = IconUtil.scale(PACKAGE_INFO_ERROR_ICON, null, 0.5f);
    private final Predicate<PsiJavaFile> problemFilePredicate;

    public PackageInfoProjectViewNodeDecorator() {
        this(PackageInfoProjectViewNodeDecorator::isProblemFile);
    }

    PackageInfoProjectViewNodeDecorator(@NotNull Predicate<PsiJavaFile> problemFilePredicate) {
        this.problemFilePredicate = problemFilePredicate;
    }

    @Override
    public void decorate(@NotNull ProjectViewNode<?> node, @NotNull PresentationData data) {
        if (!PackageInfoSettings.getInstance().isPackageBadgeVisible()) {
            return;
        }

        List<PsiJavaFile> packageInfoFiles = findPackageInfoFiles(node.getValue());
        if (packageInfoFiles.isEmpty()) {
            return;
        }

        Icon baseIcon = data.getIcon(false);
        if (baseIcon == null) {
            return;
        }

        Icon badge = packageInfoFiles.stream().anyMatch(problemFilePredicate) ? ERROR_BADGE : BADGE;
        LayeredIcon decoratedIcon = new LayeredIcon(2);
        decoratedIcon.setIcon(baseIcon, 0);
        decoratedIcon.setIcon(
                badge,
                1,
                Math.max(0, baseIcon.getIconWidth() - badge.getIconWidth()),
                Math.max(0, baseIcon.getIconHeight() - badge.getIconHeight())
        );
        data.setIcon(decoratedIcon);
    }

    private static @NotNull List<PsiJavaFile> findPackageInfoFiles(Object value) {
        if (value instanceof PsiDirectory directory) {
            PsiJavaFile packageInfo = PackageInfoTreeStructureProvider.findPackageInfo(directory);
            return packageInfo == null ? List.of() : List.of(packageInfo);
        }
        if (value instanceof PsiPackage psiPackage) {
            return findPackageInfoFiles(psiPackage);
        }
        if (value instanceof PackageElement packageElement) {
            return findPackageInfoFiles(packageElement.getPackage());
        }
        return List.of();
    }

    private static @NotNull List<PsiJavaFile> findPackageInfoFiles(@NotNull PsiPackage psiPackage) {
        List<PsiJavaFile> packageInfoFiles = new ArrayList<>();
        for (PsiDirectory directory : psiPackage.getDirectories()) {
            PsiJavaFile packageInfo = PackageInfoTreeStructureProvider.findPackageInfo(directory);
            if (packageInfo != null) {
                packageInfoFiles.add(packageInfo);
            }
        }
        return packageInfoFiles;
    }

    private static boolean isProblemFile(@NotNull PsiJavaFile packageInfoFile) {
        return WolfTheProblemSolver.getInstance(packageInfoFile.getProject())
                .isProblemFile(packageInfoFile.getVirtualFile());
    }
}
