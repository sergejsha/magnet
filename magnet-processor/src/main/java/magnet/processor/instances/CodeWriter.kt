package magnet.processor.instances

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.Filer

class CodeWriter(
    private var filePackage: String,
    private var fileTypeSpec: TypeSpec
) {

    fun writeInto(filer: Filer) {
        JavaFile
            .builder(filePackage, fileTypeSpec)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }
}
