package com.uddernetworks.discordinjector.main;

import java.io.File;
import java.nio.file.Path;

public class ReplacementProfile {

    // If it is a raw file being executed at the beginning
    private boolean isDirectFile;

    // The file in which to apply to
    private String remotePath;

    // The path of where the file will be
    private String localPath;

    // The base directory of the local file
    private String base;

    // The code that will be removed
    private String replacingCode;

    // Is the file executing client node.js script
    private boolean isNode;

    public ReplacementProfile(boolean isNode, boolean isDirectFile, String remotePath, String localPath, String base, String replacingCode) {
        this.isNode = isNode;
        this.isDirectFile = isDirectFile;
        this.remotePath = remotePath;
        this.base = base;
        this.localPath = localPath;
        this.replacingCode = replacingCode;
    }

    public boolean isDirectFile() {
        return isDirectFile;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public String getReplacingCode() {
        return replacingCode;
    }

    public boolean isNode() {
        return isNode;
    }

    public Path getLocalFile() {
        return new File(base, localPath).toPath();
    }
}
