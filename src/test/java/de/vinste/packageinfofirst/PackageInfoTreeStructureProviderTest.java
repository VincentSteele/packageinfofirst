package de.vinste.packageinfofirst;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.NodeSortKey;
import com.intellij.ide.projectView.NodeSortOrder;
import com.intellij.ide.projectView.NodeSortSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.List;

public final class PackageInfoTreeStructureProviderTest extends LightJavaCodeInsightFixtureTestCase {
    public void testRecognizesPackageInfoInJavaSourcePackage() {
        PsiFile file = myFixture.addFileToProject(
                "com/example/package-info.java",
                "/** Example package. */\npackage com.example;"
        );

        assertTrue(PackageInfoTreeStructureProvider.isJavaPackageInfo(file));
    }

    public void testRejectsOtherJavaFiles() {
        PsiFile file = myFixture.addFileToProject(
                "com/example/Example.java",
                "package com.example;\nfinal class Example {}"
        );

        assertFalse(PackageInfoTreeStructureProvider.isJavaPackageInfo(file));
    }

    public void testRejectsDetachedPackageInfoFile() {
        PsiFile file = PsiFileFactory.getInstance(getProject()).createFileFromText(
                "package-info.java",
                JavaFileType.INSTANCE,
                "package com.example;"
        );

        assertFalse(PackageInfoTreeStructureProvider.isJavaPackageInfo(file));
    }

    @SuppressWarnings("UnstableApiUsage")
    public void testReplacesPackageInfoWithTopSortedNode() {
        PsiFile regularFile = myFixture.addFileToProject(
                "com/example/Example.java",
                "package com.example;\nfinal class Example {}"
        );
        PsiFile packageInfoFile = myFixture.addFileToProject(
                "com/example/package-info.java",
                "package com.example;"
        );
        PsiFileNode regularNode = new PsiFileNode(getProject(), regularFile, ViewSettings.DEFAULT);
        PsiFileNode packageInfoNode = new PsiFileNode(getProject(), packageInfoFile, ViewSettings.DEFAULT);
        List<AbstractTreeNode<?>> children = List.of(regularNode, packageInfoNode);

        List<AbstractTreeNode<?>> result = List.copyOf(new PackageInfoTreeStructureProvider().modify(
                regularNode,
                children,
                ViewSettings.DEFAULT
        ));
        assertTrue(result.get(1) instanceof PackageInfoFileNode);
        PackageInfoFileNode replacement = (PackageInfoFileNode) result.get(1);
        NodeSortSettings sortSettings = NodeSortSettings.of(false, NodeSortKey.BY_NAME, false);

        assertSame(regularNode, result.get(0));
        assertEquals(packageInfoFile, replacement.getValue());
        assertEquals(NodeSortOrder.PROJECT_ROOT, replacement.getSortOrder(sortSettings));
        replacement.update();
        assertEquals("Package Info", replacement.getPresentation().getPresentableText());
    }
}
