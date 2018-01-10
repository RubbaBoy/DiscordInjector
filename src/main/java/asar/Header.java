package asar;

import org.json.JSONObject;

/**
 * Represents the header of an asar file
 */
public class Header {
    private final int size;
    private final JSONObject json;

    Header(int size, JSONObject json) {
        this.size = size;
        this.json = json;
    }

    /**
     * Returns the size of the header, in bytes
     *
     * @return The size
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the {@link JSONObject} of this header
     *
     * @return The {@link JSONObject}
     */
    public JSONObject getJson() {
        return new JSONObject(json.toString());
    }
}