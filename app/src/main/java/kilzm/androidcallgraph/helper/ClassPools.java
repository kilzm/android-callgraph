package kilzm.androidcallgraph.helper;

import org.checkerframework.checker.units.qual.C;
import proguard.classfile.ClassPool;
import proguard.classfile.util.ClassReferenceInitializer;
import proguard.classfile.util.ClassSubHierarchyInitializer;
import proguard.classfile.util.ClassSuperHierarchyInitializer;
import proguard.classfile.visitor.ClassPoolFiller;
import proguard.io.*;
import proguard.util.ExtensionMatcher;

import java.io.File;
import java.io.IOException;

public class ClassPools {
    public ClassPool programClassPool;
    public ClassPool libraryClassPool;

    ClassPoolFiller programClassPoolFiller;
    ClassPoolFiller libraryClassPoolFiller;


    public ClassPools() {
        this.programClassPool = new ClassPool();
        this.libraryClassPool = new ClassPool();
        this.programClassPoolFiller = new ClassPoolFiller(programClassPool);
        this.libraryClassPoolFiller = new ClassPoolFiller(libraryClassPool);
    }


    public void addFromJdk(String jdkDirPath) throws IOException {
        DataEntrySource jmodBaseFile =
                new FileSource(
                new File(jdkDirPath + "/jmods/java.base.jmod"));


        DataEntryReader jmodReader =
                new JarReader(true,
                new ClassFilter(
                new ClassReader(
                        true,
                        false,
                        false,
                        false,
                        null,
                        libraryClassPoolFiller)));

        jmodBaseFile.pumpDataEntries(jmodReader);
    }


    public void addFromSdk(String sdkFilePath) throws IOException {
        DataEntrySource sdkJarFile =
                new FileSource(
                new File(sdkFilePath));

        DataEntryReader jarReader =
                new JarReader(
                new ClassFilter(
                new ClassReader(
                        true,
                        false,
                        false,
                        false,
                        null,
                        libraryClassPoolFiller)));

        sdkJarFile.pumpDataEntries(jarReader);
    }


    public void addFromApk(String apkFilePath) throws IOException {
        DataEntrySource apkFile =
                new FileSource(
                new File(apkFilePath));


        DataEntryReader dexReader =
                new JarReader(
                new NameFilteredDataEntryReader("classes*.dex",
                new DexClassReader(true, programClassPoolFiller)));

        DataEntryReader apkReader =
                new FilteredDataEntryReader(
                new DataEntryNameFilter(
                new ExtensionMatcher("apk")), dexReader);

        apkFile.pumpDataEntries(apkReader);
    }


    public void initialize() {
        ClassReferenceInitializer classReferenceInitializer = new ClassReferenceInitializer(programClassPool, libraryClassPool);
        ClassSuperHierarchyInitializer classSuperHierarchyInitializer = new ClassSuperHierarchyInitializer(programClassPool, libraryClassPool);
        ClassSubHierarchyInitializer classSubHierarchyInitializer = new ClassSubHierarchyInitializer();


        programClassPool.classesAccept(classReferenceInitializer);
        programClassPool.classesAccept(classSuperHierarchyInitializer);
        programClassPool.accept(classSubHierarchyInitializer);

        libraryClassPool.classesAccept(classReferenceInitializer);
        libraryClassPool.classesAccept(classSuperHierarchyInitializer);
        libraryClassPool.accept(classSubHierarchyInitializer);
    }
}