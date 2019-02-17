package nextflow.script

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */

@CompileStatic
class ScriptMeta {

    static private Map<BaseScript,ScriptMeta> REGISTRY = new HashMap<>(10)

    static ScriptMeta get(BaseScript script) {
        REGISTRY.get(script)
    }

    private Class<? extends BaseScript> clazz

    private Path scriptPath

    private List<WorkflowDef> definedWorkflows = new ArrayList<>(10)

    private List<ProcessDef> definedProcesses = new ArrayList<>(10)

    private List<FunctionDef> definedFunctions = new ArrayList<>(10)

    Path getScriptPath() { scriptPath }

    @PackageScope setScriptPath(Path path ) { scriptPath = path }

    List<WorkflowDef> getDefinedWorkflows() { definedWorkflows }

    List<ProcessDef> getDefinedProcesses() { definedProcesses }

    List<FunctionDef> getDefinedFunctions() { definedFunctions }

    Set<String> getWorkflowNames() {
        def result = new LinkedHashSet(definedWorkflows.size())
        for( def work : definedWorkflows )
            result.add(work.name)
        return result
    }

    Set<String> getProcessNames() {
        def result = new LinkedHashSet(definedProcesses.size())
        for( def proc : definedProcesses )
            result.add(proc.name)
        return result
    }

    Set<String> getFunctionNames() {
        def result = new LinkedHashSet(definedFunctions.size())
        for( def func: definedFunctions )
            result.add(func.name)
        return result
    }

    Set<String> getAllDefinedNames() {
        def result = new HashSet(definedWorkflows.size() + definedProcesses.size() + definedFunctions.size())
        result.addAll( getFunctionNames() )
        result.addAll( getWorkflowNames() )
        result.addAll( getProcessNames() )
        return result
    }

    @PackageScope
    static ScriptMeta register(BaseScript script) {
        def meta = new ScriptMeta(
                clazz: script.class,
                definedFunctions: definedFunctions0(script) )
        
        REGISTRY.put(script, meta)
        return meta
    }

    static List<FunctionDef> definedFunctions0(BaseScript script) {
        def allMethods = script.class.getDeclaredMethods()
        def result = new ArrayList(allMethods.length)
        for( Method method : allMethods ) {
            if( !Modifier.isPublic(method.getModifiers()) ) continue
            if( Modifier.isStatic(method.getModifiers())) continue
            if( method.name.startsWith('super$')) continue

            result.add(new FunctionDef(script, method))
        }
        return result
    }

    ScriptMeta addWorkflowDef(WorkflowDef workflow) {
        definedWorkflows.add(workflow)
        return this
    }

    WorkflowDef getWorkflowDef(String name) {
        for( def work : definedWorkflows ) {
            if( work.name == name ) return work
        }
        return null
    }

    ScriptMeta addProcessDef(ProcessDef process) {
        definedProcesses.add(process)
        return this
    }

    ProcessDef getProcessDef(String name) {
        for( def proc : definedProcesses ) {
            if( proc.name == name ) return proc
        }
        return null
    }

    boolean addFunctionDef(FunctionDef function) {
        definedFunctions.add(function)
        return this
    }

    FunctionDef getFunctionDef(String name) {
        for( def func : definedFunctions ) {
            if( func.name == name ) return func
        }
        return null
    }

    boolean containsDef( String name ) {
        getProcessDef(name) ?: getWorkflowDef(name) ?: getFunctionDef(name)
    }
}
