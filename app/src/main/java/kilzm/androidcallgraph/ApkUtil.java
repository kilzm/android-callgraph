package kilzm.androidcallgraph;

import org.checkerframework.checker.units.qual.C;
import proguard.classfile.ClassPool;
import proguard.classfile.visitor.ClassPoolFiller;
import proguard.io.*;
import proguard.util.ExtensionMatcher;

import java.io.File;
import java.io.IOException;

public class ApkUtil {
    public static ClassPool generateClassPool(String apkFilePath) throws IOException {
        ClassPool classPool = new ClassPool();
        ClassPoolFiller classPoolFiller = new ClassPoolFiller(classPool);

        // Create a FileSource for the apk
        DataEntrySource source =
                new FileSource(
                        new File(apkFilePath));

        // Read all dex files starting with "classes"
        DataEntryReader jarReader =
                new JarReader(
                        new NameFilteredDataEntryReader("classes*.dex",
                                new DexClassReader(true, classPoolFiller)));

        DataEntryReader apkReader =
                new FilteredDataEntryReader(
                        new DataEntryNameFilter(
                                new ExtensionMatcher("apk")), jarReader);

        source.pumpDataEntries(apkReader);

        return classPool;
    }
}
