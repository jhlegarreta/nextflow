package nextflow.script

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovyx.gpars.dataflow.DataflowVariable
import groovyx.gpars.dataflow.DataflowWriteChannel
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class WorkflowDef implements Cloneable {

    private String name

    private TaskBody body

    private List<String> declaredInputs

    private Set<String> variableNames

    private output

    WorkflowDef(String name, TaskBody body, List<String> inputs = Collections.emptyList() ) {
        this.name = name
        this.body = body
        this.declaredInputs = inputs
        this.variableNames = getVarNames0()
    }

    WorkflowDef clone() {
        def copy = (WorkflowDef)super.clone()
        copy.@body = body.clone()
        return copy
    }


    String getName() { name }

    def getOutput() { output }

    @PackageScope TaskBody getBody() { body }

    @PackageScope List<String> getDeclaredInputs() { declaredInputs }

    @PackageScope String getSource() { body.source }

    @PackageScope List<String> getDeclaredVariables() { new ArrayList<String>(variableNames) }


    private Set<String> getVarNames0() {
        def variableNames = body.getValNames()
        if( variableNames ) {
            Set<String> declaredNames = []
            declaredNames.addAll( declaredInputs )
            if( declaredNames )
                variableNames = variableNames - declaredNames
        }
        return variableNames
    }

    protected Binding createContext(Binding parent) {

        def binding = new Binding()
        for( String name : variableNames ) {
            binding.setProperty( name, parent.getProperty(name) )
        }

        return binding
    }

    protected void collectInputs(Binding context, Object[] args) {
        if( args.size() != declaredInputs.size() )
            throw new IllegalArgumentException("Workflow `$name` declares ${declaredInputs.size()} input channels but ${args.size()} were specified")

        // attach declared inputs with the invocation arguments
        for( int i=0; i< declaredInputs.size(); i++ ) {
            final name = declaredInputs[i]
            context.setProperty( name, args[i] )
        }
    }

    protected DataflowWriteChannel makeChannel(value) {
        def result = new DataflowVariable()
        result.bind(value)
        return result
    }

    protected Object collectOutputs(Object value) {
        if( value==null )
            return null

        if( value instanceof ProcessOutputArray )
            return value

        def result = new ArrayList(10)
        if( value instanceof List ) {
            for( def item : value ) result.add(item)
        }
        else {
            result.add(value)
        }
        if( result.size()==1 )
            return result[0]

        return new ProcessOutputArray(result)
    }

    Object invoke(Binding scope, Object... args) {
        // use this instance an workflow template, therefore clone it
        final workflow = this.clone()
        // workflow execution
        final result = workflow.run0(scope, args)
        // register this workflow invocation in the current context
        // so that it can be accessed in the outer execution scope
        scope.setProperty(name, workflow)
        return result
    }

    protected Object run0(Binding scope, Object[] args) {
        // setup the execution context
        final context = createContext(scope)
        // setup the workflow inputs
        collectInputs(context, args)
        // invoke the workflow execution
        final closure = body.closure
        closure.delegate = context
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        final result = closure.call()
        // collect the workflow outputs
        output = collectOutputs(result)
    }

}
