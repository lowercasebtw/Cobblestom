package com.imjustdoom.cobblemon;

import com.imjustdoom.packet.SyncPacket;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;

public class Cobblemon {

    public void start() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerPacketEvent.class, playerPacketEvent -> {

            Player player = playerPacketEvent.getPlayer();
            System.out.println("packet - " + playerPacketEvent.getPacket());

            if (playerPacketEvent.getPacket() instanceof ClientPluginMessagePacket) {
                System.out.println("type - " + playerPacketEvent.getPacket());
                System.out.println("data - " + new String((((ClientPluginMessagePacket) playerPacketEvent.getPacket()).data())));

                if (((ClientPluginMessagePacket) playerPacketEvent.getPacket()).channel().equals("cobblemon:request_starter_screen")) {
                    System.out.println("start");
                    NetworkBuffer buffer = NetworkBuffer.resizableBuffer(0);

                    buffer.write(NetworkBuffer.INT, 3); //category size

                    addRegion(buffer, "Kanto", 2); // pokemon count in category

                    addStarter(buffer, "charmander"); // must be registered in the SyncCommand class
                    addStarter(buffer, "pikachu");

                    addRegion(buffer, "Kalos", 2);

                    addStarter(buffer, "seel");
                    addStarter(buffer, "jynx");

                    addRegion(buffer, "test", 1);

                    addStarter(buffer, "mew");

                    player.sendPluginMessage("cobblemon:open_starter", buffer.read(NetworkBuffer.RAW_BYTES));
                    System.out.println("end");
                } else if (((ClientPluginMessagePacket) playerPacketEvent.getPacket()).channel().equals("cobblemon:select_starter")) {
                    player.sendMessage("[EA] Selecting a starter costs $9.99. Purchase?");
                }
            }
        }).addListener(PlayerSpawnEvent.class, playerLoginEvent -> {
            Player player = playerLoginEvent.getPlayer();
            if (!playerLoginEvent.isFirstSpawn()) return;

//            player.sendMessage("To sync with cobblemon type \"/sync\". I need to make it work on login still");

            System.out.println(1);
            NetworkBuffer buffer = NetworkBuffer.resizableBuffer(0);

            SyncPacket.SERIALIZER.write(buffer,
                    new SyncPacket("cobblemon:general", false, false, false,
                            false, false, null, null, null));

            System.out.println(2);
            player.sendPluginMessage("cobblemon:set_client_playerdata", buffer.read(NetworkBuffer.RAW_BYTES));
            System.out.println(3);

            buffer = NetworkBuffer.resizableBuffer(1024);
            NetworkBuffer listBuffer = NetworkBuffer.resizableBuffer(512);
            listBuffer.write(NetworkBuffer.VAR_INT, 5); // count of pokemon to add

            Cobblemon.addEntity(listBuffer, "charmander", "Charmander", "fire");
            Cobblemon.addEntity(listBuffer, "pikachu", "Pikachu", "electric");
            Cobblemon.addEntity(listBuffer, "mew", "Mew", "psychic");
            Cobblemon.addEntity(listBuffer, "seel", "Seel", "water");
            Cobblemon.addEntity(listBuffer, "jynx", "Jynx", "dark");

            byte[] bytes = listBuffer.read(NetworkBuffer.RAW_BYTES);
            buffer.write(NetworkBuffer.INT, bytes.length);
            buffer.write(NetworkBuffer.RAW_BYTES, bytes);

            System.out.println(5);
            player.sendPluginMessage("cobblemon:species_sync", buffer.read(NetworkBuffer.RAW_BYTES));
            System.out.println(6);
        });
    }

    private void addStarter(NetworkBuffer buffer, String id) {
        buffer.write(NetworkBuffer.STRING, "cobblemon:" + id); //cobblemon.species.charmander
        buffer.write(NetworkBuffer.BYTE, (byte) 0); // aspects size?
    }

    private void addRegion(NetworkBuffer buffer, String name, int size) {
        buffer.write(NetworkBuffer.STRING, "Kanto"); // cat name
        buffer.write(NetworkBuffer.STRING, "cobblemon.starterselection.category." + name.toLowerCase()); // display name
        buffer.write(NetworkBuffer.INT, size); // size of list?
    }

    public static void addEntity(NetworkBuffer buffer, String id, String name, String type) {
        buffer.write(NetworkBuffer.STRING, "cobblemon:" + id); // species i think

        buffer.write(NetworkBuffer.BOOLEAN, true);
        buffer.write(NetworkBuffer.STRING, name);
        buffer.write(NetworkBuffer.INT, 5);

        // base stats map aaaa
        buffer.write(NetworkBuffer.VAR_INT, 0);

        buffer.write(NetworkBuffer.STRING, type);

        buffer.write(NetworkBuffer.BOOLEAN, false);

        buffer.write(NetworkBuffer.STRING, "medium_slow");
        buffer.write(NetworkBuffer.FLOAT, 6.0f);
        buffer.write(NetworkBuffer.FLOAT, 85.0f);
        buffer.write(NetworkBuffer.FLOAT, 0.7f);
        buffer.write(NetworkBuffer.FLOAT, 1.0f);

        // dimesnions
        buffer.write(NetworkBuffer.FLOAT, 0.7f);
        buffer.write(NetworkBuffer.FLOAT, 1.1f);
        buffer.write(NetworkBuffer.BOOLEAN, false);

        buffer.write(NetworkBuffer.BYTE, (byte) 0); //

        buffer.write(NetworkBuffer.VAR_INT, 1);
        buffer.write(NetworkBuffer.STRING, "cobblemon.species." + id + ".desc");

        buffer.write(NetworkBuffer.VAR_INT, 0);

        buffer.write(NetworkBuffer.STRING, "cobblemon:battle.pvw.default");

        buffer.write(NetworkBuffer.VAR_INT, 0);

        buffer.write(NetworkBuffer.BOOLEAN, false);

        buffer.write(NetworkBuffer.INT, 0);
        buffer.write(NetworkBuffer.VAR_INT, 0);
    }
}
