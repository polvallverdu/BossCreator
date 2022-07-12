package engineer.pol.bosscreator.utils;

import com.google.gson.*;

import java.io.*;

public class GsonUtils {

    public static Gson gson = new Gson();

    public static JsonObject jsonFromString(String json) {
        try {
            JsonElement parser = JsonParser.parseString(json);
            return parser.getAsJsonObject();
        } catch (JsonParseException | IllegalStateException e) {
            return new JsonObject();
        }
    }

    public static JsonObject jsonFromFile(File file) throws IOException {
        String content = readFromFile(file);
        return jsonFromString(content);
    }

    public static String jsonToString(JsonObject json) {
        return json.toString();
    }

    private static String readFromFile(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } finally {
            inputStream.close();
        }
        return resultStringBuilder.toString();
    }
}
