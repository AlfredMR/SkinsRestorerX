package skinsrestorer.bungee.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlineProxiedPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import skinsrestorer.bungee.SkinsRestorer;
import skinsrestorer.shared.storage.Config;
import skinsrestorer.shared.storage.Locale;
import skinsrestorer.shared.utils.ServiceChecker;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@CommandAlias("sr|skinsrestorer") @CommandPermission("%sr")
public class SrCommand extends BaseCommand {
    private SkinsRestorer plugin;

    public SrCommand(SkinsRestorer plugin) {
        this.plugin = plugin;
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("reload") @CommandPermission("%srReload")
    @Description("%helpSrReload")
    public void onReload(CommandSender sender) {
        Locale.load(SkinsRestorer.getInstance().getConfigPath());
        Config.load(SkinsRestorer.getInstance().getConfigPath(), SkinsRestorer.getInstance().getResourceAsStream("config.yml"));
        sender.sendMessage(new TextComponent(Locale.RELOAD));
    }


    @Subcommand("status") @CommandPermission("%srStatus")
    @Description("%helpSrStatus")
    public void onStatus(CommandSender sender) {
        sender.sendMessage(new TextComponent("§3----------------------------------------------"));
        sender.sendMessage(new TextComponent("§7Checking needed services for SR to work properly..."));

        ProxyServer.getInstance().getScheduler().runAsync(SkinsRestorer.getInstance(), () -> {
            ServiceChecker checker = new ServiceChecker();
            checker.setMojangAPI(plugin.getMojangAPI());
            checker.checkServices();

            ServiceChecker.ServiceCheckResponse response = checker.getResponse();
            List<String> results = response.getResults();

            for (String result : results) {
                sender.sendMessage(new TextComponent(result));
            }
            sender.sendMessage(new TextComponent("§7Working UUID API count: §6 " + response.getWorkingUUID()));
            sender.sendMessage(new TextComponent("§7Working Profile API count: §6" + response.getWorkingProfile()));
            if (response.getWorkingUUID() >= 1 && response.getWorkingProfile() >= 1)
                sender.sendMessage(new TextComponent("§aThe plugin currently is in a working state."));
            else
                sender.sendMessage(new TextComponent("§cPlugin currently can't fetch new skins. You might check out our discord at https://discord.me/servers/skinsrestorer"));
            sender.sendMessage(new TextComponent("§3----------------------------------------------"));
            sender.sendMessage(new TextComponent("§7SkinsRestorer §6v" + plugin.getVersion()));
            sender.sendMessage(new TextComponent("§7Server: §6" + plugin.getProxy().getVersion()));
            sender.sendMessage(new TextComponent("§7BungeeMode: §6Bungee-Plugin"));
            sender.sendMessage(new TextComponent("§7Finished checking services."));
            sender.sendMessage(new TextComponent("§3----------------------------------------------"));
        });
    }


    @Subcommand("drop") @CommandPermission("%srDrop")
    @CommandCompletion("@players")
    @Description("%helpSrDrop")
    public void onDrop(CommandSender sender, OnlineProxiedPlayer target) {
        String player = target.getPlayer().getName();
        plugin.getSkinStorage().removeSkinData(player);
        sender.sendMessage(new TextComponent(Locale.SKIN_DATA_DROPPED.replace("%player", player)));
    }


    @Subcommand("props") @CommandPermission("%srProps")
    @CommandCompletion("@players")
    @Description("%helpSrProps")
    public void onProps(CommandSender sender, OnlineProxiedPlayer target) {
        InitialHandler h = (InitialHandler) target.getPlayer().getPendingConnection();
        LoginResult.Property prop = h.getLoginProfile().getProperties()[0];

        if (prop == null) {
            sender.sendMessage(new TextComponent(Locale.NO_SKIN_DATA));
            return;
        }

        sender.sendMessage(new TextComponent("\n§aName: §8" + prop.getName()));
        sender.sendMessage(new TextComponent("\n§aValue : §8" + prop.getValue()));
        sender.sendMessage(new TextComponent("\n§aSignature : §8" + prop.getSignature()));

        byte[] decoded = Base64.getDecoder().decode(prop.getValue());
        sender.sendMessage(new TextComponent("\n§aValue Decoded: §e" + Arrays.toString(decoded)));

        sender.sendMessage(new TextComponent("\n§e" + Arrays.toString(decoded)));

        sender.sendMessage(new TextComponent("§cMore info in console!"));
    }
}
