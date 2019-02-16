package nextflow.script

import spock.lang.Specification

import nextflow.ast.NextflowDSL
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import test.MockSession
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ProcessDefTest extends Specification {

    def 'should define and invoke process' () {

        given:
        def session = new MockSession()
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
