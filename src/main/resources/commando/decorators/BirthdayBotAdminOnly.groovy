package commando.decorators

import co.vulpin.commando.CommandEvent
import com.github.yihtserns.groovy.decorator.Function
import com.github.yihtserns.groovy.decorator.MethodDecorator
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@MethodDecorator({ Function func ->
    return { args ->
        def event = args[0] as CommandEvent

        def adminRoleId = System.getenv("DISCORD_ADMIN_ROLE_ID")
        def shardManager = event.JDA.asBot().shardManager
        def adminRole = shardManager.getRoleById(adminRoleId)
        def supportServer = adminRole.guild

        if(supportServer.getMemberById(event.author.id)?.roles?.contains(adminRole))
            func.call(args)
        else
            event.replyError("This command can only be used by BirthdayBot admins!").queue()
    }
})
@GroovyASTTransformationClass("com.github.yihtserns.groovy.decorator.DecoratorASTTransformation")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface BirthdayBotAdminOnly {}
