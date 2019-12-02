package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class InstanceDisposerTest {

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

    @Test
    fun `Disposer method gets generated`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation1.java")
            )
        CompilationSubject.assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/Implementation1MagnetFactory")
            .hasSourceEquivalentTo(withResource("expected/Implementation1MagnetFactory.java"))
    }

    @Test
    fun `Disposer must be present`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation2.java")
            )
        CompilationSubject.assertThat(compilation).failed()
        CompilationSubject.assertThat(compilation).hadErrorContaining("disposer method")
    }

    @Test
    fun `Disposer must have no parameters`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation3.java")
            )
        CompilationSubject.assertThat(compilation).failed()
        CompilationSubject.assertThat(compilation).hadErrorContaining("must have no parameters")
    }

    @Test
    fun `Disposer must return void`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation4.java")
            )
        CompilationSubject.assertThat(compilation).failed()
        CompilationSubject.assertThat(compilation).hadErrorContaining("must return void")
    }

    @Test
    fun `Disposer cannot be used with UNSCOPED instances`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface.java"),
                withResource("Implementation5.java")
            )
        CompilationSubject.assertThat(compilation).failed()
        CompilationSubject.assertThat(compilation).hadErrorContaining("UNSCOPED")
    }
}
