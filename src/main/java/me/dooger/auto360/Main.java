package me.dooger.auto360;

import me.dooger.auto360.utils.ChatColor;
import me.dooger.auto360.utils.References;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;

@Mod(modid = References.MODID, name = References.MODNAME, clientSideOnly = true, version = References.VERSION, acceptedMinecraftVersions = "1.8.9")
public class Main extends CommandBase {

    private static Main instance;
    private boolean isSilent;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        registerListeners(this);
        registerCommands(this);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

    public static Main getInstance() {
        return instance;
    }

    private void registerListeners(Object... objects) {
        for (Object o : objects) {
            MinecraftForge.EVENT_BUS.register(o);
        }
    }

    private void registerCommands(ICommand... command) {
        Arrays.stream(command).forEachOrdered(ClientCommandHandler.instance::registerCommand);
    }

    private float[] rotateToEntity(EntityPlayer entityPlayer, EntityPlayer entity) {

        Vec3 playerPosition = entityPlayer.getPositionVector();
        Vec3 difference = entity.getPositionVector().subtract(playerPosition);

        double hypotenuse = Math.hypot(difference.xCoord, difference.zCoord);

        float yaw = (float) -Math.toDegrees(Math.atan2(difference.xCoord, difference.zCoord));
        float pitch = (float) -Math.toDegrees(Math.atan2(difference.yCoord, hypotenuse));

        return new float[] { yaw, pitch };
    }

    @SubscribeEvent
    public void onHit(AttackEntityEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        final boolean isSilent = this.isSilent;
        if (event.entityPlayer.equals(player)) {
            new Thread(() -> {
                try {
                    if (isSilent) {
                        Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(player.rotationYaw + 180, player.rotationPitch, player.onGround));
                        Thread.sleep(100);
                        Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(player.rotationYaw + 360, player.rotationPitch, player.onGround));
                    } else {
                        player.rotationYaw += 180;
                        Thread.sleep(100);
                        player.rotationYaw += 180;
                    }
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }

    @Override
    public String getCommandName() {
        return "silent";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Alternates between Silent and Not Silent";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (args.length == 1) {
            if (!args[0].equalsIgnoreCase("false") && !args[0].equalsIgnoreCase("true")) {
                player.addChatMessage(new ChatComponentText(ChatColor.RED + "Incorrect usage. /silent <true/false>"));
            } else {
                isSilent = Boolean.parseBoolean(args[0]);
                player.addChatMessage(new ChatComponentText(ChatColor.GREEN + "Updated to: " + isSilent));
            }
        } else {
            player.addChatMessage(new ChatComponentText(ChatColor.RED + "Incorrect usage. /silent <true/false>"));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }

}
