package com.argguy;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The Red Room is its own dimension (argguy:red_room, defined by the data JSON in this mod: an empty void,
 * no sky, fixed night). When Fractured catches you ("dragged out of the map") you are teleported into a
 * freshly built suite of three connected rooms: red, white-with-missing-textures, red. Silent copies of the
 * entity stand in the rooms, always facing you. After ~60 s you are sent back where you were.
 * Nothing here flashes: the only screen effect is vanilla Darkness.
 *
 * Session state is in memory. If the server restarts while you are inside, the tick check below
 * (or /fractured exit) sends you back to the overworld spawn.
 */
public final class RedRoom {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, ArgGuyMod.id("red_room"));
    private static final int STAY_TICKS = 1200;
    private static final int ROOM_SPACING = 14;   // rooms are 15 wide and share one wall

    private record Session(ServerLevel origin, double ox, double oy, double oz, float yaw, float pitch,
                           int cx, int cy, int cz, long enteredAt) {}

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static int nextSlot = 0;

    private RedRoom() {}

    public static boolean isIn(UUID id) {
        return SESSIONS.containsKey(id);
    }

    private static void console(ServerPlayer p, String key) {
        p.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.GRAY), false);
    }

    // ------------------------------------------------------------------ build

    private static void buildSuite(ServerLevel level, int cx, int cy, int cz) {
        for (int i = 0; i < 3; i++) {
            int ox = cx + i * ROOM_SPACING;
            boolean white = (i % 2) == 1;
            for (int dx = -7; dx <= 7; dx++) {
                for (int dz = -7; dz <= 7; dz++) {
                    for (int dy = 0; dy <= 6; dy++) {
                        boolean shell = dx == -7 || dx == 7 || dz == -7 || dz == 7 || dy == 0 || dy == 6;
                        BlockState st = Blocks.AIR.defaultBlockState();
                        if (shell) {
                            if (white) {
                                st = (dy == 0)
                                        ? (((dx + dz) & 1) == 0 ? Blocks.PURPLE_CONCRETE : Blocks.BLACK_CONCRETE).defaultBlockState()
                                        : Blocks.WHITE_CONCRETE.defaultBlockState();
                            } else {
                                st = Blocks.RED_CONCRETE.defaultBlockState();
                            }
                        }
                        level.setBlock(new BlockPos(ox + dx, cy + dy, cz + dz), st, 3);
                    }
                }
            }
            level.setBlock(new BlockPos(ox, cy + 6, cz),
                    (white ? Blocks.SEA_LANTERN : Blocks.CRYING_OBSIDIAN).defaultBlockState(), 3);
        }
        // open a 3x3 doorway through each shared wall
        for (int i = 0; i < 2; i++) {
            int wx = cx + i * ROOM_SPACING + 7;
            for (int dz = -1; dz <= 1; dz++)
                for (int dy = 1; dy <= 3; dy++)
                    level.setBlock(new BlockPos(wx, cy + dy, cz + dz), Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static void spawnWatcher(ServerLevel level, double x, double y, double z) {
        FracturedEntity w = new FracturedEntity(ArgGuyMod.FRACTURED, level);
        w.makeRoomWatcher();
        w.setPos(x, y, z);
        level.addFreshEntity(w);
    }

    // ------------------------------------------------------------------ enter / exit

    public static void enter(ServerPlayer p) {
        if (SESSIONS.containsKey(p.getUUID())) return;
        MinecraftServer server = p.level().getServer();
        if (server == null) return;
        ServerLevel room = server.getLevel(DIMENSION);
        if (room == null) {
            p.displayClientMessage(Component.literal("] map: red_room (dimension not loaded: is the mod's data pack active?)"), false);
            return;
        }
        ServerLevel origin = (ServerLevel) p.level();

        int slot = nextSlot++;
        int cx = (slot % 32) * 64;
        int cz = (slot / 32) * 64;
        int cy = 60;
        buildSuite(room, cx, cy, cz);

        // silent copies: one at the far end of the corridor, two in the middle room
        spawnWatcher(room, cx + 2 * ROOM_SPACING + 0.5, cy + 1, cz + 0.5);
        spawnWatcher(room, cx + ROOM_SPACING + 4.5, cy + 1, cz + 5.5);
        spawnWatcher(room, cx + ROOM_SPACING - 4.5, cy + 1, cz - 5.5);

        SESSIONS.put(p.getUUID(), new Session(origin, p.getX(), p.getY(), p.getZ(), p.getYRot(), p.getXRot(),
                cx, cy, cz, room.getGameTime()));
        p.teleportTo(room, cx + 0.5, cy + 1, cz + 0.5, Set.of(), -90.0F, 0.0F, false);
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, STAY_TICKS, 0, false, false));
        console(p, "console.argguy.room1");
        console(p, "console.argguy.room2");
    }

    /** Called every server tick. */
    public static void tick(MinecraftServer server) {
        ServerLevel room = server.getLevel(DIMENSION);
        if (room == null) return;

        for (Map.Entry<UUID, Session> e : new ArrayList<>(SESSIONS.entrySet())) {
            Session s = e.getValue();
            long t = room.getGameTime() - s.enteredAt();
            ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
            if (p != null) {
                if (t == 200) console(p, "console.argguy.room3");
                if (t == 600) p.playNotifySound(ModSounds.VOICE_BANGING, SoundSource.HOSTILE, 2.4F, 1.0F);
                if (t == STAY_TICKS - 60) {
                    p.playNotifySound(ModSounds.CONNECTION_LOST, SoundSource.HOSTILE, 2.0F, 1.0F);
                    console(p, "console.argguy.lost");
                }
            }
            if (t >= STAY_TICKS) exit(e.getKey(), server);
        }

        // anyone in the dimension without a session (server restarted, /tp, ...) gets sent home
        if (server.getTickCount() % 40 == 0) {
            for (ServerPlayer p : new ArrayList<>(room.players())) {
                if (!SESSIONS.containsKey(p.getUUID())) rescue(p, server);
            }
        }
    }

    public static void exit(UUID id, MinecraftServer server) {
        Session s = SESSIONS.remove(id);
        if (s == null) return;
        ServerLevel room = server.getLevel(DIMENSION);
        if (room != null) {
            AABB box = new AABB(s.cx() - 9, s.cy() - 1, s.cz() - 9, s.cx() + 2 * ROOM_SPACING + 9, s.cy() + 9, s.cz() + 9);
            for (FracturedEntity w : room.getEntitiesOfClass(FracturedEntity.class, box)) {
                if (w.getTags().contains(FracturedEntity.ROOM_TAG)) w.discard();
            }
        }
        ServerPlayer p = server.getPlayerList().getPlayer(id);
        if (p != null) {
            p.teleportTo(s.origin(), s.ox(), s.oy(), s.oz(), Set.of(), s.yaw(), s.pitch(), false);
            console(p, "console.argguy.back");
        }
    }

    /** Sends a player stuck in the Red Room dimension to the overworld spawn area. */
    public static void rescue(ServerPlayer p, MinecraftServer server) {
        if (p.level().dimension() != DIMENSION) return;
        ServerLevel overworld = server.overworld();
        BlockPos pos = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.ZERO);
        p.teleportTo(overworld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, Set.of(), 0.0F, 0.0F, false);
        console(p, "console.argguy.back");
    }
}
