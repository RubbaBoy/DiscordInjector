package asar;

import com.uddernetworks.discordinjector.main.ReplacementProfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Extracts files from an {@link AsarArchive}
 */
public final class AsarExtractor {
    private static String injectLoader;
    private static Map<ReplacementProfile, String> inject;

    private AsarExtractor() {
    }

    /**
     * Extracts a single file from an {@link AsarArchive}
     *
     * @see AsarExtractor#extract(AsarArchive, String, File)
     */
    public static void extract(AsarArchive asar, String filePath, String destination) throws IOException {
        extract(asar, filePath, new File(destination));
    }

    /**
     * Extracts a single file from an {@link AsarArchive}
     *
     * @param asar        The source
     * @param filePath    The path inside the asar file of the wanted file
     * @param destination The {@link File} to save the extracted file
     * @throws IOException              If there's an error writing the file
     * @throws IllegalArgumentException If the path to extract doesn't exist
     */
    public static void extract(AsarArchive asar, String filePath, File destination) throws IOException {
        if (asar == null || filePath == null || destination == null) throw new NullPointerException();
        VirtualFile vf = null;
        for (VirtualFile v : asar) {
            if (v.getPath().equals(filePath)) {
                vf = v;
                break;
            }
        }
        if (vf == null) throw new IllegalArgumentException("No file " + filePath + " in the asar archive");
        extract(vf, destination);
    }

    /**
     * Extracts all the contents of an {@link AsarArchive} to a given folder
     *
     * @see AsarExtractor#extractAll(AsarArchive, String)
     */
    public static void extractAll(AsarArchive asar, String destination) throws IOException {
        extractAll(asar, new File(destination), null, null);
    }

    /**
     * Extracts all the contents of an {@link AsarArchive} to a given folder
     *
     * @param asar        The source asar
     * @param destination The destination folder
     * @throws IOException              If there's an error writing the files
     * @throws IllegalArgumentException If the destination is invalid
     */

    public static void extractAll(AsarArchive asar, File destination, String injectLoader,  Map<ReplacementProfile, String> inject) throws IOException {
        AsarExtractor.injectLoader = injectLoader;
        AsarExtractor.inject = inject;
        if (asar == null || destination == null) throw new NullPointerException();
        if (destination.exists() && !destination.isDirectory())
            throw new IllegalArgumentException("destination must be a directory or not exist");
        for (VirtualFile f : asar) {
            File d = new File(destination, f.getPath());
            d.getParentFile().mkdirs();

            extract(f, d);
        }
    }

    private static boolean written = false;
    private static boolean writtenIndex = false;

    private static final String replacing = "mainWindow.webContents.on('will-navigate', function (evt, url) {\n" +
            "    if (!insideAuthFlow && !url.startsWith(WEBAPP_ENDPOINT)) {\n" +
            "      evt.preventDefault();\n" +
            "    }\n" +
            "  });";

    private static void extract(VirtualFile vf, File to) throws IOException {
        written = false;

        to.getParentFile().mkdirs();

        try (RandomAccessFile raf = new RandomAccessFile(to, "rw"); FileChannel fc = raf.getChannel()) {

            inject.forEach(((replacementProfile, s) -> {
                if (written) return;
                try {
                    if (!replacementProfile.isDirectFile()) {
                        if (to.getAbsolutePath().endsWith(replacementProfile.getRemotePath())) {
                            String file = new String(vf.read());
                            String injectingFileContents = new String(Files.readAllBytes(replacementProfile.getLocalFile()));

                            if (to.getAbsolutePath().endsWith("mainScreen.js")) {
                                writtenIndex = true;
                                file = file.replace(replacing, replacing + "\n\n" + injectLoader);
                            }

                            file = file.replace(replacementProfile.getReplacingCode(), injectingFileContents);

                            Files.write(to.toPath(), file.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

                            written = true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            if (!writtenIndex && to.getAbsolutePath().endsWith("mainScreen.js")) {
                String file = new String(vf.read());

                file = file.replace(replacing, replacing + "\n\n" + injectLoader);

                Files.write(to.toPath(), file.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

                written = true;
            }

            if (written) return;

            vf.read(fc.map(FileChannel.MapMode.READ_WRITE, 0, vf.getSize()));
        }
    }
}

