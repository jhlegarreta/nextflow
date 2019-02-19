package nextflow.extension

import spock.lang.Specification

import groovyx.gpars.dataflow.DataflowBroadcast
import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.DataflowVariable
import nextflow.script.WorkflowDef
import nextflow.script.WorkflowScope

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class DataflowScopeTest extends Specification {

    def 'should create dataflow variable or queue' () {

        expect:
        ChannelScope.create(true) instanceof DataflowVariable
        ChannelScope.create(false) instanceof DataflowQueue
        
        when:
        WorkflowScope.get().push(Mock(WorkflowDef))
        then:
        ChannelScope.create(true) instanceof DataflowVariable
        ChannelScope.create(false) instanceof DataflowBroadcast

        cleanup:
        WorkflowScope.get().pop()

    }

}
