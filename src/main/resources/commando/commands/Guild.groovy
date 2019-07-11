package commando.commands

import co.vulpin.birthday.db.Database
import co.vulpin.commando.CommandEvent
import co.vulpin.commando.annotations.Cmd
import co.vulpin.commando.annotations.Options
import co.vulpin.commando.annotations.check.BotPerms
import co.vulpin.commando.annotations.check.GuildAdminOnly
import com.jagrosh.jdautilities.commons.utils.FinderUtil
import commando.decorators.BasicPerms

import static com.google.cloud.firestore.SetOptions.merge
import static net.dv8tion.jda.core.Permission.MANAGE_ROLES

@Options(
    aliases = ["server"],
    optional = true
)
class Guild {

    class Role {

        private static final BIRTHDAY_CAKE = "\uD83C\uDF82"

        @Cmd
        @Options(optional = true)
        @GuildAdminOnly
        @BasicPerms
        void set(CommandEvent event, String roleQuery) {
            def role = FinderUtil.findRoles(roleQuery, event.guild)[0]

            if(role) {
                db.getGuildRef(event.guild.id).set([
                    birthdayRoleId: role.id
                ], merge())
                event.reply("The birthday role has been set to ${role.asMention}").queue()
            } else {
                event.reply("I couldn't find a role for that name :pensive:").queue()
            }
        }

        @Cmd
        @Options(aliases = ["disable", "stop"])
        @GuildAdminOnly
        @BasicPerms
        void remove(CommandEvent event) {
            db.getGuildRef(event.guild.id).set([
                birthdayRoleId: null
            ], merge())
            event.reply("Removed this server's birthday role. " +
                    "It will no longer be assigned on people's birthdays.").queue()
        }

        @Cmd
        @GuildAdminOnly
        @BasicPerms
        @BotPerms([MANAGE_ROLES])
        void create(CommandEvent event) {
            def roleAction = event.guild.controller.createRole()
                    .setName(BIRTHDAY_CAKE)
                    .setHoisted(true)

            roleAction.queue({
                db.getGuildRef(event.guild.id).set([
                    birthdayRoleId: it.id
                ], merge())
                event.reply("Successfully created a birthday role.").queue()
            })
        }

        @Cmd
        @Options(optional = true)
        @BasicPerms
        void get(CommandEvent event) {
            def dbGuild = db.getGuild(event.guild.id)
            if(dbGuild.birthdayRoleId) {
                def role = event.guild.getRoleById(dbGuild.birthdayRoleId)
                if(role)
                    event.reply(role.asMention).queue()
                else
                    event.reply("It appears the current birthday role has been deleted. " +
                            "Please setup a new one.").queue()
            } else {
                event.reply("A birthday role has not been setup on this server.").queue()
            }
        }

        private static Database getDb() {
            return Database.instance
        }

    }

}
