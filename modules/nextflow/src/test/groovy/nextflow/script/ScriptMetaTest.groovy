package nextflow.script

import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ScriptMetaTest extends Specification {

    def 'should return all defined names' () {

        given:
        def script = Mock(BaseScript); script.getBinding() >> new ScriptBinding()

        def proc1 = new ProcessDef(script, 'proc1', Mock(ProcessConfig), Mock(TaskBody))
        def proc2 = new ProcessDef(script, 'proc2', Mock(ProcessConfig), Mock(TaskBody))
        def func1 = new FunctionDef(name: 'func1')
        def work1 = new WorkflowDef(Mock(TaskBody), 'workflow1')

        when:
        def meta = new ScriptMeta()
        meta.addFunctionDef(func1)
        meta.addWorkflowDef(work1)
        meta.addProcessDef(proc1)
        meta.addProcessDef(proc2)

        then:
        meta.getWorkflowDef('workflow1') == work1
        meta.getFunctionDef('func1') == func1
        meta.getProcessDef('proc1') == proc1
        meta.getProcessDef('proc2') == proc2

        then:
        meta.getAllDefinedNames() == ['proc1','proc2','func1','workflow1'] as Set

        then:
        meta.containsDef('proc1')
        meta.containsDef('proc2')
        meta.containsDef('func1')
        meta.containsDef('workflow1')
        !meta.containsDef('proc3')

        then:
        meta.getDefinedProcesses() == [proc1, proc2]
        meta.getDefinedWorkflows() == [work1]
        meta.getDefinedFunctions() == [func1]


    }
}
