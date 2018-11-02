package test;

import magnet.Classifier;
import magnet.Scope;

@Scope
interface Scope2 {

    void bind1(String value);
    void bind2(@Classifier("bind2") String value);

}