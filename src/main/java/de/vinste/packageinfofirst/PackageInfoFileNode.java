package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.NodeSortOrder;
import com.intellij.ide.projectView.NodeSortSettings;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import org.jetbrains.annotations.NotNull;

/**
 * Project View node that places {@code package-info.java} before every standard
 * node category.
 */
final class PackageInfoFileNode extends PsiFileNode {
    private static final String DISPLAY_NAME = "Package Info";

    PackageInfoFileNode(PsiFileNode original, ViewSettings settings) {
        super(original.getProject(), original.getValue(), settings);
    }

    @Override
    protected void updateImpl(@NotNull PresentationData data) {
        super.updateImpl(data);
        if (PackageInfoSettings.getInstance().isDisplayNameEnabled()) {
            data.setPresentableText(DISPLAY_NAME);
        }
    }

    @Override
    public @NotNull NodeSortOrder getSortOrder(@NotNull NodeSortSettings settings) {
        return NodeSortOrder.PROJECT_ROOT; 
    }
}
