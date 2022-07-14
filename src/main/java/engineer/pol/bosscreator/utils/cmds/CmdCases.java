package engineer.pol.bosscreator.utils.cmds;

public enum CmdCases {

    START(),
    START_BOSSFIGHT(),
    START_PLAYERFIGHT(),
    FINISH(),
    FINISH_BOSSFIGHT(),
    FINISH_PLAYERFIGHT(),
    FORCE_FINISH(),
    FORCE_FINISH_BOSSFIGHT(),
    FORCE_FINISH_PLAYERFIGHT(),
    BOSSBAR_BLOCK(),
    BOSSBAR_UNBLOCK(),
    LEFT_WIN(),
    RIGHT_WIN(),
    MORPHED_PLAYER_ADD(),
    MORPHED_PLAYER_REMOVE(),
    BOSSFIGHT_PLAYER_KILL(),
    BOSSFIGHT_BOSS_KILL();

    public static CmdCases get(String name) {
        for (CmdCases cmdCase : CmdCases.values()) {
            if (cmdCase.name().equalsIgnoreCase(name)) {
                return cmdCase;
            }
        }
        return null;
    }

}
