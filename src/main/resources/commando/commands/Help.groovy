package commando.commands

import co.vulpin.commando.CommandEvent
import co.vulpin.commando.annotations.Cmd
import co.vulpin.commando.annotations.Options
import commando.decorators.BasicPerms
import net.dv8tion.jda.core.EmbedBuilder

class Help {

    private static final String SPACER = "\n\u200B"

    @Cmd
    @Options(optional = true)
    @BasicPerms
    get(CommandEvent event) {
        def embed = new EmbedBuilder().with {
            color = event.guild?.selfMember?.color

            def self = event.JDA.selfUser
            setAuthor(self.name, null, self.effectiveAvatarUrl)

            addField(
                "bday set [day], [month], [year], [gmt offset]",
                "Enters your birthday into the system. **You cannot change this once it has been set!**\n\n" +
                        "If you don't know what a GMT offset is, " +
                        "[click here](https://www.timeanddate.com/time/map/) and hover over your location " +
                        "on the map. Your GMT offset is the value at the bottom that is highlighted " +
                        "(if the highlighted value at the bottom simply says `UTC`, then your GMT offset " +
                        "is 0.).\n\nExample: `bday set 30, 9, 1999, -4` $SPACER",
                false
            )

            addField(
                "bday role set [role name]",
                "Selects a role to use as the birthday role.",
                false
            )

            addField(
                "bday role create",
                "Automatically creates a birthday role and stores it. $SPACER",
                false
            )

            addField(
                "bday invite",
                "Invites the bot to your server",
                false
            )

            addField(
                "bday support",
                "Displays an invite to BirthdayBot's support server",
                false
            )

            build()
        }

        event.channel.sendMessage(embed).queue()
    }

}
