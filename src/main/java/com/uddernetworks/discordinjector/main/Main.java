package com.uddernetworks.discordinjector.main;

import asar.AsarArchive;
import asar.AsarExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    private static Map<ReplacementProfile, String> inject = new HashMap<>();
    private static List<String> cssInject = new ArrayList<>();

    private static final boolean askForReplace = false;

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        File base = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath(), "inject");

        System.out.println("Inject files location = " + base.getAbsolutePath());

        cssInject.add("style.css");

        // Loads the local file (Copied to discord's directory) "cssInject.js" into Discord. The path to put this file is "app\cssInject.js" //// This implements the live CSS
        inject.put(new ReplacementProfile(false, true, null,"cssInject.js", base.getAbsolutePath(), null), "app\\cssInject.js");

        // Loads the local file (Copied to discord's directory) "cssInject.js" into Discord. The path to put this file is "app\cssInject.js" //// This implements the live CSS
        inject.put(new ReplacementProfile(false, true, null,"cssInjectDynamic.js", base.getAbsolutePath(), null), "app\\cssInjectDynamic.js");

        // Loads the local file (Copied to discord's directory) "inject.js" into Discord. The path to put this file is "app\inject.js"
        inject.put(new ReplacementProfile(false, true, null,"inject.js", base.getAbsolutePath(), null), "app\\inject.js");



        // This injects the contents of local NodeJS file "node_inject.js" to load when Discord loads
        inject.put(new ReplacementProfile(true, true, null, "node_inject.js", base.getAbsolutePath(), null), "");

        // Replaces the code "// Code here" with the contents of local NodeJS file "node_inject.js"
        // inject.put(new ReplacementProfile(true, true, "index.js", "node_inject.js", base.getAbsolutePath(), "// Code here"), "");

        Scanner scanner = new Scanner(System.in);

        System.out.println("Killing Discord.exe...");

        final Process p = Runtime.getRuntime().exec("taskkill /F /IM Discord.exe");

        new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            try {
                while ((line = input.readLine()) != null)
                    System.out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        p.waitFor();

        System.out.println("Complete.");

        File discordMain = new File(System.getProperty("user.home"), "AppData\\Roaming\\discord");

        System.out.println("discordMain = " + discordMain.getAbsolutePath());

        List<File> appFiles = new ArrayList<>();

        Arrays.stream(discordMain.listFiles()).filter(file -> file.isDirectory() && file.getName().contains(".")).forEach(appFiles::add);

        File newest = appFiles.get(0);

        String version = newest.getName();

        for (File file : appFiles) {
            if (file.lastModified() > newest.lastModified()) newest = file;
        }

        System.out.println("Newest: " + newest.getAbsolutePath());

        File asarFile = new File(newest, "modules\\discord_desktop_core\\core.asar");
        File newAsarFile = new File(newest, "modules\\discord_desktop_core\\core-original.asar");
        File extractedAsarFile = new File(newest, "modules\\discord_desktop_core\\core");

        if (!asarFile.exists()) {
            if (newAsarFile.exists()) {
                asarFile = newAsarFile;
            } else {
                System.out.println("Asar file at path " + asarFile.getAbsolutePath() + " does not exist!");
                System.exit(0);
            }
        }

        Files.move(asarFile.toPath(), newAsarFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        if (extractedAsarFile.exists()) {

            if (askForReplace) {
                System.out.println("Extracted asar already exists! Do you want to delete the old data? (y/n)");
                String reply = scanner.nextLine();

                if (reply.equalsIgnoreCase("y")) {

                } else if (reply.equalsIgnoreCase("n")) {
                    System.exit(0);
                    return;
                } else {
                    System.out.println("Unknown answer, exiting!");
                    System.exit(0);
                    return;
                }
            } else {
                System.out.println("Extracted asar already exists! Deleting old data.");
            }

            System.out.println("Deleting old data...");

            recursiveDelete(extractedAsarFile);

            System.out.println("Complete.");
        } else {
            extractedAsarFile.mkdirs();
        }

        StringBuilder injectSub = new StringBuilder();
        StringBuilder nodeInject = new StringBuilder();

        inject.forEach(((replacementProfile, s) -> {
            try {
                if (replacementProfile.isDirectFile()) {
                    if (replacementProfile.isNode()) {

                        String code = new String(Files.readAllBytes(replacementProfile.getLocalFile()));

                        nodeInject.append("\n/* ----------===========[ From file: ").append(replacementProfile.getLocalFile().toAbsolutePath()).append(" ]===========---------- */\n").append(code).append("\n/* ----------===========[ Ended file's code ]===========---------- */\n");
                    } else {
                        injectSub.append("        mainWindow.webContents.executeJavaScript(_fs2.default.readFileSync('").append(extractedAsarFile.getAbsolutePath().replace("\\", "\\\\")).append("\\\\").append(s.replace("\\", "\\\\")).append("', 'utf-8'));\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        StringBuilder cssInjectString = new StringBuilder();

        File cssBase = new File(newest, "modules\\discord_desktop_core\\core\\app");

        for (String cssFile : cssInject) {
            cssInjectString.append("window.applyAndWatchCSS('").append(new File(cssBase.getAbsolutePath() + "\\" + cssFile).getAbsolutePath().replace("\\", "\\\\")).append("');\n");
        }

        File cssInjectDynamic = new File(base, "cssInjectDynamic.js");

        Files.write(cssInjectDynamic.toPath(), cssInjectString.toString().getBytes());

        StringBuilder injectLoader = new StringBuilder(nodeInject).append("\n\nvar _fs = require('fs');\n" +
                "\n" +
                "var _fs2 = _interopRequireDefault(_fs);\n\n" +
                "mainWindow.webContents.on('dom-ready', function () {\n");

        injectLoader.append(injectSub);

        injectLoader.append("});");

        System.out.println("Starting extraction...");

        AsarArchive asar = new AsarArchive(newAsarFile);

        AsarExtractor.extractAll(asar, extractedAsarFile, injectLoader.toString(), inject);

        System.out.println("Complete.");

        System.out.println("Copying inject files...");

        inject.forEach((replacementProfile, s) -> {
            if (replacementProfile.isDirectFile() && !replacementProfile.isNode()) {
                File destInject = new File(extractedAsarFile, s);

                try {
                    Files.copy(replacementProfile.getLocalFile(), destInject.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        cssInject.forEach(cssFile -> {
            try {
                Files.copy(new File(base, cssFile).toPath(), new File(cssBase.getAbsolutePath() + "\\" + cssFile).toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Complete.");

        System.out.println("Modifying other miscellaneous files...");

        Path indexJsPath = new File(newest, "modules\\discord_desktop_core\\index.js").toPath();

        String indexJsContents = new String(Files.readAllBytes(indexJsPath));

        Files.write(indexJsPath, indexJsContents.replace(".asar", "").getBytes());

        System.out.println("Complete.");

        System.out.println("Launching Discord...");

        File discordExe = new File(System.getProperty("user.home"), "AppData\\local\\Discord\\app-" + version + "\\Discord.exe");

        final Process p2 = Runtime.getRuntime().exec(discordExe.getAbsolutePath());

        new Thread(() -> {
            while (scanner.hasNext()) {
                if (scanner.nextLine().equalsIgnoreCase("stop")) {
                    System.out.println("Stopping...");
                    p2.destroyForcibly();

                    System.exit(-1);
                    Runtime.getRuntime().exit(-1);
                }
            }
        }).start();

        new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(p2.getInputStream()));
            String line;
            try {
                while ((line = input.readLine()) != null)
                    System.out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        p2.waitFor();
    }

    private static void recursiveDelete(File dir) {
        if (dir == null || dir.listFiles() == null) return;

        Arrays.stream(dir.listFiles()).forEach(file -> {
            if (file.isDirectory()) recursiveDelete(file);
            if (!file.delete()) System.out.println("Couldn't delete " + file.getAbsolutePath());
        });
    }

}
