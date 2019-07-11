package co.vulpin.birthday.bot

import co.vulpin.birthday.db.Database
import co.vulpin.birthday.db.entities.User
import co.vulpin.commando.Commando
import com.google.cloud.firestore.DocumentSnapshot
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Game

import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService

import static com.google.cloud.firestore.DocumentChange.Type.*
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static net.dv8tion.jda.core.Permission.MANAGE_ROLES

class Bot {

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor()

    private Map<String, Future> startFutures = [:]
    private Map<String, Future> endFutures = [:]

    @Delegate
    private ShardManager shardManager

    Bot() {
        def token = System.getenv("DISCORD_TOKEN")

        def prefix = System.getenv("DISCORD_PREFIX")
        def ownerId = System.getenv("DISCORD_OWNER_ID")
        def commando = new Commando({[{ prefix }]}, ownerId)

        def game = Game.of(Game.GameType.DEFAULT, "$prefix help")

        shardManager = new DefaultShardManagerBuilder()
            .setToken(token)
            .addEventListeners(commando)
            .addEventListeners(new JoinListener(this), new ServerCountUpdater())
            .setGame(game)
            .build()

        db.collection("users").addSnapshotListener({ snap, e ->
            if(e)
                return

            snap.documentChanges.each {
                def doc = it.document
                switch (it.type) {
                    case ADDED:
                        scheduleBirthday(doc)
                        break
                    case MODIFIED:
                        unhappyBirthday(doc.id)
                        scheduleBirthday(doc)
                        break
                    case REMOVED:
                        unhappyBirthday(doc.id)
                }
            }
        })
    }

    protected void scheduleBirthday(DocumentSnapshot snapshot) {
        def user = snapshot.toObject(User)

        def now = OffsetDateTime.now()

        def birthdayStart = user.birthdayStart
        def birthdayEnd = user.birthdayEnd

        while(!birthdayEnd.isAfter(now)) {
            birthdayStart = birthdayStart.plusYears(1)
            birthdayEnd = birthdayEnd.plusYears(1)
        }

        long startDelay = Duration.between(now, birthdayStart).toMillis()
        startFutures.put(snapshot.id, executor.schedule({
            happyBirthday(snapshot.id)
        }, startDelay, MILLISECONDS))?.cancel(false)

        long endDelay = Duration.between(now, birthdayEnd).toMillis()
        endFutures.put(snapshot.id, executor.schedule({
            unhappyBirthday(snapshot.id)
        }, endDelay, MILLISECONDS))?.cancel(false)
    }

    protected void happyBirthday(String userId) {
        def user = getUserById(userId)

        user.mutualGuilds.each {
            happyBirthday(it.id, userId)
        }
    }

    protected void happyBirthday(String guildId, String userId) {
        def guild = getGuildById(guildId)
        def dbGuild = db.getGuild(guildId)

        def member = guild.getMemberById(userId)
        def selfMember = guild.selfMember

        if(dbGuild.birthdayRoleId) {
            def birthdayRole = guild.getRoleById(dbGuild.birthdayRoleId)

            if(member.roles.contains(birthdayRole))
                return

            if(selfMember.hasPermission(MANAGE_ROLES) && selfMember.canInteract(birthdayRole)) {
                guild.controller.addSingleRoleToMember(member, birthdayRole).queue()
            } else {
                member.user.openPrivateChannel().queue({
                    it.sendMessage("I tried to assign you the birthday role in **${guild.name}**, " +
                            "but I don't have enough permissions! Please notify the admins in that server.").queue()
                })
            }
        }

    }

    protected void unhappyBirthday(String userId) {
        def user = getUserById(userId)

        user.mutualGuilds.each {
            unhappyBirthday(it.id, userId)
        }
    }

    protected void unhappyBirthday(String guildId, String userId) {
        def guild = getGuildById(guildId)
        def dbGuild = db.getGuild(guildId)

        def member = guild.getMemberById(userId)
        def selfMember = guild.selfMember

        if(dbGuild.birthdayRoleId) {
            def birthdayRole = guild.getRoleById(dbGuild.birthdayRoleId)

            if(!member.roles.contains(birthdayRole))
                return

            if(selfMember.hasPermission(MANAGE_ROLES) && selfMember.canInteract(birthdayRole)) {
                guild.controller.removeSingleRoleFromMember(member, birthdayRole).queue()
            } else {
                member.user.openPrivateChannel().queue({
                    it.sendMessage("I tried to remove your the birthday role in **${guild.name}**, " +
                            "but I don't have enough permissions! Please notify the admins in that server.").queue()
                })
            }
        }

    }

    private static Database getDb() { Database.instance }

}
