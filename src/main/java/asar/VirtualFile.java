package asar;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents a file inside an {@link AsarArchive}
 */
public final class VirtualFile {
    private final AsarArchive asar;
    private final String path;
    private final long offset, size;

    VirtualFile(AsarArchive asar, String path, long offset, long size) {
        this.asar = asar;
        this.path = path;
        this.offset = offset;
        this.size = size;
    }

    /**
     * Returns the path inside the asar archive of this file
     *
     * @return The path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the offset inside the sar archive of this file's contents
     *
     * @return The offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Returns the size of this file, in bytes
     *
     * @return The size
     */
    public long getSize() {
        return size;
    }

    /**
     * Reads the contents of this file to the given {@link ByteBuffer}
     *
     * @param buffer The buffer to store the contents
     *
     * @return The given buffer, for chaining calls
     */
    public ByteBuffer read(ByteBuffer buffer) {
        try {
            return buffer.put(asar.contents(offset, size));
        } catch(IOException e) {
            throw new AsarException("Error reading", e);
        }
    }

    /**
     * Returns a {@link byte[]} with the contents of this file
     *
     * @return The contents
     *
     * @see #read(ByteBuffer)
     */
    public byte[] read() {
        if(size > Integer.MAX_VALUE) {
            throw new IllegalStateException("Cannot read to a byte array a file that's longer than 2GB");
        }
        return read(ByteBuffer.allocate((int)size)).array();
    }
}
