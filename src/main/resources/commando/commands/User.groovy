package commando.commands

import co.vulpin.birthday.db.Database
import co.vulpin.birthday.db.entities.User as DbUser
import co.vulpin.commando.CommandEvent
import co.vulpin.commando.annotations.Cmd
import co.vulpin.commando.annotations.Options
import com.jagrosh.jdautilities.commons.utils.FinderUtil
import commando.decorators.BasicPerms
import commando.decorators.BirthdayBotAdminOnly

import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import static com.google.cloud.firestore.SetOptions.merge
import static java.lang.System.getenv

@Options(
    aliases = ["me"],
    optional = true
)
class User {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy O")

    @Cmd
    @BasicPerms
    set(CommandEvent event, String day, String month, String year, String gmtOffset) {
        def ref = db.getUserRef(event.author.id)

        def date
        try {
            date = parseDate(day, month, year, gmtOffset)
        } catch (e) {
            event.replyError(
                "An error occurred while parsing that date!",
                "**$e.class.simpleName:** $e.message"
            ).queue()
            return
        }

        def user = db.getUser(event.author.id)
        if(user?.hasBirthday()) {
            event.replyError("You have already set a birthday! " +
                "**You cannot change your birthday once it has been set to prevent abuse.** " +
                "If you have entered the wrong date, please contact a moderator in " +
                "[BirthdayBot Support](${getenv("DISCORD_SUPPORT_SERVER_INVITE")}).").queue()
            return
        }

        ref.set([
            birthdayEpochSeconds: date.toEpochSecond(),
            gmtOffset: date.offset.totalSeconds
        ])

        return "Successfully set your birthday to **${date.format(dateFormatter)}**!"
    }


    @Cmd
    @BasicPerms
    set(CommandEvent event, String ignored) {
        return "Looks like your forgot a couple parameters! An example of the correct usage is: " +
            "`bday set 30, 9, 1999, -4`"
    }


    @Cmd
    @BasicPerms
    get(CommandEvent event) {
        def dbUser = db.getUser(event.author.id)
        def date = dbUser?.birthdayStart

        if(!date) {
            event.replyError("You haven't set a birthday yet!").queue()
            return
        }

        return date.format(dateFormatter)
    }

    @Cmd
    @BasicPerms
    get(CommandEvent event, String input) {
        def user = FinderUtil.findMembers(input, event.guild)[0]?.user
        if(!user) {
            event.replyError("I couldn't find a person for that input :pensive:").queue()
            return
        }

        def dbUser = db.getUser(user.id)
        def date = dbUser?.birthdayStart

        if(!date)
            return "That person hasn't set a birthday yet!"

        return dateFormatter.format(date)
    }

    @Cmd
    @BirthdayBotAdminOnly
    @BasicPerms
    void reset(CommandEvent event, String input) {
        def user = FinderUtil.findMembers(input, event.guild)[0]?.user
        user ?= FinderUtil.findUsers(input, event.JDA)[0]

        if(!user) {
            event.replyError("I couldn't find a person for that input :pensive:").queue()
            return
        }

        db.getUserRef(user.id).set([
            birthdayEpochSeconds: null,
            gmtOffset: null
        ], merge())

        event.reply("${user.asMention}'s birthday has been reset.").queue()
    }

    private OffsetDateTime parseDate(String day, String month, String year, String gmtOffset) {
        return parseDate(day as int, month as int, year as int, gmtOffset as double)
    }

    private OffsetDateTime parseDate(int day, int month, int year, double gmtOffset) {
        def date = LocalDate.of(year, month, day)
        def time = LocalTime.of(0, 0)

        def seconds = gmtOffset * 60 * 60 as int

        def zone = ZoneOffset.ofTotalSeconds(seconds)

        def birthday = OffsetDateTime.of(date, time, zone)

        return birthday
    }

    private static Database getDb() {
        return Database.instance
    }

    private DbUser getDbUser(String userId) {
        return Database.instance.getUser(userId)
    }

}
