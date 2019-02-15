package nextflow.ast

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import nextflow.script.BaseScript

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */

@CompileStatic
class ScriptMeta {

    static private Map<String,ScriptMeta> REGISTRY = new HashMap<>(10)

    @PackageScope
    static void put(String className, ScriptMeta meta) {
        assert className
        assert meta
        REGISTRY.put(className, meta)
    }

    static ScriptMeta get(String className) {
        REGISTRY.get(className)
    }

    static ScriptMeta get(Class clazz) {
        get(clazz.name)
    }

    static ScriptMeta get(BaseScript script) {
        get(script.class.name)
    }

    /**
     * The list of process defined in the pipeline script
     */
    private Set<String> processNames

    private Set<String> functionNames

    private Set<String> workflowNames

    Set<String> getWorkflowNames() { workflowNames }

    Set<String> getProcessNames() { processNames }

    Set<String> getFunctionNames() { functionNames }

}
