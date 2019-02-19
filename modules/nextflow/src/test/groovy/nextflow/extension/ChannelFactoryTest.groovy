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
class ChannelFactoryTest extends Specification {

    def 'should create dataflow variable or queue' () {

        expect:
        ChannelFactory.create() instanceof DataflowQueue
        ChannelFactory.create(false) instanceof DataflowQueue
        ChannelFactory.create(true) instanceof DataflowVariable

        ChannelFactory.createBy(new DataflowVariable()) instanceof DataflowVariable
        ChannelFactory.createBy(new DataflowQueue()) instanceof DataflowQueue


        when:
        WorkflowScope.get().push(Mock(WorkflowDef))
        then:
        ChannelFactory.create() instanceof DataflowBroadcast
        ChannelFactory.create(false) instanceof DataflowBroadcast
        ChannelFactory.create(true) instanceof DataflowVariable

        ChannelFactory.createBy(new DataflowVariable()) instanceof DataflowVariable
        ChannelFactory.createBy(new DataflowQueue()) instanceof DataflowBroadcast

        cleanup:
        WorkflowScope.get().pop()

    }

}
