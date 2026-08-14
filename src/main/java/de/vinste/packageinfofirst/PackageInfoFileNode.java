package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.NodeSortOrder;
import com.intellij.ide.projectView.NodeSortSettings;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import org.jetbrains.annotations.NotNull;

/**
 * A regular file node with a sort position immediately after package folders.
 */
final class PackageInfoFileNode extends PsiFileNode {
    private static final int AFTER_PACKAGE_FOLDERS_WEIGHT = 4;

    PackageInfoFileNode(PsiFileNode original, ViewSettings settings) {
        super(original.getProject(), original.getValue(), settings);
        setParent(original.getParent());
    }

    @Override
    public @NotNull NodeSortOrder getSortOrder(@NotNull NodeSortSettings settings) {
        return settings.isFoldersAlwaysOnTop() ? NodeSortOrder.FOLDER : super.getSortOrder(settings);
    }

    @Override
    public int getTypeSortWeight(boolean sortByType) {
        // IntelliJ package folders use weight 3. A slightly larger non-zero
        // weight keeps this node below them while it remains above file nodes.
        return AFTER_PACKAGE_FOLDERS_WEIGHT;
    }
}

