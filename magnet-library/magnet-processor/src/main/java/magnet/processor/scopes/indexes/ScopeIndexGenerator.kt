package magnet.processor.scopes.indexes

import magnet.processor.scopes.Generator

class ScopeIndexGenerator : Generator(
    classGenerator = ScopeIndexClassGenerator()
)