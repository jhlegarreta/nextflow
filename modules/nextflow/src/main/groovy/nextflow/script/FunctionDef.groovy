package nextflow.script

import java.lang.reflect.Method

import groovy.transform.CompileStatic
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class FunctionDef {

    BaseScript owner

    Method method

    Method getMethod() { method }

    String getName() { method.name }

    BaseScript getOwner() { owner }

    Object invoke(Object...args) {
        method.invoke(owner, args)
    }
}
