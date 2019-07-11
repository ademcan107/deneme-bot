package co.vulpin.birthday.bot

import co.vulpin.birthday.db.Database
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

class JoinListener extends ListenerAdapter {

    private Bot bot

    JoinListener(Bot bot) {
        this.bot = bot
    }

    @Override
    void onGuildMemberJoin(GuildMemberJoinEvent event) {
        def user = Database.instance.getUser(event.member.user.id)
        if(user?.isBirthday())
            bot.happyBirthday(event.guild.id, event.member.user.id)
    }

}
