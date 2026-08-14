package de.vinste.packageinfofirst;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.ide.projectView.NodeSortKey;
import com.intellij.ide.projectView.NodeSortOrder;
import com.intellij.ide.projectView.NodeSortSettings;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.ui.LayeredIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaResourceRootType;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.util.List;

public final class PackageInfoTreeStructureProviderTest extends LightJavaCodeInsightFixtureTestCase {
    private boolean originalPackageInfoHidden;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        VirtualFile sourceRoot = myFixture.getTempDirFixture().findOrCreateDir("src");
        PsiTestUtil.addSourceRoot(getModule(), sourceRoot);

        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        originalPackageInfoHidden = settings.isPackageInfoHidden();
        settings.setPackageInfoHidden(false);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            PackageInfoSettings.getInstance().setPackageInfoHidden(originalPackageInfoHidden);
        } finally {
            super.tearDown();
        }
    }

    public void testRecognizesAndFindsPackageInfoInJavaSourcePackage() {
        PsiFile file = addJavaFile(
                "com/example/package-info.java",
                "/** Example package. */\npackage com.example;"
        );
        PsiDirectory directory = file.getContainingDirectory();
        assertNotNull(directory);

        assertTrue(PackageInfoTreeStructureProvider.isJavaPackageInfo(file));
        assertEquals(file, PackageInfoTreeStructureProvider.findPackageInfo(directory));

        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(directory);
        assertNotNull(psiPackage);
        assertEquals(file, PackageInfoTreeStructureProvider.findPackageInfo(psiPackage));
    }

    public void testRejectsOtherJavaFilesAndDetachedPackageInfo() {
        PsiFile regularFile = addJavaFile(
                "com/example/Example.java",
                "package com.example;\nfinal class Example {}"
        );
        PsiFile detachedPackageInfo = PsiFileFactory.getInstance(getProject()).createFileFromText(
                "package-info.java",
                JavaFileType.INSTANCE,
                "package com.example;"
        );

        assertFalse(PackageInfoTreeStructureProvider.isJavaPackageInfo(regularFile));
        assertFalse(PackageInfoTreeStructureProvider.isJavaPackageInfo(detachedPackageInfo));
    }

    @SuppressWarnings("UnstableApiUsage")
    public void testShowsTopSortedPackageInfoNodeByDefault() {
        PsiFile regularFile = addJavaFile(
                "com/example/Example.java",
                "package com.example;\nfinal class Example {}"
        );
        PsiFile packageInfoFile = addJavaFile(
                "com/example/package-info.java",
                "package com.example;"
        );
        PsiFileNode regularNode = new PsiFileNode(getProject(), regularFile, ViewSettings.DEFAULT);
        PsiFileNode packageInfoNode = new PsiFileNode(getProject(), packageInfoFile, ViewSettings.DEFAULT);

        List<AbstractTreeNode<?>> result = modify(List.of(regularNode, packageInfoNode));

        assertEquals(2, result.size());
        assertSame(regularNode, result.get(0));
        assertInstanceOf(result.get(1), PackageInfoFileNode.class);
        PackageInfoFileNode replacement = (PackageInfoFileNode)result.get(1);
        NodeSortSettings sortSettings = NodeSortSettings.of(false, NodeSortKey.BY_NAME, false);
        assertEquals(NodeSortOrder.PROJECT_ROOT, replacement.getSortOrder(sortSettings));
        replacement.update();
        assertEquals("Package Info", replacement.getPresentation().getPresentableText());
    }

    public void testHidesPackageInfoNodeWhenOptionIsEnabled() {
        PsiFile regularFile = addJavaFile(
                "com/example/Example.java",
                "package com.example;\nfinal class Example {}"
        );
        PsiFile packageInfoFile = addJavaFile(
                "com/example/package-info.java",
                "package com.example;"
        );
        PsiFileNode regularNode = new PsiFileNode(getProject(), regularFile, ViewSettings.DEFAULT);
        PsiFileNode packageInfoNode = new PsiFileNode(getProject(), packageInfoFile, ViewSettings.DEFAULT);
        PackageInfoSettings.getInstance().setPackageInfoHidden(true);

        List<AbstractTreeNode<?>> result = modify(List.of(regularNode, packageInfoNode));

        assertEquals(List.of(regularNode), result);
    }

    public void testAddsBadgeOnlyToPackagesWithPackageInfo() {
        PsiFile packageInfoFile = addJavaFile(
                "com/documented/package-info.java",
                "package com.documented;"
        );
        PsiFile regularFile = addJavaFile(
                "com/undocumented/Example.java",
                "package com.undocumented;\nfinal class Example {}"
        );
        PsiDirectory documentedDirectory = packageInfoFile.getContainingDirectory();
        PsiDirectory undocumentedDirectory = regularFile.getContainingDirectory();
        assertNotNull(documentedDirectory);
        assertNotNull(undocumentedDirectory);

        PackageInfoProjectViewNodeDecorator decorator = new PackageInfoProjectViewNodeDecorator();
        Icon documentedBaseIcon = testIcon();
        PresentationData documentedPresentation = new PresentationData();
        documentedPresentation.setIcon(documentedBaseIcon);
        decorator.decorate(
                new PsiDirectoryNode(getProject(), documentedDirectory, ViewSettings.DEFAULT),
                documentedPresentation
        );

        Icon undocumentedBaseIcon = testIcon();
        PresentationData undocumentedPresentation = new PresentationData();
        undocumentedPresentation.setIcon(undocumentedBaseIcon);
        decorator.decorate(
                new PsiDirectoryNode(getProject(), undocumentedDirectory, ViewSettings.DEFAULT),
                undocumentedPresentation
        );

        assertInstanceOf(documentedPresentation.getIcon(false), LayeredIcon.class);
        assertSame(undocumentedBaseIcon, undocumentedPresentation.getIcon(false));
    }

    public void testDoesNotBadgeResourceDirectoryContainingPackageInfoFile() throws Exception {
        VirtualFile resourceRoot = myFixture.getTempDirFixture().findOrCreateDir("resources");
        PsiTestUtil.addSourceRoot(getModule(), resourceRoot, JavaResourceRootType.RESOURCE);
        PsiFile resourcePackageInfo = myFixture.addFileToProject(
                "resources/package-info.java",
                "package resources;"
        );
        PsiDirectory resourceDirectory = resourcePackageInfo.getContainingDirectory();
        assertNotNull(resourceDirectory);
        assertFalse(PackageInfoTreeStructureProvider.isJavaPackageInfo(resourcePackageInfo));

        Icon baseIcon = testIcon();
        PresentationData presentation = new PresentationData();
        presentation.setIcon(baseIcon);
        new PackageInfoProjectViewNodeDecorator().decorate(
                new PsiDirectoryNode(getProject(), resourceDirectory, ViewSettings.DEFAULT),
                presentation
        );

        assertSame(baseIcon, presentation.getIcon(false));
    }

    public void testEditActionIsVisibleOnlyForDocumentedPackages() {
        PsiFile packageInfoFile = addJavaFile(
                "com/documented/package-info.java",
                "package com.documented;"
        );
        PsiFile regularFile = addJavaFile(
                "com/undocumented/Example.java",
                "package com.undocumented;\nfinal class Example {}"
        );
        PsiDirectory documentedDirectory = packageInfoFile.getContainingDirectory();
        PsiDirectory undocumentedDirectory = regularFile.getContainingDirectory();
        assertNotNull(documentedDirectory);
        assertNotNull(undocumentedDirectory);

        EditPackageInfoAction action = new EditPackageInfoAction();
        AnActionEvent documentedEvent = actionEvent(action, documentedDirectory);
        action.update(documentedEvent);
        assertTrue(documentedEvent.getPresentation().isEnabledAndVisible());

        AnActionEvent undocumentedEvent = actionEvent(action, undocumentedDirectory);
        action.update(undocumentedEvent);
        assertFalse(undocumentedEvent.getPresentation().isEnabledAndVisible());
    }

    public void testEditActionUsesPreviewTabDescriptor() {
        PsiFile packageInfoFile = addJavaFile(
                "com/example/package-info.java",
                "package com.example;"
        );

        OpenFileDescriptor descriptor = EditPackageInfoAction.previewDescriptor(
                getProject(),
                packageInfoFile.getVirtualFile()
        );

        assertTrue(descriptor.isUsePreviewTab());
        assertEquals(packageInfoFile.getVirtualFile(), descriptor.getFile());
    }

    public void testDeleteActionIsVisibleOnlyForHiddenPackageInfo() {
        PsiFile packageInfoFile = addJavaFile(
                "com/documented/package-info.java",
                "package com.documented;"
        );
        PsiFile regularFile = addJavaFile(
                "com/undocumented/Example.java",
                "package com.undocumented;\nfinal class Example {}"
        );
        PsiDirectory documentedDirectory = packageInfoFile.getContainingDirectory();
        PsiDirectory undocumentedDirectory = regularFile.getContainingDirectory();
        assertNotNull(documentedDirectory);
        assertNotNull(undocumentedDirectory);

        DeletePackageInfoAction action = new DeletePackageInfoAction();
        AnActionEvent visibleFileEvent = actionEvent(action, documentedDirectory);
        action.update(visibleFileEvent);
        assertFalse(visibleFileEvent.getPresentation().isEnabledAndVisible());

        PackageInfoSettings.getInstance().setPackageInfoHidden(true);
        AnActionEvent documentedEvent = actionEvent(action, documentedDirectory);
        action.update(documentedEvent);
        assertTrue(documentedEvent.getPresentation().isEnabledAndVisible());

        AnActionEvent undocumentedEvent = actionEvent(action, undocumentedDirectory);
        action.update(undocumentedEvent);
        assertFalse(undocumentedEvent.getPresentation().isEnabledAndVisible());
    }

    public void testHideOptionDefaultsToOff() {
        assertFalse(new PackageInfoSettings.SettingsState().packageInfoHidden);
    }

    private List<AbstractTreeNode<?>> modify(List<AbstractTreeNode<?>> children) {
        return List.copyOf(new PackageInfoTreeStructureProvider().modify(
                children.get(0),
                children,
                ViewSettings.DEFAULT
        ));
    }

    private PsiJavaFile addJavaFile(String relativePath, String text) {
        return (PsiJavaFile)myFixture.addFileToProject("src/" + relativePath, text);
    }

    public void testAddActionIsVisibleOnlyForPackagesWithoutPackageInfo() {
        PsiFile packageInfoFile = addJavaFile(
                "com/documented/package-info.java",
                "package com.documented;"
        );
        PsiFile regularFile = addJavaFile(
                "com/undocumented/Example.java",
                "package com.undocumented;\nfinal class Example {}"
        );
        PsiDirectory documentedDirectory = packageInfoFile.getContainingDirectory();
        PsiDirectory undocumentedDirectory = regularFile.getContainingDirectory();
        assertNotNull(documentedDirectory);
        assertNotNull(undocumentedDirectory);

        AddPackageInfoAction action = new AddPackageInfoAction();
        AnActionEvent documentedEvent = actionEvent(action, documentedDirectory);
        action.update(documentedEvent);
        assertFalse(documentedEvent.getPresentation().isEnabledAndVisible());

        AnActionEvent undocumentedEvent = actionEvent(action, undocumentedDirectory);
        action.update(undocumentedEvent);
        assertTrue(undocumentedEvent.getPresentation().isEnabledAndVisible());
        assertEquals("Add Package Info", undocumentedEvent.getPresentation().getText());
    }

    private AnActionEvent actionEvent(com.intellij.openapi.actionSystem.AnAction action, PsiDirectory directory) {
        IdeView view = new IdeView() {
            @Override
            public @NotNull PsiDirectory[] getDirectories() {
                return new PsiDirectory[]{directory};
            }

            @Override
            public PsiDirectory getOrChooseDirectory() {
                return directory;
            }
        };
        DataContext context = SimpleDataContext.builder()
                .add(CommonDataKeys.PROJECT, getProject())
                .add(CommonDataKeys.PSI_ELEMENT, directory)
                .add(LangDataKeys.IDE_VIEW, view)
                .build();
        return TestActionEvent.createTestEvent(action, context);
    }

    private static Icon testIcon() {
        return new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
    }
}
