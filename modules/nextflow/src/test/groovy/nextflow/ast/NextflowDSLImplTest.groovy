package nextflow.ast

import spock.lang.Specification

import nextflow.Session
import nextflow.script.BaseScript
import nextflow.script.ScriptBinding
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class NextflowDSLImplTest extends Specification {

    private BaseScript parse(Binding binding, CompilerConfiguration config, String text) {
        (BaseScript)new GroovyShell(binding, config).parse(text)
    }

    def 'should fetch method names' () {

        given:
        def binding = new ScriptBinding(Mock(Session))
        def config = new CompilerConfiguration()
        config.setScriptBaseClass(BaseScript.class.name)
        config.addCompilationCustomizers( new ASTTransformationCustomizer(NextflowDSL))

        def SCRIPT = '''
            def foo() { 
                return 0 
            }
            
            def bar() { 
                return 1 
            }
            
            private baz() { 
                return 2 
            }
            
            process alpha {
              /hello/
            }

        '''
        when:
        def script = parse(binding, config, SCRIPT)
        then:
        ScriptMeta.get(script).workflowNames == [] as Set
        ScriptMeta.get(script).processNames == ['alpha'] as Set
        ScriptMeta.get(script).functionNames == ['foo','bar'] as Set
    }

    def 'should not allow duplicate process names' () {
        given:
        def binding = new ScriptBinding(Mock(Session))
        def config = new CompilerConfiguration()
        config.setScriptBaseClass(BaseScript.class.name)
        config.addCompilationCustomizers( new ASTTransformationCustomizer(NextflowDSL))

        def SCRIPT = '''
                    
            process alpha {
              /hello/
            }
        
            process alpha {
              /world/
            }

        '''

        when:
        parse(binding, config, SCRIPT)
        then:
        def err = thrown(MultipleCompilationErrorsException)
        err.message.contains 'Identifier `alpha` is already used by another definition'
    }

    def 'should parse workflow' () {

        given:
        def binding = new ScriptBinding(Mock(Session))
        def config = new CompilerConfiguration()
        config.setScriptBaseClass(BaseScript.class.name)
        config.addCompilationCustomizers( new ASTTransformationCustomizer(NextflowDSL))

        def SCRIPT = '''     
            x=new Object()
            y=new Object()
            workflow foo(x, y) {
              println 'hello'
            }
        '''

        when:
        def script = parse(binding, config, SCRIPT)
        then:
        ScriptMeta.get(script).workflowNames == ['foo'] as Set

        when:
        script.run()
        then:
        true

    }

}
