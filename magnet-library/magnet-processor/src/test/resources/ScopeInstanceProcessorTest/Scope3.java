package test;

import magnet.ParentScope;
import magnet.Scope;

@Scope
interface Scope3 {

    void bind(@ParentScope Scope3_1 scope);

}