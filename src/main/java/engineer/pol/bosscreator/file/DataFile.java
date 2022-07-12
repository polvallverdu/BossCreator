package engineer.pol.bosscreator.file;

import com.google.gson.JsonObject;
import engineer.pol.bosscreator.utils.GsonUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataFile {

    private final File dir;
    private final File file;

    public DataFile() {
        this.dir = new File("./config/bosscreator");
        this.file = new File("./config/bosscreator/data.json");

        this.dir.mkdirs();
        try {
            this.file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to create data.json file");
        }
    }

    public File getFile() {
        return file;
    }

    public void save(JsonObject json) {
        String jsonString = GsonUtils.jsonToString(json);
        try {
            FileWriter writer = new FileWriter(this.file, false);
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonObject load() {
        try {
            return GsonUtils.jsonFromFile(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
