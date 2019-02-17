package nextflow.script

import groovy.transform.CompileStatic
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class WorkflowStack {

    static WorkflowStack INSTANCE = new WorkflowStack()

    static WorkflowStack get() { INSTANCE }

    List<WorkflowDef> stack = new ArrayList<>()

    WorkflowDef current() {
        stack ? stack.get(0) : null
    }

    void push(WorkflowDef workflow) {
        stack.push(workflow)
    }

    WorkflowDef pop() {
        stack.pop()
    }

    int size() {
        stack.size()
    }

    boolean asBoolean() {
        stack.size()>0
    }

}
