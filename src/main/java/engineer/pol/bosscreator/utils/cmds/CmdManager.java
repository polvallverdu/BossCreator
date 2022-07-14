package engineer.pol.bosscreator.utils.cmds;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import engineer.pol.bosscreator.FightCreator;
import engineer.pol.bosscreator.file.DataFile;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CmdManager {

    private final DataFile dataFile;

    private HashMap<CmdCases, List<String>> cmds;

    public CmdManager() {
        this.dataFile = new DataFile("cmds");
        this.cmds = new HashMap<>();
        this.load();
    }

    private void load() {
        JsonObject jsonObject = this.dataFile.load();
        if (jsonObject == null) {
            jsonObject = new JsonObject();
        }

        this.cmds.clear();

        if (!jsonObject.has("commands")) {
            jsonObject.add("commands", new JsonArray());
        }

        for (CmdCases cmdCase : CmdCases.values()) {
            if (!jsonObject.has(cmdCase.name())) {
                jsonObject.add(cmdCase.name(), new JsonArray());
            }
            List<String> commands = new ArrayList<>();
            jsonObject.get(cmdCase.name()).getAsJsonArray().forEach(jsonElement -> commands.add(jsonElement.getAsString()));

            this.cmds.put(cmdCase, commands);
        }
    }

    public void save() {
        JsonObject jsonObject = new JsonObject();
        for (CmdCases cmdCase : CmdCases.values()) {
            JsonArray jsonArray = new JsonArray();
            this.cmds.get(cmdCase).forEach(jsonArray::add);
            jsonObject.add(cmdCase.name(), jsonArray);
        }
        this.dataFile.save(jsonObject);
    }

    public List<String> getCommands(CmdCases cmdCase) {
        return this.cmds.get(cmdCase);
    }

    public void addCommand(CmdCases cmdCase, String command) {
        this.cmds.get(cmdCase).add(command);
        this.save();
    }

    public void removeCommand(CmdCases cmdCase, int index) {
        this.cmds.get(cmdCase).remove(index);
        this.save();
    }

    public void runCommands(String... commands) {
        for (String cmd : commands) {
            FightCreator.SERVER.getCommandManager().execute(FightCreator.SERVER.getCommandSource(), cmd);
        }
    }

    public void runCommands(CmdCases cmdCase, List<ServerPlayerEntity> fightingPlayers, String... placeholders) {
        HashMap<String, String> placeholdersMap = new HashMap<>();

        for (int i = 0; i < placeholders.length; i+=2) {
            placeholdersMap.put("\\{" + placeholders[i] + "}", placeholders[i+1]);
        }

        this.getCommands(cmdCase).forEach(command -> {
            String cmd = command;
            for (String placeholder : placeholdersMap.keySet()) {
                cmd = cmd.replaceAll(placeholder, placeholdersMap.get(placeholder));
            }
            if (cmd.contains("{players}")) {
                String finalCmd = cmd;
                this.runCommands(fightingPlayers.stream().map(p -> p.getName().getString()).map(pn -> finalCmd.replaceAll("\\{players}", pn)).toArray(String[]::new));
            } else {
                this.runCommands(cmd);
            }
        });
    }

}
