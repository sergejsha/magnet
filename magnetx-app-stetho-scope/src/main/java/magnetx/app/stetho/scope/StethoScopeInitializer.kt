package magnetx.app.stetho.scope

import android.content.Context
import com.facebook.stetho.Stetho
import magnet.Instance
import magnet.Scope
import magnet.Scoping
import magnetx.app.stetho.StethoAppExtension

@Instance(
    type = StethoAppExtension.Initializer::class,
    scoping = Scoping.UNSCOPED
)
internal class StethoScopeInitializer(
    private val scope: Scope
) : StethoAppExtension.Initializer {
    override fun initialize(builder: Stetho.InitializerBuilder, context: Context) {
        builder.enableDumpapp {
            Stetho.DefaultDumperPluginsBuilder(context)
                .provide(StethoScopeDumpPlugin(scope))
                .finish()
        }
    }
}
