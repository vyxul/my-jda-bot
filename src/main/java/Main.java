import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.List;

public class Main extends ListenerAdapter {
    public static void main (String args[]) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = "NTkyMTQ3MTU2OTMzMTQ4Njcz.XQ-XQg.YqcHlrBfn9KTJsm6OeFCZS_d6uA";
        builder.setToken(token);
        builder.addEventListener(new Main());
        builder.buildAsync();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        User author = event.getAuthor();
        Message message = event.getMessage();

        System.out.println("(" + guild.getName() + ") " +
                author.getName() + ": " +
                message.getContentDisplay() + "\n"
        );

        if (author.isBot())
            return;

        String messageArray[] = message.getContentRaw().split(" ");
        if (messageArray[0].charAt(0) == '!') {
            String command = messageArray[0].substring(1);
            int index;
            String summoner, info;

            switch (command) {
                case ("ping"):
                    channel.sendMessage("pong").queue();
                    return;

                case ("lol"):
                    index = message.getContentRaw().indexOf(" ");
                    summoner = message.getContentRaw().substring(index + 1);

                    if (summoner.length() == 0 || index == -1)
                        info = "Type !lol [summoner name] to use command.\n" +
                                "This command will look up a summoners account and will list their:\n" +
                                "Level, rank, and their top 3 champions.";
                    else
                        info = LeagueFunctions.getSummonerInfo(summoner);

                    channel.sendMessage(info).queue();

                    return;

                case ("tilted"):
                    index = message.getContentRaw().indexOf(" ");
                    summoner = message.getContentRaw().substring(index + 1);

                    if (summoner.length() == 0 || index == -1)
                        info = "Type !lol [summoner name] to use command.\n" +
                                "This command will look up a summoners account and will list their:\n" +
                                "Level, rank, and their top 3 champions.";
                    else
                        info = LeagueFunctions.isSummonerTilted(summoner);

                    channel.sendMessage(info).queue();
                    return;

                /*
                case ("kick"):
                    if (message.isFromType(ChannelType.TEXT))
                        if ()
                        if (message.getMentionedMembers().isEmpty())
                            channel.sendMessage("You must mention 1 or more users to be kicked!").queue();
                        else {
                            Member selfMember = guild.getSelfMember();

                            if (!selfMember.hasPermission(Permission.KICK_MEMBERS)) {
                                channel.sendMessage("I don't have permission to kick anyone.");
                                return;
                            }

                            List<User> mentionedUsers = message.getMentionedUsers();
                            for (User user : mentionedUsers) {
                                Member member = guild.getMember(user);

                                if (!selfMember.canInteract(member)) {
                                    channel.sendMessage("Cannot kick member: ")
                                           .append(member.getEffectiveName())
                                           .append(", they are higher in the hierarchy than I am!")
                                           .queue();
                                    continue;
                                }

                                guild.getController().kick(member).queue(
                                        success -> channel.sendMessage("Kicked ").append(member.getEffectiveName()).append("! Cya!").queue(),
                                        error -> {
                                            if (error instanceof PermissionException) {
                                                PermissionException pe = (PermissionException) error;
                                                Permission missingPermission = pe.getPermission();

                                                channel.sendMessage("PermissionError kicking [")
                                                       .append(member.getEffectiveName()).append("]: ")
                                                       .append(error.getMessage()).queue();
                                            }
                                            else
                                                channel.sendMessage("Unknown error while kicking [")
                                                       .append(member.getEffectiveName())
                                                       .append("]: <").append(error.getClass().getSimpleName()).append(">: ")
                                                       .append(error.getMessage()).queue();
                                        }
                                );
                            }
                        }
                    else
                        channel.sendMessage("This is a Guild-Only command!").queue();

                    return;
                 */

                case ("exit"):
                    if (author.getId().equals("97887362280861696")) {
                        channel.sendMessage("Bye!").queue();
                        System.exit(0);
                    }
                    else {
                        channel.sendMessage("You can't use that command idiot.").queue();
                        return;
                    }

                default:
                    channel.sendMessage("Don't message me idiot.").queue();
            }
        }
    }
}
