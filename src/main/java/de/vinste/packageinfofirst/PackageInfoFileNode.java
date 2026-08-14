package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.NodeSortOrder;
import com.intellij.ide.projectView.NodeSortSettings;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
 * Project View node that places {@code package-info.java} before every standard
 * node category.
 */
final class PackageInfoFileNode extends PsiFileNode {
    private static final String DISPLAY_NAME = "Package Info";
    private static final Icon ICON = IconLoader.getIcon("/icons/packageInfo.svg", PackageInfoFileNode.class);

    PackageInfoFileNode(PsiFileNode original, ViewSettings settings) {
        super(original.getProject(), original.getValue(), settings);
    }

    @Override
    protected void updateImpl(@NotNull PresentationData data) {
        super.updateImpl(data);
        data.setPresentableText(DISPLAY_NAME);
        data.setIcon(ICON);
        data.setForcedTextForeground(UIUtil.getContextHelpForeground());
    }

    @Override
    public @NotNull NodeSortOrder getSortOrder(@NotNull NodeSortSettings settings) {
        // human comment here: looks hacky as all hell
        return NodeSortOrder.PROJECT_ROOT; 
    }
}
