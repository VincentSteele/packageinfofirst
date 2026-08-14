package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.nodes.PackageElement;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/** Adds a small marker to Java packages with package-info.java. */
public final class PackageInfoProjectViewNodeDecorator implements ProjectViewNodeDecorator {
    private static final Icon PACKAGE_INFO_ICON = IconLoader.getIcon(
            "/icons/packageInfo.svg",
            PackageInfoProjectViewNodeDecorator.class
    );
    private static final Icon BADGE = IconUtil.scale(PACKAGE_INFO_ICON, null, 0.5f);

    @Override
    public void decorate(@NotNull ProjectViewNode<?> node, @NotNull PresentationData data) {
        if (!hasPackageInfo(node.getValue())) {
            return;
        }

        Icon baseIcon = data.getIcon(false);
        if (baseIcon == null) {
            return;
        }

        LayeredIcon decoratedIcon = new LayeredIcon(2);
        decoratedIcon.setIcon(baseIcon, 0);
        decoratedIcon.setIcon(
                BADGE,
                1,
                Math.max(0, baseIcon.getIconWidth() - BADGE.getIconWidth()),
                Math.max(0, baseIcon.getIconHeight() - BADGE.getIconHeight())
        );
        data.setIcon(decoratedIcon);
    }

    private static boolean hasPackageInfo(Object value) {
        if (value instanceof PsiDirectory directory) {
            return PackageInfoTreeStructureProvider.findPackageInfo(directory) != null;
        }
        if (value instanceof PsiPackage psiPackage) {
            return PackageInfoTreeStructureProvider.findPackageInfo(psiPackage) != null;
        }
        if (value instanceof PackageElement packageElement) {
            return PackageInfoTreeStructureProvider.findPackageInfo(packageElement.getPackage()) != null;
        }
        return false;
    }
}
