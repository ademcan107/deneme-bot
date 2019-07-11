package commando.decorators

import co.vulpin.commando.CommandEvent
import com.github.yihtserns.groovy.decorator.Function
import com.github.yihtserns.groovy.decorator.MethodDecorator
import net.dv8tion.jda.core.Permission
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@MethodDecorator({ Function func ->
    return { args ->
        def event = args[0] as CommandEvent

        if(event.guild) {

            def self = event.guild.selfMember
            def channel = event.textChannel

            if(!self.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                event.replyError("I am missing this permission", "Embed Links").queue()
                return
            }

        }

        func.call(args)
    }
})
@GroovyASTTransformationClass("com.github.yihtserns.groovy.decorator.DecoratorASTTransformation")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface BasicPerms {}