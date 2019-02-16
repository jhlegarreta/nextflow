/*
 * Copyright 2013-2019, Centre for Genomic Regulation (CRG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nextflow.script


import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import nextflow.Session
import nextflow.processor.TaskProcessor
/**
 * Any user defined script will extends this class, it provides the base execution context
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
abstract class BaseScript extends Script {

    private Session session

    private ProcessFactory processFactory

    private TaskProcessor taskProcessor

    private boolean module

    @Lazy InputStream stdin = { System.in }()

    BaseScript() {
        ScriptMeta.register(this)
    }

    BaseScript(Binding binding) {
        super(binding)
        ScriptMeta.register(this)
    }

    ScriptBinding getBinding() {
        (ScriptBinding)super.getBinding()
    }

    /**
     * Holds the configuration object which will used to execution the user tasks
     */
    protected Map getConfig() {
        log.warn "The access of `config` object is deprecated"
        session.getConfig()
    }

    /**
     * Access to the last *process* object -- only for testing purpose
     */
    @PackageScope
    TaskProcessor getTaskProcessor() { taskProcessor }

    /**
     * Enable disable task 'echo' configuration property
     * @param value
     */
    protected void echo(boolean value = true) {
        log.warn "The use of `echo` method is deprecated"
        session.getConfig().process.echo = value
    }

    private void setup() {
        session = binding.getSession()
        module = binding.module
        processFactory = session.newProcessFactory(this)

        binding.setVariable( 'baseDir', session.baseDir )
        binding.setVariable( 'workDir', session.workDir )
        binding.setVariable( 'workflow', session.workflowMetadata )
        binding.setVariable( 'nextflow', session.workflowMetadata?.nextflow )
    }

    protected process( Map<String,?> args, String name, Closure body ) {
        throw new DeprecationException("This process invocation syntax is deprecated")
    }

    protected process( String name, Closure body ) {

        if( module ) {
            def proc = processFactory.defineProcess(name, body)
            ScriptMeta.get(this).setProcessDef(proc)
        }
        else {
            // legacy process definition an execution
            taskProcessor = processFactory.createProcessor(name, body)
            taskProcessor.run()
        }
    }

    protected workflow(String name, TaskBody body, List<String> declaredInputs = Collections.emptyList()) {
        ScriptMeta.get(this).setWorkflowDef(new WorkflowDef(name,body,declaredInputs))
    }

    protected void require(path) {
        require(Collections.emptyMap(), path)
    }

    protected void require(Map opts, path) {
        final params = opts.params ? (Map)opts.params : null
        // TODO
        throw new UnsupportedOperationException("TODO")
    }

    @Override
    Object invokeMethod(String name, Object args) {
        module ? invokeFromModule(name,args) : invokeFromMain(name,args)
    }

    private Object invokeFromModule(String name, Object args) {
        def meta = ScriptMeta.get(this)
        def process = meta.getProcessDef(name)
        if( process )
            return process.invoke(binding, args)

        def workflow = meta.getWorkflowDef(name)
        if( workflow )
            return workflow.invoke(binding, args)

        return super.invokeMethod(name, args)
    }

    private Object invokeFromMain(String name, Object args) {
        super.invokeMethod(name,args)
    }

    Object run() {
        setup()
        runScript()
    }

    protected abstract Object runScript()

}
