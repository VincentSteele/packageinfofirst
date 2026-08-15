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
    private boolean originalPackageBadgeEnabled;
    private boolean originalDisplayNameEnabled;
    private boolean originalContextActionsEnabled;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        VirtualFile sourceRoot = myFixture.getTempDirFixture().findOrCreateDir("src");
        PsiTestUtil.addSourceRoot(getModule(), sourceRoot);

        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        originalPackageInfoHidden = settings.isPackageInfoHidden();
        originalPackageBadgeEnabled = settings.isPackageBadgeEnabled();
        originalDisplayNameEnabled = settings.isDisplayNameEnabled();
        originalContextActionsEnabled = settings.isContextActionsEnabled();
        settings.setPackageInfoHidden(false);
        settings.setPackageBadgeEnabled(true);
        settings.setDisplayNameEnabled(true);
        settings.setContextActionsEnabled(true);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            PackageInfoSettings settings = PackageInfoSettings.getInstance();
            settings.setPackageInfoHidden(originalPackageInfoHidden);
            settings.setPackageBadgeEnabled(originalPackageBadgeEnabled);
            settings.setDisplayNameEnabled(originalDisplayNameEnabled);
            settings.setContextActionsEnabled(originalContextActionsEnabled);
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

    public void testIgnoresPackageInfoOutsideJavaSourceRoot() {
        PsiFile packageInfoFile = myFixture.addFileToProject(
                "unmarked/package-info.java",
                "package unmarked;"
        );
        PsiDirectory directory = packageInfoFile.getContainingDirectory();
        assertNotNull(directory);
        PsiTestUtil.addExcludedRoot(getModule(), directory.getVirtualFile());

        assertFalse(PackageInfoTreeStructureProvider.isJavaPackageDirectory(directory));
        assertFalse(PackageInfoTreeStructureProvider.isJavaPackageInfo(packageInfoFile));

        PsiFileNode node = new PsiFileNode(getProject(), packageInfoFile, ViewSettings.DEFAULT);
        assertEquals(List.of(node), modify(List.of(node)));
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

    public void testDisplayNameChangeCanBeDisabled() {
        PsiFile packageInfoFile = addJavaFile(
                "com/example/package-info.java",
                "package com.example;"
        );
        PsiFileNode packageInfoNode = new PsiFileNode(getProject(), packageInfoFile, ViewSettings.DEFAULT);
        PackageInfoSettings.getInstance().setDisplayNameEnabled(false);

        PackageInfoFileNode replacement = (PackageInfoFileNode)modify(List.of(packageInfoNode)).get(0);
        replacement.update();

        assertEquals("package-info.java", replacement.getPresentation().getPresentableText());
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

        LayeredIcon documentedIcon = assertInstanceOf(
                documentedPresentation.getIcon(false),
                LayeredIcon.class
        );
        assertSame(PackageInfoProjectViewNodeDecorator.BADGE, documentedIcon.getIcon(1));
        assertSame(undocumentedBaseIcon, undocumentedPresentation.getIcon(false));
    }

    public void testUsesErrorBadgeWhenPackageInfoIsAProblemFile() {
        PsiFile packageInfoFile = addJavaFile(
                "com/documented/package-info.java",
                "package com.documented;"
        );
        PsiDirectory documentedDirectory = packageInfoFile.getContainingDirectory();
        assertNotNull(documentedDirectory);

        PresentationData presentation = new PresentationData();
        presentation.setIcon(testIcon());
        new PackageInfoProjectViewNodeDecorator(file -> file.equals(packageInfoFile)).decorate(
                new PsiDirectoryNode(getProject(), documentedDirectory, ViewSettings.DEFAULT),
                presentation
        );

        LayeredIcon decoratedIcon = assertInstanceOf(
                presentation.getIcon(false),
                LayeredIcon.class
        );
        assertSame(PackageInfoProjectViewNodeDecorator.ERROR_BADGE, decoratedIcon.getIcon(1));
    }

    public void testBadgeCanBeDisabledWhenVisibleAndIsForcedOnWhenHidden() {
        PsiFile packageInfoFile = addJavaFile(
                "com/documented/package-info.java",
                "package com.documented;"
        );
        PsiDirectory documentedDirectory = packageInfoFile.getContainingDirectory();
        assertNotNull(documentedDirectory);

        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        settings.setPackageBadgeEnabled(false);
        PackageInfoProjectViewNodeDecorator decorator = new PackageInfoProjectViewNodeDecorator();

        Icon visibleBaseIcon = testIcon();
        PresentationData visiblePresentation = new PresentationData();
        visiblePresentation.setIcon(visibleBaseIcon);
        decorator.decorate(
                new PsiDirectoryNode(getProject(), documentedDirectory, ViewSettings.DEFAULT),
                visiblePresentation
        );
        assertSame(visibleBaseIcon, visiblePresentation.getIcon(false));

        settings.setPackageInfoHidden(true);
        PresentationData hiddenPresentation = new PresentationData();
        hiddenPresentation.setIcon(testIcon());
        decorator.decorate(
                new PsiDirectoryNode(getProject(), documentedDirectory, ViewSettings.DEFAULT),
                hiddenPresentation
        );
        assertInstanceOf(hiddenPresentation.getIcon(false), LayeredIcon.class);
        assertFalse(settings.isPackageBadgeEnabled());
        assertTrue(settings.isPackageBadgeVisible());
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

    public void testDeleteActionFollowsContextToggleAndIsForcedOnWhenHidden() {
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
        assertTrue(visibleFileEvent.getPresentation().isEnabledAndVisible());

        PackageInfoSettings.getInstance().setContextActionsEnabled(false);
        AnActionEvent disabledEvent = actionEvent(action, documentedDirectory);
        action.update(disabledEvent);
        assertFalse(disabledEvent.getPresentation().isEnabledAndVisible());

        PackageInfoSettings.getInstance().setPackageInfoHidden(true);
        AnActionEvent documentedEvent = actionEvent(action, documentedDirectory);
        action.update(documentedEvent);
        assertTrue(documentedEvent.getPresentation().isEnabledAndVisible());

        AnActionEvent undocumentedEvent = actionEvent(action, undocumentedDirectory);
        action.update(undocumentedEvent);
        assertFalse(undocumentedEvent.getPresentation().isEnabledAndVisible());
    }

    public void testContextActionsCanBeDisabledOnlyWhilePackageInfoIsVisible() {
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

        PackageInfoSettings settings = PackageInfoSettings.getInstance();
        settings.setContextActionsEnabled(false);

        EditPackageInfoAction editAction = new EditPackageInfoAction();
        AnActionEvent disabledEditEvent = actionEvent(editAction, documentedDirectory);
        editAction.update(disabledEditEvent);
        assertFalse(disabledEditEvent.getPresentation().isEnabledAndVisible());

        AddPackageInfoAction addAction = new AddPackageInfoAction();
        AnActionEvent disabledAddEvent = actionEvent(addAction, undocumentedDirectory);
        addAction.update(disabledAddEvent);
        assertFalse(disabledAddEvent.getPresentation().isEnabledAndVisible());

        settings.setPackageInfoHidden(true);
        AnActionEvent forcedEditEvent = actionEvent(editAction, documentedDirectory);
        editAction.update(forcedEditEvent);
        assertTrue(forcedEditEvent.getPresentation().isEnabledAndVisible());

        AnActionEvent forcedAddEvent = actionEvent(addAction, undocumentedDirectory);
        addAction.update(forcedAddEvent);
        assertTrue(forcedAddEvent.getPresentation().isEnabledAndVisible());
    }

    public void testDefaultSettingsShowFileAndIconWithoutContextActions() {
        PackageInfoSettings.SettingsState defaults = new PackageInfoSettings.SettingsState();
        assertFalse(defaults.packageInfoHidden);
        assertTrue(defaults.customIconEnabled);
        assertTrue(defaults.packageBadgeEnabled);
        assertTrue(defaults.displayNameEnabled);
        assertFalse(defaults.contextActionsEnabled);
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
