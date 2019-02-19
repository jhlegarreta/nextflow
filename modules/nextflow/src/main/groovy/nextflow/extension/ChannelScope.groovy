package nextflow.extension

import groovy.transform.CompileStatic
import groovyx.gpars.dataflow.DataflowBroadcast
import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowVariable
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.script.WorkflowScope

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class ChannelScope {

    static Map<DataflowQueue, DataflowBroadcast> bridges = new HashMap<>(10)

    static DataflowWriteChannel create(boolean value) {
        if( value )
            return new DataflowVariable()

        if( WorkflowScope.get().current() == null )
            return new DataflowQueue()

        else
            return new DataflowBroadcast()
    }

    static DataflowReadChannel get(DataflowQueue queue) {
        def workflow = WorkflowScope.get().current()
        if( workflow==null )
            return queue

        def broadcast = bridges.get(queue)
        if( broadcast == null ) {
            broadcast = new DataflowBroadcast()
            bridges.put(queue, broadcast)
        }
        return broadcast.createReadChannel()
    }

    static DataflowReadChannel get(DataflowBroadcast channel) {
        def workflow = WorkflowScope.get().current()
        if( workflow==null )
            throw new IllegalStateException("Broadcast channel are only allowed in a workflow definition scope")

        channel.createReadChannel()
    }

    static void broadcast() {
        // connect all dataflow queue variables to associated broadcast channel 
        for( DataflowQueue queue : bridges.keySet() ) {
            def broadcast = bridges.get(queue)
            queue.into(broadcast)
        }
    }

}
