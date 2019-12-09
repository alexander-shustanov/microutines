package joroutine;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.TaskInputsInternal;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

public class ProcessSuspendableTask extends DefaultTask {
    public static final String SUSPENDABLE = "joroutine.core.Suspendable";
    public static final String SUSPENDABLE_WITH_RESULT = "joroutine.core.SuspendableWithResult";

    @InputFiles
    FileCollection classPath;

    @TaskAction
    public void run() throws IOException {
        TaskInputsInternal inputs = getInputs();
        ArrayList<File> files = new ArrayList<>(inputs.getFiles().getFiles());


        URL[] urls = Stream.concat(
                classPath != null ? classPath.getFiles().stream() : Stream.empty(),
                files.stream()
        ).map(this::fileToUrl).toArray(URL[]::new);

        URLClassLoader classLoader = new URLClassLoader(urls);

        for (File file : files) {

            URL url = file.toURI().toURL();
            Path classPath = file.toPath();

            Files.walk(file.toPath())
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> processFile(classLoader, classPath, path));
        }
    }

    private URL fileToUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void processFile(URLClassLoader classLoader, Path classPath, Path path) {
        try {
            doProcess(classLoader, classPath, path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doProcess(URLClassLoader classLoader, Path classPath, Path path) throws Exception {
        String className = getClassName(classPath, path);
        Class<?> currentClass = classLoader.loadClass(className);

        Class<?> suspendable = classLoader.loadClass(SUSPENDABLE);
        Class<?> suspendableWithResult = classLoader.loadClass(SUSPENDABLE_WITH_RESULT);

        boolean isSuspendable = suspendable.isAssignableFrom(currentClass) && !Modifier.isAbstract(currentClass.getModifiers());
        boolean isSuspendableWithResult = suspendableWithResult.isAssignableFrom(currentClass) && !Modifier.isAbstract(currentClass.getModifiers());

        if (!isSuspendable && !isSuspendableWithResult) {
            return;
        }

        ClassReader classReader = new ClassReader(Files.readAllBytes(path));
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            @Override
            protected ClassLoader getClassLoader() {
                return classLoader;
            }
        };

        new SuspendableConverter(classLoader, classWriter, classReader, currentClass)
                .process();


        byte[] bytes = classWriter.toByteArray();

        Files.write(path, bytes);
    }

    private String getClassName(Path classPath, Path path) {
        String relative = classPath.relativize(path).toString().replace('/', '.');
        return relative.substring(0, relative.length() - 6);
    }


}
