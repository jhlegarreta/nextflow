package nextflow.script

import spock.lang.Specification

import nextflow.Session
import nextflow.ast.NextflowDSL
import nextflow.executor.Executor
import nextflow.executor.ExecutorFactory
import nextflow.executor.MockExecutor
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ProcessDefTest extends Specification {

    static class TestExecutorFactory extends ExecutorFactory {
        @Override
        protected Class<? extends Executor> getExecutorClass(String executorName) {
            return MockExecutor
        }

        @Override
        protected boolean isTypeSupported(ScriptType type, Object executor) {
            true
        }
    }

    def 'should define and invoke process' () {

        given:
        def session = new Session()
        session.executorFactory = new TestExecutorFactory()
        def binding = new ScriptBinding(session).setModule(true)
        def config = new CompilerConfiguration()
        config.setScriptBaseClass(BaseScript.class.name)
        config.addCompilationCustomizers( new ASTTransformationCustomizer(NextflowDSL))

        def SCRIPT = '''
            
            process foo {
              input: val data 
              output: val result
              exec:
                result = "$data mundo"
            }     
            
            process bar {
                input: val data 
                output: val result
                exec: 
                  result = data.toUpperCase()
            }   
            
            foo('Hola')
            bar( foo.output )
        '''

        when:
        def script = (BaseScript)new GroovyShell(binding,config).parse(SCRIPT)
        def result = script.run()
        session.await()
        then:
        result.val == 'HOLA MUNDO'
        binding.foo.output.val == 'Hola mundo'
        binding.bar.output.val == 'HOLA MUNDO'

    }
}
