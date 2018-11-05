package test;

import magnet.ParentScope;
import magnet.Scope;

@Scope
interface Scope7 {

    void bind(@ParentScope Scope7_1 scope);

}