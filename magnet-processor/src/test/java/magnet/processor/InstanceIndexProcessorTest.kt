package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class InstanceIndexProcessorTest {

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

    @Test
    fun `InstanceFactory index gets generated with classifier`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface1.java"),
                withResource("Implementation1.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet.index/app_test_Implementation1MagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/app_test_Implementation1MagnetFactory.java"))
    }

    @Test
    fun `InstanceFactory index gets generated without classifier`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface2.java"),
                withResource("Implementation2.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("magnet.index/app_test_Implementation2MagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/app_test_Implementation2MagnetFactory.java"))
    }
}
